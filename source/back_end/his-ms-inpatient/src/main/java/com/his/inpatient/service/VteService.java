package com.his.inpatient.service;

import com.his.common.base.PageResult;
import com.his.inpatient.dto.*;
import com.his.inpatient.vo.*;

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
    PageResult<VteRiskListVO> riskListPage(VteRiskPageQueryDTO query);

    /**
     * 预防措施记录分页
     */
    PageResult<VtePreventVO> preventListPage(VtePreventPageQueryDTO query);

    /**
     * 某次住院的措施记录（按措施类别排序）
     */
    List<VtePreventVO> preventListByAdmission(Long admissionId);

    /**
     * 登记/落实措施（风险等级与措施类别服务端补全）
     */
    VtePreventVO preventUpsert(VtePreventUpsertDTO dto);

    /**
     * 删除一条预防措施记录。
     * 用于「VTE 风险防控」页落实记录行的删除。
     */
    int preventDeleteById(Long id);

    /**
     * 措施项下拉（recommend 由风险等级算）
     */
    List<VteMeasureOptionVO> measureOptions(Integer riskLevel);

    /**
     * VTE 事件分页
     */
    PageResult<VteEventVO> eventListPage(VteEventPageQueryDTO query);

    /**
     * 某次住院登记的全部 VTE 事件。
     * 供院内监测按患者维度追溯本次住院的不良事件明细。
     */
    List<VteEventVO> eventListByAdmission(Long admissionId);

    /**
     * 登记/修改 VTE 事件
     */
    VteEventVO eventUpsert(VteEventUpsertDTO dto);

    /**
     * 删除一条 VTE 事件登记。
     * 供院内监测事件台账的误登记录清理。
     */
    int eventDeleteById(Long id);

    /**
     * 实时试算（不落库；报数以快照为准）
     */
    VteStatsVO previewStats(String statMonth);

    /**
     * 生成/重算月度快照（同月同范围覆盖）
     */
    List<VteStatsVO> generateStats(VteStatsGenerateDTO dto);

    /**
     * 已生成的月度防控指标快照分页。
     * 用于「VTE 院内监测」页的指标台账表格与报数取数。
     */
    PageResult<VteStatsVO> statsListPage(VteStatsPageQueryDTO query);

    /**
     * 导出 CSV（BOM，上限 5000 行）
     */
    String statsExportCsv(VteStatsPageQueryDTO query);
}
