package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.EmergencyConfigUpsertDTO;
import com.his.system.dto.HospitalInfoUpsertDTO;
import com.his.system.dto.InpatientConfigUpsertDTO;
import com.his.system.dto.LabCriticalConfigUpsertDTO;
import com.his.system.dto.SignConfigUpsertDTO;
import com.his.system.service.SysConfigService;
import com.his.system.vo.EmergencyConfigVO;
import com.his.system.vo.HospitalInfoVO;
import com.his.system.vo.InpatientConfigVO;
import com.his.system.vo.LabCriticalConfigVO;
import com.his.system.vo.SignConfigVO;
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

    @Operation(summary = "获取急诊参数")
    @GetMapping("/emergencyInfo")
    public Result<EmergencyConfigVO> emergencyInfo() {
        return Result.success(sysConfigService.getEmergencyConfig());
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存急诊参数")
    @PostMapping("/emergencyUpsert")
    public Result<Void> emergencyUpsert(@Valid @RequestBody EmergencyConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertEmergencyConfig(upsertDTO);
        return Result.success("保存成功", null);
    }

    @Operation(summary = "获取检验危急值参数")
    @GetMapping("/labCriticalInfo")
    public Result<LabCriticalConfigVO> labCriticalInfo() {
        return Result.success(sysConfigService.getLabCriticalConfig());
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存检验危急值参数")
    @PostMapping("/labCriticalUpsert")
    public Result<Void> labCriticalUpsert(@Valid @RequestBody LabCriticalConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertLabCriticalConfig(upsertDTO);
        return Result.success("保存成功", null);
    }

    @Operation(summary = "获取住院 / 床位 / 转诊 / 住院证参数")
    @GetMapping("/inpatientInfo")
    public Result<InpatientConfigVO> inpatientInfo() {
        return Result.success(sysConfigService.getInpatientConfig());
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存住院 / 床位 / 转诊 / 住院证参数")
    @PostMapping("/inpatientUpsert")
    public Result<Void> inpatientUpsert(@Valid @RequestBody InpatientConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertInpatientConfig(upsertDTO);
        return Result.success("保存成功", null);
    }

    @Operation(summary = "获取电子签名 / 可信时间戳参数")
    @GetMapping("/signInfo")
    public Result<SignConfigVO> signInfo() {
        return Result.success(sysConfigService.getSignConfig());
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存电子签名 / 可信时间戳参数")
    @PostMapping("/signUpsert")
    public Result<Void> signUpsert(@Valid @RequestBody SignConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertSignConfig(upsertDTO);
        return Result.success("保存成功", null);
    }
}
