package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.InpatientNursingService;
import com.his.patient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 护理文书（三测单 / 护理记录单 / 生命体征监测）。
 *
 * <p><b>三测单同一时点只能有一条</b>：唯一索引 {@code uk_nr_admission_type_time} 兜底，
 * 重复录入会收到明确报错，不是静默覆盖 —— 同一次测量录两条，曲线上就是两个点，护士不知道该信哪个。
 */
@Tag(name = "住院护理文书")
@RestController
@RequestMapping("/patient/inpatient/nursing")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:nurse:list')")
public class InpatientNursingController {

    private final InpatientNursingService nursingService;

    @Operation(summary = "护理文书分页（按测量时间升序）")
    @GetMapping("/listPage")
    public Result<IPage<NursingRecordVO>> listPage(@Valid NursingRecordQueryPageDTO query) {
        return Result.success(nursingService.listPage(query));
    }

    @Operation(summary = "护理文书详情")
    @GetMapping("/detail")
    public Result<NursingRecordVO> detail(@RequestParam Long id) {
        return Result.success(nursingService.detail(id));
    }

    @PreAuthorize("hasAuthority('ipd:nurse:add')")
    @Operation(summary = "录入/修改护理文书（三测单按时点唯一；修改逐字段留痕）")
    @PostMapping("/save")
    public Result<NursingRecordVO> save(@Valid @RequestBody NursingRecordUpsertDTO dto) {
        return Result.success("护理文书已保存", nursingService.save(dto));
    }

    @Operation(summary = "三测单数据（按测量时间升序给全，前端据此自动画体温曲线；曲线不分页）")
    @GetMapping("/tempSheet")
    public Result<TempSheetVO> tempSheet(@RequestParam Long admissionId,
                                         @RequestParam(required = false) String beginDate,
                                         @RequestParam(required = false) String endDate) {
        return Result.success(nursingService.tempSheet(admissionId, beginDate, endDate));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "护理文书类型下拉")
    @GetMapping("/type/selectList")
    public Result<List<CodeOptionVO>> typeOptions() {
        return Result.success(nursingService.typeOptions());
    }

    // G14 护理完整体

    @PreAuthorize("hasAuthority('ipd:nurse:add')")
    @Operation(summary = "体温单批量录入（一次测量动作 × 多个在院患者；整体事务）")
    @PostMapping("/saveBatch")
    public Result<Integer> saveBatch(@Valid @RequestBody NursingRecordBatchUpsertDTO dto) {
        return Result.success(nursingService.saveBatch(dto));
    }

    @PreAuthorize("hasAuthority('ipd:nurse:add')")
    @Operation(summary = "录入/修改护理评估单（风险等级按量表分数段后端算）")
    @PostMapping("/assessment/save")
    public Result<NursingAssessmentVO> saveAssessment(@Valid @RequestBody NursingAssessmentUpsertDTO dto) {
        return Result.success("评估单已保存", nursingService.saveAssessment(dto));
    }

    @Operation(summary = "护理评估单分页（必须按入院ID或病区查询）")
    @GetMapping("/assessment/listPage")
    public Result<IPage<NursingAssessmentVO>> assessmentListPage(@Valid NursingAssessmentQueryPageDTO query) {
        return Result.success(nursingService.assessmentListPage(query));
    }

    @Operation(summary = "专项评估透视（每类量表最新一条：1-Braden 2-Morse 3-NRS 4-Caprini 5-管路滑脱）")
    @GetMapping("/assessment/latestByType")
    public Result<List<NursingAssessmentVO>> assessmentLatestByType(@RequestParam Long admissionId) {
        return Result.success(nursingService.assessmentLatestByType(admissionId));
    }

    @Operation(summary = "出入量小结（从护理文书原始测量行按日复算）")
    @GetMapping("/intakeOutputSummary")
    public Result<IntakeOutputSummaryVO> intakeOutputSummary(@RequestParam Long admissionId,
                                                             @RequestParam(required = false) String beginDate,
                                                             @RequestParam(required = false) String endDate) {
        return Result.success(nursingService.intakeOutputSummary(admissionId, beginDate, endDate));
    }
}
