package com.his.miniapp.controller;

import com.his.system.dto.ScheduleSelectQueryDTO;
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

    /**
     * 开放科室列表：患者端可见、可线上挂号的科室名册。
     * 用于小程序「搜索」页按科室名检索候选。
     */
    @Operation(summary = "开放科室列表")
    @GetMapping("/deptList")
    public Result<List<MiniDeptSelectListVO>> deptList() {
        return Result.success(miniDirectoryService.openDepartments());
    }

    /**
     * 科室医生名册：指定科室下可挂号的出诊医生。
     * 用于小程序「预约挂号」页选科室后的医生列表，以及「搜索」页按医生名的检索结果。
     */
    @Operation(summary = "科室医生名册")
    @GetMapping("/doctorList")
    public Result<List<MiniDoctorSelectListVO>> doctorList(@RequestParam Long deptId) {
        return Result.success(miniDirectoryService.doctorsByDept(deptId));
    }

    /**
     * 可挂号源：按日期取排班号源（含预约池余号与挂号费），患者端原样取数不做二次加工。
     * 用于小程序「预约挂号」页的日期号源表与「复诊预约」页的改约选号。
     */
    @Operation(summary = "可挂号源（复用排班域 service，患者端原样取数）")
    @PostMapping("/scheduleList")
    public Result<List<MiniScheduleSelectVO>> scheduleList(@Valid @RequestBody ScheduleSelectQueryDTO queryDTO) {
        return Result.success(miniDirectoryService.schedules(queryDTO));
    }

    /**
     * 患者档案：就诊人基本信息、余额与就诊次数，只允许查绑定关系内的就诊人。
     * 用于小程序「我的」页顶部就诊人卡片展示。
     */
    @Operation(summary = "患者档案（只允许查绑定关系内的就诊人）")
    @GetMapping("/patient")
    public Result<MiniPatientDetailVO> patient(@RequestParam Long patientId) {
        return Result.success(miniDirectoryService.patientProfile(patientId));
    }
}
