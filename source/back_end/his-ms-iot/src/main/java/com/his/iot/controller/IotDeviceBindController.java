package com.his.iot.controller;

import com.his.common.base.Result;
import com.his.iot.dto.IotBindQueryDTO;
import com.his.iot.service.IotDeviceBindService;
import com.his.iot.vo.IotDeviceBindVO;
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
 * 设备—患者绑定查询（只读）。
 * <p>
 * 绑定/解绑不再暴露为独立接口：唯一写入口是「设备共享调配」的发放/验收
 * （{@code IotDeviceRentalServiceImpl} 内部调 bindDevice/unbindDevice）。
 * 监控大屏、设备总览只读取在用绑定，避免任何页面绕过申请单直接把泵挂到患者。
 */
@Tag(name = "物联网设备绑定")
@RestController
@RequestMapping("/iot/bind")
@RequiredArgsConstructor
public class IotDeviceBindController {

    private final IotDeviceBindService iotDeviceBindService;

    @Operation(summary = "当前在用绑定（listActiveBinds）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/listActiveBinds")
    public Result<List<IotDeviceBindVO>> listActiveBinds(@Valid @RequestBody IotBindQueryDTO query) {
        return Result.success(iotDeviceBindService.listActiveBinds(query));
    }
}
