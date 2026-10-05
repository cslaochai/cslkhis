package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.emr.service.FollowupTaskService;
import com.his.emr.vo.BizFollowupTaskVO;
import com.his.miniapp.dto.FollowupReplyDTO;
import com.his.patient.service.PatientGuardianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者端随访（G-06）：患者查看自己的随访任务并提交反馈。
 * 随访计划与反馈回写都归随访域（his-emr），这里只做患者端入口与归属校验。
 */
@Tag(name = "患者端-我的随访")
@RestController
@RequestMapping("/miniapp/followup")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniappFollowupController {

    private final FollowupTaskService followupTaskService;

    private final PatientGuardianService patientGuardianService;

    @Operation(summary = "我的随访任务（只含本人名下，按计划时间倒序）")
    @GetMapping("/myList")
    public Result<List<BizFollowupTaskVO>> myList(@RequestParam Long patientId) {
        // 只允许查自己绑定的就诊人 —— 服务端按绑定关系收窄
        if (!patientGuardianService.canAccessPatient(patientId)) {
            return Result.error("无权查询该就诊人的随访任务");
        }
        return Result.success(followupTaskService.listForPatient(patientId));
    }

    @Operation(summary = "提交随访反馈（写回任务的患者反馈列）")
    @PostMapping("/reply")
    public Result<Void> reply(@RequestBody @Valid FollowupReplyDTO dto) {
        if (!patientGuardianService.canAccessPatient(dto.getPatientId())) {
            return Result.error("无权为该就诊人提交反馈");
        }
        followupTaskService.replyFromPatient(dto.getTaskId(), dto.getPatientId(), dto.getReplyText());
        return Result.success("反馈已提交", null);
    }
}
