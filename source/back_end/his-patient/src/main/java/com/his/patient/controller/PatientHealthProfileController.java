package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.vo.PatientHealthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者健康档案聚合控制器
 *
 * <p>一次带回六组明细 + 主档文本投影快照 + 分叉标记，供「健康档案」页面使用。
 *
 * <p>为什么不拆成六个请求：页面要同时判断「自述文本与结构化明细是否分叉」，
 * 这个判据必须在同一份快照上算 —— 文本与明细来自不同的表，分六次取数时
 * 只要有人同时在改档案，用户就会看到「文本说明细为空、明细其实刚加进来」这种自相矛盾的画面。
 */
@Tag(name = "患者健康档案")
@RestController
@RequestMapping("/patient/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:profile:list')")
public class PatientHealthProfileController {

    private final PatientHealthProfileService patientHealthProfileService;

    @Operation(summary = "查询患者健康档案（六组）")
    @GetMapping("/getDetail")
    public Result<PatientHealthProfileVO> getDetail(@RequestParam Long patientId) {
        return Result.success(patientHealthProfileService.getProfile(patientId));
    }
}
