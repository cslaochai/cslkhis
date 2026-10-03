package com.his.report.support;

/**
 * CDR 用到的**全部码值字典**（状态、类型）集中在这里。
 *
 * <p>两条纪律：
 * <ol>
 *   <li>码值口径以**库里列注释**为准（已逐个核过），不是凭常识写的
 *       —— 例如处方主表的处方状态的 4 是"已发药"不是"已完成"。</li>
 *   <li>命中不了的码值一律返回未知(n)，**绝不回落成某个合法值**。
 *       回落会让"码值对不上"这种真问题在页面上看不出来。</li>
 * </ol>
 *
 * <p>这里只做翻译，不做判断 —— 判断（哪些算缺口、哪些算异常）在 Service 层。
 */
public final class CdrStatusTexts {

    private CdrStatusTexts() {
    }

    /** 兜底：未知码值原样报出 */
    public static String unknown(Integer code) {
        return yet(code);
    }

    /** 空值不伪装成 0 / 未知，直接给空串，前端就不显示状态标签 */
    private static String yet(Integer code) {
        if (code == null) {
            return null;
        }
        return "未知(" + code + ")";
    }

    /** 安全取值，null → -1（-1 永远落到 default 分支，即未知） */
    private static int nz(Integer v) {
        return v == null ? -1 : v;
    }

    // 门诊域

    public static String registStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "已挂号";
            case 2 -> "已签到";
            case 3 -> "已接诊";
            case 4 -> "已就诊";
            case 5 -> "已退号";
            case 6 -> "已过号";
            default -> yet(v);
        };
    }

    public static String visitStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "已取消";
            case 1 -> "进行中";
            case 2 -> "已完成";
            default -> yet(v);
        };
    }

    public static String outpatientRecordStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已归档";
            case 4 -> "已作废";
            default -> yet(v);
        };
    }

    public static String prescriptionStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已审核";
            case 4 -> "已发药";
            case 5 -> "已取消";
            case 6 -> "已退药";
            default -> yet(v);
        };
    }

    public static String prescriptionType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "西药处方";
            case 2 -> "中成药处方";
            case 3 -> "中药饮片处方";
            default -> yet(v);
        };
    }

    public static String labApplyStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "已提交";
            case 2 -> "已缴费";
            case 3 -> "已采样";
            case 4 -> "检验中";
            case 5 -> "已出报告";
            case 6 -> "已取消";
            default -> yet(v);
        };
    }

    public static String labRecordStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "已登记";
            case 2 -> "已采样";
            case 3 -> "已接收";
            case 4 -> "检测中";
            case 5 -> "已出结果";
            case 6 -> "已审核";
            case 7 -> "已发布";
            case 8 -> "已取消";
            default -> yet(v);
        };
    }

    public static String inspApplyStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "已提交";
            case 2 -> "已缴费";
            case 3 -> "已预约";
            case 4 -> "检查中";
            case 5 -> "已出报告";
            case 6 -> "已取消";
            default -> yet(v);
        };
    }

    public static String inspRecordStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "已登记";
            case 2 -> "已签到";
            case 3 -> "检查中";
            case 4 -> "已出结果";
            case 5 -> "已审核";
            case 6 -> "已发布";
            case 7 -> "已取消";
            default -> yet(v);
        };
    }

    public static String treatmentStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "待执行";
            case 1 -> "已执行";
            case 2 -> "已取消";
            default -> yet(v);
        };
    }

    public static String chargeStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待收费";
            case 2 -> "已收费";
            case 3 -> "已退费";
            case 4 -> "部分退费";
            case 5 -> "已取消";
            default -> yet(v);
        };
    }

    public static String chargeType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "挂号费";
            case 2 -> "药品费";
            case 3 -> "检查费";
            case 4 -> "检验费";
            case 5 -> "治疗费";
            case 6 -> "综合收费";
            default -> yet(v);
        };
    }

    public static String insuranceSettleStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待结算";
            case 2 -> "已结算";
            case 3 -> "已上传";
            case 4 -> "已审核";
            default -> yet(v);
        };
    }

    public static String queueStatus(Integer v) {
        return switch (nz(v)) {
            case 2 -> "候诊中";
            case 3 -> "就诊中";
            case 4 -> "已就诊";
            case 5 -> "已退号";
            case 6 -> "已过号";
            default -> yet(v);
        };
    }

    public static String archiveStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待归档";
            case 2 -> "已归档";
            case 3 -> "已封存";
            default -> yet(v);
        };
    }

    public static String emergencyStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "候诊";
            case 2 -> "诊治中";
            case 3 -> "留观";
            case 4 -> "转住院";
            case 5 -> "离院";
            case 6 -> "死亡";
            default -> yet(v);
        };
    }

    public static String emergencyTriage(Integer v) {
        return switch (nz(v)) {
            case 1 -> "I 级（濒危）";
            case 2 -> "II 级（危重）";
            case 3 -> "III 级（急症）";
            case 4 -> "IV 级（非急症）";
            default -> yet(v);
        };
    }

    // 住院域

    public static String admitStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "已出院";
            case 1 -> "在院";
            default -> yet(v);
        };
    }

    public static String admitWay(Integer v) {
        return switch (nz(v)) {
            case 1 -> "门诊";
            case 2 -> "急诊";
            case 3 -> "转院";
            case 4 -> "其他";
            default -> yet(v);
        };
    }

    public static String inpatientRecordType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "入院记录";
            case 2 -> "首次病程";
            case 3 -> "日常病程";
            case 4 -> "术前小结";
            case 5 -> "手术记录";
            case 6 -> "术后首次病程";
            case 7 -> "出院记录";
            case 8 -> "死亡记录";
            case 9 -> "会诊记录";
            case 10 -> "转科记录";
            case 11 -> "输血记录";
            default -> yet(v);
        };
    }

    public static String inpatientRecordStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已归档";
            default -> yet(v);
        };
    }

    public static String orderStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待校对";
            case 2 -> "已校对";
            case 3 -> "执行中";
            case 4 -> "已完成";
            case 5 -> "已停止";
            case 6 -> "已作废";
            case 7 -> "已退回";
            default -> yet(v);
        };
    }

    public static String orderType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "长期";
            case 2 -> "临时";
            default -> yet(v);
        };
    }

    public static String orderClass(Integer v) {
        return switch (nz(v)) {
            case 1 -> "药品";
            case 2 -> "检查";
            case 3 -> "检验";
            case 4 -> "治疗";
            case 5 -> "护理";
            case 6 -> "手术";
            case 7 -> "输血";
            case 8 -> "监护";
            case 9 -> "其他";
            case 10 -> "临床营养";
            default -> yet(v);
        };
    }

    public static String diagType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "主要诊断";
            case 2 -> "其他诊断";
            default -> yet(v);
        };
    }

    public static String admitCondition(Integer v) {
        // 病案首页"入院病情"：1-有 2-临床未确定 3-情况不明 4-无
        return switch (nz(v)) {
            case 1 -> "入院时已有";
            case 2 -> "临床未确定";
            case 3 -> "情况不明";
            case 4 -> "入院后新发";
            default -> yet(v);
        };
    }

    public static String summaryStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已归档";
            default -> yet(v);
        };
    }

    public static String operationStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "待排期";
            case 1 -> "已排期";
            case 2 -> "术前核对完成";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> yet(v);
        };
    }

    public static String consultStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "待应答";
            case 1 -> "已完成";
            case 2 -> "已取消";
            case 3 -> "会诊中";
            default -> yet(v);
        };
    }

    public static String consultType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "科内会诊";
            case 2 -> "科间会诊";
            case 3 -> "全院会诊";
            default -> yet(v);
        };
    }

    public static String transferStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "待接收";
            case 1 -> "已完成";
            case 2 -> "已取消";
            default -> yet(v);
        };
    }

    public static String transfusionStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "待配血";
            case 1 -> "已配血";
            case 2 -> "已发血";
            case 3 -> "输注中";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> yet(v);
        };
    }

    public static String nursingType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "三测单";
            case 2 -> "护理记录单";
            case 3 -> "生命体征监测";
            default -> yet(v);
        };
    }

    public static String nursingLevel(Integer v) {
        return switch (nz(v)) {
            case 1 -> "特级护理";
            case 2 -> "一级护理";
            case 3 -> "二级护理";
            case 4 -> "三级护理";
            default -> yet(v);
        };
    }

    public static String dischargeStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "正常出院";
            case 2 -> "转科";
            case 3 -> "自动出院";
            default -> yet(v);
        };
    }

    /** 结算账单的账单状态（出院结算节点翻译的是账单状态，不是"结清/欠费"这种派生值） */
    public static String inpatientSettleStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待支付";
            case 2 -> "部分支付";
            case 3 -> "已支付";
            case 4 -> "已作废";
            case 5 -> "已退费";
            default -> yet(v);
        };
    }

    /** 支付资金流水的支付方式（{@code PaymentMethodEnum}，与收费台同一套码值） */
    public static String prepayType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "现金";
            case 2 -> "微信";
            case 3 -> "支付宝";
            case 4 -> "医保个账";
            case 5 -> "院内余额";
            case 6 -> "银行卡";
            case 7 -> "转账";
            default -> yet(v);
        };
    }

    // 患者级

    public static String criticalValueStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待接收";
            case 2 -> "已接收";
            case 3 -> "已处置";
            case 4 -> "已作废";
            default -> yet(v);
        };
    }

    public static String qcStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待处理";
            case 2 -> "已处理";
            case 3 -> "已忽略";
            default -> yet(v);
        };
    }

    public static String followupStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待随访";
            case 2 -> "随访中";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> yet(v);
        };
    }

    public static String referralStatus(Integer v) {
        return switch (nz(v)) {
            case 0 -> "待确认";
            case 1 -> "已确认";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> yet(v);
        };
    }

    public static String publicHealthReportStatus(Integer v) {
        return switch (nz(v)) {
            case 1 -> "待审核";
            case 2 -> "审核通过";
            case 3 -> "审核驳回";
            default -> yet(v);
        };
    }

    public static String publicHealthReportType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "传染病";
            case 2 -> "死因监测";
            case 3 -> "慢性病";
            case 4 -> "其他";
            default -> yet(v);
        };
    }

    /** 检验危急值的危急值类型：1-偏低 2-偏高 */
    public static String criticalType(Integer v) {
        return switch (nz(v)) {
            case 1 -> "偏低";
            case 2 -> "偏高";
            default -> yet(v);
        };
    }
}
