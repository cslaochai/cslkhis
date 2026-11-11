package com.his.app.controller;

import com.his.app.dto.MiniExamBookDTO;
import com.his.app.dto.MiniExamCancelDTO;
import com.his.app.service.MiniExamService;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.spi.vo.PatientExamApptVO;
import com.his.spi.vo.PatientExamApplyVO;
import com.his.spi.vo.PatientExamSlotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端-检查预约（缴费后自助约时段、到院检查、取消）。
 */
@Tag(name = "患者端-检查预约")
@RestController
@RequestMapping("/miniapp/exam")
@RequiredArgsConstructor
public class MiniExamController {

    private final MiniExamService miniExamService;

    @Operation(summary = "待预约检查申请")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/pending")
    public Result<PageResult<PatientExamApplyVO>> pending(@RequestParam Long patientId,
                                                          @RequestParam int pageNum,
                                                          @RequestParam int pageSize) {
        return Result.success(miniExamService.pendingApplies(patientId, pageNum, pageSize));
    }

    @Operation(summary = "可选检查时段")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/slots")
    public Result<List<PatientExamSlotVO>> slots(@RequestParam Long patientId,
                                                 @RequestParam Long applyId,
                                                 @RequestParam(required = false) String examDate,
                                                 @RequestParam(required = false) String afterTime) {
        return Result.success(miniExamService.recommendSlots(patientId, applyId, examDate, afterTime));
    }

    @Operation(summary = "预约检查")
    @PreAuthorize("hasAuthority('PATIENT')")
    @PostMapping("/book")
    public Result<PatientExamApptVO> book(@Valid @RequestBody MiniExamBookDTO dto) {
        return Result.success(miniExamService.book(dto));
    }

    @Operation(summary = "取消检查预约")
    @PreAuthorize("hasAuthority('PATIENT')")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody MiniExamCancelDTO dto) {
        miniExamService.cancel(dto);
        return Result.success();
    }

    @Operation(summary = "我的检查预约")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/myAppointments")
    public Result<PageResult<PatientExamApptVO>> myAppointments(@RequestParam Long patientId,
                                                                @RequestParam int pageNum,
                                                                @RequestParam int pageSize) {
        return Result.success(miniExamService.myAppointments(patientId, pageNum, pageSize));
    }
}
