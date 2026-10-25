package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.MedicalRecordQueryDTO;
import com.his.clinical.dto.MedicalRecordPageQueryDTO;
import com.his.clinical.service.EmrService;
import com.his.clinical.vo.BizMedicalRecordVO;
import com.his.clinical.vo.EmrRecordDetailVO;
import com.his.clinical.vo.VisitDocsPrintVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子病历控制器：门诊病历的按患者与按就诊取数、分页检索、详情展开与审核入口。
 */
@Tag(name = "电子病历管理")
@RestController
@RequestMapping("/emr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'emr:medicalRecord:list', 'emr:medicalReview:list', 'opd:todayVisits:list')")
public class EmrController {

    private final EmrService emrService;

    /**
     * 取一名患者的历次门诊病历清单。
     * 用于医生工作站选中患者后的历史病历列表，供医生翻阅既往诊疗。
     */
    @Operation(summary = "查询病历列表")
    @GetMapping("/getByPatientId")
    public Result<List<BizMedicalRecordVO>> getByPatientId(@Valid MedicalRecordQueryDTO queryDTO) {
        return Result.success(emrService.getByPatientId(queryDTO));
    }

    /**
     * 取一次挂号对应的当前病历。
     * 用于医生工作站进入就诊时载入本次病历，以及今日就诊详情取病历主体。
     */
    @Operation(summary = "查询当前病历")
    @GetMapping("/getByRegistId")
    public Result<BizMedicalRecordVO> getByRegistId(@RequestParam Long registId) {
        return Result.success(emrService.getByRegistId(registId));
    }

    /**
     * 按患者、科室、时间与病历状态分页检索病历。
     * 用于病历管理与病历审核页面的主列表查询卡。
     */
    @Operation(summary = "分页查询病历列表")
    @GetMapping("/listPage")
    public Result<PageResult<BizMedicalRecordVO>> listPage(@Valid MedicalRecordPageQueryDTO queryDTO) {
        return Result.success(emrService.listPage(queryDTO));
    }

    /**
     * 按病历主键取病历正文及其关联的处方、检查申请、检验申请。
     * 用于病历详情弹框，病历管理、病历审核、门诊日志与今日就诊均从这里展开全套内容。
     */
    @Operation(summary = "根据ID获取病历详情（含处方、检查申请、检验申请）")
    @GetMapping("/getRecordDetailById")
    public Result<EmrRecordDetailVO> getRecordDetailById(@RequestParam Long recordId) {
        return Result.success(emrService.getRecordDetail(recordId));
    }

    /**
     * 记录一次病历审核结论（通过或驳回）并回写病历状态。
     * 用于病历审核页面的通过/驳回操作；仅处于待审核状态的病历可提交。
     */
    @PreAuthorize("hasAuthority('emr:medicalRecord:edit')")
    @Operation(summary = "审核病历")
    @PostMapping("/recordReview")
    public Result<Void> recordReview(@RequestParam Long recordId,
                                     @RequestParam boolean approved,
                                     @RequestParam(required = false) String remark,
                                     @RequestParam(required = false) String reviewerName) {
        boolean success = emrService.reviewRecord(recordId, approved, remark, reviewerName);
        return success ? Result.success("审核完成", null) : Result.error("审核失败");
    }

    /**
     * 医生点「打印就诊文书」：现场生成引导单 + 普通处方签 + 饮片处方签三份 PDF。
     *
     * <p>结诊时不自动生成 PDF（医生不一定打印，白写盘）；这里按需现场画，
     * 每次都重画以反映最新开方/检查结果。返回三份 PDF 的相对路径，前端分别打开打印。
     */
    @Operation(summary = "打印就诊文书（引导单+处方签）")
    @GetMapping("/printVisitDocs")
    public Result<VisitDocsPrintVO> printVisitDocs(@RequestParam Long recordId) {
        return Result.success(emrService.printVisitDocs(recordId));
    }
}
