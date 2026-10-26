package com.his.outpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.outpatient.dto.DutyCallArriveDTO;
import com.his.outpatient.dto.DutyCallCancelDTO;
import com.his.outpatient.dto.DutyCallPageQueryDTO;
import com.his.outpatient.dto.DutyCallUpsertDTO;
import com.his.outpatient.service.DutyCallService;
import com.his.outpatient.vo.DutyCallTargetVO;
import com.his.outpatient.vo.DutyCallVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 临床值班听班呼叫控制器（sql/297，菜单 29616）。
 *
 * <p>呼叫链路的三个动作分权：发起（临床一线/医务科）、到岗应答（只有被叫值班人）、
 * 取消（只有发起人）。查看对全院医生开放 —— 半夜打电话找不到人时，
 * 能看到「今晚二线是谁」比什么都强。
 */
@Tag(name = "听班呼叫")
@RestController
@RequestMapping("/system/dutyCall")
@RequiredArgsConstructor
public class DutyCallController {

    private final DutyCallService dutyCallService;

    /**
     * 可呼叫的听班目标下拉：这一班这一科室有几线听班、各是谁。
     * 呼叫方在发起前就能看到「今晚二线在不在」，避免叫了才知道没人。
     */
    @Operation(summary = "可呼叫的听班目标下拉（按科室+日期+班次）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listTargets")
    public Result<List<DutyCallTargetVO>> listTargets(@RequestParam Long deptId,
                                                       @RequestParam(required = false) LocalDate dutyDate,
                                                       @RequestParam(required = false) Integer shiftType) {
        return Result.success(dutyCallService.listTargets(deptId, dutyDate, shiftType));
    }

    /**
     * 发起听班呼叫：后端按科室+层级+班次解析当班听班人并通知，不接受前端指定被叫人。
     */
    @Operation(summary = "发起听班呼叫")
    @PreAuthorize("hasAuthority('org:dutyCall:call')")
    @PostMapping("/call")
    public Result<DutyCallVO> call(@RequestBody @Valid DutyCallUpsertDTO dto) {
        return Result.success("呼叫已发出", dutyCallService.call(dto));
    }

    /**
     * 到岗应答：只有被叫值班人本人可应答。
     */
    @Operation(summary = "到岗应答")
    @PreAuthorize("hasAuthority('org:dutyCall:arrive')")
    @PostMapping("/arrive")
    public Result<DutyCallVO> arrive(@RequestBody @Valid DutyCallArriveDTO dto) {
        return Result.success("已登记到岗", dutyCallService.arrive(dto));
    }

    /**
     * 取消呼叫：只有发起人可取消。
     */
    @Operation(summary = "取消听班呼叫")
    @PreAuthorize("hasAuthority('org:dutyCall:cancel')")
    @PostMapping("/cancel")
    public Result<DutyCallVO> cancel(@RequestBody @Valid DutyCallCancelDTO dto) {
        return Result.success("呼叫已取消", dutyCallService.cancel(dto));
    }

    /**
     * 呼叫单分页。
     */
    @Operation(summary = "听班呼叫单分页")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/listPage")
    public Result<PageResult<DutyCallVO>> listPage(@Valid @RequestBody DutyCallPageQueryDTO queryDTO) {
        return Result.success(dutyCallService.listPage(queryDTO));
    }

    /**
     * 呼叫单详情（含呼叫流水）。
     */
    @Operation(summary = "听班呼叫单详情")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail")
    public Result<DutyCallVO> detail(@RequestParam Long id) {
        return Result.success(dutyCallService.detail(id));
    }

    /**
     * 手工补跑超时升级（与定时任务同一入口，幂等）。
     */
    @Operation(summary = "手工补跑听班呼叫超时升级")
    @PreAuthorize("hasAuthority('org:dutyCall:call')")
    @PostMapping("/escalateOverdue")
    public Result<Integer> escalateOverdue() {
        int n = dutyCallService.escalateOverdueManual();
        return Result.success("已升级 " + n + " 条超时未到岗的呼叫", n);
    }
}
