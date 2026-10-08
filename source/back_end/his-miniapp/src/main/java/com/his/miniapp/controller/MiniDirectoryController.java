package com.his.miniapp.controller;

import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.common.base.Result;
import com.his.miniapp.service.MiniDirectoryService;
import com.his.miniapp.vo.MiniDeptSelectListVO;
import com.his.miniapp.vo.MiniDoctorSelectListVO;
import com.his.miniapp.vo.MiniScheduleSelectVO;
import com.his.miniapp.vo.MiniPatientDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
public class MiniDirectoryController {

    private final MiniDirectoryService miniDirectoryService;

    @Operation(summary = "开放科室列表")
    @GetMapping("/deptList")
    public Result<List<MiniDeptSelectListVO>> deptList() {
        return Result.success(miniDirectoryService.openDepartments());
    }

    @Operation(summary = "科室医生名册")
    @GetMapping("/doctorList")
    public Result<List<MiniDoctorSelectListVO>> doctorList(@RequestParam Long deptId) {
        return Result.success(miniDirectoryService.doctorsByDept(deptId));
    }

    @Operation(summary = "可挂号源（复用排班域 service，患者端原样取数）")
    @PostMapping("/scheduleList")
    public Result<List<MiniScheduleSelectVO>> scheduleList(@Valid @RequestBody ScheduleSelectQueryDTO queryDTO) {
        return Result.success(miniDirectoryService.schedules(queryDTO));
    }

    @Operation(summary = "患者档案（只允许查绑定关系内的就诊人）")
    @GetMapping("/patient")
    public Result<MiniPatientDetailVO> patient(@RequestParam Long patientId) {
        return Result.success(miniDirectoryService.patientProfile(patientId));
    }
}
