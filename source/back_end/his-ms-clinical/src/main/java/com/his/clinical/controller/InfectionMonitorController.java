package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.InfectionMonitorDTO;
import com.his.clinical.service.InfectionMonitorService;
import com.his.clinical.vo.InfectionMonitorVO;
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

    /**
     * 院感病例统计卡：待核实 / 已确认 / 已排除 / 漏报补报四段计数，附院内感染与今日新增。
     * 用于「院感监测」页院感病例页签顶部统计，计数由后端聚合、不随分页变化。
     */
    @Operation(summary = "病例统计卡（四态+漏报补报+院内感染+今日新增）")
    @GetMapping("/case/stats")
    public Result<InfectionMonitorVO.CaseStats> caseStats() {
        return Result.success(infectionMonitorService.caseStats());
    }

    /**
     * 院感病例报告卡分页：按核实状态、发现来源、是否漏报、关键字与上报日期区间筛选。
     * 用于「院感监测」页院感病例页签查询卡。
     */
    @Operation(summary = "病例分页")
    @PostMapping("/case/listPage")
    public Result<PageResult<InfectionMonitorVO.CaseRow>> caseListPage(
            @Valid @RequestBody InfectionMonitorDTO.CasePageQuery dto) {
        return Result.success(infectionMonitorService.casePage(dto));
    }

    /**
     * 单张院感病例报告卡详情。
     * 报卡建卡与感控核实环节回读整卡内容时取数。
     */
    @Operation(summary = "病例详情")
    @GetMapping("/case/getDetailById")
    public Result<InfectionMonitorVO.CaseRow> caseGetDetailById(@RequestParam Long id) {
        return Result.success(infectionMonitorService.caseGetDetailById(id));
    }

    /**
     * 院感病例建卡：正常报卡与漏报补报共用一个入口，补报卡带漏报标记。
     * 用于院感病例页签的报卡弹窗（漏报调查入口从列表筛出漏报后补建）。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:add')")
    @Operation(summary = "病例报卡/补报建卡（漏报调查入口传 leakFlag=1）")
    @PostMapping("/case/upsert")
    public Result<InfectionMonitorVO.CaseRow> caseUpsert(@Valid @RequestBody InfectionMonitorDTO.CaseUpsert dto) {
        Long id = infectionMonitorService.caseUpsert(dto);
        return Result.success("报卡已保存", infectionMonitorService.caseGetDetailById(id));
    }

    /**
     * 感控核实结论回写：待核实转为已确认或已排除，并记录核实意见。
     * 用于院感病例页签操作列的核实弹窗；结论一次性，事后要改只能另建新卡。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:edit')")
    @Operation(summary = "感控核实（1待核实 → 2已确认/3已排除，结论一次性，订正建新卡）")
    @PostMapping("/case/audit")
    public Result<Void> caseAudit(@Valid @RequestBody InfectionMonitorDTO.CaseAudit dto) {
        infectionMonitorService.caseAudit(dto);
        return Result.success("核实完成", null);
    }

    // 目标性监测

    /**
     * 目标性监测登记分页：按监测类型、在管状态、是否确认感染与关键字筛选。
     * 用于「院感监测」页目标性监测页签查询卡。
     */
    @Operation(summary = "监测分页")
    @PostMapping("/monitor/listPage")
    public Result<PageResult<InfectionMonitorVO.MonitorRow>> monitorListPage(
            @Valid @RequestBody InfectionMonitorDTO.MonitorPageQuery dto) {
        return Result.success(infectionMonitorService.monitorPage(dto));
    }

    /**
     * 单例导管相关监测详情，含导管日累计。
     * 监测登记保存后回读整单时取数。
     */
    @Operation(summary = "监测详情（含导管日）")
    @GetMapping("/monitor/getDetailById")
    public Result<InfectionMonitorVO.MonitorRow> monitorGetDetailById(@RequestParam Long id) {
        return Result.success(infectionMonitorService.monitorGetDetailById(id));
    }

    /**
     * 登记一例导管相关目标性监测：记录置管起点与患者/住院就诊，进入在管。
     * 用于目标性监测页签的监测登记弹窗。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:add')")
    @Operation(summary = "监测登记（导管相关三类型）")
    @PostMapping("/monitor/add")
    public Result<InfectionMonitorVO.MonitorRow> monitorAdd(@Valid @RequestBody InfectionMonitorDTO.MonitorAdd dto) {
        Long id = infectionMonitorService.monitorAdd(dto);
        return Result.success("监测登记已保存", infectionMonitorService.monitorGetDetailById(id));
    }

    /**
     * 在管病例每日打卡：记一个导管日，只增不删且同日重复打卡被拒。
     * 用于目标性监测页签行上的每日打卡操作，是感染率分母的唯一来源。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:edit')")
    @Operation(summary = "每日打卡（导管日留痕，只增禁删；同日防重）")
    @PostMapping("/monitor/punchDaily")
    public Result<Void> monitorPunchDaily(@Valid @RequestBody InfectionMonitorDTO.PunchDaily dto) {
        infectionMonitorService.monitorPunchDaily(dto);
        return Result.success("打卡成功", null);
    }

    /**
     * 拔管登记：在管转为已拔管并终止导管日累计，记录拔管日期与说明。
     * 用于目标性监测页签行上的拔管操作。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:delete')")
    @Operation(summary = "拔管（1在管 → 2已拔管）")
    @PostMapping("/monitor/remove")
    public Result<Void> monitorRemove(@Valid @RequestBody InfectionMonitorDTO.MonitorRemove dto) {
        infectionMonitorService.monitorRemove(dto);
        return Result.success("拔管登记完成", null);
    }

    /**
     * 确认本例监测发生院内感染，只打感染标记、不改在管状态。
     * 用于目标性监测页签行上的感染确认操作——拔管前先确认，感染例次与导管日才同口径。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:edit')")
    @Operation(summary = "感染确认（infectionFlag=1，不改在管状态；导管日统计才完整）")
    @PostMapping("/monitor/confirmInfection")
    public Result<Void> monitorConfirmInfection(@Valid @RequestBody InfectionMonitorDTO.ConfirmInfection dto) {
        infectionMonitorService.monitorConfirmInfection(dto);
        return Result.success("感染确认完成", null);
    }

    /**
     * 监测统计：在管/已拔管数、导管日、感染例次与感染率（‰），并按监测类型分组。
     * 用于目标性监测页签顶部统计。
     */
    @Operation(summary = "监测统计（导管日/感染例次/感染率‰，按类型分组）")
    @GetMapping("/monitor/stats")
    public Result<InfectionMonitorVO.MonitorStats> monitorStats() {
        return Result.success(infectionMonitorService.monitorStats());
    }

    /**
     * 某例监测的逐日打卡明细，即导管日清单。
     * 用于院感监测页的导管日明细弹框。
     */
    @Operation(summary = "某监测的每日打卡明细（导管日清单）")
    @GetMapping("/monitor/dailyList")
    public Result<List<InfectionMonitorVO.DailyRow>> monitorDailyList(@RequestParam Long monitorId) {
        return Result.success(infectionMonitorService.monitorDailyList(monitorId));
    }

    // 手卫生依从性

    /**
     * 手卫生观察记录分页：按监测科室、观察对象与观察日期区间筛选。
     * 用于「院感监测」页手卫生页签的观察记录表格。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:list')")
    @Operation(summary = "观察记录分页")
    @PostMapping("/handObs/listPage")
    public Result<PageResult<InfectionMonitorVO.HandObsRow>> handObsListPage(
            @Valid @RequestBody InfectionMonitorDTO.HandObsPageQuery dto) {
        return Result.success(infectionMonitorService.handObsPage(dto));
    }

    /**
     * 新增一条手卫生观察记录（时机数与执行数一次登记，落库后不改）。
     * 用于手卫生页签的观察登记弹窗，该面板同时服务手卫生依从性独立页。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:add')")
    @Operation(summary = "观察登记（只增不改）")
    @PostMapping("/handObs/add")
    public Result<InfectionMonitorVO.HandObsRow> handObsAdd(@Valid @RequestBody InfectionMonitorDTO.HandObsAdd dto) {
        Long id = infectionMonitorService.handObsAdd(dto);
        return Result.success("观察记录已保存", infectionMonitorService.handObsGetDetailById(id));
    }

    /**
     * 手卫生依从性统计：时机数、执行数与依从率（%），并按观察对象分组。
     * 用于手卫生页签统计条，不传日期窗口时按近 30 天口径聚合。
     */
    @PreAuthorize("hasAuthority('emr:infectionMonitor:list')")
    @Operation(summary = "依从率统计（先聚合再算比率，默认近 30 天）")
    @PostMapping("/handObs/stats")
    public Result<InfectionMonitorVO.HandObsStats> handObsStats(
            @Valid @RequestBody InfectionMonitorDTO.HandObsStatsQuery dto) {
        return Result.success(infectionMonitorService.handObsStats(dto));
    }
}
