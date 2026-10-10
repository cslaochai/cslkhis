package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.InfectionMonitorDTO;
import com.his.emr.service.InfectionMonitorService;
import com.his.emr.vo.InfectionMonitorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 院感监测控制器（L10：病例报告卡 / 目标性监测 / 手卫生依从性）。
 */
@Tag(name = "院感监测")
@RestController
@RequestMapping("/emr/infection")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('emr:infectionMonitor:list')")
public class InfectionMonitorController {

    private final InfectionMonitorService infectionMonitorService;

    // 病例报告卡

    @Operation(summary = "病例统计卡（四态+漏报补报+院内感染+今日新增）")
    @GetMapping("/case/stats")
    public Result<InfectionMonitorVO.CaseStats> caseStats() {
        return Result.success(infectionMonitorService.caseStats());
    }

    @Operation(summary = "病例分页")
    @PostMapping("/case/listPage")
    public Result<PageResult<InfectionMonitorVO.CaseRow>> caseListPage(
            @Valid @RequestBody InfectionMonitorDTO.CaseQueryPage dto) {
        return Result.success(infectionMonitorService.casePage(dto));
    }

    @Operation(summary = "病例详情")
    @GetMapping("/case/getDetailById")
    public Result<InfectionMonitorVO.CaseRow> caseGetDetailById(@RequestParam Long id) {
        return Result.success(infectionMonitorService.caseGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:add')")
    @Operation(summary = "病例报卡/补报建卡（漏报调查入口传 leakFlag=1）")
    @PostMapping("/case/upsert")
    public Result<InfectionMonitorVO.CaseRow> caseUpsert(@Valid @RequestBody InfectionMonitorDTO.CaseUpsert dto) {
        Long id = infectionMonitorService.caseUpsert(dto);
        return Result.success("报卡已保存", infectionMonitorService.caseGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:edit')")
    @Operation(summary = "感控核实（1待核实 → 2已确认/3已排除，结论一次性，订正建新卡）")
    @PostMapping("/case/audit")
    public Result<Void> caseAudit(@Valid @RequestBody InfectionMonitorDTO.CaseAudit dto) {
        infectionMonitorService.caseAudit(dto);
        return Result.success("核实完成", null);
    }

    // 目标性监测

    @Operation(summary = "监测分页")
    @PostMapping("/monitor/listPage")
    public Result<PageResult<InfectionMonitorVO.MonitorRow>> monitorListPage(
            @Valid @RequestBody InfectionMonitorDTO.MonitorQueryPage dto) {
        return Result.success(infectionMonitorService.monitorPage(dto));
    }

    @Operation(summary = "监测详情（含导管日）")
    @GetMapping("/monitor/getDetailById")
    public Result<InfectionMonitorVO.MonitorRow> monitorGetDetailById(@RequestParam Long id) {
        return Result.success(infectionMonitorService.monitorGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:add')")
    @Operation(summary = "监测登记（导管相关三类型）")
    @PostMapping("/monitor/add")
    public Result<InfectionMonitorVO.MonitorRow> monitorAdd(@Valid @RequestBody InfectionMonitorDTO.MonitorAdd dto) {
        Long id = infectionMonitorService.monitorAdd(dto);
        return Result.success("监测登记已保存", infectionMonitorService.monitorGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:edit')")
    @Operation(summary = "每日打卡（导管日留痕，只增禁删；同日防重）")
    @PostMapping("/monitor/punchDaily")
    public Result<Void> monitorPunchDaily(@Valid @RequestBody InfectionMonitorDTO.PunchDaily dto) {
        infectionMonitorService.monitorPunchDaily(dto);
        return Result.success("打卡成功", null);
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:delete')")
    @Operation(summary = "拔管（1在管 → 2已拔管）")
    @PostMapping("/monitor/remove")
    public Result<Void> monitorRemove(@Valid @RequestBody InfectionMonitorDTO.MonitorRemove dto) {
        infectionMonitorService.monitorRemove(dto);
        return Result.success("拔管登记完成", null);
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:edit')")
    @Operation(summary = "感染确认（infectionFlag=1，不改在管状态；导管日统计才完整）")
    @PostMapping("/monitor/confirmInfection")
    public Result<Void> monitorConfirmInfection(@Valid @RequestBody InfectionMonitorDTO.ConfirmInfection dto) {
        infectionMonitorService.monitorConfirmInfection(dto);
        return Result.success("感染确认完成", null);
    }

    @Operation(summary = "监测统计（导管日/感染例次/感染率‰，按类型分组）")
    @GetMapping("/monitor/stats")
    public Result<InfectionMonitorVO.MonitorStats> monitorStats() {
        return Result.success(infectionMonitorService.monitorStats());
    }

    @Operation(summary = "某监测的每日打卡明细（导管日清单）")
    @GetMapping("/monitor/dailyList")
    public Result<List<InfectionMonitorVO.DailyRow>> monitorDailyList(@RequestParam Long monitorId) {
        return Result.success(infectionMonitorService.monitorDailyList(monitorId));
    }

    // 手卫生依从性（菜单 617 /hand-hygiene 与「院感监测」页第三个页签共用同一组件）

    // 读接口一律挂 list：挂 edit 会让「只能看不能改」的角色整页 403（AGENTS 第 4 节）
    @PreAuthorize("hasAuthority('emr:infectionMonitor:list')")
    @Operation(summary = "观察记录分页")
    @PostMapping("/handObs/listPage")
    public Result<PageResult<InfectionMonitorVO.HandObsRow>> handObsListPage(
            @Valid @RequestBody InfectionMonitorDTO.HandObsQueryPage dto) {
        return Result.success(infectionMonitorService.handObsPage(dto));
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:add')")
    @Operation(summary = "观察登记（只增不改）")
    @PostMapping("/handObs/add")
    public Result<InfectionMonitorVO.HandObsRow> handObsAdd(@Valid @RequestBody InfectionMonitorDTO.HandObsAdd dto) {
        Long id = infectionMonitorService.handObsAdd(dto);
        return Result.success("观察记录已保存", infectionMonitorService.handObsGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:infectionMonitor:list')")
    @Operation(summary = "依从率统计（先聚合再算比率，默认近 30 天）")
    @PostMapping("/handObs/stats")
    public Result<InfectionMonitorVO.HandObsStats> handObsStats(
            @Valid @RequestBody InfectionMonitorDTO.HandObsStatsQuery dto) {
        return Result.success(infectionMonitorService.handObsStats(dto));
    }
}
