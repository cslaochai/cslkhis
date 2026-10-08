package com.his.miniapp.controller;

import com.his.appoint.dto.AppointCheckInUpdateDTO;
import com.his.appoint.service.BizQueueService;
import com.his.appoint.vo.PatientQueueVO;
import com.his.common.base.Result;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端排队叫号
 */
@Tag(name = "患者端-排队叫号")
@RestController
@RequestMapping("/miniapp/queue")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappQueueController {

    private final BizQueueService bizQueueService;
    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "我的排队（近3日挂号 + 位次 + 前方等待人数）")
    @GetMapping("/myQueue")
    public Result<List<PatientQueueVO>> myQueue(@RequestParam Long patientId) {
        // patientId 由前端传入，但只允许查自己绑定的就诊人——服务端按绑定关系收窄
        if (!patientGuardianService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的排队信息");
        }
        return Result.success(bizQueueService.myQueue(patientId));
    }

    @Operation(summary = "到院签到")
    @PostMapping("/myCheckIn")
    public Result<Void> myCheckIn(@RequestBody @Valid AppointCheckInUpdateDTO updateDTO) {
        // 归属从挂号记录反查，不看前端传的是谁——否则改 registId 就能替他人签到
        Long ownerPatientId = bizQueueService.patientIdOfRegist(updateDTO.getRegistId());
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientGuardianService.canAccessPatient(ownerPatientId)) {
            return Result.error("无权为该就诊人签到");
        }
        boolean success = bizQueueService.checkInByRegistId(updateDTO);
        return success ? Result.success() : Result.error("签到失败");
    }
}
