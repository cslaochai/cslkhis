package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NutritionScreenQueryPageDTO;
import com.his.patient.dto.NutritionScreenUpsertDTO;
import com.his.patient.vo.NutritionScreenVO;

import java.util.List;

/**
 * 营养风险筛查与评定服务（sql/168 §1）。
 *
 * <p>口径：NRS2002 总分 = 营养状态受损(0~3) + 疾病严重程度(0~3) + 年龄(≥70 岁 1 分)，
 * <b>总分 ≥3 判为有营养风险</b>；判阴性者自动留下 7 天后的复筛日期。
 */
public interface NutritionScreenService {

    PageResult<NutritionScreenVO> screenListPage(NutritionScreenQueryPageDTO query);

    List<NutritionScreenVO> screenListByAdmission(Long admissionId);

    /** 登记/修改筛查（总分、判定、BMI、复筛日期一律服务端算） */
    NutritionScreenVO screenUpsert(NutritionScreenUpsertDTO dto);

    int screenDeleteById(Long id);
}
