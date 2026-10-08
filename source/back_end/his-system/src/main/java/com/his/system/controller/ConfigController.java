package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.HospitalInfoUpsertDTO;
import com.his.system.service.SysConfigService;
import com.his.system.vo.HospitalInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 系统参数配置控制器
 */
@Tag(name = "系统参数配置")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "获取医院基础信息")
    @GetMapping("/hospitalInfo")
    public Result<HospitalInfoVO> hospitalInfo() {
        return Result.success(sysConfigService.getHospitalInfo());
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存医院基础信息")
    @PostMapping("/hospitalUpsert")
    public Result<Void> hospitalUpsert(@Valid @RequestBody HospitalInfoUpsertDTO upsertDTO) {
        sysConfigService.upsertHospitalInfo(upsertDTO);
        return Result.success("保存成功", null);
    }
}
