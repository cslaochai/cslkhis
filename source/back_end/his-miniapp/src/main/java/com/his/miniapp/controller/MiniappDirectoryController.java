package com.his.miniapp.controller;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.common.base.Result;
import com.his.miniapp.service.MiniappDirectoryService;
import com.his.miniapp.vo.DeptSelectListVO;
import com.his.miniapp.vo.DoctorSelectListVO;
import com.his.miniapp.vo.PatientDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端挂号目录（科室/医生/号源/患者档案）。
 */
@Tag(name = "患者端-挂号目录")
@RestController
@RequestMapping("/miniapp/directory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappDirectoryController {

    private final MiniappDirectoryService miniappDirectoryService;

    @Operation(summary = "开放科室列表")
    @GetMapping("/deptList")
    public Result<List<DeptSelectListVO>> deptList() {
        return Result.success(miniappDirectoryService.openDepartments());
    }

    @Operation(summary = "科室医生名册")
    @GetMapping("/doctorList")
    public Result<List<DoctorSelectListVO>> doctorList(@RequestParam Long deptId) {
        return Result.success(miniappDirectoryService.doctorsByDept(deptId));
    }

    @Operation(summary = "可挂号源（复用排班域 service，患者端原样取数）")
    @PostMapping("/scheduleList")
    public Result<List<ScheduleSelectListVO>> scheduleList(@RequestBody ScheduleSelectQueryDTO queryDTO) {
        return Result.success(miniappDirectoryService.schedules(queryDTO));
    }

    @Operation(summary = "患者档案（只允许查绑定关系内的就诊人）")
    @GetMapping("/patient")
    public Result<PatientDetailVO> patient(@RequestParam Long patientId) {
        return Result.success(miniappDirectoryService.patientProfile(patientId));
    }
}
