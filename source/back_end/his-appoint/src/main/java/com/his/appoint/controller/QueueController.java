package com.his.appoint.controller;

import com.his.appoint.dto.*;
import com.his.appoint.service.QueueService;
import com.his.appoint.vo.*;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分诊叫号控制器
 */
@Tag(name = "分诊叫号")
@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:doctorWorkstation:list', 'opd:todayVisits:list', 'opd:triage:list')")
public class QueueController {

    private final QueueService queueService;

    @Operation(summary = "查询医生接诊状态")
    @PostMapping("/getDoctorStatus")
    public Result<DoctorStatusVO> getDoctorStatus(@Valid @RequestBody DoctorStatusQueryDTO dto) {
        return Result.success(queueService.doctorStatusOf(dto.getDoctorId()));
    }

    @Operation(summary = "读取当前登录医生的接诊状态（0 空闲 / 1 接诊中 / 2 暂离）")
    @GetMapping("/doctorStatus/current")
    public Result<DoctorStatusVO> getCurrentDoctorStatus() {
        return Result.success(queueService.currentDoctorStatus());
    }

    @Operation(summary = "设置当前登录医生的接诊状态：0 恢复接诊 / 2 暂离")
    @PostMapping("/doctorStatus/set")
    public Result<Void> setCurrentDoctorStatus(@RequestBody @Valid DoctorStatusSetDTO dto) {
        queueService.setCurrentDoctorStatus(dto);
        return Result.success();
    }

    @Operation(summary = "批量查询医生接诊状态")
    @PostMapping("/batchGetDoctorStatus")
    public Result<List<DoctorStatusVO>> batchGetDoctorStatus(@Valid @RequestBody DoctorStatusBatchQueryDTO dto) {
        return Result.success(queueService.batchDoctorStatus(dto));
    }

    @Operation(summary = "查询队列列表")
    @GetMapping("/getTodayQueueList")
    public Result<List<BizQueueListVO>> getTodayQueueList(@Valid QueueTodayQueryDTO queueQueryDTO) {
        return Result.success(queueService.getTodayQueueList(queueQueryDTO));
    }

    @Operation(summary = "查询队列列表")
    @GetMapping("/listPage")
    public Result<PageResult<BizQueueListVO>> listPage(@Valid QueueQueryDTO queueQueryDTO) {
        return Result.success(queueService.listPage(queueQueryDTO));
    }

    @Operation(summary = "门诊日志分页（跨科室，筛选条件下推）")
    @GetMapping("/opdLogListPage")
    public Result<PageResult<OpdLogListVO>> listPage(@Valid OpdLogQueryPageDTO queryPageDTO) {
        return Result.success(queueService.listPage(queryPageDTO));
    }

    @Operation(summary = "门诊日志统计条（与分页同一套筛选条件）")
    @GetMapping("/opdLogStats")
    public Result<OpdLogStatsVO> opdLogStats(@Valid OpdLogQueryPageDTO queryPageDTO) {
        return Result.success(queueService.opdLogStats(queryPageDTO));
    }

    @Operation(summary = "队列统计")
    @GetMapping("/stats")
    public Result<QueueStatsVO> queueStats(@Valid QueueQueryDTO queueQueryDTO) {
        return Result.success(queueService.stats(queueQueryDTO));
    }

    @PreAuthorize("hasAuthority('opd:triage:edit')")
    @Operation(summary = "按挂号记录签到")
    @PostMapping("/checkInByRegist")
    public Result<Void> checkInByRegistId(@RequestBody @Valid AppointCheckInUpdateDTO updateDTO) {
        queueService.checkInByRegist(updateDTO);
        return Result.success();
    }

    @PreAuthorize("hasAnyAuthority('opd:triage:edit', 'opd:doctorWorkstation:edit')")
    @Operation(summary = "叫下一位（返回接诊回执：叫到了谁）")
    @PostMapping("/callNext")
    public Result<QueueCallNextVO> callNext(@Valid @RequestBody QueueCallNextDTO queueCallNextDTO) {
        return Result.success(queueService.callNextByOperator(queueCallNextDTO));
    }

    @Operation(summary = "过号处理")
    @PostMapping("/overdueQueue")
    public Result<Void> overdueQueue(@Valid @RequestBody QueueOverdueDTO dto) {
        boolean success = queueService.overdueQueue(dto.getId(), dto.getReason());
        return success ? Result.success() : Result.error("操作失败");
    }

    @Operation(summary = "医保费用预估")
    @PostMapping("/estimate")
    public Result<InsuranceEstimateVO> estimate(@Valid @RequestBody InsuranceEstimateDTO dto) {
        InsuranceEstimateVO vo = queueService.estimateInsurance(dto);
        return Result.success(vo);
    }

    @PreAuthorize("hasAnyAuthority('opd:triage:edit', 'opd:doctorWorkstation:edit')")
    @Operation(summary = "呼叫患者（返回接诊回执：叫到了谁）")
    @PostMapping("/callPatient")
    public Result<QueueCallNextVO> callPatient(@RequestParam Long queueId) {
        return Result.success(queueService.callPatient(queueId));
    }

    @PreAuthorize("hasAnyAuthority('opd:triage:edit', 'opd:doctorWorkstation:edit')")
    @Operation(summary = "重呼患者")
    @PostMapping("/recallPatient")
    public Result<Void> recallPatient(@RequestParam Long queueId) {
        boolean success = queueService.recallPatient(queueId);
        return success ? Result.success() : Result.error("重呼失败");
    }

    @PreAuthorize("hasAuthority('opd:triage:edit')")
    @Operation(summary = "复诊插队（返回插队后的顺序号）")
    @PostMapping("/rejoinQueue")
    public Result<Integer> rejoinQueue(@RequestParam Long queueId) {
        Integer seq = queueService.rejoinQueue(queueId);
        return Result.success("已入队，当前序号 " + seq, seq);
    }

    @Operation(summary = "诊室状态")
    @GetMapping("/doctor/consulting")
    public Result<List<DoctorConsultingVO>> getDoctorConsultingInfo() {
        return Result.success(queueService.doctorConsultingInfoOfCurrentDept());
    }

    @Operation(summary = "获取分诊台统计数据")
    @GetMapping("/statsCard")
    public Result<QueueStatsVO> getStatsCard() {
        return Result.success(queueService.statsCardOfCurrentDept());
    }

    @Operation(summary = "获取当前就诊中的患者")
    @GetMapping("/consultingPatients")
    public Result<List<DoctorConsultingVO>> getConsultingPatients() {
        return Result.success(queueService.consultingPatientsOfCurrentDept());
    }

    @Operation(summary = "获取复诊等候超时患者")
    @GetMapping("/revisitTimeout")
    public Result<List<BizQueueListVO>> getRevisitTimeoutPatients() {
        return Result.success(queueService.revisitTimeoutPatientsOfCurrentDept());
    }

    @Operation(summary = "获取医生接诊统计")
    @GetMapping("/doctorStats")
    public Result<List<DoctorStatsVO>> getDoctorStats() {
        return Result.success(queueService.doctorStatsOfCurrentDept());
    }

    @PreAuthorize("hasAuthority('opd:triage:add')")
    @Operation(summary = "保存门诊分诊（写分诊记录 + 回写队列当前生效值）")
    @PostMapping("/triage/save")
    public Result<TriageRecordVO> saveTriage(@RequestBody @Valid TriageUpsertDTO dto) {
        TriageRecordVO vo = queueService.saveTriage(dto);
        return Result.success("分诊已保存", vo);
    }

    @Operation(summary = "分诊卡回显（当前生效值 + 历史留痕）")
    @GetMapping("/triage/getByQueueId")
    public Result<TriageDetailVO> getTriageByQueueId(@RequestParam Long queueId) {
        return Result.success(queueService.getTriageByQueueId(queueId));
    }

    @Operation(summary = "已缴费未签到的挂号列表（分诊台「待签到」抽屉，口径同 /stats 的 unchecked）")
    @GetMapping("/uncheckedList")
    public Result<List<BizQueueListVO>> uncheckedList(@RequestParam(required = false) Long deptId,
                                                      @RequestParam(required = false) String visitDate) {
        return Result.success(queueService.uncheckedList(deptId, visitDate));
    }
}
