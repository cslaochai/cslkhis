package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.MealGenerateDTO;
import com.his.patient.dto.MealOrderQueryPageDTO;
import com.his.patient.dto.MealStatusDTO;
import com.his.patient.vo.MealGenerateVO;
import com.his.patient.vo.MealOrderVO;

import java.util.List;

/**
 * 订餐配送服务（sql/168 §3，院内工作站侧，不含金额）。
 *
 * <p>状态机 0-待配餐 → 1-已配餐 → 2-已配送 → 3-已签收，只许一级推进；4-已取消是旁路且必填原因。
 */
public interface MealOrderService {

    PageResult<MealOrderVO> mealListPage(MealOrderQueryPageDTO query);

    List<MealOrderVO> mealListByPlan(Long dietPlanId);

    /** 按执行中且口服的膳食方案批量生成某日餐单 */
    MealGenerateVO mealGenerate(MealGenerateDTO dto);

    /** 批量推进状态或退订（全成功或全不生效） */
    int mealStatus(MealStatusDTO dto);

    /** 删除误生成的餐行（物理删，撞 uk_meal_order） */
    int mealDeleteById(Long id);
}
