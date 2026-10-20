package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.InfectionMonitorDTO;
import com.his.emr.vo.InfectionMonitorVO;

/**
 * 院感监测服务（L10）：病例报告卡 / 目标性监测 / 手卫生依从性。
 */
public interface InfectionMonitorService {

    // 病例报告卡

    InfectionMonitorVO.CaseStats caseStats();

    /**
     * 院感病例报卡分页：按状态、来源、漏报标志与上报时间区间筛选，科室数据范围随登录岗位收口。
     * 用于「院感监测」页「院感病例」页签的查询卡与表格。
     */
    PageResult<InfectionMonitorVO.CaseRow> casePage(InfectionMonitorDTO.CasePageQuery q);

    /**
     * 单张院感病例报卡详情：含感染部位、病原菌与感控核实结论。
     * 用于报卡弹框保存后回读最新行，以及病例详情弹框按 id 取数。
     */
    InfectionMonitorVO.CaseRow caseGetDetailById(Long id);

    /**
     * 院感病例建卡与修改：新增落待核实态并生成病例号，只有待核实状态可改，核实结论错了另建新卡订正。
     * 用于「院感监测」页报卡弹框保存，漏报调查补报入口带漏报标志提交同一入口。
     */
    Long caseUpsert(InfectionMonitorDTO.CaseUpsert dto);

    /**
     * 感控核实：把待核实病例判为已确认或已排除，记录核实人、时间与结论备注，一次性不可重复审。
     * 用于「院感监测」页「院感病例」页签行上的核实操作。
     */
    void caseAudit(InfectionMonitorDTO.CaseAudit dto);

    // 目标性监测

    /**
     * 导管目标性监测登记分页：按监测类型、在管状态、感染标志筛选，置管日期倒序，科室数据范围随登录岗位收口。
     * 用于「院感监测」页「目标性监测」页签的查询卡与表格。
     */
    PageResult<InfectionMonitorVO.MonitorRow> monitorPage(InfectionMonitorDTO.MonitorPageQuery q);

    /**
     * 单条监测登记详情：连同已累计的导管日天数一起给出。
     * 用于监测登记保存后回读最新行，以及监测详情弹框按 id 取数。
     */
    InfectionMonitorVO.MonitorRow monitorGetDetailById(Long id);

    /**
     * 新增导管监测登记：生成监测号、按住院记录锚定科室、置管日期不得为未来，初始为在管未感染。
     * 用于「院感监测」页「目标性监测」页签的监测登记弹框保存。
     */
    Long monitorAdd(InfectionMonitorDTO.MonitorAdd dto);

    /**
     * 监测每日打卡：为在管监测补记一个导管日，只增禁删、同日查重，拔管后不再收卡。
     * 用于「院感监测」页「目标性监测」页签行上的每日打卡操作。
     */
    void monitorPunchDaily(InfectionMonitorDTO.PunchDaily dto);

    /**
     * 拔管登记：在管转已拔管并记拔除日期，拔除日不得早于置入日或晚于今天。
     * 用于「院感监测」页「目标性监测」页签行上的拔管操作。
     */
    void monitorRemove(InfectionMonitorDTO.MonitorRemove dto);

    /**
     * 感染确认：给监测登记打院内感染标志并记感染日期与诊断，只改感染标志不动在管状态，已确认的不允许覆盖。
     * 用于「院感监测」页「目标性监测」页签行上的感染确认操作。
     */
    void monitorConfirmInfection(InfectionMonitorDTO.ConfirmInfection dto);

    /**
     * 导管监测统计：在管/已拔管数、导管日合计、感染确认例次与千日感染率，并按监测类型分组。
     * 用于「院感监测」页「目标性监测」页签顶部的统计卡。
     */
    InfectionMonitorVO.MonitorStats monitorStats();

    /**
     * 一条监测名下全部每日打卡明细：打卡日期倒序，即导管日留痕清单。
     * 用于「院感监测」页「目标性监测」行点开的导管日明细弹框。
     */
    java.util.List<InfectionMonitorVO.DailyRow> monitorDailyList(Long monitorId);

    // 手卫生依从性

    PageResult<InfectionMonitorVO.HandObsRow> handObsPage(InfectionMonitorDTO.HandObsPageQuery q);

    /**
     * 手卫生观察登记：一次观察记时机数与执行数，执行数不得大于时机数、观察日期不得为未来，只增不改。
     * 用于手卫生依从性面板的登记弹框保存（「院感监测」页手卫生页签与独立手卫生页共用）。
     */
    Long handObsAdd(InfectionMonitorDTO.HandObsAdd dto);

    /**
     * 单条手卫生观察记录详情：含依从率计算结果。
     * 用于观察登记保存后回读新建行。
     */
    InfectionMonitorVO.HandObsRow handObsGetDetailById(Long id);

    /**
     * 手卫生依从率统计：区间内先汇总时机数与执行数再算比率，按观察对象分组，科室数据范围随登录岗位收口。
     * 用于手卫生依从性面板的统计区，入参缺日期时按截至今天的近 30 天口径。
     */
    InfectionMonitorVO.HandObsStats handObsStats(InfectionMonitorDTO.HandObsStatsQuery q);
}
