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

    PageResult<InfectionMonitorVO.CaseRow> casePage(InfectionMonitorDTO.CaseQueryPage q);

    InfectionMonitorVO.CaseRow caseGetDetailById(Long id);

    Long caseUpsert(InfectionMonitorDTO.CaseUpsert dto);

    void caseAudit(InfectionMonitorDTO.CaseAudit dto);

    // 目标性监测

    PageResult<InfectionMonitorVO.MonitorRow> monitorPage(InfectionMonitorDTO.MonitorQueryPage q);

    InfectionMonitorVO.MonitorRow monitorGetDetailById(Long id);

    Long monitorAdd(InfectionMonitorDTO.MonitorAdd dto);

    void monitorPunchDaily(InfectionMonitorDTO.PunchDaily dto);

    void monitorRemove(InfectionMonitorDTO.MonitorRemove dto);

    void monitorConfirmInfection(InfectionMonitorDTO.ConfirmInfection dto);

    InfectionMonitorVO.MonitorStats monitorStats();

    java.util.List<InfectionMonitorVO.DailyRow> monitorDailyList(Long monitorId);

    // 手卫生依从性

    PageResult<InfectionMonitorVO.HandObsRow> handObsPage(InfectionMonitorDTO.HandObsQueryPage q);

    Long handObsAdd(InfectionMonitorDTO.HandObsAdd dto);

    InfectionMonitorVO.HandObsRow handObsGetDetailById(Long id);

    InfectionMonitorVO.HandObsStats handObsStats(InfectionMonitorDTO.HandObsStatsQuery q);
}
