package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.SpecialDrugFlagEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.AmpouleReturnDTO;
import com.his.emr.dto.NarcoticRegisterQueryPageDTO;
import com.his.emr.entity.BizDrugDispensing;
import com.his.emr.entity.BizNarcoticRegister;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.enums.AmpouleStatusEnum;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.mapper.NarcoticRegisterMapper;
import com.his.emr.service.NarcoticControlService;
import com.his.emr.vo.*;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 麻精药品特殊管理服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NarcoticControlServiceImpl extends ServiceImpl<NarcoticRegisterMapper, BizNarcoticRegister> implements NarcoticControlService {

    /**
     * 违规级别
     */
    private static final String LEVEL_BLOCK = "BLOCK";
    private static final String LEVEL_WARN = "WARN";
    /**
     * 「N次/N剂」里的数量，兼容阿拉伯数字与中文数字
     */
    private static final Pattern P_TIMES = Pattern.compile("([0-9]+|[一二两三四五六七八九十]+)\\s*(?:次|剂)");
    /**
     * 「每N小时」
     */
    private static final Pattern P_EVERY_HOURS = Pattern.compile("每\\s*([0-9]+)\\s*小时");
    /**
     * 提取数字（用于 single_dosage 这类「1」「1袋」「10ml」的字段）
     */
    private static final Pattern P_FIRST_NUMBER = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)");
    /**
     * 中文数字 → 阿拉伯数字。
     * <p>用静态块而不是 {@code Map.of} —— {@code Map.of} 最多只支持 10 组键值对，
     * 这里 11 组会直接把编译打挂（踩过）。
     */
    private static final Map<String, Integer> CN_NUMBER;

    static {
        Map<String, Integer> cn = new HashMap<>();
        cn.put("一", 1);
        cn.put("二", 2);
        cn.put("两", 2);
        cn.put("三", 3);
        cn.put("四", 4);
        cn.put("五", 5);
        cn.put("六", 6);
        cn.put("七", 7);
        cn.put("八", 8);
        cn.put("九", 9);
        cn.put("十", 10);
        CN_NUMBER = Collections.unmodifiableMap(cn);
    }

    private final NarcoticRegisterMapper narcoticRegisterMapper;
    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final RedisSequenceService redisSequenceService;

    private final DeptScopeService deptScopeService;

    // 规则口径

    private static boolean isInjection(String dosageForm) {
        return TextUtil.hasText(dosageForm)
                && (dosageForm.contains("注射") || dosageForm.contains("大输液") || dosageForm.contains("输液"));
    }

    private static boolean isControlledRelease(String dosageForm) {
        // 只认「缓释/控释」——「肠溶片」常被误当成控缓释制剂，但法条里控缓释制剂不含肠溶制剂
        return TextUtil.hasText(dosageForm)
                && (dosageForm.contains("缓释") || dosageForm.contains("控释"));
    }

    private static BigDecimal firstNumber(String text) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        Matcher m = P_FIRST_NUMBER.matcher(text);
        if (!m.find()) {
            return null;
        }
        try {
            return new BigDecimal(m.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String flagText(Integer specialFlag) {
        if (specialFlag == null) {
            return "未知管制品种";
        }
        if (specialFlag == SpecialDrugFlagEnum.NARCOTIC.getCode()) {
            return "麻醉药品";
        }
        if (specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_1.getCode()) {
            return "第一类精神药品";
        }
        if (specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_2.getCode()) {
            return "第二类精神药品";
        }
        if (specialFlag == SpecialDrugFlagEnum.TOXIC.getCode()) {
            return "毒性药品";
        }
        return "普通药品";
    }

    private static String dosageFormText(String dosageForm) {
        if (isInjection(dosageForm)) {
            return "注射剂";
        }
        if (isControlledRelease(dosageForm)) {
            return "控缓释制剂";
        }
        return TextUtil.hasText(dosageForm) ? dosageForm : "其他剂型";
    }

    // 处方限量校验

    @Override
    public Integer limitDaysOf(Integer specialFlag, String dosageForm) {
        if (specialFlag == null || specialFlag == SpecialDrugFlagEnum.NORMAL.getCode()) {
            return null;
        }
        if (specialFlag == SpecialDrugFlagEnum.NARCOTIC.getCode()
                || specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_1.getCode()) {
            // 《处方管理办法》第23条：注射剂一次常用量(1日)；控缓释制剂 ≤7日；其他剂型 ≤3日
            if (isInjection(dosageForm)) {
                return 1;
            }
            return isControlledRelease(dosageForm) ? 7 : 3;
        }
        if (specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_2.getCode()) {
            // 第24条：一般 ≤7日常用量
            return 7;
        }
        if (specialFlag == SpecialDrugFlagEnum.TOXIC.getCode()) {
            // 《医疗用毒性药品管理办法》第9条：每次处方剂量不得超过二日极量
            return 2;
        }
        // 未知分类不装作认识：不给限量，但也不静默放过 —— checkPrescription 里会计入 LIMIT_UNRESOLVED
        return null;
    }

    @Override
    public boolean isControlled(Integer specialFlag) {
        return specialFlag != null && specialFlag != SpecialDrugFlagEnum.NORMAL.getCode();
    }

    @Override
    public boolean requiresDualCheck(Integer specialFlag) {
        if (specialFlag == null) {
            return false;
        }
        // 麻醉药品、第一类精神药品：
        //   《医疗机构麻醉药品、第一类精神药品管理规定》第17条 —— 调配时应当双人复核。
        // 毒性药品：
        //   《医疗用毒性药品管理办法》第9条第2款 —— "调配处方时……并由配方人员及具有药师以上
        //   技术职称的复核人员签名盖章后方可发出"。这也是双人复核，只是法条不在同一个文件里。
        // 第二类精神药品**不在其中** —— 地西泮片这类日常用量大，一并要求双人会把药房堵死，
        //   且于法无据。严格不等于乱加码。
        return specialFlag == SpecialDrugFlagEnum.NARCOTIC.getCode()
                || specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_1.getCode()
                || specialFlag == SpecialDrugFlagEnum.TOXIC.getCode();
    }

    // 双人复核闸门

    @Override
    public boolean requiresAmpouleTracking(Integer specialFlag, String dosageForm) {
        // 空安瓿回收只对**麻醉药品与第一类精神药品的注射剂**有法定要求
        // （《医疗机构麻醉药品、第一类精神药品管理规定》）。毒性药品虽然也走双人复核，
        // 但法规没有"回收空安瓿"这条，所以不能在这里顺手带上 flag=4 —— 多要求一步
        // 会让药房在专册里永远挂着回收不了的"待回收"。
        return specialFlag != null
                && (specialFlag == SpecialDrugFlagEnum.NARCOTIC.getCode() || specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_1.getCode())
                && isInjection(dosageForm);
    }

    // 专册登记

    @Override
    public List<NarcoticViolationVO> checkPrescription(Long prescriptionId, String overLimitReason) {
        // C-非 web 入参：除 GET 直传外还被发药服务 DrugDispensingServiceImpl.assertPrescriptionQuota 直调，
        // 那条路径不过 Bean Validation，保留
        if (prescriptionId == null) {
            throw new BusinessException("处方ID不能为空");
        }
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            throw new BusinessException("处方不存在（处方ID：" + prescriptionId + "）");
        }
        List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId)
                        .orderByAsc(BizPrescriptionDetail::getId));
        if (details.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, NarcoticRegisterMapper.DrugSpecialRow> drugMap = loadDrugSpecial(details);
        List<BizPrescriptionDetail> controlled = details.stream()
                .filter(d -> {
                    NarcoticRegisterMapper.DrugSpecialRow row = drugMap.get(d.getDrugId());
                    return row != null && isControlled(row.getSpecialFlag());
                })
                .collect(Collectors.toList());
        if (controlled.isEmpty()) {
            return Collections.emptyList();   // 普通处方不受麻精规则约束
        }

        List<NarcoticViolationVO> violations = new ArrayList<>();

        // 麻精处方必须含临床诊断
        if (!TextUtil.hasText(rx.getDiagnosis())) {
            NarcoticViolationVO v = new NarcoticViolationVO();
            v.setLevel(LEVEL_BLOCK);
            v.setCode("NO_DIAGNOSIS");
            v.setMessage("处方 " + rx.getPrescriptionNo() + " 含麻精药品，但未填写临床诊断，不得发药");
            v.setDrugName(controlled.stream().map(BizPrescriptionDetail::getDrugName)
                    .filter(TextUtil::hasText).collect(Collectors.joining("、")));
            violations.add(v);
        }

        for (BizPrescriptionDetail detail : controlled) {
            NarcoticRegisterMapper.DrugSpecialRow row = drugMap.get(detail.getDrugId());
            Integer specialFlag = row.getSpecialFlag();
            String dosageForm = TextUtil.hasText(detail.getDosageForm()) ? detail.getDosageForm() : row.getDosageForm();

            // 分类值不在 1~4 之内 = 字典没有这一档，此时 limitDaysOf 返回 null。
            // 不能因为"规则不认识它"就放行 —— 那等于给未来新增的管制档位留了一个后门。
            if (limitDaysOf(specialFlag, dosageForm) == null) {
                NarcoticViolationVO v = new NarcoticViolationVO();
                v.setLevel(LEVEL_BLOCK);
                v.setCode("LIMIT_UNRESOLVED");
                v.setMessage(String.format("「%s」的管制分类为 %s，系统没有对应的限量规则，请先由药房维护药品特殊管理分类后再发药",
                        detail.getDrugName(), specialFlag));
                v.setPrescriptionDetailId(detail.getId());
                v.setDrugName(detail.getDrugName());
                v.setSpecialFlag(specialFlag);
                violations.add(v);
                continue;
            }

            int limitDays = limitDaysOf(specialFlag, dosageForm);
            Integer actualDays = resolveDays(detail);
            if (actualDays == null) {
                NarcoticViolationVO v = new NarcoticViolationVO();
                v.setLevel(LEVEL_BLOCK);
                v.setCode("LIMIT_UNRESOLVED");
                v.setMessage(String.format("「%s」的处方天数无法核定（疗程为空且无法由数量/单次剂量/频次推算），麻精处方不得据此发药",
                        detail.getDrugName()));
                v.setPrescriptionDetailId(detail.getId());
                v.setDrugName(detail.getDrugName());
                v.setSpecialFlag(specialFlag);
                v.setLimitDays(limitDays);
                violations.add(v);
                continue;
            }
            if (actualDays <= limitDays) {
                continue;
            }

            boolean canWaive = specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_2.getCode() && TextUtil.hasText(overLimitReason);
            NarcoticViolationVO v = new NarcoticViolationVO();
            v.setLevel(canWaive ? LEVEL_WARN : LEVEL_BLOCK);
            v.setCode("OVER_LIMIT");
            v.setMessage(String.format("「%s」为%s，%s，每张处方限 %d 日用量的，本处方 %d 日，超限 %d 日%s",
                    detail.getDrugName(), flagText(specialFlag), dosageFormText(dosageForm),
                    limitDays, actualDays, actualDays - limitDays,
                    canWaive ? "（已填写超量理由，按《处方管理办法》第24条放行并记入专册）"
                            : (specialFlag == SpecialDrugFlagEnum.PSYCHOTROPIC_2.getCode() ? "（第二类精神药品超7日须由医师注明理由）" : "")));
            v.setPrescriptionDetailId(detail.getId());
            v.setDrugName(detail.getDrugName());
            v.setSpecialFlag(specialFlag);
            v.setLimitDays(limitDays);
            v.setActualDays(actualDays);
            violations.add(v);
        }
        return violations;
    }

    /**
     * 核定处方天数。
     *
     * <ol>
     *   <li>有疗程 → 直接用疗程</li>
     *   <li>否则用「数量 ÷ (单次剂量 × 每日次数)」向上取整</li>
     *   <li>都推不出 → {@code null}（调用方按"拦"处理）</li>
     * </ol>
     * <p>实测本库处方明细.frequency 是中文自由文本
     * （「一日三次」「每日2次」「每日一剂」「qd」混着写），所以频次解析必须同时吃三种写法。
     */
    private Integer resolveDays(BizPrescriptionDetail detail) {
        Integer duration = detail.getDuration();
        if (duration != null && duration > 0) {
            return duration;
        }
        BigDecimal quantity = detail.getQuantity();
        BigDecimal single = firstNumber(detail.getSingleDosage());
        Integer times = timesPerDay(detail.getFrequency());
        if (quantity == null || quantity.signum() <= 0 || single == null || single.signum() <= 0
                || times == null || times <= 0) {
            return null;
        }
        BigDecimal dailyDosage = single.multiply(BigDecimal.valueOf(times));
        if (dailyDosage.signum() <= 0) {
            return null;
        }
        // 向上取整：2.3 日按 3 日判，不靠小数蒙边界
        return quantity.divide(dailyDosage, 0, RoundingMode.CEILING).intValue();
    }

    /**
     * 频次文字 → 每日次数；解析不出返回 {@code null}
     */
    private Integer timesPerDay(String frequency) {
        if (!TextUtil.hasText(frequency)) {
            return null;
        }
        String text = frequency.trim().toLowerCase().replace(" ", "");
        // 英文简写
        switch (text) {
            case "qd":
                return 1;
            case "bid":
                return 2;
            case "tid":
                return 3;
            case "qid":
                return 4;
            case "qn":
                return 1;
            case "qod":
                return null;   // 隔日一次：按日用量折算会产生 0.5，属于"核不出"，交给调用方拦
            default:
                break;
        }
        // q8h / q12h
        if (text.startsWith("q") && text.endsWith("h")) {
            try {
                int hours = Integer.parseInt(text.substring(1, text.length() - 1));
                if (hours > 0 && 24 % hours == 0) {
                    return 24 / hours;
                }
            } catch (NumberFormatException ignored) {
                // 落到下面的中文解析
            }
        }
        Matcher everyHours = P_EVERY_HOURS.matcher(text);
        if (everyHours.find()) {
            int hours = Integer.parseInt(everyHours.group(1));
            if (hours > 0 && 24 % hours == 0) {
                return 24 / hours;
            }
        }
        Matcher times = P_TIMES.matcher(text);
        if (times.find()) {
            String token = times.group(1);
            if (token.matches("[0-9]+")) {
                int n = Integer.parseInt(token);
                return n > 0 ? n : null;
            }
            Integer n = CN_NUMBER.get(token);
            return n;
        }
        return null;
    }

    // 空安瓿回收

    @Override
    public String resolveAndAssertChecker(Long drugId, Long dispenserId, Long checkerId) {
        NarcoticRegisterMapper.DrugSpecialRow row = loadDrugSpecialOne(drugId);
        if (row == null || !requiresDualCheck(row.getSpecialFlag())) {
            return null;
        }
        // C-非 web 入参：只被发药服务 DrugDispensingServiceImpl 拆开 DTO 直调（普通药品行 checkerId 允许为空，
        // 只有麻精行才必填），Bean Validation 不覆盖这一层，保留
        if (checkerId == null) {
            throw new BusinessException(String.format(
                    "「%s」为%s，调配必须双人复核：请选择复核药师后再发药", row.getDrugName(), flagText(row.getSpecialFlag())));
        }
        if (dispenserId != null && dispenserId.equals(checkerId)) {
            throw new BusinessException(String.format(
                    "「%s」的复核人不能是发药人自己（ID %s）—— 双人复核的意义就在于两个人",
                    row.getDrugName(), checkerId));
        }
        // 姓名由服务端反查：前端传的姓名不采信。
        // 查不到 = 该员工不存在 / 已停用 / **不是药剂师岗位**（三选一，都该拒）。
        String checkerName = narcoticRegisterMapper.selectActivePharmacistName(checkerId);
        if (!TextUtil.hasText(checkerName)) {
            throw new BusinessException("复核人无效（ID：" + checkerId
                    + "）—— 复核人须为在职且具有药师以上技术职称（药剂师岗位）的员工，请重新选择");
        }
        return checkerName;
    }

    // 查询

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String registerOnDispense(BizDrugDispensing dispensing, Long checkerId, String checkerName,
                                     String overLimitReason) {
        if (dispensing == null || dispensing.getDrugId() == null) {
            return null;
        }
        NarcoticRegisterMapper.DrugSpecialRow row = loadDrugSpecialOne(dispensing.getDrugId());
        if (row == null || !isControlled(row.getSpecialFlag())) {
            return null;   // 普通药品不进专册
        }
        BizPrescriptionDetail detail = dispensing.getPrescriptionDetailId() == null
                ? null : bizPrescriptionDetailMapper.selectById(dispensing.getPrescriptionDetailId());
        BizPrescription rx = dispensing.getPrescriptionId() == null
                ? null : bizPrescriptionMapper.selectById(dispensing.getPrescriptionId());
        String dosageForm = detail != null && TextUtil.hasText(detail.getDosageForm())
                ? detail.getDosageForm() : row.getDosageForm();

        LocalDateTime now = TimeUtil.nowSeconds();
        BizNarcoticRegister entity = new BizNarcoticRegister();
        entity.setRegisterNo(redisSequenceService.generateNarcoticRegisterNo());

        entity.setPrescriptionId(dispensing.getPrescriptionId());
        entity.setPrescriptionNo(dispensing.getPrescriptionNo());
        entity.setDispensingId(dispensing.getId());
        entity.setDispensingNo(dispensing.getDispensingNo());

        entity.setPatientId(dispensing.getPatientId());
        entity.setPatientNo(dispensing.getPatientNo());
        entity.setPatientName(dispensing.getPatientName());
        if (rx != null) {
            entity.setGender(rx.getGender());
            entity.setAge(rx.getAge());
            entity.setDiagnosis(rx.getDiagnosis());
            entity.setDeptId(rx.getDeptId());
            entity.setDeptName(rx.getDeptName());
            entity.setDoctorId(rx.getDoctorId());
            entity.setDoctorName(rx.getDoctorName());
            entity.setAuditBy(rx.getAuditBy());
        }
        // 麻醉药品与第一类精神药品需实名登记 → 取患者身份证号
        if (dispensing.getPatientId() != null && requiresDualCheck(row.getSpecialFlag())) {
            entity.setIdCard(narcoticRegisterMapper.selectPatientIdCard(dispensing.getPatientId()));
        }

        entity.setDrugId(dispensing.getDrugId());
        entity.setDrugCode(TextUtil.hasText(row.getDrugCode()) ? row.getDrugCode() : dispensing.getDrugCode());
        entity.setDrugName(TextUtil.hasText(row.getDrugName()) ? row.getDrugName() : dispensing.getDrugName());
        entity.setSpecification(TextUtil.hasText(row.getSpecification()) ? row.getSpecification() : dispensing.getSpecification());
        entity.setUnit(TextUtil.hasText(row.getUnit()) ? row.getUnit() : dispensing.getUnit());
        entity.setSpecialFlag(row.getSpecialFlag());
        entity.setDosageForm(dosageForm);
        entity.setQuantity(dispensing.getQuantity());

        // 批号：从 FEFO 实际扣减流水回查，不取前端传值
        entity.setBatchNo(resolveBatchNo(dispensing.getId()));

        if (detail != null) {
            // 处方天数存**核定值**，不是 detail 里的疗程原值。
            // 两者不等价：医师经常不填疗程，此时天数由数量÷(单次×每日次数) 向上取整推出。
            // 必须存 resolveDays 的返回值 —— 它**就是过限量闸门时用的那个数**。
            // 存原始疗程的话：要么恒 NULL（专册「限量/实际」永远显示「—」，
            // 事后审计看到"限 7 日 / 实际 —"根本判断不出当时按几天放的行），
            // 要么与判据各算各的（同一条语义两处计算，迟早对不上）。
            entity.setDuration(resolveDays(detail));
            BigDecimal daily = resolveDailyDosage(detail);
            entity.setDailyDosage(daily);
        }
        entity.setLimitDays(limitDaysOf(row.getSpecialFlag(), dosageForm));

        entity.setDispenseById(dispensing.getPharmacistId());
        entity.setDispenseBy(dispensing.getPharmacistName());
        entity.setDispenseTime(dispensing.getDispensingTime() != null ? dispensing.getDispensingTime() : now);
        entity.setCheckerId(checkerId);
        entity.setCheckerName(checkerName);
        entity.setCheckTime(checkerId != null || TextUtil.hasText(checkerName) ? now : null);

        if (requiresAmpouleTracking(row.getSpecialFlag(), dosageForm)) {
            entity.setAmpouleStatus(AmpouleStatusEnum.PENDING.getCode());
            entity.setAmpouleIssued(dispensing.getQuantity());
        } else {
            entity.setAmpouleStatus(AmpouleStatusEnum.NA.getCode());
        }
        if (TextUtil.hasText(overLimitReason)) {
            entity.setRemark("超量理由：" + overLimitReason);
        }

        narcoticRegisterMapper.insert(entity);
        log.info("麻精专册登记 {} | 患者={} 药品={} 批号={} 发药={} 复核={}",
                entity.getRegisterNo(), entity.getPatientName(), entity.getDrugName(),
                entity.getBatchNo(), entity.getDispenseBy(), entity.getCheckerName());
        return entity.getRegisterNo();
    }

    /**
     * 核定日用量（专册留痕用）。核不出返回 {@code null} —— 专册允许该列为空，
     * 但发药闸门（{@link #checkPrescription}）那边是拦住不放的，两处口径不冲突。
     */
    private BigDecimal resolveDailyDosage(BizPrescriptionDetail detail) {
        if (detail == null) {
            return null;
        }
        Integer days = resolveDays(detail);
        if (days == null || days <= 0 || detail.getQuantity() == null) {
            return null;
        }
        return detail.getQuantity().divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }

    /**
     * 批号字符串：把本次发药 FEFO 扣减的批次按顺序去重后用逗号拼接。
     * 跨批次时能看出"这一笔是从哪几个批号里出的"，这是麻精追溯的关键。
     */
    private String resolveBatchNo(Long dispensingId) {
        if (dispensingId == null) {
            return null;
        }
        List<NarcoticRegisterMapper.BatchRow> rows = narcoticRegisterMapper.selectDeductBatches("dispensing", dispensingId);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Map<String, BigDecimal> byBatch = new LinkedHashMap<>();
        for (NarcoticRegisterMapper.BatchRow r : rows) {
            if (!TextUtil.hasText(r.getBatchNo())) {
                continue;
            }
            byBatch.merge(r.getBatchNo(), r.getQty() == null ? BigDecimal.ZERO : r.getQty(), BigDecimal::add);
        }
        return byBatch.entrySet().stream()
                .map(e -> e.getKey() + "×" + e.getValue().stripTrailingZeros().toPlainString())
                .collect(Collectors.joining(","));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizNarcoticRegisterVO updateAmpouleReturn(AmpouleReturnDTO dto) {
        BizNarcoticRegister exists = narcoticRegisterMapper.selectById(dto.getId());
        if (exists == null) {
            throw new BusinessException("专册登记不存在（ID：" + dto.getId() + "）");
        }
        int status = exists.getAmpouleStatus() == null ? AmpouleStatusEnum.NA.getCode() : exists.getAmpouleStatus();
        if (status == AmpouleStatusEnum.NA.getCode()) {
            throw new BusinessException("该登记不涉及空安瓿回收（非麻醉/第一类精神药品注射剂）");
        }
        if (status == AmpouleStatusEnum.RETURNED.getCode()) {
            throw new BusinessException("空安瓿已回收登记，不可重复登记（登记号：" + exists.getRegisterNo() + "）");
        }
        BigDecimal issued = exists.getAmpouleIssued() == null ? BigDecimal.ZERO : exists.getAmpouleIssued();
        BigDecimal returned = dto.getAmpouleReturned();
        // D 取值规则（非空校验已下沉 DTO @NotNull）：回收数不得为负
        if (returned.signum() < 0) {
            throw new BusinessException("回收空安瓿数必须为非负数");
        }
        if (returned.compareTo(issued) > 0) {
            throw new BusinessException(String.format("回收空安瓿数（%s）不能大于发出数（%s）",
                    returned.stripTrailingZeros().toPlainString(), issued.stripTrailingZeros().toPlainString()));
        }

        // 只补记回收字段：显式 new patch，杜绝"读出整行改两个字段再整行写回"把业务字段也覆盖掉
        BizNarcoticRegister patch = new BizNarcoticRegister();
        patch.setId(exists.getId());
        patch.setAmpouleStatus(AmpouleStatusEnum.RETURNED.getCode());
        patch.setAmpouleReturned(returned);
        patch.setAmpouleDestroyed(dto.getAmpouleDestroyed());
        patch.setReturnRemark(dto.getReturnRemark());
        patch.setReturnBy(UserUtils.getCurrentUser().getRealName());
        patch.setReturnTime(TimeUtil.nowSeconds());
        narcoticRegisterMapper.updateById(patch);

        return toVO(narcoticRegisterMapper.selectById(exists.getId()));
    }

    // 辅助

    @Override
    public PageResult<BizNarcoticRegisterVO> listPage(NarcoticRegisterQueryPageDTO query) {
        // 专册按开单科室收口（登记行的 dept_id 即处方开单科室）；发药/回收写链路属药房跨科室操作，不在此收口
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        LambdaQueryWrapper<BizNarcoticRegister> wrapper = new LambdaQueryWrapper<>();
        if (TextUtil.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(BizNarcoticRegister::getPatientName, kw)
                    .or().like(BizNarcoticRegister::getPatientNo, kw)
                    .or().like(BizNarcoticRegister::getPrescriptionNo, kw)
                    .or().like(BizNarcoticRegister::getRegisterNo, kw)
                    .or().like(BizNarcoticRegister::getDrugName, kw)
                    .or().like(BizNarcoticRegister::getBatchNo, kw));
        }
        wrapper.eq(query.getSpecialFlag() != null, BizNarcoticRegister::getSpecialFlag, query.getSpecialFlag())
                .eq(query.getAmpouleStatus() != null, BizNarcoticRegister::getAmpouleStatus, query.getAmpouleStatus())
                .eq(query.getPatientId() != null, BizNarcoticRegister::getPatientId, query.getPatientId())
                .in(scope != null, BizNarcoticRegister::getDeptId, scope)
                .ge(query.getDispenseDateStart() != null, BizNarcoticRegister::getDispenseTime,
                        TimeUtil.dayStart(query.getDispenseDateStart()))
                .le(query.getDispenseDateEnd() != null, BizNarcoticRegister::getDispenseTime,
                        TimeUtil.dayEnd(query.getDispenseDateEnd()))
                // 二级键 id：dispense_time 是 DATETIME(0) 秒精度，同秒多行顺序不稳 → 翻页会重复/丢行且不报错
                .orderByDesc(BizNarcoticRegister::getDispenseTime)
                .orderByDesc(BizNarcoticRegister::getId);

        Page<BizNarcoticRegister> page = narcoticRegisterMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<BizNarcoticRegisterVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public NarcoticRegisterCountVO statusCount() {
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        NarcoticRegisterCountVO counts = new NarcoticRegisterCountVO();
        counts.setTotal(narcoticRegisterMapper.selectCount(
                new LambdaQueryWrapper<BizNarcoticRegister>().in(scope != null, BizNarcoticRegister::getDeptId, scope)));
        counts.setPendingAmpoule(narcoticRegisterMapper.selectCount(
                new LambdaQueryWrapper<BizNarcoticRegister>().in(scope != null, BizNarcoticRegister::getDeptId, scope)
                        .eq(BizNarcoticRegister::getAmpouleStatus, AmpouleStatusEnum.PENDING.getCode())));
        counts.setReturnedAmpoule(narcoticRegisterMapper.selectCount(
                new LambdaQueryWrapper<BizNarcoticRegister>().in(scope != null, BizNarcoticRegister::getDeptId, scope)
                        .eq(BizNarcoticRegister::getAmpouleStatus, AmpouleStatusEnum.RETURNED.getCode())));
        return counts;
    }

    @Override
    public NarcoticPrecheckVO precheck(Long prescriptionId, String overLimitReason) {
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        if (rx == null) {
            throw new BusinessException("处方不存在（处方ID：" + prescriptionId + "）");
        }
        List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId)
                        .orderByAsc(BizPrescriptionDetail::getId));

        NarcoticPrecheckVO vo = new NarcoticPrecheckVO();
        vo.setPrescriptionId(prescriptionId);
        vo.setPrescriptionNo(rx.getPrescriptionNo());
        vo.setHasDiagnosis(TextUtil.hasText(rx.getDiagnosis()));

        // ① 管制明细清单（合规的也要带出来：窗口要知道"这单要不要选复核药师"）
        List<ControlledDrugVO> controlled = new ArrayList<>();
        if (!details.isEmpty()) {
            Map<Long, NarcoticRegisterMapper.DrugSpecialRow> drugMap = loadDrugSpecial(details);
            for (BizPrescriptionDetail detail : details) {
                NarcoticRegisterMapper.DrugSpecialRow row = drugMap.get(detail.getDrugId());
                if (row == null || !isControlled(row.getSpecialFlag())) {
                    continue;
                }
                String dosageForm = TextUtil.hasText(detail.getDosageForm())
                        ? detail.getDosageForm() : row.getDosageForm();
                ControlledDrugVO item = new ControlledDrugVO();
                item.setPrescriptionDetailId(detail.getId());
                item.setDrugId(detail.getDrugId());
                item.setDrugName(detail.getDrugName());
                item.setSpecialFlag(row.getSpecialFlag());
                item.setDosageForm(dosageForm);
                item.setLimitDays(limitDaysOf(row.getSpecialFlag(), dosageForm));
                item.setActualDays(resolveDays(detail));
                item.setRequiresDualCheck(requiresDualCheck(row.getSpecialFlag()));
                item.setRequiresAmpouleTracking(requiresAmpouleTracking(row.getSpecialFlag(), dosageForm));
                controlled.add(item);
            }
        }
        vo.setControlledDrugs(controlled);
        vo.setHasControlledDrug(!controlled.isEmpty());
        vo.setRequiresDualCheck(controlled.stream().anyMatch(c -> Boolean.TRUE.equals(c.getRequiresDualCheck())));
        vo.setRequiresAmpouleTracking(controlled.stream()
                .anyMatch(c -> Boolean.TRUE.equals(c.getRequiresAmpouleTracking())));

        List<NarcoticViolationVO> violations = checkPrescription(prescriptionId, overLimitReason);
        vo.setViolations(violations);

        List<NarcoticViolationVO> blocks = violations.stream()
                .filter(v -> LEVEL_BLOCK.equals(v.getLevel()))
                .collect(Collectors.toList());
        vo.setCanDispense(blocks.isEmpty());

        // ③ 卡点是否只是"缺一个二类精神超量理由"——
        //    只有全部 BLOCK 都属这一类且还没给理由时才算，否则填了理由照样发不出去，
        //    那时提示"可填写理由后放行"就是骗人。
        boolean onlyOverLimitWaivable = !blocks.isEmpty() && !TextUtil.hasText(overLimitReason)
                && blocks.stream().allMatch(b -> "OVER_LIMIT".equals(b.getCode())
                && b.getSpecialFlag() != null && b.getSpecialFlag() == SpecialDrugFlagEnum.PSYCHOTROPIC_2.getCode());
        vo.setOverLimitReasonRequired(onlyOverLimitWaivable);
        return vo;
    }

    @Override
    public Map<Long, Integer> specialFlagMap(List<Long> drugIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (drugIds == null || drugIds.isEmpty()) {
            return result;
        }
        List<Long> distinct = drugIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return result;
        }
        for (NarcoticRegisterMapper.DrugSpecialRow row : narcoticRegisterMapper.selectDrugSpecial(distinct)) {
            if (row.getDrugId() != null && row.getSpecialFlag() != null) {
                result.put(row.getDrugId(), row.getSpecialFlag());
            }
        }
        return result;
    }

    private Map<Long, NarcoticRegisterMapper.DrugSpecialRow> loadDrugSpecial(List<BizPrescriptionDetail> details) {
        List<Long> drugIds = details.stream().map(BizPrescriptionDetail::getDrugId)
                .filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (drugIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return narcoticRegisterMapper.selectDrugSpecial(drugIds).stream()
                .collect(Collectors.toMap(NarcoticRegisterMapper.DrugSpecialRow::getDrugId, r -> r, (a, b) -> a));
    }

    private NarcoticRegisterMapper.DrugSpecialRow loadDrugSpecialOne(Long drugId) {
        if (drugId == null) {
            return null;
        }
        List<NarcoticRegisterMapper.DrugSpecialRow> rows =
                narcoticRegisterMapper.selectDrugSpecial(Collections.singletonList(drugId));
        return rows.isEmpty() ? null : rows.get(0);
    }

    private BizNarcoticRegisterVO toVO(BizNarcoticRegister entity) {
        if (entity == null) {
            return null;
        }
        BizNarcoticRegisterVO vo = new BizNarcoticRegisterVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
