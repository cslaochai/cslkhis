package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.AttendanceDTO;
import com.his.system.entity.BizStaffAttendance;
import com.his.system.service.StaffAttendanceService;
import com.his.system.vo.CalibrationAdviceVO;
import com.his.system.vo.StaffWorktimeVO;
import com.his.system.vo.WorktimeSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 实际出勤（闭环第 3、4 步：执行与回填）。
 *
 * <p>读（对照/汇总/建议）只要求登录：今天谁来了、谁的工时还没回填，是排班员、
 * 护士长、科主任都要看的信息，按菜单收口只会让人对着一个空面板排班。
 * 真正改数据的是「缺勤确认」「工时修正」「科室确认」「撤销」，这些按排班权限收口。
 *
 * <p>签到/签退只要求登录：这是员工对自己出勤的登记动作。
 * 是否"替别人签到"由科室确认环节兜住的（确认状态会留异议），
 * 不给打卡权限等于让没有 PC 权限的科室没法登记夜班出勤。
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
    public Result<BizStaffAttendance> checkIn(@RequestBody AttendanceDTO dto) {
        BizStaffAttendance row = staffAttendanceService.checkIn(dto);
        return Result.success(say(row.getAttendanceStatus()), row);
    }

    @Operation(summary = "签退（算实际工时/超时工时，给出迟到早退判定）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkOut")
    public Result<BizStaffAttendance> checkOut(@RequestBody AttendanceDTO dto) {
        BizStaffAttendance row = staffAttendanceService.checkOut(dto);
        return Result.success(row.getActualMinutes() != null
                ? "已签退，实际工时 " + row.getActualMinutes() + " 分钟" : "已签退", row);
    }

    @Operation(summary = "确认缺勤（全系统唯一能产生「缺勤」的入口，须科室确认）")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @PostMapping("/markAbsent")
    public Result<BizStaffAttendance> markAbsent(@RequestBody AttendanceDTO dto) {
        return Result.success("已确认为缺勤", staffAttendanceService.markAbsent(dto));
    }

    @Operation(summary = "手工登记/修正工时（没有打卡数据的日子由护士长补登）")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @PostMapping("/adjust")
    public Result<BizStaffAttendance> adjust(@RequestBody AttendanceDTO dto) {
        return Result.success("工时已登记", staffAttendanceService.adjust(dto));
    }

    @Operation(summary = "科室确认（0-待确认 1-已确认 2-有异议）")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestParam Long id, @RequestParam Integer confirmStatus) {
        staffAttendanceService.confirm(id, confirmStatus);
        return Result.success("已确认", null);
    }

    @Operation(summary = "撤销一条出勤登记（物理删，好可以把那天重新签一次）")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
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

    private String say(Integer status) {
        return switch (status == null ? 1 : status) {
            case 2 -> "已签到（迟到）";
            case 5 -> "已签到（替班）";
            case 6 -> "已签到（加班：当天没有排班计划）";
            case 7 -> "已签到（支援：实际出勤单元与计划不同）";
            default -> "已签到";
        };
    }
}
