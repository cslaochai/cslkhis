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
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        if (patientGuardianService.patientScopeViolated(queryDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的处方");
        }
        List<BizPrescriptionVO> resultList = prescriptionService.getByPatientId(queryDTO);
        return Result.success(resultList);
    }

    @Operation(summary = "处方分页查询（审方工作台；unauditedOnly=true 只看未审方）")
    @GetMapping("/listPage")
    public Result<PageResult<BizPrescriptionVO>> listPage(@Valid PrescriptionQueryPageDTO query) {
        return Result.success(prescriptionService.listPage(query));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:edit')")
    @Operation(summary = "处方审核（审方药师签名；处方状态置「已审核」）")
    @PostMapping("/audit")
    public Result<BizPrescriptionVO> audit(@Valid @RequestBody PrescriptionAuditDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        return Result.success(prescriptionService.auditPrescription(
                dto,
                UserUtils.getCurrentUser().getEmployeeId(),
                UserUtils.getCurrentUser().getRealName(),
                user == null ? null : user.getDeptId(),
                user == null ? null : user.getDeptName()));
    }

    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:prescriptionAudit:list')")
    @Operation(summary = "处方合理用药批量审查（只读标注，不落库）")
    @PostMapping("/rationalCheck")
    public Result<List<PrescriptionRationalVO>> rationalCheck(@Valid @RequestBody PrescriptionRationalQueryDTO queryDTO) {
        return Result.success(prescriptionService.rationalCheck(queryDTO.getPrescriptionIds()));
    }
}
