package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.NutritionStatsGenerateDTO;
import com.his.patient.dto.NutritionStatsQueryPageDTO;
import com.his.patient.entity.BizNutritionStats;
import com.his.patient.enums.StatsScopeEnum;
import com.his.patient.mapper.BizNutritionStatsMapper;
import com.his.patient.mapper.NutritionStatMapper;
import com.his.patient.service.NutritionStatsService;
import com.his.patient.support.NutritionRules;
import com.his.patient.vo.DeptStatRowVO;
import com.his.patient.vo.NutritionOverviewVO;
import com.his.patient.vo.NutritionStatsVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 营养膳食指标与看板实现（sql/168 §4）。
 *
 * <p>两条口径写死在这里：
 * <ol>
 *   <li><b>率值只做展示，分子分母必须落库</b>：评审查的是"9/10"，不是一个孤零零的 90.00%；
 *       而且只有分母在，才能看出"这个月只出院了 3 个人"这种统计上无意义的比值。</li>
 *   <li><b>试算与快照共用 {@link #compute}</b>：两套算法必然漂移，
 *       漂移的结果是"页面试算 92%，报出去的快照 88%"，而这种差异没人能解释。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionStatsServiceImpl implements NutritionStatsService {

    private static final DateTimeFormatter CSV_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BizNutritionStatsMapper statsMapper;
    private final NutritionStatMapper statMapper;

    @Override
    public NutritionOverviewVO overview() {
        LocalDate today = LocalDate.now();
        NutritionOverviewVO vo = new NutritionOverviewVO();

        int inHospital = (int) statMapper.countInHospital();
        int screened = (int) statMapper.countInHospitalScreened();
        vo.setInHospitalCount(inHospital);
        vo.setInHospitalScreenedCount(screened);
        vo.setMissedScreenCount(Math.max(inHospital - screened, 0));
        vo.setInHospitalRiskCount((int) statMapper.countInHospitalRisk());
        vo.setReScreenDueCount((int) statMapper.countReScreenDue());
        vo.setPendingConfirmPlanCount((int) statMapper.countPendingConfirmPlan());

        int meals = (int) statMapper.countMealOfDay(today);
        vo.setTodayMealCount(meals);
        vo.setTodayMealSignedCount((int) statMapper.countMealSignedOfDay(today));
        vo.setTodayMealPendingCount((int) statMapper.countMealPendingOfDay(today));
        vo.setTodayMealSignRate(rate(vo.getTodayMealSignedCount(), meals));

        vo.setConsultUnfinishedCount((int) statMapper.countConsultUnfinished());
        vo.setConsultOverdueCount((int) statMapper.countConsultOverdue());
        return vo;
    }

    @Override
    public NutritionStatsVO previewStats(String statMonth) {
        YearMonth ym = requireMonth(statMonth);
        BizNutritionStats row = compute(ym, statMonth.trim(), StatsScopeEnum.HOSPITAL.getCode(), null, null);
        NutritionStatsVO vo = toVO(row);
        vo.setRemark("实时试算（未落库）：与已生成快照可能存在差异，报数请以快照为准");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<NutritionStatsVO> generateStats(NutritionStatsGenerateDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) { throw new BusinessException("当前用户信息不存在"); }
        YearMonth ym = requireMonth(dto.getStatMonth());
        String operator = operatorUser.getRealName();
        List<NutritionStatsVO> result = new ArrayList<>();

        // scopeType 合法性由 DTO 的 @InEnum 把关（1-全院 2-科室），这里只分派
        if (Objects.equals(StatsScopeEnum.DEPT.getCode(), dto.getScopeType())) {
            List<DeptStatRowVO> depts = statMapper.selectDischargeDepts(from(ym), to(ym));
            if (CollectionUtils.isEmpty(depts)) {
                throw new BusinessException(ym + " 没有已出院患者，无法按科室生成快照");
            }
            for (DeptStatRowVO d : depts) {
                result.add(toVO(upsertRow(compute(ym, dto.getStatMonth(), StatsScopeEnum.DEPT.getCode(),
                        d.getDeptId(), d.getDeptName()), operator)));
            }
        } else {
            result.add(toVO(upsertRow(compute(ym, dto.getStatMonth(), StatsScopeEnum.HOSPITAL.getCode(),
                    null, null), operator)));
        }
        log.info("营养膳食指标生成 月份={} 范围={} 行数={} 操作人={}", dto.getStatMonth(), dto.getScopeType(),
                result.size(), operator);
        return result;
    }

    @Override
    public PageResult<NutritionStatsVO> statsListPage(NutritionStatsQueryPageDTO query) {
        Page<NutritionStatsVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<NutritionStatsVO> result = (Page<NutritionStatsVO>) statsMapper.selectStatsPage(page, query);
        result.getRecords().forEach(this::applyTargets);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public String statsExportCsv(NutritionStatsQueryPageDTO query) {
        List<NutritionStatsVO> rows = statsMapper.selectStatsForExport(query);
        StringBuilder sb = new StringBuilder(1024);
        sb.append('\uFEFF'); // BOM：Excel 打开中文不乱码
        sb.append("统计月份,范围,科室,出院患者数,已筛查人数,筛查率(%),筛查阳性人数,阳性率(%),"
                + "膳食方案数,已接收方案数,膳食医嘱执行率(%),营养会诊数,按时应答数,会诊及时应答率(%),"
                + "订餐条数,已签收条数,订餐签收率(%),退订条数,生成人,生成时间\n");
        for (NutritionStatsVO r : rows) {
            sb.append(csv(r.getStatMonth())).append(',')
                    .append(r.getScopeType() != null && r.getScopeType() == StatsScopeEnum.DEPT.getCode()
                            ? "科室" : "全院").append(',')
                    .append(csv(r.getDeptName())).append(',')
                    .append(n(r.getDischargeCount())).append(',')
                    .append(n(r.getScreenedCount())).append(',')
                    .append(r.getScreenRate()).append(',')
                    .append(n(r.getRiskCount())).append(',')
                    .append(r.getRiskRate()).append(',')
                    .append(n(r.getDietPlanCount())).append(',')
                    .append(n(r.getDietConfirmCount())).append(',')
                    .append(r.getDietConfirmRate()).append(',')
                    .append(n(r.getConsultCount())).append(',')
                    .append(n(r.getConsultOnTimeCount())).append(',')
                    .append(r.getConsultOnTimeRate()).append(',')
                    .append(n(r.getMealOrderCount())).append(',')
                    .append(n(r.getMealSignedCount())).append(',')
                    .append(r.getMealSignRate()).append(',')
                    .append(n(r.getMealCancelCount())).append(',')
                    .append(csv(r.getGenerateBy())).append(',')
                    .append(r.getGenerateTime() == null ? "" : CSV_TIME_FMT.format(r.getGenerateTime()))
                    .append('\n');
        }
        return sb.toString();
    }

    /**
     * 指标复算（试算与落库共用，保证两处口径一致）
     */
    private BizNutritionStats compute(YearMonth ym, String statMonth, int scopeType, Long deptId, String deptName) {
        LocalDateTime from = from(ym);
        LocalDateTime to = to(ym);
        LocalDate fromDate = ym.atDay(1);
        LocalDate toDate = ym.atEndOfMonth();

        BizNutritionStats row = new BizNutritionStats();
        row.setStatMonth(statMonth.trim());
        row.setScopeType(scopeType);
        row.setDeptId(deptId);
        row.setDeptName(deptName);

        long discharge = statMapper.countDischarge(from, to, deptId);
        long screened = statMapper.countScreened(from, to, deptId);
        long risk = statMapper.countRisk(from, to, deptId);
        long plans = statMapper.countDietPlan(from, to, deptId);
        long confirmed = statMapper.countDietConfirm(from, to, deptId);
        long consults = statMapper.countConsult(from, to, deptId);
        long consultOnTime = statMapper.countConsultOnTime(from, to, deptId);
        long meals = statMapper.countMeal(fromDate, toDate, deptId);
        long mealSigned = statMapper.countMealSigned(fromDate, toDate, deptId);
        long mealCancel = statMapper.countMealCancel(fromDate, toDate, deptId);

        row.setDischargeCount((int) discharge);
        row.setScreenedCount((int) screened);
        row.setScreenRate(rate(screened, discharge));
        row.setRiskCount((int) risk);
        row.setRiskRate(rate(risk, screened));
        row.setDietPlanCount((int) plans);
        row.setDietConfirmCount((int) confirmed);
        row.setDietConfirmRate(rate(confirmed, plans));
        row.setConsultCount((int) consults);
        row.setConsultOnTimeCount((int) consultOnTime);
        row.setConsultOnTimeRate(rate(consultOnTime, consults));
        row.setMealOrderCount((int) meals);
        row.setMealSignedCount((int) mealSigned);
        row.setMealSignRate(rate(mealSigned, meals));
        row.setMealCancelCount((int) mealCancel);
        return row;
    }

    /**
     * 同月同范围覆盖（唯一键 uk_nutrition_stats，本表无 del_flag，重算只更新同一行）
     */
    private BizNutritionStats upsertRow(BizNutritionStats row, String operator) {
        BizNutritionStats exist = statsMapper.selectOneSnapshot(row.getStatMonth(), row.getScopeType(),
                row.getDeptId());
        row.setGenerateBy(operator);
        row.setGenerateTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        if (exist == null) {
            statsMapper.insert(row);
            return row;
        }
        row.setId(exist.getId());
        row.setCreateTime(exist.getCreateTime());
        statsMapper.updateById(row);
        return row;
    }

    private NutritionStatsVO toVO(BizNutritionStats r) {
        NutritionStatsVO vo = new NutritionStatsVO();
        vo.setId(r.getId());
        vo.setStatMonth(r.getStatMonth());
        vo.setScopeType(r.getScopeType());
        vo.setScopeTypeText(r.getScopeType() != null && r.getScopeType() == StatsScopeEnum.DEPT.getCode()
                ? "科室" : "全院");
        vo.setDeptId(r.getDeptId());
        vo.setDeptName(r.getDeptName());
        vo.setDischargeCount(r.getDischargeCount());
        vo.setScreenedCount(r.getScreenedCount());
        vo.setScreenRate(r.getScreenRate());
        vo.setRiskCount(r.getRiskCount());
        vo.setRiskRate(r.getRiskRate());
        vo.setDietPlanCount(r.getDietPlanCount());
        vo.setDietConfirmCount(r.getDietConfirmCount());
        vo.setDietConfirmRate(r.getDietConfirmRate());
        vo.setConsultCount(r.getConsultCount());
        vo.setConsultOnTimeCount(r.getConsultOnTimeCount());
        vo.setConsultOnTimeRate(r.getConsultOnTimeRate());
        vo.setMealOrderCount(r.getMealOrderCount());
        vo.setMealSignedCount(r.getMealSignedCount());
        vo.setMealSignRate(r.getMealSignRate());
        vo.setMealCancelCount(r.getMealCancelCount());
        vo.setGenerateBy(r.getGenerateBy());
        vo.setGenerateTime(r.getGenerateTime());
        vo.setRemark(r.getRemark());
        return applyTargets(vo);
    }

    /**
     * 目标值随每行带出：阈值是评审口径，不该由前端写死一份
     */
    private NutritionStatsVO applyTargets(NutritionStatsVO vo) {
        vo.setScreenRateTarget(NutritionRules.TARGET_SCREEN_RATE);
        vo.setDietConfirmRateTarget(NutritionRules.TARGET_DIET_CONFIRM_RATE);
        vo.setConsultOnTimeRateTarget(NutritionRules.TARGET_CONSULT_ONTIME_RATE);
        vo.setMealSignRateTarget(NutritionRules.TARGET_MEAL_SIGN_RATE);
        return vo;
    }

    // 工具

    private LocalDateTime from(YearMonth ym) {
        return ym.atDay(1).atStartOfDay();
    }

    private LocalDateTime to(YearMonth ym) {
        return ym.atEndOfMonth().atTime(23, 59, 59);
    }

    private YearMonth requireMonth(String statMonth) {
        // ②非web入口：两个入口共用的「取值+解析」守卫。POST 侧 statMonth 必填已收口到 DTO @NotBlank + @Valid，
        // GET previewStats 用的是 @RequestParam（没有 DTO 字段可挂注解），故这里保留一句兜底
        if (!StringUtils.hasText(statMonth)) {
            throw new BusinessException("统计月份不能为空");
        }
        try {
            return YearMonth.parse(statMonth.trim());
        } catch (Exception e) {
            throw new BusinessException("统计月份格式不正确（yyyy-MM）");
        }
    }

    /**
     * 百分比（分母为 0 返回 0.00，不返回 NaN —— 空表跑出 NaN 会让人以为系统坏了）
     */
    private BigDecimal rate(long num, long den) {
        if (den <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(num).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(den), 2, RoundingMode.HALF_UP);
    }

    private String csv(String v) {
        if (!StringUtils.hasText(v)) {
            return "";
        }
        String s = v.trim();
        return s.contains(",") || s.contains("\"") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    private int n(Integer v) {
        return v == null ? 0 : v;
    }
}
