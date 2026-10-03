package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.service.ExamAppointmentService;
import com.his.medicaltech.vo.ExamApptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 检查预约工作台接口（URL 前缀 /medicaltech/examAppoint）
 */
@Tag(name = "检查预约-预约中心")
@RestController
@RequestMapping("/medicaltech/examAppoint")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:examAppoint:list')")
public class ExamAppointmentController {

    private final ExamAppointmentService appointmentService;

    @Operation(summary = "待预约申请分页（已缴费/已提交急诊，且无在办预约）")
    @PostMapping("/pendingListPage")
    public Result<PageResult<ExamApptVO.ApplyVO>> pendingListPage(@RequestBody ExamApptDTO.ApplyQuery query) {
        return Result.success(appointmentService.pendingListPage(query));
    }

    @Operation(summary = "预约台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<ExamApptVO.ApptVO>> listPage(@RequestBody ExamApptDTO.ApptQuery query) {
        return Result.success(appointmentService.listPage(query));
    }

    @Operation(summary = "预约台账状态分布（与分页同口径，后端统计）")
    @PostMapping("/statusCount")
    public Result<List<ExamApptVO.StatusCountVO>> statusCount(@RequestBody ExamApptDTO.ApptQuery query) {
        return Result.success(appointmentService.statusCount(query));
    }

    @Operation(summary = "预约中心统计")
    @GetMapping("/stats")
    public Result<ExamApptVO.StatsVO> stats() {
        return Result.success(appointmentService.stats());
    }

    @Operation(summary = "预约单详情")
    @GetMapping("/getDetailById")
    public Result<ExamApptVO.ApptDetailVO> getDetailById(@RequestParam Long apptId) {
        return Result.success(appointmentService.getDetail(apptId));
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "占号预约（设备/患者/时长/流程四道冲突检测）")
    @PostMapping("/book")
    public Result<ExamApptVO.ApptDetailVO> book(@Valid @RequestBody ExamApptDTO.Book dto) {
        ExamApptVO.ApptDetailVO vo = appointmentService.book(dto);
        return Result.success("预约成功：" + vo.getApptNo() + " " + vo.getExamDate() + " " + vo.getTimeRange()
                + " " + vo.getDeviceName(), vo);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "改约（终结旧单 + 重新占号）")
    @PostMapping("/reschedule")
    public Result<ExamApptVO.ApptDetailVO> reschedule(@Valid @RequestBody ExamApptDTO.Reschedule dto) {
        ExamApptVO.ApptDetailVO vo = appointmentService.reschedule(dto);
        return Result.success("已改约为：" + vo.getApptNo() + " " + vo.getExamDate() + " " + vo.getTimeRange(), vo);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:delete')")
    @Operation(summary = "取消预约（退号 + 申请单回退）")
    @PostMapping("/cancel")
    public Result<Boolean> cancel(@Valid @RequestBody ExamApptDTO.Cancel dto) {
        appointmentService.cancel(dto);
        return Result.success("预约已取消", true);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "到检签到")
    @PostMapping("/arrive")
    public Result<Boolean> arrive(@Valid @RequestBody ExamApptDTO.ApptIdOnly dto) {
        appointmentService.arrive(dto);
        return Result.success("已到检", true);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "完成检查")
    @PostMapping("/finish")
    public Result<Boolean> finish(@Valid @RequestBody ExamApptDTO.ApptIdOnly dto) {
        appointmentService.finish(dto);
        return Result.success("检查已完成", true);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "可选时段推荐")
    @PostMapping("/recommend")
    public Result<ExamApptVO.RecommendVO> recommend(@Valid @RequestBody ExamApptDTO.Recommend dto) {
        return Result.success(appointmentService.recommend(dto));
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "手工补跑爽约扫描（定时漏跑时的运维入口）")
    @PostMapping("/autoNoShow")
    public Result<Integer> autoNoShow() {
        int n = appointmentService.autoNoShow();
        return Result.success("本次判定爽约 " + n + " 张", n);
    }
}
