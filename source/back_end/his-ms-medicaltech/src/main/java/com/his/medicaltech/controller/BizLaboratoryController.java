package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.LabResultSaveDTO;
import com.his.medicaltech.dto.LaboratoryAuditDTO;
import com.his.medicaltech.dto.LaboratoryReceiveDTO;
import com.his.medicaltech.dto.LaboratoryRecordQueryDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizLaboratoryRecordVO;
import com.his.medicaltech.vo.LaboratoryDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检验管理控制器：检验记录检索与接收标本、结果录入、报告审核流转的接口入口。
 */
@Tag(name = "医技管理")
@RestController
@RequestMapping("/medicaltech/laboratory")
// G5：类级兜底（方法级已有注解的保持不变）。
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:laboratoryWorkstation:list', 'opd:emergencyDoctor:list')")
@RequiredArgsConstructor
public class BizLaboratoryController {

    private final MedicalTechService medicalTechService;

    /**
     * 检验记录分页：按患者或检验科室筛出检验记录行。
     * 用于检验工作站的检验记录查询表格，也用于住院医嘱工作区的检验记录页签。
     */
    @Operation(summary = "分页查询检验记录列表")
    @PostMapping("/listPage")
    public Result<PageResult<BizLaboratoryRecordVO>> laboratoryListPage(@Valid @RequestBody LaboratoryRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectLaboratoryRecordPageVO(
                queryDTO.getPatientId(), queryDTO.getLaboratoryDeptId(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 检验记录全量列表（不分页）：按患者或检验科室一次取齐历次检验记录。
     * 用于医生工作站从检验申请单反查执行记录（申请单没带执行记录号时按患者翻全量再匹配）。
     */
    @Operation(summary = "查询检验记录列表（不分页）")
    @PostMapping("/list")
    public Result<List<BizLaboratoryRecordVO>> laboratoryList(@Valid @RequestBody LaboratoryRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectLaboratoryRecordListVO(
                queryDTO.getPatientId(), queryDTO.getLaboratoryDeptId()));
    }

    // 同 BizInspectionController#getInspectionDetail：医生站与检验技师站共用这个入口，
    // 临床侧走 emr:records:list，技师侧走 medtech:laboratoryWorkstation:list。
    @Operation(summary = "根据ID获取检验记录详情（含结果明细与报告）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:laboratoryWorkstation:list', 'opd:emergencyDoctor:list')")
    public Result<LaboratoryDetailVO> getLaboratoryDetail(@RequestParam Long recordId) {
        return Result.success(medicalTechService.getLaboratoryDetail(recordId));
    }

    /**
     * 接收标本：登记接收人与接收时刻，检验记录推进到已接收。
     * 用于检验工作站列表行的「接收标本」按钮，是标本进入实验室的落账动作。
     */
    @Operation(summary = "接收标本")
    @PostMapping("/receive")
    public Result<Void> receiveSpecimen(@Valid @RequestBody LaboratoryReceiveDTO receiveDTO) {
        boolean success = medicalTechService.receiveSpecimen(receiveDTO.getRecordId(), receiveDTO.getReceiveBy());
        return success ? Result.success("接收成功", null) : Result.error("接收失败");
    }

    /**
     * 录入检验结果：整单落到各项目结果值与诊断意见、建议，异常标志与危急值由服务端按参考区间判定。
     * 用于检验工作站「结果录入」弹框提交。
     */
    @Operation(summary = "录入检验结果")
    @PostMapping("/inputResult")
    public Result<Void> inputLabResult(@Valid @RequestBody LabResultSaveDTO labResultSaveDTO) {
        boolean success = medicalTechService.saveResult(labResultSaveDTO);
        return success ? Result.success("录入成功", null) : Result.error("录入失败");
    }


    /**
     * 审核检验报告：审核人签名后记录发布，并向开单医生推送结果通知。
     * 用于检验工作站「审核」弹框提交，是检验流程出报告的最后一道。
     */
    @Operation(summary = "审核检验报告")
    @PostMapping("/audit")
    public Result<Void> auditLaboratory(@Valid @RequestBody LaboratoryAuditDTO auditDTO) {
        boolean success = medicalTechService.auditLaboratory(auditDTO.getRecordId(), auditDTO.getAuditBy(), auditDTO.getPin());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }

    /**
     * 检验报告签名 PDF 验签：重算文件 SHA-256 与落库签名值 + 证书公钥比对。
     */
    @Operation(summary = "检验报告签名 PDF 验签")
    @GetMapping("/verifyPdf")
    public Result<com.his.medicaltech.dto.ReportPdfVerifyVO> verifyLaboratoryPdf(@RequestParam Long recordId) {
        return Result.success(medicalTechService.verifyLaboratoryReportPdf(recordId));
    }
}
