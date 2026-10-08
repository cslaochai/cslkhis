package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.DietConfirmDTO;
import com.his.patient.dto.DietPlanQueryPageDTO;
import com.his.patient.dto.DietPlanStopDTO;
import com.his.patient.dto.DietPlanUpsertDTO;
import com.his.patient.entity.*;
import com.his.patient.enums.*;
import com.his.patient.mapper.*;
import com.his.patient.service.DietPlanService;
import com.his.patient.support.NutritionRules;
import com.his.patient.vo.DietPlanVO;
import com.his.patient.vo.DietTypeOptionVO;
import com.his.patient.vo.WardVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 膳食方案实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietPlanServiceImpl extends ServiceImpl<BizDietPlanMapper, BizDietPlan> implements DietPlanService {
    private final RedisSequenceService redisSequenceService;
    private final DeptScopeProvider deptScopeProvider;
    private final BizDietPlanMapper bizDietPlanMapper;
    private final BizMealOrderMapper bizMealOrderMapper;
    private final BizAdmissionMapper bizAdmissionMapper;
    private final BizPatientMapper bizPatientMapper;
    private final SysBedMapper sysBedMapper;
    private final NutritionStatMapper nutritionStatMapper;

    // 下拉 / 查询

    private static String appendRemark(String origin, String add) {
        if (!TextUtil.hasText(origin)) {
            return add;
        }
        return origin.trim() + "；" + add;
    }

    @Override
    public List<DietTypeOptionVO> dietTypeOptions() {
        List<DietTypeOptionVO> list = new ArrayList<>();
        for (NutritionRules.Diet d : NutritionRules.DIETS) {
            if (NutritionRules.CODE_TO_DETERMINE.equals(d.code())) {
                continue; // 占位档不是选项
            }
            DietTypeOptionVO vo = new DietTypeOptionVO();
            vo.setCode(d.code());
            vo.setName(d.name());
            vo.setCategory(d.category());
            vo.setCategoryText(DietCategoryEnum.getText(d.category()));
            vo.setRoute(d.route());
            vo.setRouteText(DietRouteEnum.getText(d.route()));
            vo.setNeedsMeal(NutritionRules.needsMealDelivery(d.route()));
            vo.setCalorieTarget(d.calorie());
            vo.setProteinTarget(d.protein());
            vo.setMealTypes(d.mealTypes());
            vo.setMealTypesText(NutritionRules.mealTypesText(d.mealTypes()));
            vo.setDesc(d.desc());
            list.add(vo);
        }
        return list;
    }

    // 登记 / 修改

    @Override
    public List<WardVO> wardOptions() {
        return sysBedMapper.selectWardList();
    }

    @Override
    public PageResult<DietPlanVO> planListPage(DietPlanQueryPageDTO query) {
        query.setKeyword(TextUtil.trim(query.getKeyword()));
        query.setDietCode(TextUtil.trim(query.getDietCode()));
        applyDeptScope(query);
        Page<DietPlanVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<DietPlanVO> result = (Page<DietPlanVO>) bizDietPlanMapper.selectPlanPage(page, query);
        result.getRecords().forEach(this::decorate);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    // 营养科接收 / 退回

    @Override
    public List<DietPlanVO> planListByAdmission(Long admissionId) {
        List<DietPlanVO> rows = bizDietPlanMapper.selectByAdmission(admissionId);
        rows.forEach(this::decorate);
        return rows;
    }

    // 停餐 / 删除

    /**
     * 餐次文案由规则表算（SQL 里再抄一份映射迟早和 NutritionRules 漂移）
     */
    private void decorate(DietPlanVO vo) {
        vo.setMealTypesText(NutritionRules.mealTypesText(vo.getMealTypes()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DietPlanVO planUpsert(DietPlanUpsertDTO dto) {
        String code = TextUtil.trim(dto.getDietCode());
        NutritionRules.Diet diet = NutritionRules.dietOf(code);
        if (diet == null) {
            throw new BusinessException("饮食类型码不合法，请从饮食目录中选择");
        }
        if (NutritionRules.CODE_TO_DETERMINE.equals(diet.code())) {
            throw new BusinessException("「待指定饮食」是医嘱派生的占位档，登记方案时必须选定真实饮食类型");
        }

        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDietPlan row;
        boolean insert = dto.getId() == null;
        if (insert) {
            BizAdmission admission = requireAdmission(dto.getAdmissionId());
            BizPatient patient = admission.getPatientId() == null ? null
                    : bizPatientMapper.selectById(admission.getPatientId());
            row = new BizDietPlan();
            row.setDietNo(redisSequenceService.generateDietPlanNo());
            row.setAdmissionId(admission.getAdmissionId());
            row.setPatientId(admission.getPatientId());
            row.setPatientNo(patient == null ? null : patient.getPatientNo());
            row.setPatientName(patient == null ? null : patient.getPatientName());
            row.setDeptId(admission.getDeptId());
            row.setDeptName(admission.getDeptId() == null ? null : nutritionStatMapper.selectDeptName(admission.getDeptId()));
            row.setWardId(admission.getWardId());
            row.setWardName(wardName(admission.getWardId()));
            row.setBedNo(bedNo(admission.getBedId()));
            row.setSource(DietPlanSourceEnum.NUTRITIONIST.getCode());
            row.setPlanStatus(PlanStatusEnum.RUNNING.getCode());
            // 营养师自己登记的方案不需要"自己接收自己"，直接计为已接收
            row.setConfirmStatus(DietConfirmStatusEnum.DONE.getCode());
            row.setConfirmTime(TimeUtil.nowSeconds());
            row.setConfirmerId(UserUtils.getCurrentUser().getEmployeeId());
            row.setConfirmerName(operatorUser.getRealName());
        } else {
            row = bizDietPlanMapper.selectById(dto.getId());
            if (row == null) {
                throw new BusinessException("膳食方案不存在或已删除");
            }
            if (Objects.equals(PlanStatusEnum.CANCELED.getCode(), row.getPlanStatus())) {
                throw new BusinessException("方案已作废，不能再修改；请重新登记");
            }
            if (Objects.equals(DietConfirmStatusEnum.REJECTED.getCode(), row.getConfirmStatus())) {
                // 改完回到待接收：退回的单不许"悄悄变绿"，必须由营养科重新接一次
                row.setConfirmStatus(DietConfirmStatusEnum.PENDING.getCode());
                row.setRejectReason(null);
            }
        }

        row.setDietCode(diet.code());
        row.setDietCategory(diet.category());
        row.setDietName(TextUtil.hasText(dto.getDietName()) ? TextUtil.trim(dto.getDietName()) : diet.name());
        row.setRoute(diet.route());
        row.setFeedWay(TextUtil.cut(TextUtil.trim(dto.getFeedWay()), 100));
        row.setCalorieTarget(dto.getCalorieTarget() == null ? diet.calorie() : dto.getCalorieTarget());
        row.setProteinTarget(dto.getProteinTarget() == null ? diet.protein() : dto.getProteinTarget());
        row.setFluidTarget(dto.getFluidTarget());
        row.setMealTypes(normalizeMealTypes(dto.getMealTypes(), diet));
        if (row.getStartTime() == null) {
            row.setStartTime(TimeUtil.toSeconds(dto.getStartTime() == null ? LocalDateTime.now() : dto.getStartTime()));
        }
        row.setRemark(TextUtil.cut(TextUtil.trim(dto.getRemark()), 500));

        if (insert) {
            try {
                bizDietPlanMapper.insert(row);
            } catch (DuplicateKeyException e) {
                throw new BusinessException("膳食方案号生成冲突（并发登记），请重试");
            }
            log.info("膳食方案登记 住院={} 饮食={} 途径={} 操作人={}", row.getAdmissionId(), diet.code(),
                    diet.route(), operatorUser.getRealName());
        } else {
            bizDietPlanMapper.updateById(row);
            log.info("膳食方案修改 id={} 饮食={} 操作人={}", row.getId(), diet.code(), operatorUser.getRealName());
        }
        return bizDietPlanMapper.selectVoById(row.getId());
    }

    // 医嘱链钩子

    /**
     * 餐次：提交值优先但必须落在 1~4 且去重；不合法一律回退目录默认（订餐据此拆行，脏值会让整批生成漏餐）
     */
    private String normalizeMealTypes(String submitted, NutritionRules.Diet diet) {
        String base = TextUtil.hasText(submitted) ? submitted : diet.mealTypes();
        List<Integer> types = NutritionRules.mealTypesOf(base);
        if (types.isEmpty()) {
            return null;
        }
        return types.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int planConfirm(DietConfirmDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        boolean accept = Boolean.TRUE.equals(dto.getAccept());
        String reason = TextUtil.cut(TextUtil.trim(dto.getRejectReason()), 500);
        if (!accept && !TextUtil.hasText(reason)) {
            // ①条件必填：只有退回（accept=false）才必填原因，@NotBlank 会把合法的接收请求挡成 400
            throw new BusinessException("退回膳食方案必须填写原因");
        }
        List<Long> ids = dto.getIds().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            // ②非web入口：兜「提交数组全为 null 元素」这种 @NotEmpty 拦不住的畸形入参
            throw new BusinessException("请选择要处理的膳食方案");
        }

        LocalDateTime now = TimeUtil.nowSeconds();
        // 批量要么全成要么全不动：一半接收一半报错，营养科说不清哪些单子已经生效
        List<BizDietPlan> rows = new ArrayList<>(ids.size());
        for (Long id : ids) {
            BizDietPlan row = bizDietPlanMapper.selectById(id);
            if (row == null) {
                throw new BusinessException("膳食方案不存在（ID=" + id + "）");
            }
            if (!Objects.equals(PlanStatusEnum.RUNNING.getCode(), row.getPlanStatus())) {
                throw new BusinessException("方案 " + row.getDietNo() + " 已"
                        + PlanStatusEnum.labelOrUnknown(row.getPlanStatus()) + "，不能再接收或退回");
            }
            if (Objects.equals(DietConfirmStatusEnum.DONE.getCode(), row.getConfirmStatus())) {
                throw new BusinessException("方案 " + row.getDietNo() + " 已接收，无需重复操作");
            }
            if (accept && NutritionRules.CODE_TO_DETERMINE.equals(row.getDietCode())) {
                throw new BusinessException("方案 " + row.getDietNo() + " 的饮食类型仍是「待指定饮食」，"
                        + "请先选定真实饮食类型再接收（饮食类型决定食堂做什么饭）");
            }
            rows.add(row);
        }

        for (BizDietPlan row : rows) {
            row.setConfirmStatus(accept ? DietConfirmStatusEnum.DONE.getCode() : DietConfirmStatusEnum.REJECTED.getCode());
            row.setConfirmTime(now);
            row.setConfirmerId(UserUtils.getCurrentUser().getEmployeeId());
            row.setConfirmerName(operatorUser.getRealName());
            row.setRejectReason(accept ? null : reason);
            bizDietPlanMapper.updateById(row);
        }
        log.info("膳食方案{} 条数={} 操作人={} ids={}", accept ? "接收" : "退回", rows.size(), operatorUser.getRealName(), ids);
        return rows.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DietPlanVO planStop(DietPlanStopDTO dto) {
        BizDietPlan row = bizDietPlanMapper.selectById(dto.getId());
        if (row == null) {
            throw new BusinessException("膳食方案不存在或已删除");
        }
        if (!Objects.equals(PlanStatusEnum.RUNNING.getCode(), row.getPlanStatus())) {
            throw new BusinessException("方案 " + row.getDietNo() + " 当前为「"
                    + PlanStatusEnum.labelOrUnknown(row.getPlanStatus()) + "」，不需要再停止");
        }
        LocalDateTime stopTime = TimeUtil.toSeconds(dto.getStopTime() == null ? LocalDateTime.now() : dto.getStopTime());
        applyStop(row, stopTime, TextUtil.trim(dto.getReason()));
        return bizDietPlanMapper.selectVoById(row.getId());
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int planDeleteById(Long id) {
        BizDietPlan row = bizDietPlanMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("膳食方案不存在或已删除");
        }
        // 物理删：uk_diet_plan_order 不含 del_flag，软删会占住这条医嘱的键位
        return bizDietPlanMapper.purgeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long deriveFromOrder(BizInpatientOrder order) {
        if (order == null || order.getId() == null) {
            return null;
        }
        BizDietPlan exist = bizDietPlanMapper.selectOne(new LambdaQueryWrapper<BizDietPlan>()
                .eq(BizDietPlan::getOrderId, order.getId())
                .last("LIMIT 1"));
        if (exist != null) {
            // 幂等：重复校对（或接口重放）不再新建，也不覆盖营养师已经改过的饮食类型
            return exist.getId();
        }

        String guessed = NutritionRules.guessDietCode(order.getItemCode(), order.getItemName());
        NutritionRules.Diet diet = NutritionRules.dietOf(guessed);
        String code = diet == null ? NutritionRules.CODE_TO_DETERMINE : diet.code();

        BizDietPlan row = new BizDietPlan();
        row.setDietNo(redisSequenceService.generateDietPlanNo());
        row.setAdmissionId(order.getAdmissionId());
        row.setPatientId(order.getPatientId());
        row.setPatientNo(order.getPatientNo());
        row.setPatientName(order.getPatientName());
        row.setDeptId(order.getDeptId());
        row.setDeptName(order.getDeptName());
        row.setWardId(order.getWardId());
        row.setWardName(order.getWardName());
        row.setBedNo(order.getBedNo());
        row.setOrderId(order.getId());
        row.setOrderNo(order.getOrderNo());
        row.setSource(DietPlanSourceEnum.ORDER_DERIVED.getCode());
        row.setDietCode(code);
        row.setDietCategory(diet == null ? DietCategoryEnum.BASIC.getCode() : diet.category());
        // 名称一律用医嘱原文（"个体化糖尿病饮食"这类描述比目录名更准），认不出时给占位文案
        row.setDietName(TextUtil.cut(TextUtil.hasText(order.getItemName())
                ? order.getItemName().trim() : (diet == null ? "待指定饮食" : diet.name()), 100));
        row.setRoute(diet == null ? DietRouteEnum.ORAL.getCode() : diet.route());
        row.setFeedWay(TextUtil.cut(TextUtil.trim(order.getRoute()), 100));
        row.setCalorieTarget(diet == null ? null : diet.calorie());
        row.setProteinTarget(diet == null ? null : diet.protein());
        row.setMealTypes(diet == null ? null : normalizeMealTypes(null, diet));
        row.setStartTime(TimeUtil.toSeconds(order.getStartTime() == null ? LocalDateTime.now() : order.getStartTime()));
        row.setPlanStatus(PlanStatusEnum.RUNNING.getCode());
        row.setConfirmStatus(DietConfirmStatusEnum.PENDING.getCode());
        row.setRemark(TextUtil.cut("由医嘱 " + order.getOrderNo() + " 校对派生", 500));
        try {
            bizDietPlanMapper.insert(row);
        } catch (DuplicateKeyException e) {
            BizDietPlan race = bizDietPlanMapper.selectOne(new LambdaQueryWrapper<BizDietPlan>()
                    .eq(BizDietPlan::getOrderId, order.getId()).last("LIMIT 1"));
            log.warn("膳食方案派生撞唯一键（并发校对）orderNo={} 已存在方案={}", order.getOrderNo(),
                    race == null ? null : race.getDietNo());
            return race == null ? null : race.getId();
        }
        log.info("膳食方案派生 医嘱={} 患者={} 饮食={} 途径={} 订餐={}", order.getOrderNo(), order.getPatientName(),
                code, row.getRoute(), NutritionRules.needsMealDelivery(row.getRoute()) ? "是" : "否");
        return row.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopFromOrder(Long orderId, LocalDateTime stopTime, String reason) {
        BizDietPlan row = findPlanByOrderId(orderId);
        if (row == null || !Objects.equals(PlanStatusEnum.RUNNING.getCode(), row.getPlanStatus())) {
            return;
        }
        applyStop(row, TimeUtil.toSeconds(stopTime == null ? LocalDateTime.now() : stopTime), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFromOrder(Long orderId) {
        BizDietPlan row = findPlanByOrderId(orderId);
        if (row == null || Objects.equals(PlanStatusEnum.CANCELED.getCode(), row.getPlanStatus())) {
            return;
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        row.setPlanStatus(PlanStatusEnum.CANCELED.getCode());
        row.setStopTime(now);
        row.setRemark(TextUtil.cut(appendRemark(row.getRemark(), "来源医嘱已作废"), 500));
        bizDietPlanMapper.updateById(row);
        cancelFutureMeals(row.getId(), now.toLocalDate(), "来源膳食医嘱作废，方案同步作废");
        log.info("膳食方案作废 方案={} 医嘱ID={}", row.getDietNo(), orderId);
    }

    private BizDietPlan findPlanByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return bizDietPlanMapper.selectOne(new LambdaQueryWrapper<BizDietPlan>()
                .eq(BizDietPlan::getOrderId, orderId).last("LIMIT 1"));
    }

    /**
     * 停止方案 + 退订未送出的未来餐（已配送/已签收的既成事实不动）
     */
    private void applyStop(BizDietPlan row, LocalDateTime stopTime, String reason) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        row.setPlanStatus(PlanStatusEnum.STOPPED.getCode());
        row.setStopTime(stopTime);
        row.setRemark(TextUtil.cut(appendRemark(row.getRemark(),
                "停止：" + (TextUtil.hasText(reason) ? reason.trim() : "营养师手工停餐")), 500));
        bizDietPlanMapper.updateById(row);
        cancelFutureMeals(row.getId(), stopTime.toLocalDate(),
                TextUtil.cut("方案停止（" + row.getDietNo() + "）" + (TextUtil.hasText(reason) ? reason.trim() : ""), 500));
        log.info("膳食方案停止 方案={} 停止时间={} 操作人={}", row.getDietNo(), stopTime, operatorUser.getRealName());
    }

    private void cancelFutureMeals(Long dietPlanId, LocalDate fromDate, String reason) {
        if (dietPlanId == null) {
            return;
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        int n = bizMealOrderMapper.update(null, new LambdaUpdateWrapper<BizMealOrder>()
                .eq(BizMealOrder::getDietPlanId, dietPlanId)
                .ge(BizMealOrder::getMealDate, fromDate)
                .in(BizMealOrder::getDeliverStatus, MealDeliverStatusEnum.PENDING.getCode(), MealDeliverStatusEnum.PREPARED.getCode())
                .set(BizMealOrder::getDeliverStatus, MealDeliverStatusEnum.CANCELED.getCode())
                .set(BizMealOrder::getCancelTime, TimeUtil.nowSeconds())
                .set(BizMealOrder::getCancelReason, reason)
                .set(BizMealOrder::getUpdateBy, operatorUser.getRealName())
                .set(BizMealOrder::getUpdateTime, TimeUtil.nowSeconds()));
        if (n > 0) {
            log.info("膳食方案停/废联动退订 方案={} 条数={} 原因={}", dietPlanId, n, reason);
        }
    }

    private void applyDeptScope(DietPlanQueryPageDTO query) {
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        if (allowed != null) {
            query.setScopeDeptIds(new ArrayList<>(allowed));
        }
    }

    private BizAdmission requireAdmission(Long admissionId) {
        BizAdmission admission = bizAdmissionMapper.selectById(admissionId);
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        return admission;
    }

    private String wardName(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = sysBedMapper.selectWardById(wardId);
        return ward == null ? null : ward.getWardName();
    }

    private String bedNo(Long bedId) {
        if (bedId == null) {
            return null;
        }
        SysBed bed = sysBedMapper.selectById(bedId);
        return bed == null ? null : bed.getBedNo();
    }

}
