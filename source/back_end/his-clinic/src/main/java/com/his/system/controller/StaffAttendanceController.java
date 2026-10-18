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

    /**
     * 代登签到：按入参指定的员工与排班落一条上班记录，重复刷卡保留最早那次时间。
     * 供护士长代替在岗人员登记出勤的流程节点使用；员工本人打卡走顶栏自助入口。
     */
    @Operation(summary = "签到（幂等：重复刷卡不改写最早那次签到时间）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkIn")
    public Result<StaffAttendanceVO> checkIn(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.checkIn(dto);
        return Result.success(vo.getMessage(), vo);
    }

    /**
     * 代登签退：补齐下班时点并算出实际工时、超时工时与迟到早退判定。
     * 与代登签到配成一对，供无打卡设备时由他人代为回填出勤的流程节点使用。
     */
    @Operation(summary = "签退（算实际工时/超时工时，给出迟到早退判定）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkOut")
    public Result<StaffAttendanceVO> checkOut(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.checkOut(dto);
        return Result.success(vo.getMessage(), vo);
    }

    /**
     * 确认缺勤：全系统唯一能把某天判成「缺勤」的入口，未登记不等于缺勤。
     * 用于病区护理排班页「出勤对照」抽屉里对某个护士某天的处置。
     */
    @Operation(summary = "确认缺勤（全系统唯一能产生「缺勤」的入口，须科室确认）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/markAbsent")
    public Result<StaffAttendanceVO> markAbsent(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.markAbsent(dto);
        return Result.success(vo.getMessage(), vo);
    }

    /**
     * 手工登记/修正工时：把某人某天的出勤时段与实际工时补进台账，供无打卡数据的日子回填。
     * 用于「出勤对照」抽屉中「未回填」行旁的补登动作。
     */
    @Operation(summary = "手工登记/修正工时（没有打卡数据的日子由护士长补登）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/adjust")
    public Result<StaffAttendanceVO> adjust(@Valid @RequestBody AttendanceDTO dto) {
        StaffAttendanceVO vo = staffAttendanceService.adjust(dto);
        return Result.success(vo.getMessage(), vo);
    }

    /**
     * 科室确认出勤行：按待确认/已确认/有异议三态回写，确认动作不改工时数值本身。
     * 供出勤台账的科室复核环节回写状态，有异议的行由人工再核。
     */
    @Operation(summary = "科室确认（0-待确认 1-已确认 2-有异议）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestParam Long id, @RequestParam Integer confirmStatus) {
        staffAttendanceService.confirm(id, confirmStatus);
        return Result.success("已确认", null);
    }

    /**
     * 撤销一条出勤登记：物理删掉这行，腾出那天以便重新签到或补登。
     * 用于「出勤对照」抽屉里发现补登/签到有误时的撤销动作。
     */
    @Operation(summary = "撤销一条出勤登记（物理删，好可以把那天重新签一次）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/remove")
    public Result<Void> remove(@RequestParam Long id) {
        staffAttendanceService.remove(id);
        return Result.success("已撤销", null);
    }

    /**
     * 计划与实际出勤的行级对照：一行一名护士一天，带差异类型和写给人看的原因。
     * 用于病区护理排班页点某一天后打开的「出勤对照」抽屉表格。
     */
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

    /**
     * 单元按日出勤执行汇总：计划/实到/缺勤/未回填/加班的人数与工时差。
     * 用于病区护理排班页「周排班矩阵」顶部的本周出勤执行条，点某一天可下钻到明细。
     */
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

    /**
     * 编制校准建议：把一段时间的执行结果折算成对人力配置标准的建议，只给建议不改编制。
     * 用于病区护理排班页出勤条末尾的「执行回看」提示，标准调整仍回人力配置标准页人工做。
     */
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

    /**
     * 本人签到：员工 id 只从登录态取，不收任何入参，杜绝「传谁的 id 就给谁打卡」。
     * 用于顶栏自助打卡按钮，打卡成功后同处再拉一次当日状态回显。
     */
    @Operation(summary = "我自己签到")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myCheckIn")
    public Result<StaffAttendanceVO> myCheckIn() {
        StaffAttendanceVO vo = staffAttendanceService.myCheckIn();
        return Result.success(vo.getMessage(), vo);
    }

    /**
     * 本人签退：同上只认登录态里的员工，落下班时点并回算当日工时。
     * 用于顶栏自助打卡按钮，与本人签到共同构成一天的上下班闭环。
     */
    @Operation(summary = "我自己签退")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myCheckOut")
    public Result<StaffAttendanceVO> myCheckOut() {
        StaffAttendanceVO vo = staffAttendanceService.myCheckOut();
        return Result.success(vo.getMessage(), vo);
    }
}
