package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.appoint.dto.AppointUpsertDTO;
import com.his.appoint.dto.RevisitFeePreviewDTO;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.service.AppointService;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.appoint.vo.RevisitFeePreviewVO;
import com.his.appoint.vo.RevisitRecordSelectVO;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者端·自助复诊预约（来源固定 3-患者自助）。
 *
 * <p>与 {@code /miniapp/appoint/upsert} 分开开，是因为<b>复诊来源必须由服务端定死</b>：
 * 「1-当日回诊」不占号源且整单免收，它的正当性来自"这还是一次就诊"，只有医生站在原诊当天才成立。
 * 若让患者自己传来源，改个数字就能拿上个月的病历反复挂 0 元号。
 * 费用预估也按来源 3 算 —— 预览与实收一旦分口径，患者就会在线上确认了 0 元、到院被告知要交钱。
 *
 * <p>原病历不复用 {@code /miniapp/emr/myRecords}：那个 VO 只给名字不给科室/医生主键，
 * 而策略判定命中不命中「同医生/同科室」差别很大，患者端要按它把号源分成"推荐/全部"两栏。
 */
@Tag(name = "患者端-复诊预约")
@RestController
@RequestMapping("/miniapp/revisit")
@RequiredArgsConstructor
public class MiniappRevisitController {

    /** 2-复诊 */
    private static final int VISIT_TYPE_REVISIT = 2;
    /** 3-患者自助复诊 */
    private static final int REVISIT_SOURCE_PATIENT_SELF = 3;
    /** 4-预约挂号：线上只从排班的预约池扣号，传别的等于网上抢现场号 */
    private static final int REGIST_SOURCE_APPOINTMENT = 4;

    private final AppointService appointService;
    private final PatientGuardianService patientGuardianService;

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "复诊原病历候选（按就诊日倒序，只含真正看过病的记录）")
    @GetMapping("/recordSelectList")
    public Result<List<RevisitRecordSelectVO>> recordSelectList(@RequestParam Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的病历");
        }
        return Result.success(appointService.revisitRecordSelectList(patientId, 20));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "复诊费用预估（按复诊收费策略，只读）")
    @PostMapping("/feePreview")
    public Result<RevisitFeePreviewVO> feePreview(@RequestBody @Valid RevisitFeePreviewDTO previewDTO) {
        if (!patientGuardianService.canAccessPatient(previewDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的费用");
        }
        previewDTO.setRevisitSource(REVISIT_SOURCE_PATIENT_SELF);
        return Result.success(appointService.revisitFeePreview(previewDTO));
    }

    @PreAuthorize("hasAuthority('PATIENT')")
    @Operation(summary = "自助复诊预约（占号源，收免费按策略判定）")
    @PostMapping("/upsert")
    public Result<BizAppointInfoListVO> upsert(@RequestBody @Valid AppointUpsertDTO upsertDTO) {
        if (!patientGuardianService.canAccessPatient(upsertDTO.getPatientId())) {
            return Result.error("无权为该就诊人预约");
        }
        upsertDTO.setId(null);
        upsertDTO.setVisitType(VISIT_TYPE_REVISIT);
        upsertDTO.setRevisitSource(REVISIT_SOURCE_PATIENT_SELF);
        upsertDTO.setRegistSource(REGIST_SOURCE_APPOINTMENT);
        BizAppointInfo result = appointService.addAppoint(upsertDTO);
        return Result.success("复诊预约成功", toVO(result));
    }

    private BizAppointInfoListVO toVO(BizAppointInfo entity) {
        if (entity == null) {
            return null;
        }
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
