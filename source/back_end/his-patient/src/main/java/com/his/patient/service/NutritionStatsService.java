package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NutritionStatsGenerateDTO;
import com.his.patient.dto.NutritionStatsQueryPageDTO;
import com.his.patient.vo.NutritionOverviewVO;
import com.his.patient.vo.NutritionStatsVO;

import java.util.List;

/**
 * 营养膳食指标与看板服务（sql/168 §4）。
 */
public interface NutritionStatsService {

    /**
     * 看板（在院视角："今天该干什么"）
     */
    NutritionOverviewVO overview();

    /**
     * 实时试算（不落库）
     */
    NutritionStatsVO previewStats(String statMonth);

    /**
     * 生成/重算月度快照（同月同范围覆盖）
     */
    List<NutritionStatsVO> generateStats(NutritionStatsGenerateDTO dto);

    PageResult<NutritionStatsVO> statsListPage(NutritionStatsQueryPageDTO query);

    /**
     * 导出 CSV（BOM，上限 5000 行）
     */
    String statsExportCsv(NutritionStatsQueryPageDTO query);
}
