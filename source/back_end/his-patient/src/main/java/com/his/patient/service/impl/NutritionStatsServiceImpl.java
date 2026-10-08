package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.NutritionStatsGenerateDTO;
import com.his.patient.dto.NutritionStatsQueryPageDTO;
import com.his.patient.entity.BizNutritionStats;
import com.his.patient.enums.StatsScopeEnum;
import com.his.patient.mapper.BizNutritionStatsMapper;
import com.his.patient.mapper.NutritionStatMapper;
import com.his.patient.service.NutritionStatsService;
import com.his.patient.support.NutritionRules;
import com.his.patient.vo.DeptCountRowVO;
import com.his.patient.vo.NutritionOverviewVO;
import com.his.patient.vo.NutritionStatsVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 营养膳食指标与看板实现（sql/168 §4）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionStatsServiceImpl extends ServiceImpl<BizNutritionStatsMapper, BizNutritionStats> implements NutritionStatsService {

    private final BizNutritionStatsMapper bizNutritionStatsMapper;
    private final NutritionStatMapper nutritionStatMapper;

    @Override
    public NutritionOverviewVO overview() {
        LocalDate today = LocalDate.now();
        NutritionOverviewVO vo = new NutritionOverviewVO();

        int inHospital = (int) nutritionStatMapper.countInHospital();
        int screened = (int) nutritionStatMapper.countInHospitalScreened();
        vo.setInHospitalCount(inHospital);
        vo.setInHospitalScreenedCount(screened);
        vo.setMissedScreenCount(Math.max(inHospital - screened, 0));
        vo.setInHospitalRiskCount((int) nutritionStatMapper.countInHospitalRisk());
        vo.setReScreenDueCount((int) nutritionStatMapper.countReScreenDue());
        vo.setPendingConfirmPlanCount((int) nutritionStatMapper.countPendingConfirmPlan());

        int meals = (int) nutritionStatMapper.countMealOfDay(today);
        vo.setTodayMealCount(meals);
        vo.setTodayMealSignedCount((int) nutritionStatMapper.countMealSignedOfDay(today));
        vo.setTodayMealPendingCount((int) nutritionStatMapper.countMealPendingOfDay(today));
        vo.setTodayMealSignRate(rate(vo.getTodayMealSignedCount(), meals));

        vo.setConsultUnfinishedCount((int) nutritionStatMapper.countConsultUnfinished());
        vo.setConsultOverdueCount((int) nutritionStatMapper.countConsultOverdue());
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
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        YearMonth ym = requireMonth(dto.getStatMonth());
        String operator = operatorUser.getRealName();
        List<NutritionStatsVO> result = new ArrayList<>();

        // scopeType 合法性由 DTO 的 @InEnum 把关（1-全院 2-科室），这里只分派
        if (Objects.equals(StatsScopeEnum.DEPT.getCode(), dto.getScopeType())) {
            List<DeptCountRowVO> depts = nutritionStatMapper.selectDischargeDepts(from(ym), to(ym));
            if (CollectionUtils.isEmpty(depts)) {
                throw new BusinessException(ym + " 没有已出院患者，无法按科室生成快照");
            }
            for (DeptCountRowVO d : depts) {
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
        Page<NutritionStatsVO> result = (Page<NutritionStatsVO>) bizNutritionStatsMapper.selectStatsPage(page, query);
        result.getRecords().forEach(this::applyTargets);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    @Override
    public String statsExportCsv(NutritionStatsQueryPageDTO query) {
        List<NutritionStatsVO> rows = bizNutritionStatsMapper.selectStatsForExport(query);
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
                    .append(r.getGenerateTime() == null ? "" : DateFormats.DATETIME.format(r.getGenerateTime()))
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

        long discharge = nutritionStatMapper.countDischarge(from, to, deptId);
        long screened = nutritionStatMapper.countScreened(from, to, deptId);
        long risk = nutritionStatMapper.countRisk(from, to, deptId);
        long plans = nutritionStatMapper.countDietPlan(from, to, deptId);
        long confirmed = nutritionStatMapper.countDietConfirm(from, to, deptId);
        long consults = nutritionStatMapper.countConsult(from, to, deptId);
        long consultOnTime = nutritionStatMapper.countConsultOnTime(from, to, deptId);
        long meals = nutritionStatMapper.countMeal(fromDate, toDate, deptId);
        long mealSigned = nutritionStatMapper.countMealSigned(fromDate, toDate, deptId);
        long mealCancel = nutritionStatMapper.countMealCancel(fromDate, toDate, deptId);

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
        BizNutritionStats exist = bizNutritionStatsMapper.selectOneSnapshot(row.getStatMonth(), row.getScopeType(),
                row.getDeptId());
        row.setGenerateBy(operator);
        row.setGenerateTime(TimeUtil.nowSeconds());
        if (exist == null) {
            bizNutritionStatsMapper.insert(row);
            return row;
        }
        row.setId(exist.getId());
        row.setCreateTime(exist.getCreateTime());
        bizNutritionStatsMapper.updateById(row);
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
        return TimeUtil.dayStart(ym.atDay(1));
    }

    private LocalDateTime to(YearMonth ym) {
        return TimeUtil.dayEnd(ym.atEndOfMonth());
    }

    private YearMonth requireMonth(String statMonth) {
        // C-非 web 入参：私有 requireXxx helper，除 DTO 入口外还被 previewStats 的 GET 标量参数复用
        // （@RequestParam String 只保证「带了参数」，空串照样进来），Bean Validation 不覆盖，保留
        if (!TextUtil.hasText(statMonth)) {
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
        if (!TextUtil.hasText(v)) {
            return "";
        }
        String s = v.trim();
        return s.contains(",") || s.contains("\"") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    private int n(Integer v) {
        return v == null ? 0 : v;
    }
}
