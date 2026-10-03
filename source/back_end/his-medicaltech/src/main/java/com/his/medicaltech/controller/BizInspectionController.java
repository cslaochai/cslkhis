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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检查管理控制器
 */
@Tag(name = "检查管理")
@RestController
@RequestMapping("/medicaltech/inspection")
// G5：类级兜底（方法级已有注解的保持不变）。
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'medtech:inspectionWorkstation:list')")
@RequiredArgsConstructor
public class BizInspectionController {

    private final MedicalTechService medicalTechService;

    @Operation(summary = "分页查询检查记录列表")
    @PostMapping("/listPage")
    public Result<PageResult<BizInspectionRecordVO>> inspectionListPage(@RequestBody InspectionRecordQueryDTO queryDTO) {
        return Result.success(medicalTechService.selectInspectionRecordPageVO(
                queryDTO.getPatientId(), queryDTO.getInspectionDeptId(),
                queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @Operation(summary = "查询检查记录列表（不分页）")
    @PostMapping("/list")
    public Result<List<BizInspectionRecordVO>> inspectionList(@RequestBody InspectionRecordQueryDTO queryDTO) {
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

    @Operation(summary = "检查签到")
    @PostMapping("/checkIn")
    public Result<Void> checkIn(@RequestParam Long recordId) {
        boolean success = medicalTechService.checkIn(recordId);
        return success ? Result.success("签到成功", null) : Result.error("签到失败");
    }

    @Operation(summary = "开始检查")
    @PostMapping("/start")
    public Result<Void> startInspection(@RequestParam Long recordId) {
        boolean success = medicalTechService.startInspection(recordId);
        return success ? Result.success("开始检查", null) : Result.error("操作失败");
    }

    @Operation(summary = "执行检查")
    @PostMapping("/execute")
    public Result<Void> executeInspection(@RequestBody InspectionExecuteDTO executeDTO) {
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

    @Operation(summary = "审核检查报告")
    @PostMapping("/audit")
    public Result<Void> auditInspection(@RequestBody InspectionAuditDTO auditDTO) {
        boolean success = medicalTechService.auditInspection(auditDTO.getRecordId(), auditDTO.getAuditBy());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }
}
