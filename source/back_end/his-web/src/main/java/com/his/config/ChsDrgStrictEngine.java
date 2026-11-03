package com.his.config;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CHS-DRG 2.0 严格合规分组引擎
 * 特点：
 * 1. 使用 HashSet 模拟标准 DI/OI 字典表，体现 "ZYZD in DI_E00" 规则
 * 2. 严格遵循 MDC -> ADRG -> DRG 逐层细化流程
 * 3. 处理 CC/MCC 排除逻辑（主诊相关并发症不计入）
 */
public class ChsDrgStrictEngine {

    // ====================== 1. 模拟国家医保局发布的标准字典表 (DI/OI Tables) ======================
    
    // DI_E00: 呼吸系统疾病主要诊断表 (MDC E)
    private static final Set<String> DI_E00 = new HashSet<>(Arrays.asList(
        "J18.900", "J44.100", "J96.000", "J06.900", "J12.900"
    ));

    // DI_I00: 循环系统疾病主要诊断表 (MDC I, 对应规范中的 MDCF 或 MDCI，视版本而定，此处以循环系为例)
    private static final Set<String> DI_I00 = new HashSet<>(Arrays.asList(
        "I21.000", "I63.900", "I10.x00", "I50.900"
    ));

    // DI_H00: 肝胆胰疾病主要诊断表 (MDC H)
    private static final Set<String> DI_H00 = new HashSet<>(Arrays.asList(
        "K80.000", "K86.000", "C22.000"
    ));

    // OI_HC2: 胆囊切除手术操作表 (用于匹配 ADRG HC2)
    private static final Set<String> OI_HC2 = new HashSet<>(Arrays.asList(
        "51.2300", "51.2200", "51.2100"
    ));

    // MCC 列表 (重要并发症)
    private static final Set<String> MCC_LIST = new HashSet<>(Arrays.asList(
        "E87.100", "J96.000", "I50.900", "K72.900"
    ));

    // CC 列表 (一般并发症)
    private static final Set<String> CC_LIST = new HashSet<>(Arrays.asList(
        "E11.900", "I10.x00", "N18.200"
    ));

    // 主诊相关并发症排除表 (示例：J18 肺炎导致的 J96 呼吸衰竭不算 MCC)
    private static final Map<String, Set<String>> EXCLUSION_MAP = new HashMap<>();
    static {
        Set<String> j18Exclusions = new HashSet<>();
        j18Exclusions.add("J96.000"); // 肺炎导致的呼衰
        EXCLUSION_MAP.put("J18.900", j18Exclusions);
    }

    // ====================== 2. 数据模型 (严格符合医保结算清单接口) ======================

    static class Diagnosis {
        String code;
        String type; // "1": 主要诊断, "2": 其他诊断
        
        public Diagnosis(String code, String type) {
            this.code = code;
            this.type = type;
        }
    }

    static class Operation {
        String code;
        String type; // "1": 主要手术, "2": 其他操作
        
        public Operation(String code, String type) {
            this.code = code;
            this.type = type;
        }
    }

    static class MedicalCase {
        String caseId;
        int age;
        int ventHours; // 有创呼吸机时长
        List<Diagnosis> diagnoses = new ArrayList<>();
        List<Operation> operations = new ArrayList<>();
        
        public MedicalCase(String caseId, int age, int ventHours) {
            this.caseId = caseId;
            this.age = age;
            this.ventHours = ventHours;
        }
    }

    static class GroupResult {
        String mdc;
        String adrg;
        String drg;
        String ccMccLevel;
        List<String> logs = new ArrayList<>();
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n========== 分组结果 ========== \n");
            sb.append("MDC: ").append(mdc).append("\n");
            sb.append("ADRG: ").append(adrg).append("\n");
            sb.append("DRG: ").append(drg).append("\n");
            sb.append("CC/MCC: ").append(ccMccLevel).append("\n");
            sb.append("---------- 分组路径 ---------- \n");
            for (String log : logs) {
                sb.append(log).append("\n");
            }
            return sb.toString();
        }
    }

    // ====================== 3. 核心分组引擎 (严格体现规则) ======================

    public GroupResult group(MedicalCase patient) {
        GroupResult result = new GroupResult();
        result.logs.add("开始 CHS-DRG 2.0 分组计算...");

        // --- 第一步：MDC 判定 (严格遵循五步法) ---
        result.logs.add("【Step 1】MDC 判定");
        
        // 1.1 先期分组 MDCA: 呼吸机 >= 96h 或 ECMO 或 移植
        if (patient.ventHours >= 96) {
            result.mdc = "MDCA";
            result.adrg = "AA1"; // 简化
            result.drg = "AA11";
            result.ccMccLevel = "无";
            result.logs.add("-> 规则: 呼吸机 >= 96小时 -> 进入 MDCA (先期分组)");
            return result;
        }
        result.logs.add("-> 校验: 呼吸机 < 96小时, 不进入 MDCA");

        // 1.2 新生儿 MDCP: 年龄 < 29天
        if (patient.age < 29) {
            result.mdc = "MDCP";
            result.logs.add("-> 规则: 年龄 < 29天 -> 进入 MDCP (新生儿)");
            // 后续逻辑略，直接返回示意
            return result;
        }
        result.logs.add("-> 校验: 年龄 >= 29天, 不进入 MDCP");

        // 1.3 HIV MDCY: 主诊或其他诊断含 HIV (B20-B24)
        boolean hasHiv = patient.diagnoses.stream().anyMatch(d -> d.code.startsWith("B2"));
        if (hasHiv) {
            result.mdc = "MDCY";
            result.logs.add("-> 规则: 存在 HIV 诊断 -> 进入 MDCY");
            return result;
        }
        result.logs.add("-> 校验: 无 HIV 诊断, 不进入 MDCY");

        // 1.4 创伤 MDCZ: 多处严重创伤 (简化判断，实际需查创伤表)
        // 此处省略复杂创伤逻辑，假设不满足

        // 1.5 常规 MDC: 主要诊断匹配 DI_xx 表
        Diagnosis mainDiag = patient.diagnoses.stream()
                .filter(d -> "1".equals(d.type))
                .findFirst()
                .orElse(null);
        
        if (mainDiag == null) {
            result.mdc = "0000";
            result.logs.add("-> 错误: 无主要诊断 -> 进入 0000 未入组");
            return result;
        }
        result.logs.add("-> 锁定主要诊断: " + mainDiag.code);

        // 【核心规则体现】 ZYZD in DI_E00 ?
        if (DI_E00.contains(mainDiag.code)) {
            result.mdc = "MDCE";
            result.logs.add("-> 规则: ZYZD in DI_E00 -> 进入 MDCE (呼吸系统)");
        } 
        // 【核心规则体现】 ZYZD in DI_I00 ?
        else if (DI_I00.contains(mainDiag.code)) {
            result.mdc = "MDCI";
            result.logs.add("-> 规则: ZYZD in DI_I00 -> 进入 MDCI (循环系统)");
        }
        // 【核心规则体现】 ZYZD in DI_H00 ?
        else if (DI_H00.contains(mainDiag.code)) {
            result.mdc = "MDCH";
            result.logs.add("-> 规则: ZYZD in DI_H00 -> 进入 MDCH (肝胆胰)");
        }
        else {
            result.mdc = "MDCW";
            result.logs.add("-> 规则: 主要诊断未匹配任何 DI 表 -> 进入 MDCW (未入组/其他)");
            return result;
        }

        // --- 第二步：ADRG 判定 (优先匹配手术，其次内科) ---
        result.logs.add("【Step 2】ADRG 判定 (MDC: " + result.mdc + ")");
        
        Operation mainOp = patient.operations.stream()
                .filter(o -> "1".equals(o.type))
                .findFirst()
                .orElse(null);

        String adrgCode = null;
        boolean isSurgical = false;

        // 如果有主要手术，尝试匹配外科 ADRG (OI 表)
        if (mainOp != null) {
            // 示例：在 MDCH 中匹配胆囊手术
            if ("MDCH".equals(result.mdc) && OI_HC2.contains(mainOp.code)) {
                adrgCode = "HC2";
                isSurgical = true;
                result.logs.add("-> 规则: 主要手术 in OI_HC2 -> 匹配外科 ADRG: HC2 (胆囊切除)");
            }
            // 其他 MDC 的外科匹配逻辑...
        }

        // 如果没匹配到外科，或者没有手术，匹配内科 ADRG (基于主诊)
        if (adrgCode == null) {
            if ("MDCE".equals(result.mdc)) {
                // 简化：肺炎进 ER1
                if (mainDiag.code.startsWith("J18")) {
                    adrgCode = "ER1";
                    result.logs.add("-> 规则: 主诊匹配内科表 -> 匹配内科 ADRG: ER1 (呼吸道感染)");
                } else if (mainDiag.code.startsWith("J44")) {
                    adrgCode = "ER2";
                    result.logs.add("-> 规则: 主诊匹配内科表 -> 匹配内科 ADRG: ER2 (慢阻肺)");
                }
            } else if ("MDCI".equals(result.mdc)) {
                 // 简化：心梗进 IB1
                 if (mainDiag.code.startsWith("I21")) {
                    adrgCode = "IB1";
                    result.logs.add("-> 规则: 主诊匹配内科表 -> 匹配内科 ADRG: IB1 (急性心梗)");
                 }
            }
        }

        if (adrgCode == null) {
            result.adrg = "QY"; // 歧义组
            result.drg = "QY00";
            result.logs.add("-> 错误: 无法匹配 ADRG -> 进入 QY 歧义组");
            return result;
        }
        result.adrg = adrgCode;

        // --- 第三步：DRG 判定 (CC/MCC 细分) ---
        result.logs.add("【Step 3】DRG 判定 (ADRG: " + result.adrg + ")");
        
        // 获取其他诊断
        List<Diagnosis> otherDiags = patient.diagnoses.stream()
                .filter(d -> "2".equals(d.type))
                .collect(Collectors.toList());

        // 判定 CC/MCC 等级
        String ccMcc = "无";
        
        // 【核心规则体现】 排除主诊相关并发症
        Set<String> exclusions = EXCLUSION_MAP.getOrDefault(mainDiag.code, new HashSet<>());
        
        for (Diagnosis d : otherDiags) {
            // 如果该诊断是主诊的直接并发症，跳过
            if (exclusions.contains(d.code)) {
                result.logs.add("-> 排除: " + d.code + " 为主诊相关并发症，不计入 CC/MCC");
                continue;
            }
            
            if (MCC_LIST.contains(d.code)) {
                ccMcc = "MCC";
                result.logs.add("-> 命中: " + d.code + " 为 MCC (重要并发症)");
                break; // MCC 优先级最高，找到一个即可
            }
        }

        if ("无".equals(ccMcc)) {
            for (Diagnosis d : otherDiags) {
                if (exclusions.contains(d.code)) continue;
                
                if (CC_LIST.contains(d.code)) {
                    ccMcc = "CC";
                    result.logs.add("-> 命中: " + d.code + " 为 CC (一般并发症)");
                    break;
                }
            }
        }
        
        if ("无".equals(ccMcc)) {
            result.logs.add("-> 结论: 无有效 CC/MCC");
        }
        result.ccMccLevel = ccMcc;

        // 最终 DRG 编码生成 (简化映射)
        String drgSuffix = "";
        if ("MCC".equals(ccMcc)) drgSuffix = "1";
        else if ("CC".equals(ccMcc)) drgSuffix = "3";
        else drgSuffix = "5";
        
        // 示例：ER1 + MCC -> ER11, ER1 + CC -> ER13, ER1 + 无 -> ER15
        result.drg = result.adrg + drgSuffix;
        result.logs.add("-> 最终 DRG: " + result.drg);

        return result;
    }

    // ====================== 4. 测试运行 ======================
    public static void main(String[] args) {
        ChsDrgStrictEngine engine = new ChsDrgStrictEngine();

        // 病例 1: 肺炎伴低钠血症 (MCC)
        System.out.println("======= 病例 1: 肺炎伴低钠血症 =======");
        MedicalCase p1 = new MedicalCase("ZY001", 72, 0);
        p1.diagnoses.add(new Diagnosis("J18.900", "1")); // 主诊: 肺炎
        p1.diagnoses.add(new Diagnosis("J96.000", "2")); // 其他: 呼衰 (主诊相关，应排除)
        p1.diagnoses.add(new Diagnosis("E87.100", "2")); // 其他: 低钠血症 (MCC)
        p1.diagnoses.add(new Diagnosis("E11.900", "2")); // 其他: 糖尿病 (CC)
        
        GroupResult r1 = engine.group(p1);
        System.out.println(r1);

        // 病例 2: 胆囊结石伴慢性胆囊炎，行腹腔镜胆囊切除术
        System.out.println("======= 病例 2: 胆囊切除手术 =======");
        MedicalCase p2 = new MedicalCase("ZY002", 68, 0);
        p2.diagnoses.add(new Diagnosis("K80.000", "1")); // 主诊: 胆囊结石
        p2.diagnoses.add(new Diagnosis("K86.000", "2")); // 其他: 胰腺炎 (MCC)
        p2.diagnoses.add(new Diagnosis("I10.x00", "2")); // 其他: 高血压 (CC)
        p2.operations.add(new Operation("51.2300", "1")); // 主要手术: 腹腔镜胆囊切除
        
        GroupResult r2 = engine.group(p2);
        System.out.println(r2);
    }
}
