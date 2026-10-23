package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.InspectionAuditDTO;
import com.his.medicaltech.dto.InspectionExecuteDTO;
import com.his.medicaltech.dto.InspectionRecordQueryDTO;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.vo.BizInspectionRecordVO;
import com.his.medicaltech.vo.InspectionDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检查管理控制器：检查记录检索与技师侧签到、执行、报告审核流转的接口入口。
 */
@Tag(name = "检查管理")
@RestController
@RequestMapping("/medicaltech/inspection")
// G5：类级兜底（方法级已有注解的保持不变）。
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:inspectionWorkstation:list')")
@RequiredArgsConstructor
public class BizInspectionController {

    private final MedicalTechService medicalTechService;

    /**
     * 检查记录分页：按患者或检查科室筛出检查记录行。
     * 用于检查工作站的检查记录查询表格，也用于住院医嘱工作区的检查记录页签与胶片量方发放页按患者检索候选记录。
     */
    @Operation(summary = "分页查询检查记录列表")
    @PostMapping("/listPage")
    public Result<PageResult<BizInspectionRecordVO>> inspectionListPage(@Valid @RequestBody InspectionRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectInspectionRecordPageVO(
                queryDTO.getPatientId(), queryDTO.getInspectionDeptId(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 检查记录全量列表（不分页）：按患者或检查科室筛出该患者历次检查记录。
     * 供需要一次看全一名患者检查情况的环节取数，如开单前核对该患者已有的检查项目。
     */
    @Operation(summary = "查询检查记录列表（不分页）")
    @PostMapping("/list")
    public Result<List<BizInspectionRecordVO>> inspectionList(@Valid @RequestBody InspectionRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectInspectionRecordListVO(
                queryDTO.getPatientId(), queryDTO.getInspectionDeptId()));
    }

    // 读报告详情的权限口径：这个入口被**两个岗位群**共用 ——
    //   临床侧（医生/护士/病案）走 emr:records:list；
    //   检查技师在自己的工作站看同一份报告走 medtech:inspectionWorkstation:list。
    // 所以用 hasAnyAuthority 而不是只判医生那个码（只判医生码会把技师站打 403）。
    @Operation(summary = "根据ID获取检查记录详情（含报告）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:inspectionWorkstation:list')")
    public Result<InspectionDetailVO> getInspectionDetail(@RequestParam Long recordId) {
        return Result.success(medicalTechService.getInspectionDetail(recordId));
    }

    /**
     * 检查签到：登记患者到检时刻，把检查记录推进到已签到。
     * 用于检查工作站列表行的「签到」按钮（检查技师岗）。
     */
    @Operation(summary = "检查签到")
    @PostMapping("/checkIn")
    public Result<Void> checkIn(@RequestParam Long recordId) {
        boolean success = medicalTechService.checkIn(recordId);
        return success ? Result.success("签到成功", null) : Result.error("签到失败");
    }

    /**
     * 开始检查：把检查记录推进到检查中，标记技师已上机。
     * 用于检查工作站列表行的「开始检查」按钮。
     */
    @Operation(summary = "开始检查")
    @PostMapping("/start")
    public Result<Void> startInspection(@RequestParam Long recordId) {
        boolean success = medicalTechService.startInspection(recordId);
        return success ? Result.success("开始检查", null) : Result.error("操作失败");
    }

    /**
     * 执行检查：落到检查所见、诊断结论与建议，记录推进到已出结果并生成检查报告。
     * 用于检查工作站「检查结果录入」弹框提交；放射与心电项目在这里被拒绝，各走自己的工作站。
     */
    @Operation(summary = "执行检查")
    @PostMapping("/execute")
    public Result<Void> executeInspection(@Valid @RequestBody InspectionExecuteDTO executeDTO) {
        boolean success = medicalTechService.executeInspection(executeDTO.getRecordId(),
                executeDTO.getExecuteBy(), executeDTO.getResultDescription(),
                executeDTO.getResultConclusion(), executeDTO.getSuggestions());
        return success ? Result.success("执行成功", null) : Result.error("执行失败");
    }

    /**
     * 拍片完成（放射项目专用，sql/138）。
     *
     * <p>权限仍然挂在检查工作站的按钮码上（技师岗），但它**只能推进到这里**：
     * 不建报告、不签名。诊断结论必须去放射诊断工作站（菜单 414）由诊断医师下。
     */
    @Operation(summary = "拍片完成（放射项目；技师岗终点，不产生报告）")
    @PostMapping("/finishShoot")
    @PreAuthorize("hasAuthority('medtech:inspectionWorkstation:imageAdd')")
    public Result<Void> finishShoot(@RequestParam Long recordId) {
        boolean success = medicalTechService.finishShoot(recordId);
        return success ? Result.success("拍片完成，已转到放射诊断工作站待书写报告", null)
                : Result.error("操作失败");
    }

    /**
     * 审核检查报告：审核人签名并把记录与关联报告推进到已审核、已发布。
     * 用于检查工作站「审核检查报告」弹框提交，是检查流程出报告的最后一道。
     */
    @Operation(summary = "审核检查报告")
    @PostMapping("/audit")
    public Result<Void> auditInspection(@Valid @RequestBody InspectionAuditDTO auditDTO) {
        boolean success = medicalTechService.auditInspection(auditDTO.getRecordId(), auditDTO.getAuditBy());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }

    /**
     * 检查报告签名 PDF 验签：重算文件 SHA-256 与落库签名值 + 证书公钥比对。
     */
    @Operation(summary = "检查报告签名 PDF 验签")
    @GetMapping("/verifyPdf")
    public Result<com.his.medicaltech.dto.ReportPdfVerifyVO> verifyInspectionPdf(@RequestParam Long recordId) {
        return Result.success(medicalTechService.verifyInspectionReportPdf(recordId));
    }
}
