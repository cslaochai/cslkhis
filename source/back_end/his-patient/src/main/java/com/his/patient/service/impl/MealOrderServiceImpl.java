package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.MealGenerateDTO;
import com.his.patient.dto.MealOrderQueryPageDTO;
import com.his.patient.dto.MealStatusDTO;
import com.his.patient.entity.BizDietPlan;
import com.his.patient.entity.BizMealOrder;
import com.his.patient.enums.DietRouteEnum;
import com.his.patient.enums.MealDeliverStatusEnum;
import com.his.patient.enums.MealOrderSourceEnum;
import com.his.patient.enums.PlanStatusEnum;
import com.his.patient.mapper.BizDietPlanMapper;
import com.his.patient.mapper.BizMealOrderMapper;
import com.his.patient.service.MealOrderService;
import com.his.patient.support.NutritionRules;
import com.his.patient.vo.MealGenerateVO;
import com.his.patient.vo.MealOrderVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订餐配送实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MealOrderServiceImpl extends ServiceImpl<BizMealOrderMapper, BizMealOrder> implements MealOrderService {
    private final RedisSequenceService redisSequenceService;
    private final DeptScopeService deptScopeService;
    private final BizMealOrderMapper bizMealOrderMapper;
    private final BizDietPlanMapper bizDietPlanMapper;

    // 批量生成

    @Override
    public PageResult<MealOrderVO> mealListPage(MealOrderQueryPageDTO query) {
        query.setKeyword(TextUtil.trim(query.getKeyword()));
        query.setDietCode(TextUtil.trim(query.getDietCode()));
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        if (allowed != null) {
            if (allowed.isEmpty()) {
                throw new BusinessException("当前岗位未绑定任何科室，无法查看相关数据");
            }
            query.setScopeDeptIds(new ArrayList<>(allowed));
        }
        Page<MealOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<MealOrderVO> result = (Page<MealOrderVO>) bizMealOrderMapper.selectMealPage(page, query);
        result.getRecords().forEach(this::decorate);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(),
                result.getRecords());
    }

    // 状态推进 / 退订 / 删除

    @Override
    public List<MealOrderVO> mealListByPlan(Long dietPlanId) {
        BizDietPlan plan = bizDietPlanMapper.selectById(dietPlanId);
        if (plan != null) {
            deptScopeService.assertDeptAccessible(plan.getDeptId());
        }
        List<MealOrderVO> rows = bizMealOrderMapper.selectByPlan(dietPlanId);
        rows.forEach(this::decorate);
        return rows;
    }

    /**
     * 下一步与按钮可用性由状态机现算（前端不自己判）
     */
    private void decorate(MealOrderVO vo) {
        Integer status = vo.getDeliverStatus();
        Integer next = NutritionRules.mealNextStatus(status);
        vo.setNextStatusText(next == null ? null : MealDeliverStatusEnum.getText(next));
        vo.setCanAdvance(next == null ? YesOrNoEnum.NO.getCode() : YesOrNoEnum.YES.getCode());
        vo.setCanCancel(Objects.equals(MealDeliverStatusEnum.PENDING.getCode(), status)
                || Objects.equals(MealDeliverStatusEnum.PREPARED.getCode(), status)
                || Objects.equals(MealDeliverStatusEnum.DELIVERED.getCode(), status)
                ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
    }

    // 工具

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MealGenerateVO mealGenerate(MealGenerateDTO dto) {
        LocalDate mealDate = dto.getMealDate();
        if (mealDate.isBefore(LocalDate.now())) {
            throw new BusinessException("不能为「" + mealDate + "」之前的日期生成餐单（食堂无法补送过去的餐）");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }

        LambdaQueryWrapper<BizDietPlan> wrapper = new LambdaQueryWrapper<BizDietPlan>()
                .eq(BizDietPlan::getPlanStatus, PlanStatusEnum.RUNNING.getCode())
                .eq(BizDietPlan::getRoute, DietRouteEnum.ORAL.getCode())
                // 出院/转科不自动停方案（本系统出院流程不停医嘱），但人走了还继续送饭是错的：
                // 生成餐单只认"当前在院"的住院
                .exists("SELECT 1 FROM biz_admission a WHERE a.admission_id = biz_diet_plan.admission_id"
                        + " AND a.del_flag = 0 AND a.admit_status = 1")
                .in(!CollectionUtils.isEmpty(dto.getWardIds()), BizDietPlan::getWardId, dto.getWardIds())
                .in(!CollectionUtils.isEmpty(dto.getAdmissionIds()), BizDietPlan::getAdmissionId, dto.getAdmissionIds())
                .le(BizDietPlan::getStartTime, TimeUtil.dayEnd(mealDate))
                .and(w -> w.isNull(BizDietPlan::getStopTime).or().ge(BizDietPlan::getStopTime, TimeUtil.dayStart(mealDate)))
                .orderByAsc(BizDietPlan::getWardId)
                .orderByAsc(BizDietPlan::getId);
        // 科室数据权限收口：受限岗位只给授权科室的方案配餐（空集合按配置缺失拒绝）
        Set<Long> allowedDepts = deptScopeService.allowedDeptIds();
        if (allowedDepts != null) {
            if (allowedDepts.isEmpty()) {
                throw new BusinessException("当前岗位未绑定任何科室，无法生成餐单");
            }
            wrapper.in(BizDietPlan::getDeptId, allowedDepts);
        }
        List<BizDietPlan> plans = bizDietPlanMapper.selectList(wrapper);

        MealGenerateVO vo = new MealGenerateVO();
        vo.setMealDate(mealDate);
        vo.setPlanCount(plans.size());
        if (plans.isEmpty()) {
            vo.setGeneratedCount(0);
            vo.setSkippedCount(0);
            vo.setMessage("没有符合条件的膳食方案（只给「执行中 + 口服」的方案配餐）");
            return vo;
        }

        List<Long> admissionIds = plans.stream().map(BizDietPlan::getAdmissionId)
                .filter(Objects::nonNull).distinct().toList();
        boolean overwrite = Boolean.TRUE.equals(dto.getOverwrite());

        // 已配送/已签收的餐是既成事实 → 这些人整体跳过，并把跳过数报回界面
        Set<Long> lockedAdmissions = new HashSet<>();
        List<BizMealOrder> existing = bizMealOrderMapper.selectList(new LambdaQueryWrapper<BizMealOrder>()
                .eq(BizMealOrder::getMealDate, mealDate)
                .in(BizMealOrder::getAdmissionId, admissionIds));
        for (BizMealOrder m : existing) {
            if (m.getDeliverStatus() != null && m.getDeliverStatus() >= MealDeliverStatusEnum.DELIVERED.getCode()) {
                lockedAdmissions.add(m.getAdmissionId());
            }
        }
        if (overwrite) {
            List<Long> purgeIds = admissionIds.stream().filter(id -> !lockedAdmissions.contains(id)).toList();
            if (!purgeIds.isEmpty()) {
                bizMealOrderMapper.purgePendingByDate(mealDate, purgeIds);
            }
        }
        Set<String> exists = new HashSet<>();
        if (!overwrite) {
            for (BizMealOrder m : existing) {
                exists.add(m.getAdmissionId() + "#" + m.getMealType());
            }
        }

        int generated = 0;
        // 同一个人可能同时有两条口服方案（如"糖尿病饮食 + 口服营养补充"），
        // 而 uk_meal_order 只认「人 + 日期 + 餐次」—— 批内必须去重，否则整批生成撞唯一键
        Set<String> batchKeys = new HashSet<>();
        LocalDateTime now = TimeUtil.nowSeconds();
        String operator = operatorUser.getRealName();
        for (BizDietPlan plan : plans) {
            if (lockedAdmissions.contains(plan.getAdmissionId())) {
                continue;
            }
            List<Integer> mealTypes = NutritionRules.mealTypesOf(plan.getMealTypes());
            for (Integer mealType : mealTypes) {
                String key = plan.getAdmissionId() + "#" + mealType;
                if (!batchKeys.add(key)) {
                    continue;
                }
                if (!overwrite && exists.contains(key)) {
                    continue;
                }
                BizMealOrder row = new BizMealOrder();
                row.setMealNo(redisSequenceService.generateMealOrderNo());
                row.setAdmissionId(plan.getAdmissionId());
                row.setPatientId(plan.getPatientId());
                row.setPatientNo(plan.getPatientNo());
                row.setPatientName(plan.getPatientName());
                row.setDeptId(plan.getDeptId());
                row.setDeptName(plan.getDeptName());
                row.setWardId(plan.getWardId());
                row.setWardName(plan.getWardName());
                row.setBedNo(plan.getBedNo());
                row.setDietPlanId(plan.getId());
                row.setDietCode(plan.getDietCode());
                row.setDietName(plan.getDietName());
                row.setMealDate(mealDate);
                row.setMealType(mealType);
                row.setQuantity(1);
                row.setDeliverStatus(MealDeliverStatusEnum.PENDING.getCode());
                row.setSource(MealOrderSourceEnum.GENERATE.getCode());
                row.setRemark(TextUtil.cut("按膳食方案 " + plan.getDietNo() + " 生成", 500));
                row.setCreateBy(operator);
                row.setCreateTime(now);
                bizMealOrderMapper.insert(row);
                generated++;
            }
        }
        vo.setGeneratedCount(generated);
        vo.setSkippedCount(lockedAdmissions.size());
        vo.setMessage(generated == 0
                ? "没有新增餐单（可能全部已生成，或方案未指定供应餐次）"
                : "已生成 " + generated + " 条餐单" + (lockedAdmissions.isEmpty() ? ""
                : "，跳过 " + lockedAdmissions.size() + " 人（当日餐已配送或已签收）"));
        log.info("订餐生成 日期={} 方案={} 生成={} 跳过={} 覆盖={} 操作人={}", mealDate, plans.size(),
                generated, lockedAdmissions.size(), overwrite, operator);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mealStatus(MealStatusDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Integer target = dto.getDeliverStatus();
        if (target == null || !NutritionRules.isMealStatus(target)
                || target == MealDeliverStatusEnum.PENDING.getCode()) {
            throw new BusinessException("目标状态不合法（1-已配餐 2-已配送 3-已签收 4-已取消）");
        }
        boolean cancel = Objects.equals(MealDeliverStatusEnum.CANCELED.getCode(), target);
        String cancelReason = TextUtil.cut(TextUtil.trim(dto.getCancelReason()), 500);
        if (cancel && !TextUtil.hasText(cancelReason)) {
            // ①条件必填：只有退订（目标状态=4-已取消）才必填原因，@NotBlank 会把正常的配餐/配送/签收请求挡成 400
            throw new BusinessException("退订必须填写原因（停餐/出院/拒餐/转科等）");
        }
        List<Long> ids = dto.getIds().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            // ②非web入口：兜「提交数组全为 null 元素」这种 @NotEmpty 拦不住的畸形入参
            throw new BusinessException("请选择要处理的订餐");
        }

        LocalDateTime now = TimeUtil.nowSeconds();
        String operator = operatorUser.getRealName();
        // 批量要么全推要么全不动：食堂按病区整批点"配送"，一半成功会让人以为都送出去了
        List<BizMealOrder> rows = new ArrayList<>(ids.size());
        for (Long id : ids) {
            BizMealOrder row = bizMealOrderMapper.selectById(id);
            if (row == null) {
                throw new BusinessException("订餐不存在（ID=" + id + "）");
            }
            deptScopeService.assertDeptAccessible(row.getDeptId());
            Integer current = row.getDeliverStatus();
            if (cancel) {
                if (Objects.equals(MealDeliverStatusEnum.SIGNED.getCode(), current)) {
                    throw new BusinessException("订餐 " + row.getMealNo() + " 已签收，不能退订；"
                            + "如确需撤回请作废本条并重新登记");
                }
                if (Objects.equals(MealDeliverStatusEnum.CANCELED.getCode(), current)) {
                    throw new BusinessException("订餐 " + row.getMealNo() + " 已退订，无需重复操作");
                }
            } else {
                Integer next = NutritionRules.mealNextStatus(current);
                if (next == null || !next.equals(target)) {
                    throw new BusinessException("订餐 " + row.getMealNo() + " 当前为「"
                            + MealDeliverStatusEnum.labelOrUnknown(current) + "」，只能推进到「"
                            + (next == null ? "无（已是终态）" : MealDeliverStatusEnum.getText(next)) + "」");
                }
            }
            rows.add(row);
        }

        for (BizMealOrder row : rows) {
            row.setDeliverStatus(target);
            row.setUpdateBy(operator);
            row.setUpdateTime(now);
            if (Objects.equals(MealDeliverStatusEnum.PREPARED.getCode(), target)) {
                row.setPrepareTime(now);
                if (TextUtil.hasText(dto.getDishContent())) {
                    row.setDishContent(TextUtil.cut(TextUtil.trim(dto.getDishContent()), 200));
                }
            } else if (Objects.equals(MealDeliverStatusEnum.DELIVERED.getCode(), target)) {
                row.setDeliverTime(now);
                row.setDeliverById(operatorUser.getEmployeeId());
                row.setDeliverByName(operator);
            } else if (Objects.equals(MealDeliverStatusEnum.SIGNED.getCode(), target)) {
                row.setSignTime(now);
                row.setSignBy(TextUtil.cut(TextUtil.hasText(dto.getSignBy()) ? TextUtil.trim(dto.getSignBy())
                        : (row.getPatientName() == null ? "病区护士" : row.getPatientName() + "（病区代签）"), 50));
            } else if (cancel) {
                row.setCancelTime(now);
                row.setCancelReason(cancelReason);
            }
            bizMealOrderMapper.updateById(row);
        }
        log.info("订餐状态推进 目标={} 条数={} 操作人={} ids={}", target, rows.size(), operator, ids);
        return rows.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mealDeleteById(Long id) {
        BizMealOrder row = bizMealOrderMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("订餐不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(row.getDeptId());
        // 删除是「这行从来没生成过」，退订才是「饭送出去了但不算数」。
        // 已配送/已签收的行删掉，食堂的份数与患者的吃饭记录同时对不上。
        if (row.getDeliverStatus() != null && row.getDeliverStatus() >= MealDeliverStatusEnum.DELIVERED.getCode()) {
            String st = MealDeliverStatusEnum.getText(row.getDeliverStatus());
            throw new BusinessException("该餐已" + st + "，不能删除；删除只用于误生成的行（" + row.getMealNo() + "）");
        }
        // 物理删：uk_meal_order 不含 del_flag，软删会占住"一人一天一餐"的键位
        return bizMealOrderMapper.purgeById(id);
    }

}