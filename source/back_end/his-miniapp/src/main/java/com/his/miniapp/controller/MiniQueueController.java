package com.his.miniapp.controller;

import com.his.spi.dto.AppointCheckInUpdateDTO;
import com.his.spi.service.QueueFacadeService;
import com.his.spi.service.RegistFacadeService;
import com.his.spi.vo.PatientQueueVO;
import com.his.common.base.Result;
import com.his.spi.service.PatientFacadeService;
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
public class MiniQueueController {

    private final QueueFacadeService queueFacadeService;
    private final RegistFacadeService registFacadeService;
    private final PatientFacadeService patientFacadeService;

    /**
     * 我的排队：就诊人近三日挂号的候诊位次与前方等待人数，只允许查绑定关系内的就诊人。
     * 用于小程序「排队叫号」页的候诊进度展示。
     */
    @Operation(summary = "我的排队（近3日挂号 + 位次 + 前方等待人数）")
    @GetMapping("/myQueue")
    public Result<List<PatientQueueVO>> myQueue(@RequestParam Long patientId) {
        // patientId 由前端传入，但只允许查自己绑定的就诊人——服务端按绑定关系收窄
        if (!patientFacadeService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的排队信息");
        }
        return Result.success(queueFacadeService.myQueue(patientId));
    }

    /**
     * 到院签到：为指定挂号登记到院，进入候诊队列等待叫号；归属从挂号记录反查后再校验。
     * 用于小程序「排队叫号」页患者到院后的签到动作。
     */
    @Operation(summary = "到院签到")
    @PostMapping("/myCheckIn")
    public Result<Void> myCheckIn(@RequestBody @Valid AppointCheckInUpdateDTO updateDTO) {
        // 归属从挂号记录反查，不看前端传的是谁——否则改 registId 就能替他人签到
        Long ownerPatientId = registFacadeService.patientIdOfRegist(updateDTO.getRegistId());
        if (ownerPatientId == null) {
            return Result.error("挂号记录不存在");
        }
        if (!patientFacadeService.canAccessPatient(ownerPatientId)) {
            return Result.error("无权为该就诊人签到");
        }
        boolean success = queueFacadeService.checkInByRegistId(updateDTO);
        return success ? Result.success() : Result.error("签到失败");
    }
}
