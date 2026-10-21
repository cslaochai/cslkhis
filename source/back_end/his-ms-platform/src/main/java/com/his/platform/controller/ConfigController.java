package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.dto.EmergencyConfigUpsertDTO;
import com.his.platform.dto.HospitalInfoUpsertDTO;
import com.his.platform.dto.InpatientConfigUpsertDTO;
import com.his.platform.dto.LabCriticalConfigUpsertDTO;
import com.his.platform.dto.SignConfigUpsertDTO;
import com.his.platform.service.SysConfigService;
import com.his.platform.vo.EmergencyConfigVO;
import com.his.platform.vo.HospitalInfoVO;
import com.his.platform.vo.InpatientConfigVO;
import com.his.platform.vo.LabCriticalConfigVO;
import com.his.platform.vo.SignConfigVO;
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
 * 系统参数配置控制器：医院基础信息与各业务域运行阈值的读取与保存入口。
 */
@Tag(name = "系统参数配置")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 读取医院名称、地址、联系电话、邮箱四项基础信息。
     * 用于系统管理「参数配置」页挂载时与其他四组参数一并拉取，回显「医院基础信息」页签表单。
     */
    @Operation(summary = "获取医院基础信息")
    @GetMapping("/hospitalInfo")
    public Result<HospitalInfoVO> hospitalInfo() {
        return Result.success(sysConfigService.getHospitalInfo());
    }

    /**
     * 保存医院四项基础信息。
     * 用于「参数配置」页「医院基础信息」页签的保存动作，写完后全院展示机构名称的地方跟着变。
     */
    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存医院基础信息")
    @PostMapping("/hospitalUpsert")
    public Result<Void> hospitalUpsert(@Valid @RequestBody HospitalInfoUpsertDTO upsertDTO) {
        sysConfigService.upsertHospitalInfo(upsertDTO);
        return Result.success("保存成功", null);
    }

    /**
     * 读取急诊分诊候诊时限、催办兜底接收人与留观时长阈值。
     * 用于系统管理「参数配置」页「急诊」页签的表单回显。
     */
    @Operation(summary = "获取急诊参数")
    @GetMapping("/emergencyInfo")
    public Result<EmergencyConfigVO> emergencyInfo() {
        return Result.success(sysConfigService.getEmergencyConfig());
    }

    /**
     * 保存急诊候诊时限与留观预警阈值。
     * 用于「参数配置」页「急诊」页签保存，写完即成为急诊分级的候诊与时限判定依据。
     */
    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存急诊参数")
    @PostMapping("/emergencyUpsert")
    public Result<Void> emergencyUpsert(@Valid @RequestBody EmergencyConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertEmergencyConfig(upsertDTO);
        return Result.success("保存成功", null);
    }

    /**
     * 读取检验危急值的处理时限与兜底接收人。
     * 用于系统管理「参数配置」页「检验危急值」页签的表单回显。
     */
    @Operation(summary = "获取检验危急值参数")
    @GetMapping("/labCriticalInfo")
    public Result<LabCriticalConfigVO> labCriticalInfo() {
        return Result.success(sysConfigService.getLabCriticalConfig());
    }

    /**
     * 保存检验危急值的处理时限与兜底接收人。
     * 用于「参数配置」页「检验危急值」页签保存，写完即成为危急值通报超时判定的口径。
     */
    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存检验危急值参数")
    @PostMapping("/labCriticalUpsert")
    public Result<Void> labCriticalUpsert(@Valid @RequestBody LabCriticalConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertLabCriticalConfig(upsertDTO);
        return Result.success("保存成功", null);
    }

    /**
     * 读取住院请假上限、待床与转诊预警时长、床位最长等待天数、住院证有效期等住院域阈值。
     * 用于系统管理「参数配置」页「住院 / 床位」页签的表单回显。
     */
    @Operation(summary = "获取住院 / 床位 / 转诊 / 住院证参数")
    @GetMapping("/inpatientInfo")
    public Result<InpatientConfigVO> inpatientInfo() {
        return Result.success(sysConfigService.getInpatientConfig());
    }

    /**
     * 保存住院域的各项等待与有效时长的阈值。
     * 用于「参数配置」页「住院 / 床位」页签保存，写完即成为等床预警、转诊待处理预警与住院证有效期的判定依据。
     */
    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存住院 / 床位 / 转诊 / 住院证参数")
    @PostMapping("/inpatientUpsert")
    public Result<Void> inpatientUpsert(@Valid @RequestBody InpatientConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertInpatientConfig(upsertDTO);
        return Result.success("保存成功", null);
    }

    /**
     * 读取电子签名的时间来源、证书有效期与缺证书是否自动签发。
     * 用于系统管理「参数配置」页「电子签名」页签的表单回显。
     */
    @Operation(summary = "获取电子签名 / 可信时间戳参数")
    @GetMapping("/signInfo")
    public Result<SignConfigVO> signInfo() {
        return Result.success(sysConfigService.getSignConfig());
    }

    /**
     * 保存电子签名与可信时间戳的取时与证书策略。
     * 用于「参数配置」页「电子签名」页签保存，写完即决定后续签名用的是本机时钟、院内授时还是第三方时间戳。
     */
    @PreAuthorize("hasAuthority('system:config:add')")
    @Operation(summary = "保存电子签名 / 可信时间戳参数")
    @PostMapping("/signUpsert")
    public Result<Void> signUpsert(@Valid @RequestBody SignConfigUpsertDTO upsertDTO) {
        sysConfigService.upsertSignConfig(upsertDTO);
        return Result.success("保存成功", null);
    }
}
