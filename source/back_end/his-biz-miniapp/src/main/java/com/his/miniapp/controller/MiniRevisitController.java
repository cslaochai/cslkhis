package com.his.miniapp.controller;

import com.his.api.dto.AppointUpsertDTO;
import com.his.api.dto.RevisitFeePreviewDTO;
import com.his.api.service.AppointApi;
import com.his.api.vo.BizAppointInfoListVO;
import com.his.api.vo.RevisitFeePreviewVO;
import com.his.api.vo.RevisitRecordSelectVO;
import com.his.common.base.Result;
import com.his.api.service.PatientApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端·自助复诊预约
 */
@Tag(name = "患者端-复诊预约")
@RestController
@RequestMapping("/miniapp/revisit")
@RequiredArgsConstructor
public class MiniRevisitController {

    /**
     * 2-复诊
     */
    private static final int VISIT_TYPE_REVISIT = 2;
    /**
     * 3-患者自助复诊
     */
    private static final int REVISIT_SOURCE_PATIENT_SELF = 3;
    /**
     * 4-预约挂号：线上只从排班的预约池扣号，传别的等于网上抢现场号
     */
    private static final int REGIST_SOURCE_APPOINTMENT = 4;

    private final AppointApi appointApi;
    private final PatientApi patientApi;

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "复诊原病历候选（按就诊日倒序，只含真正看过病的记录）")
    @GetMapping("/recordSelectList")
    public Result<List<RevisitRecordSelectVO>> recordSelectList(@RequestParam Long patientId) {
        if (!patientApi.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(appointApi.revisitRecordSelectList(patientId, 20));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "复诊费用预估（按复诊收费策略，只读）")
    @PostMapping("/feePreview")
    public Result<RevisitFeePreviewVO> feePreview(@RequestBody @Valid RevisitFeePreviewDTO previewDTO) {
        if (!patientApi.canAccessPatient(previewDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的费用");
        }
        previewDTO.setRevisitSource(REVISIT_SOURCE_PATIENT_SELF);
        return Result.success(appointApi.revisitFeePreview(previewDTO));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "自助复诊预约（占号源，收免费按策略判定）")
    @PostMapping("/upsert")
    public Result<BizAppointInfoListVO> upsert(@RequestBody @Valid AppointUpsertDTO upsertDTO) {
        if (!patientApi.canAccessPatient(upsertDTO.getPatientId())) {
            return Result.error("无权为该就诊人预约");
        }
        upsertDTO.setId(null);
        upsertDTO.setVisitType(VISIT_TYPE_REVISIT);
        upsertDTO.setRevisitSource(REVISIT_SOURCE_PATIENT_SELF);
        upsertDTO.setRegistSource(REGIST_SOURCE_APPOINTMENT);
        BizAppointInfoListVO result = appointApi.addAppoint(upsertDTO);
        return Result.success("复诊预约成功", result);
    }
}
