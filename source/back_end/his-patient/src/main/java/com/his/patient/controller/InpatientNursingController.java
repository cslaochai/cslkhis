package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.InpatientNursingService;
import com.his.patient.vo.*;
import com.his.common.vo.CodeOptionVO;
import com.his.spi.vo.NursingAssessmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 护理文书（三测单 / 护理记录单 / 生命体征监测）。
 */
@Tag(name = "住院护理文书")
@RestController
@RequestMapping("/patient/inpatient/nursing")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:nurse:list')")
public class InpatientNursingController {

    private final InpatientNursingService inpatientNursingService;

    /**
     * 护理文书分页：按测量时间升序，可按住院、病区与文书类型过滤。
     * 用于护士站护理文书工作台的记录表格。
     */
    @Operation(summary = "护理文书分页（按测量时间升序）")
    @GetMapping("/listPage")
    public Result<IPage<NursingRecordVO>> listPage(@Valid NursingRecordPageQueryDTO query) {
        return Result.success(inpatientNursingService.listPage(query));
    }

    /**
     * 单条护理文书详情，取回该次测量的原始记录。
     * 供护理文书按记录ID回看时使用。
     */
    @Operation(summary = "护理文书详情")
    @GetMapping("/detail")
    public Result<NursingRecordVO> detail(@RequestParam Long id) {
        return Result.success(inpatientNursingService.detail(id));
    }

    /**
     * 录入或修改护理文书：同一测量时点只允许一条，重复录入报错而非静默覆盖，修改逐字段留痕。
     * 用于护士站护理文书工作台的手工录入与订正。
     */
    @PreAuthorize("hasAuthority('ipd:nurse:add')")
    @Operation(summary = "录入/修改护理文书（三测单按时点唯一；修改逐字段留痕）")
    @PostMapping("/save")
    public Result<NursingRecordVO> save(@Valid @RequestBody NursingRecordUpsertDTO dto) {
        return Result.success("护理文书已保存", inpatientNursingService.save(dto));
    }

    /**
     * 三测单取数：按测量时间升序返回全部测量点与体温上下限，不分页（分页会让曲线断段）。
     * 用于护士站三测单页签自动绘制体温曲线。
     */
    @Operation(summary = "三测单数据（按测量时间升序给全，前端据此自动画体温曲线；曲线不分页）")
    @GetMapping("/tempSheet")
    public Result<TempSheetVO> tempSheet(@RequestParam Long admissionId,
                                         @RequestParam(required = false) String beginDate,
                                         @RequestParam(required = false) String endDate) {
        return Result.success(inpatientNursingService.tempSheet(admissionId, beginDate, endDate));
    }

    /**
     * 护理文书类型下拉候选。
     * 用于护理文书的筛选条件与录入表单选类型。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "护理文书类型下拉")
    @GetMapping("/type/selectList")
    public Result<List<CodeOptionVO>> typeOptions() {
        return Result.success(inpatientNursingService.typeOptions());
    }

    /**
     * 体温单批量录入：一次测量动作覆盖多个在院患者，任一行不合法整批拒绝。
     * 用于护士站按病区批量录入当日三测。
     */
    @PreAuthorize("hasAuthority('ipd:nurse:add')")
    @Operation(summary = "体温单批量录入（一次测量动作 × 多个在院患者；整体事务）")
    @PostMapping("/saveBatch")
    public Result<Integer> saveBatch(@Valid @RequestBody NursingRecordBatchUpsertDTO dto) {
        return Result.success(inpatientNursingService.saveBatch(dto));
    }

    /**
     * 录入或修改护理评估单：总分由明细求和复算，风险等级按量表分数段由服务端判定。
     * 用于护士站专项评估页签的评估单填写。
     */
    @PreAuthorize("hasAuthority('ipd:nurse:add')")
    @Operation(summary = "录入/修改护理评估单（风险等级按量表分数段后端算）")
    @PostMapping("/assessment/save")
    public Result<NursingAssessmentVO> saveAssessment(@Valid @RequestBody NursingAssessmentUpsertDTO dto) {
        return Result.success("评估单已保存", inpatientNursingService.saveAssessment(dto));
    }

    /**
     * 护理评估单分页：必须限定到某次住院或某个病区，不接受全院裸捞。
     * 用于护士站专项评估页签的评估台账。
     */
    @Operation(summary = "护理评估单分页（必须按入院ID或病区查询）")
    @GetMapping("/assessment/listPage")
    public Result<IPage<NursingAssessmentVO>> assessmentListPage(@Valid NursingAssessmentPageQueryDTO query) {
        return Result.success(inpatientNursingService.assessmentListPage(query));
    }

    /**
     * 专项评估透视：每类量表只返回最新一条，没评过的类型不出现在结果里。
     * 用于护士站查看某名患者当前的各类风险评估结论。
     */
    @Operation(summary = "专项评估透视（每类量表最新一条：1-Braden 2-Morse 3-NRS 4-Caprini 5-管路滑脱）")
    @GetMapping("/assessment/latestByType")
    public Result<List<NursingAssessmentVO>> assessmentLatestByType(@RequestParam Long admissionId) {
        return Result.success(inpatientNursingService.assessmentLatestByType(admissionId));
    }

    /**
     * 出入量小结：从护理文书原始测量行按日复算入量、出量与当日净平衡，日期升序。
     * 用于护士站出入量小结页签。
     */
    @Operation(summary = "出入量小结（从护理文书原始测量行按日复算）")
    @GetMapping("/intakeOutputSummary")
    public Result<IntakeOutputSummaryVO> intakeOutputSummary(@RequestParam Long admissionId,
                                                             @RequestParam(required = false) String beginDate,
                                                             @RequestParam(required = false) String endDate) {
        return Result.success(inpatientNursingService.intakeOutputSummary(admissionId, beginDate, endDate));
    }
}
