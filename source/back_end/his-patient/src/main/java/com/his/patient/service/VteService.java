package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.*;
import com.his.patient.vo.*;

import java.util.List;

/**
 * VTE 防控：中高危名单 / 预防措施落实 / 事件登记 / 月度指标。
 */
public interface VteService {

    /**
     * 看板：在院中高危多少人、多少人还没落实、本月发生了几例
     */
    VteOverviewVO overview();

    /**
     * 风险名单（默认只给中高危；风险来自每次住院最新一条 Caprini 评估）
     */
    PageResult<VteRiskListVO> riskListPage(VteRiskQueryPageDTO query);

    /**
     * 预防措施记录分页
     */
    PageResult<VtePreventVO> preventListPage(VtePreventQueryPageDTO query);

    /**
     * 某次住院的措施记录（按措施类别排序）
     */
    List<VtePreventVO> preventListByAdmission(Long admissionId);

    /**
     * 登记/落实措施（风险等级与措施类别服务端补全）
     */
    VtePreventVO preventUpsert(VtePreventUpsertDTO dto);

    int preventDeleteById(Long id);

    /**
     * 措施项下拉（recommend 由风险等级算）
     */
    List<VteMeasureOptionVO> measureOptions(Integer riskLevel);

    /**
     * VTE 事件分页
     */
    PageResult<VteEventVO> eventListPage(VteEventQueryPageDTO query);

    List<VteEventVO> eventListByAdmission(Long admissionId);

    /**
     * 登记/修改 VTE 事件
     */
    VteEventVO eventUpsert(VteEventUpsertDTO dto);

    int eventDeleteById(Long id);

    /**
     * 实时试算（不落库；报数以快照为准）
     */
    VteStatsVO previewStats(String statMonth);

    /**
     * 生成/重算月度快照（同月同范围覆盖）
     */
    List<VteStatsVO> generateStats(VteStatsGenerateDTO dto);

    PageResult<VteStatsVO> statsListPage(VteStatsQueryPageDTO query);

    /**
     * 导出 CSV（BOM，上限 5000 行）
     */
    String statsExportCsv(VteStatsQueryPageDTO query);
}
