package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.PrescriptionAuditDTO;
import com.his.emr.dto.PrescriptionQueryDTO;
import com.his.emr.dto.PrescriptionQueryPageDTO;
import com.his.emr.dto.PrescriptionRationalQueryDTO;
import com.his.emr.service.PrescriptionService;
import com.his.emr.vo.BizPrescriptionVO;
import com.his.emr.vo.PrescriptionRationalVO;
import com.his.patient.service.PatientGuardianService;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 医生工作站 - 处方管理控制器
 */
@Tag(name = "医生工作站-处方")
@RestController
@RequestMapping("/prescription")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:prescriptionAudit:list')")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "查询处方列表")
    @GetMapping("/getByPatientId")
    public Result<List<BizPrescriptionVO>> getByPatientId(@Valid PrescriptionQueryDTO queryDTO) {
        // 工作站接口，但患者 token 同样能调到这里：不校验就是「改个 patientId 读别人处方」
        if (patientGuardianService.patientScopeViolated(queryDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的处方");
        }
        List<BizPrescriptionVO> resultList = prescriptionService.getByPatientId(queryDTO);
        return Result.success(resultList);
    }

    // 患者端「我的处方」已迁至 his-miniapp 的 /miniapp/prescription/myList（患者端点集中收口）

    @Operation(summary = "处方分页查询（审方工作台；unauditedOnly=true 只看未审方）")
    @GetMapping("/listPage")
    public Result<PageResult<BizPrescriptionVO>> listPage(@Valid PrescriptionQueryPageDTO query) {
        return Result.success(prescriptionService.listPage(query));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "处方审核（审方药师签名；处方状态置「已审核」）")
    @PostMapping("/audit")
    public Result<BizPrescriptionVO> audit(@Valid @RequestBody PrescriptionAuditDTO dto) {
        // 审核人从登录态取，不从入参取 —— 入参能传"审核人"就等于签名的不可否认性可以随手伪造
        CurrentUser user = UserUtils.getCurrentUser();
        return Result.success(prescriptionService.auditPrescription(
                dto,
                UserUtils.getCurrentEmployeeId(),
                UserUtils.getCurrentEmployeeName(),
                user == null ? null : user.getDeptId(),
                user == null ? null : user.getDeptName()));
    }

    /**
     * 合理用药批量审查（相互作用 × 剂量上限）。只读标注，不改处方状态。
     *
     * <p>权限与本页列表一致（医生站与审方工作台都要看），不新增权限码：
     * 后端拦签发的那道闸在 {@code /audit} 里，本接口只负责提前把话说清楚。
     */
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:prescriptionAudit:list')")
    @Operation(summary = "处方合理用药批量审查（只读标注，不落库）")
    @PostMapping("/rationalCheck")
    public Result<List<PrescriptionRationalVO>> rationalCheck(@Valid @RequestBody PrescriptionRationalQueryDTO queryDTO) {
        return Result.success(prescriptionService.rationalCheck(queryDTO.getPrescriptionIds()));
    }
}
