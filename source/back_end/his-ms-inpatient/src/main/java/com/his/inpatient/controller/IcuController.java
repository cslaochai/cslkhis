package com.his.inpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.inpatient.dto.*;
import com.his.inpatient.service.IcuService;
import com.his.inpatient.vo.IcuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ICU 专科监护：入出科登记 + 床边监护记录单 + 床位看板与科室指标。
 */
@Tag(name = "ICU 专科监护")
@RestController
@RequestMapping("/patient/icu")
@RequiredArgsConstructor
public class IcuController {

    private final IcuService icuService;

    // 入出科

    /**
     * 入出科台账分页。
     * 用于「ICU 专科监护」页入出科台账页签的查询卡。
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "入出科台账分页")
    @PostMapping("/stay/listPage")
    public Result<PageResult<IcuVO.StayVO>> stayListPage(@Valid @RequestBody IcuStayPageQueryDTO dto) {
        return Result.success(icuService.stayListPage(dto));
    }

    /**
     * 单条入科记录详情。
     * 用于入出科台账行详情弹框，也是出科与监护填写时的上下文取数口。
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "入科记录详情")
    @GetMapping("/stay/getById")
    public Result<IcuVO.StayVO> stayGetById(@RequestParam Long id) {
        return Result.success(icuService.stayGetById(id));
    }

    /**
     * 可入科候选：在院且当前没有在科记录的患者。
     * 用于入科登记弹框里挑患者。
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "可入科候选（在院且无在科记录）")
    @GetMapping("/stay/admissions")
    public Result<List<IcuVO.AdmissionVO>> admissionsForIcu(@RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) Integer limit) {
        return Result.success(icuService.admissionsForIcu(keyword, limit));
    }

    /**
     * 入科登记以及在科期间修改：患者与床位快照由服务端重查，不采信前端传值。
     * 用于入科登记弹框提交。
     */
    @PreAuthorize("hasAuthority('ipd:icu:add')")
    @Operation(summary = "入科登记 / 在科期间修改（患者与床位快照服务端重查）")
    @PostMapping("/stay/upsert")
    public Result<IcuVO.StayVO> stayUpsert(@Valid @RequestBody IcuStayUpsertDTO dto) {
        return Result.success("入科已登记", icuService.stayUpsert(dto));
    }

    /**
     * 出科登记：入科记录的终态，转归说明按去向必填。
     * 用于出科登记弹框提交。
     */
    @PreAuthorize("hasAuthority('ipd:icu:add')")
    @Operation(summary = "出科登记（终态，转归说明按去向必填）")
    @PostMapping("/stay/out")
    public Result<IcuVO.StayVO> stayOut(@Valid @RequestBody IcuStayOutDTO dto) {
        return Result.success("已出科", icuService.stayOut(dto));
    }

    /**
     * 床位看板：在科患者配上最近一次监护读数。
     * 用于「ICU 专科监护」页床位看板页签，可按病区收口。
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "ICU 床位看板（在科患者 + 最近一次监护读数）")
    @GetMapping("/bedBoard")
    public Result<List<IcuVO.BedVO>> bedBoard(@RequestParam(required = false) Long wardId) {
        return Result.success(icuService.bedBoard(wardId));
    }

    // 监护记录单

    /**
     * 监护记录分页。
     * 用于「ICU 专科监护」页监护记录单页签的查询卡。
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "监护记录分页")
    @PostMapping("/monitor/listPage")
    public Result<PageResult<IcuVO.MonitorVO>> monitorListPage(@Valid @RequestBody IcuMonitorPageQueryDTO dto) {
        return Result.success(icuService.monitorListPage(dto));
    }

    /**
     * 单次在科的监护读数趋势，按时间正序。
     * 用于床位看板下钻与入科记录详情里的曲线查看。
     *
     * @param hours 回溯时长（小时），为空表示取全部历史记录
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "单患者监护趋势（hours 为空=全部，按时间正序）")
    @GetMapping("/monitor/trend")
    public Result<List<IcuVO.MonitorVO>> monitorTrend(@RequestParam Long stayId,
                                                      @RequestParam(required = false) Integer hours) {
        return Result.success(icuService.monitorTrend(stayId, hours));
    }

    /**
     * 登记或修改床边监护记录：出科后禁写，GCS 总分与液体平衡由服务端回算。
     * 用于监护记录单的填写弹框。
     */
    @PreAuthorize("hasAuthority('ipd:icu:edit')")
    @Operation(summary = "登记/修改监护记录（出科后禁写，GCS 总分与液体平衡服务端回算）")
    @PostMapping("/monitor/upsert")
    public Result<IcuVO.MonitorVO> monitorUpsert(@Valid @RequestBody IcuMonitorUpsertDTO dto) {
        return Result.success("监护记录已保存", icuService.monitorUpsert(dto));
    }

    // 指标

    /**
     * 科室指标：床位使用率、人均记录、带管与呼吸机分布、漏记预警。
     * 用于「ICU 专科监护」页科室指标页签。
     *
     * @param lagHours 漏记判定的超时阈值（小时）
     */
    @PreAuthorize("hasAuthority('ipd:icu:list')")
    @Operation(summary = "科室指标（床位使用率/人均记录/带管与呼吸机分布/漏记预警，窗口最长30天）")
    @GetMapping("/stats")
    public Result<IcuVO.StatsVO> stats(@RequestParam(required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                       @RequestParam(required = false) Integer lagHours) {
        return Result.success(icuService.stats(startDate, endDate, lagHours));
    }
}
