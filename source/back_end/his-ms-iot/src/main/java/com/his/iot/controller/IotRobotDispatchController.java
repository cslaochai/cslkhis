package com.his.iot.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.iot.dto.RobotDispatchAssignDTO;
import com.his.iot.dto.RobotDispatchCancelDTO;
import com.his.iot.dto.RobotDispatchCreateDTO;
import com.his.iot.dto.RobotDispatchPageQueryDTO;
import com.his.iot.service.RobotDispatchService;
import com.his.iot.vo.RobotDispatchOrderVO;
import com.his.iot.vo.RobotFleetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物流机器人调度：配送任务单发起、派车分配、取消、车队状态。
 */
@Tag(name = "机器人调度")
@RestController
@RequestMapping("/iot/robotDispatch")
@RequiredArgsConstructor
public class IotRobotDispatchController {

    private final RobotDispatchService dispatchService;

    @Operation(summary = "车队状态")
    @PreAuthorize("hasAuthority('iot:robotDispatch:list')")
    @GetMapping("/fleet")
    public Result<List<RobotFleetVO>> fleet() {
        return Result.success(dispatchService.fleet());
    }

    @Operation(summary = "任务单分页")
    @PreAuthorize("hasAuthority('iot:robotDispatch:list')")
    @PostMapping("/listPage")
    public Result<PageResult<RobotDispatchOrderVO>> listPage(@Valid @RequestBody RobotDispatchPageQueryDTO dto) {
        var page = dispatchService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "任务单详情")
    @PreAuthorize("hasAuthority('iot:robotDispatch:list')")
    @GetMapping("/getDetailById")
    public Result<RobotDispatchOrderVO> getDetailById(@RequestParam Long id) {
        return Result.success(dispatchService.getDetail(id));
    }

    @Operation(summary = "新建配送任务单")
    @PreAuthorize("hasAuthority('iot:robotDispatch:apply')")
    @PostMapping("/create")
    public Result<String> create(@Valid @RequestBody RobotDispatchCreateDTO dto) {
        return Result.success(String.valueOf(dispatchService.create(dto)));
    }

    @Operation(summary = "派车分配机器人")
    @PreAuthorize("hasAuthority('iot:robotDispatch:assign')")
    @PostMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody RobotDispatchAssignDTO dto) {
        dispatchService.assign(dto);
        return Result.success();
    }

    @Operation(summary = "取消任务单")
    @PreAuthorize("hasAuthority('iot:robotDispatch:cancel')")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody RobotDispatchCancelDTO dto) {
        dispatchService.cancel(dto);
        return Result.success();
    }
}
