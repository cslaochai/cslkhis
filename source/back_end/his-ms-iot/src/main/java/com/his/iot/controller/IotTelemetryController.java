package com.his.iot.controller;

import com.his.common.base.Result;
import com.his.iot.dto.IotHistoryQueryDTO;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.service.IotTelemetryService;
import com.his.iot.vo.IotHistoryPointVO;
import com.his.iot.vo.IotLatestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物联网设备遥测查询。
 * <p>
 * 权限暂时只要求登录：设备总览要在护士站、设备科、运维多个岗位复用，而按钮级权限码还没在
 * {@code sys_menu} 铺底——先挂具体权限码会导致「码不存在 = 任何角色拿不到 = 接口永久 403」。
 * 等页面定稿、权限码入 {@code sql/100} 后再换成 {@code hasAuthority('iot:telemetry:view')}。
 */
@Tag(name = "物联网设备遥测")
@RestController
@RequestMapping("/iot/telemetry")
@RequiredArgsConstructor
public class IotTelemetryController {

    private final IotTelemetryService iotTelemetryService;

    /**
     * 设备最新一行遥测（当前状态）。用于设备总览卡片、护士站"这床现在什么情况"。
     */
    @Operation(summary = "设备最新遥测（latestList）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/latestList")
    public Result<List<IotLatestVO>> latestList(@Valid @RequestBody IotLatestQueryDTO query) {
        return Result.success(iotTelemetryService.latestList(query));
    }

    /**
     * 单台设备的历史遥测点，按时间升序。用于趋势图、冷链温度回溯。
     */
    @Operation(summary = "设备历史遥测（historyList）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/historyList")
    public Result<List<IotHistoryPointVO>> historyList(@Valid @RequestBody IotHistoryQueryDTO query) {
        return Result.success(iotTelemetryService.historyList(query));
    }
}
