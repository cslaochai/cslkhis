package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NutritionScreenQueryPageDTO;
import com.his.patient.dto.NutritionScreenUpsertDTO;
import com.his.patient.vo.NutritionScreenVO;

import java.util.List;

/**
 * 营养风险筛查与评定服务（sql/168 §1）。
 */
public interface NutritionScreenService {

    PageResult<NutritionScreenVO> screenListPage(NutritionScreenQueryPageDTO query);

    List<NutritionScreenVO> screenListByAdmission(Long admissionId);

    /**
     * 登记/修改筛查（总分、判定、BMI、复筛日期一律服务端算）
     */
    NutritionScreenVO screenUpsert(NutritionScreenUpsertDTO dto);

    int screenDeleteById(Long id);
}
