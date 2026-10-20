package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NutritionScreenPageQueryDTO;
import com.his.patient.dto.NutritionScreenUpsertDTO;
import com.his.patient.vo.NutritionScreenVO;

import java.util.List;

/**
 * 营养风险筛查与评定服务（sql/168 §1）。
 */
public interface NutritionScreenService {

    /**
     * 筛查评定分页：dueOnly=1 时只返回到期未复筛的患者。
     * 用于「营养风险筛查」页查询卡与表格。
     */
    PageResult<NutritionScreenVO> screenListPage(NutritionScreenPageQueryDTO query);

    /**
     * 某次住院的筛查历史：按筛查时间倒序。
     * 供复筛登记前核对既往评分、以及营养会诊发起环节追溯筛查结论。
     */
    List<NutritionScreenVO> screenListByAdmission(Long admissionId);

    /**
     * 登记/修改筛查（总分、判定、BMI、复筛日期一律服务端算）
     */
    NutritionScreenVO screenUpsert(NutritionScreenUpsertDTO dto);

    /**
     * 删除筛查记录：仅限误录。
     * 用于「营养风险筛查」页列表操作列。
     */
    int screenDeleteById(Long id);
}
