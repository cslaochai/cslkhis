package com.his.iot.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.iot.dto.RobotStationPageQueryDTO;
import com.his.iot.dto.RobotStationUpsertDTO;
import com.his.iot.service.RobotStationService;
import com.his.iot.vo.RobotStationOptionsVO;
import com.his.iot.vo.RobotStationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 物流机器人收发站点主数据（设备科维护，叫车下拉只读共用）。
 */
@Tag(name = "机器人站点")
@RestController
@RequestMapping("/iot/robotStation")
@RequiredArgsConstructor
public class IotRobotStationController {

    private final RobotStationService stationService;

    @Operation(summary = "站点分页")
    @PreAuthorize("hasAuthority('iot:robotStation:list')")
    @PostMapping("/listPage")
    public Result<PageResult<RobotStationVO>> listPage(@Valid @RequestBody RobotStationPageQueryDTO dto) {
        var page = stationService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "站点详情")
    @PreAuthorize("hasAuthority('iot:robotStation:list')")
    @GetMapping("/getDetailById")
    public Result<RobotStationVO> getDetailById(@RequestParam Long id) {
        return Result.success(stationService.getDetail(id));
    }

    @Operation(summary = "站点新增/修改")
    @PreAuthorize("hasAuthority('iot:robotStation:add')")
    @PostMapping("/upsert")
    public Result<String> upsert(@Valid @RequestBody RobotStationUpsertDTO dto) {
        return Result.success(String.valueOf(stationService.upsert(dto)));
    }

    @Operation(summary = "站点删除")
    @PreAuthorize("hasAuthority('iot:robotStation:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        stationService.delete(id);
        return Result.success();
    }

    /**
     * 叫车表单候选（站点/品类/优先级）：各岗位都要用，属通用参照数据，只要求登录，
     * 不挂机器人站点维护码，否则护士叫车时下拉全空。
     */
    @Operation(summary = "叫车表单候选")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/options")
    public Result<RobotStationOptionsVO> options() {
        return Result.success(stationService.options());
    }
}
