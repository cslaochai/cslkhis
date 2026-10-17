package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.AttendanceDTO;
import com.his.system.service.StaffAttendanceService;
import com.his.system.vo.CalibrationAdviceVO;
import com.his.system.vo.StaffAttendanceVO;
import com.his.system.vo.StaffWorktimeVO;
import com.his.system.vo.WorktimeSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 实际出勤（闭环第 3、4 步：执行与回填）。
 */
@Tag(name = "实际出勤与工时归因")
@RestController
@RequestMapping("/system/staffAttendance")
@RequiredArgsConstructor
public class StaffAttendanceController {

    private final StaffAttendanceService staffAttendanceService;

    @Operation(summary = "签到（幂等：重复刷卡不改写最早那次签到时间）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkIn")
    public Result<StaffAttendanceVO> checkIn(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.checkIn(dto);
        return Result.success(vo.getMessage(), vo);
    }

    @Operation(summary = "签退（算实际工时/超时工时，给出迟到早退判定）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkOut")
    public Result<StaffAttendanceVO> checkOut(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.checkOut(dto);
        return Result.success(vo.getMessage(), vo);
    }

    @Operation(summary = "确认缺勤（全系统唯一能产生「缺勤」的入口，须科室确认）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/markAbsent")
    public Result<StaffAttendanceVO> markAbsent(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.markAbsent(dto);
        return Result.success(vo.getMessage(), vo);
    }

    @Operation(summary = "手工登记/修正工时（没有打卡数据的日子由护士长补登）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/adjust")
    public Result<StaffAttendanceVO> adjust(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.adjust(dto);
        return Result.success(vo.getMessage(), vo);
    }

    @Operation(summary = "科室确认（0-待确认 1-已确认 2-有异议）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestParam Long id, @RequestParam Integer confirmStatus) {
        staffAttendanceService.confirm(id, confirmStatus);
        return Result.success("已确认", null);
    }

    @Operation(summary = "撤销一条出勤登记（物理删，好可以把那天重新签一次）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        staffAttendanceService.remove(id);
        return Result.success("已撤销", null);
    }

    @Operation(summary = "计划 vs 实际 行级对照（可按单元/人/差异类型筛）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/comparison")
    public Result<List<StaffWorktimeVO>> comparison(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer orgType,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer staffType,
            @RequestParam(required = false) Integer diffType) {
        return Result.success(staffAttendanceService.comparison(startDate, endDate, orgType, orgId, staffType, diffType));
    }

    @Operation(summary = "单元 × 日 执行汇总（计划/实到/缺勤/未回填/加班人数与工时差）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/summary")
    public Result<List<WorktimeSummaryVO>> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer orgType,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer staffType) {
        return Result.success(staffAttendanceService.summary(startDate, endDate, orgType, orgId, staffType));
    }

    @Operation(summary = "编制校准建议（把执行结果喂回第 ① 层标准，只给建议不改编制）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/advice")
    public Result<List<CalibrationAdviceVO>> advice(
            @RequestParam(required = false) Integer orgType,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer staffType) {
        return Result.success(staffAttendanceService.advice(orgType, orgId, staffType));
    }

    // ------------------------------------------------------------------
    // 自助打卡（顶栏按钮）：employeeId 强制从 token 取，不接收入参。
    // 与上面 checkIn/checkOut（护士长代登）分开，避免传谁 ID 给谁打卡。
    // ------------------------------------------------------------------

    @Operation(summary = "查我今天的打卡状态（顶栏按钮轮询）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myToday")
    public Result<StaffAttendanceVO> myToday() {
        return Result.success(staffAttendanceService.myToday());
    }

    @Operation(summary = "我自己签到")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myCheckIn")
    public Result<StaffAttendanceVO> myCheckIn() {
        StaffAttendanceVO vo = staffAttendanceService.myCheckIn();
        return Result.success(vo.getMessage(), vo);
    }

    @Operation(summary = "我自己签退")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myCheckOut")
    public Result<StaffAttendanceVO> myCheckOut() {
        StaffAttendanceVO vo = staffAttendanceService.myCheckOut();
        return Result.success(vo.getMessage(), vo);
    }
}
