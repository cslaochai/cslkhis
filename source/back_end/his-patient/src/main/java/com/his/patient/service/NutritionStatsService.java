package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NutritionStatsGenerateDTO;
import com.his.patient.dto.NutritionStatsQueryPageDTO;
import com.his.patient.vo.NutritionOverviewVO;
import com.his.patient.vo.NutritionStatsVO;

import java.util.List;

/**
 * 营养膳食指标与看板服务（sql/168 §4）。
 *
 * <p>五项指标口径（分母一律"同期出院患者"，与 VTE/抗菌药物监测按出院归月一致）：
 * <ol>
 *   <li>营养风险筛查率 = 出院前做过 NRS2002 的人数 / 出院人数</li>
 *   <li>筛查阳性率 = 最新一次 NRS2002 总分≥3 的人数 / 已筛查人数</li>
 *   <li>膳食医嘱执行率 = 营养科已接收方案数 / 膳食方案总数</li>
 *   <li>营养会诊及时应答率 = 按时应答数 / 营养会诊数（急≤10 分钟、普通≤24 小时）</li>
 *   <li>订餐签收率 = 已签收明细数 / 未取消明细数</li>
 * </ol>
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
