package com.his.iot.controller;

import com.his.common.base.Result;
import com.his.iot.dto.IotDeviceQueryDTO;
import com.his.iot.service.IotDeviceService;
import com.his.iot.vo.IotDeviceVO;
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
 * 物联网设备档案（台账只读）。
 */
@Tag(name = "物联网设备档案")
@RestController
@RequestMapping("/iot/device")
@RequiredArgsConstructor
public class IotDeviceController {

    private final IotDeviceService iotDeviceService;

    @Operation(summary = "设备清单（selectList，按类型/编号过滤）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<IotDeviceVO>> selectList(@Valid @RequestBody IotDeviceQueryDTO query) {
        return Result.success(iotDeviceService.selectList(query));
    }
}
