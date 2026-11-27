package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.service.SimRosterService;
import com.his.platform.vo.SimClinicRosterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仿真实验室只读花名册：供 cssimlab 启动时拉取真实科室/医生/诊室/分诊账号建会话。
 */
@Tag(name = "仿真花名册")
@RestController
@RequestMapping("/system/employee")
@RequiredArgsConstructor
public class SimRosterController {

    private final SimRosterService simRosterService;

    @Operation(summary = "门诊科室出诊花名册（仿真用）")
    @GetMapping("/simClinicRoster")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SimClinicRosterVO>> simClinicRoster() {
        return Result.success(simRosterService.clinicRoster());
    }
}
