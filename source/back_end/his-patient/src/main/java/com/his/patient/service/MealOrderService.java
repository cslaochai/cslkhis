package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.MealGenerateDTO;
import com.his.patient.dto.MealOrderPageQueryDTO;
import com.his.patient.dto.MealStatusDTO;
import com.his.patient.vo.MealGenerateVO;
import com.his.patient.vo.MealOrderVO;

import java.util.List;

/**
 * 订餐配送服务（sql/168 §3，院内工作站侧，不含金额）。
 */
public interface MealOrderService {

    /**
     * 订餐明细分页：按餐期或日期区间、病区、住院次、餐次、饮食类别与配餐状态筛，数据范围按岗位科室收口。
     * 用于「订餐配送」页查询卡。
     */
    PageResult<MealOrderVO> mealListPage(MealOrderPageQueryDTO query);

    /**
     * 一条膳食方案已生成的餐单行。
     * 供按方案追查配送进度、停餐或作废前核对未配送行的环节取数。
     */
    List<MealOrderVO> mealListByPlan(Long dietPlanId);

    /**
     * 按执行中且口服的膳食方案批量生成某日餐单
     */
    MealGenerateVO mealGenerate(MealGenerateDTO dto);

    /**
     * 批量推进状态或退订（全成功或全不生效）
     */
    int mealStatus(MealStatusDTO dto);

    /**
     * 删除误生成的餐行（物理删，撞 uk_meal_order）
     */
    int mealDeleteById(Long id);
}
