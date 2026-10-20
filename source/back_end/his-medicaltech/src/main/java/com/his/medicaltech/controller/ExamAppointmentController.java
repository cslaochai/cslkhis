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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检查预约中心控制器：待约申请受理、占号/改约/到检/完成与爽约处理的接口入口。
 */
@Tag(name = "检查预约-预约中心")
@RestController
@RequestMapping("/medicaltech/examAppoint")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:examAppoint:list')")
public class ExamAppointmentController {

    private final ExamAppointmentService examAppointmentService;

    /**
     * 查询等待排期的检查申请，可按患者、科室、项目过滤。
     * 用于检查预约中心「预约工作台」页签的查询卡。
     */
    @Operation(summary = "待预约申请分页（已缴费/已提交急诊，且无在办预约）")
    @PostMapping("/pendingListPage")
    public Result<PageResult<ExamApptVO.ApplyVO>> pendingListPage(@Valid @RequestBody ExamApptDTO.ApplyQuery query) {
        return Result.success(examAppointmentService.pendingListPage(query));
    }

    /**
     * 查询在办与历史预约单。
     * 用于检查预约中心「预约台账」页签的查询卡。
     */
    @Operation(summary = "预约台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<ExamApptVO.ApptVO>> listPage(@Valid @RequestBody ExamApptDTO.ApptQuery query) {
        return Result.success(examAppointmentService.listPage(query));
    }

    /**
     * 按预约状态汇总单量，过滤条件与台账分页同口径。
     * 用于检查预约中心「预约台账」页签顶部的状态角签。
     */
    @Operation(summary = "预约台账状态分布（与分页同口径，后端统计）")
    @PostMapping("/statusCount")
    public Result<List<ExamApptVO.StatusCountVO>> statusCount(@Valid @RequestBody ExamApptDTO.ApptQuery query) {
        return Result.success(examAppointmentService.statusCount(query));
    }

    /**
     * 预约中心全局指标（待约、今日预约、爽约等）。
     * 用于检查预约中心页顶部统计卡。
     */
    @Operation(summary = "预约中心统计")
    @GetMapping("/stats")
    public Result<ExamApptVO.StatsVO> stats() {
        return Result.success(examAppointmentService.stats());
    }

    /**
     * 取单张预约单详情与关联链路。
     * 用于检查预约中心台账行点击打开的详情弹框。
     */
    @Operation(summary = "预约单详情")
    @GetMapping("/getDetailById")
    public Result<ExamApptVO.ApptDetailVO> getDetailById(@RequestParam Long apptId) {
        return Result.success(examAppointmentService.getDetail(apptId));
    }

    /**
     * 为待约申请落定设备与时段，确认前由服务端复核设备、患者、时间、时长四类冲突。
     * 用于检查预约中心「占号预约」弹框选格确认后调用。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "占号预约（设备/患者/时长/流程四道冲突检测）")
    @PostMapping("/book")
    public Result<ExamApptVO.ApptDetailVO> book(@Valid @RequestBody ExamApptDTO.Book dto) {
        ExamApptVO.ApptDetailVO vo = examAppointmentService.book(dto);
        return Result.success("预约成功：" + vo.getApptNo() + " " + vo.getExamDate() + " " + vo.getTimeRange()
                + " " + vo.getDeviceName(), vo);
    }

    /**
     * 终结原在办预约并重新占一个新时段，改约轨迹留档。
     * 用于检查预约中心「改约」弹框（与占号共用同一组件）确认后调用。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "改约（终结旧单 + 重新占号）")
    @PostMapping("/reschedule")
    public Result<ExamApptVO.ApptDetailVO> reschedule(@Valid @RequestBody ExamApptDTO.Reschedule dto) {
        ExamApptVO.ApptDetailVO vo = examAppointmentService.reschedule(dto);
        return Result.success("已改约为：" + vo.getApptNo() + " " + vo.getExamDate() + " " + vo.getTimeRange(), vo);
    }

    /**
     * 撤销预约、释放已占时段，并把源申请退回待约状态。
     * 用于检查预约中心台账行的取消动作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:delete')")
    @Operation(summary = "取消预约（退号 + 申请单回退）")
    @PostMapping("/cancel")
    public Result<Boolean> cancel(@Valid @RequestBody ExamApptDTO.Cancel dto) {
        examAppointmentService.cancel(dto);
        return Result.success("预约已取消", true);
    }

    /**
     * 记录患者在预约时段当日到达科室。
     * 用于检查预约中心台账的到检签到动作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "到检签到")
    @PostMapping("/arrive")
    public Result<Boolean> arrive(@Valid @RequestBody ExamApptDTO.ApptIdOnly dto) {
        examAppointmentService.arrive(dto);
        return Result.success("已到检", true);
    }

    /**
     * 检查执行完毕后结束在办预约，释放号源占用的最后一环。
     * 用于检查预约中心台账的完成动作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "完成检查")
    @PostMapping("/finish")
    public Result<Boolean> finish(@Valid @RequestBody ExamApptDTO.ApptIdOnly dto) {
        examAppointmentService.finish(dto);
        return Result.success("检查已完成", true);
    }

    /**
     * 按设备开放口径推算可预约的空闲时段，供受理员参考。
     * 用于检查预约中心「占号预约/改约」弹框选日期时段时调用。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "可选时段推荐")
    @PostMapping("/recommend")
    public Result<ExamApptVO.RecommendVO> recommend(@Valid @RequestBody ExamApptDTO.Recommend dto) {
        return Result.success(examAppointmentService.recommend(dto));
    }

    /**
     * 立即扫描逾期未到检的预约并判定爽约，返回本次处理张数。
     * 用于定时任务漏跑时检查预约中心页面上的人工补跑入口。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "手工补跑爽约扫描（定时漏跑时的运维入口）")
    @PostMapping("/autoNoShow")
    public Result<Integer> autoNoShow() {
        int n = examAppointmentService.autoNoShow();
        return Result.success("本次判定爽约 " + n + " 张", n);
    }
}
