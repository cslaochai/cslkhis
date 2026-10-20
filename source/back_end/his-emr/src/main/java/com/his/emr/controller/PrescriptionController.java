package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.PrescriptionAuditDTO;
import com.his.emr.dto.PrescriptionQueryDTO;
import com.his.emr.dto.PrescriptionPageQueryDTO;
import com.his.emr.dto.PrescriptionRationalQueryDTO;
import com.his.emr.service.PrescriptionService;
import com.his.emr.vo.BizPrescriptionVO;
import com.his.emr.vo.PrescriptionRationalVO;
import com.his.patient.service.PatientFacadeService;
import com.his.common.entity.CurrentUser;
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
    private final PatientFacadeService patientFacadeService;

    /**
     * 取一名患者本次挂号下的处方清单：越权访问该就诊人时直接拒绝。
     * 用于医生工作站接诊面板展示与回看已开处方。
     */
    @Operation(summary = "查询处方列表")
    @GetMapping("/getByPatientId")
    public Result<List<BizPrescriptionVO>> getByPatientId(@Valid PrescriptionQueryDTO queryDTO) {
        if (patientFacadeService.patientScopeViolated(queryDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的处方");
        }
        List<BizPrescriptionVO> resultList = prescriptionService.getByPatientId(queryDTO);
        return Result.success(resultList);
    }

    /**
     * 处方分页：关键字命中处方号/患者姓名/患者号，只看未审方时以审方签名缺失为准。
     * 用于药师审方工作台的查询卡与处方表格。
     */
    @Operation(summary = "处方分页查询（审方工作台；unauditedOnly=true 只看未审方）")
    @GetMapping("/listPage")
    public Result<PageResult<BizPrescriptionVO>> listPage(@Valid PrescriptionPageQueryDTO query) {
        return Result.success(prescriptionService.listPage(query));
    }

    /**
     * 处方审核：签审方意见并落药师电子签名，通过置已审核、退回带意见通知开方医生。
     * 用于审方工作台表格行的「审方」通过与退回两个动作；签名后处方内容锁定。
     */
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

    /**
     * 处方合理用药批量审查：相互作用与剂量上限核查结果只作行内标注，不写库、不改状态。
     * 用于审方工作台列表加载后给整页处方补一轮风险提示。
     */
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:prescriptionAudit:list')")
    @Operation(summary = "处方合理用药批量审查（只读标注，不落库）")
    @PostMapping("/rationalCheck")
    public Result<List<PrescriptionRationalVO>> rationalCheck(@Valid @RequestBody PrescriptionRationalQueryDTO queryDTO) {
        return Result.success(prescriptionService.rationalCheck(queryDTO.getPrescriptionIds()));
    }
}
