package com.his.miniapp.controller;

import com.his.provider.dto.AppointUpsertDTO;
import com.his.provider.dto.RevisitFeePreviewDTO;
import com.his.provider.service.AppointProvider;
import com.his.provider.vo.BizAppointInfoListVO;
import com.his.provider.vo.RevisitFeePreviewVO;
import com.his.provider.vo.RevisitRecordSelectVO;
import com.his.common.base.Result;
import com.his.common.enums.AppointSourceEnum;
import com.his.common.enums.RevisitSourceEnum;
import com.his.common.enums.VisitTypeEnum;
import com.his.provider.service.PatientProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final AppointProvider appointProvider;
    private final PatientProvider patientProvider;

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "复诊原病历候选（按就诊日倒序，只含真正看过病的记录）")
    @GetMapping("/recordSelectList")
    public Result<List<RevisitRecordSelectVO>> recordSelectList(@RequestParam Long patientId) {
        if (!patientProvider.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(appointProvider.revisitRecordSelectList(patientId, 20));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "复诊费用预估（按复诊收费策略，只读）")
    @PostMapping("/feePreview")
    public Result<RevisitFeePreviewVO> feePreview(@RequestBody @Valid RevisitFeePreviewDTO previewDTO) {
        if (!patientProvider.canAccessPatient(previewDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的费用");
        }
        previewDTO.setRevisitSource(RevisitSourceEnum.PATIENT_SELF.getCode());
        return Result.success(appointProvider.revisitFeePreview(previewDTO));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "自助复诊预约（占号源，收免费按策略判定）")
    @PostMapping("/upsert")
    public Result<BizAppointInfoListVO> upsert(@RequestBody @Valid AppointUpsertDTO upsertDTO) {
        if (!patientProvider.canAccessPatient(upsertDTO.getPatientId())) {
            return Result.error("无权为该就诊人预约");
        }
        upsertDTO.setId(null);
        upsertDTO.setVisitType(VisitTypeEnum.REVISIT.getCode());
        upsertDTO.setRevisitSource(RevisitSourceEnum.PATIENT_SELF.getCode());
        // 线上只从排班的预约池扣号，传别的等于网上抢现场号
        upsertDTO.setRegistSource(AppointSourceEnum.APPOINTMENT.getCode());
        BizAppointInfoListVO result = appointProvider.addAppoint(upsertDTO);
        return Result.success("复诊预约成功", result);
    }
}
