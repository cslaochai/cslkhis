package com.his.ai.controller;

import com.his.ai.dto.PatientFeeExplainDTO;
import com.his.ai.dto.PatientReportExplainDTO;
import com.his.ai.service.PatientFeeExplainCapability;
import com.his.ai.service.PatientReportExplainCapability;
import com.his.ai.vo.PatientFeeExplainVO;
import com.his.ai.vo.PatientReportExplainVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 能力 - 患者端（小程序客服台的两个"讲人话"入口）。
 * <p>
 * <b>为什么放在 his-ai 而不是 his-miniapp：</b>业务模块不得依赖 his-ai（会成环），
 * 而这两个能力要复用检验判定规则与 AI 执行器，只能落在 his-ai，
 * 由 his-ai 反向读业务表（依赖方向仍是 his-ai 在业务模块之上）。
 * <p>
 * <b>为什么只放两个接口：</b>患者端做 AI，最大的诱惑是做成"什么都能问"。
 * 这里刻意只开两个口子，且两个口子的答案都建立在<b>确定性计算</b>之上：
 * 报告解读的事实来自检验规则、白话来自人工词典；费用解释的拆分来自账单明细。
 * 模型只负责措辞，且必须过安全闸。这样模型挂了、密钥没配，页面照样能用。
 */
@Tag(name = "AI能力-患者端")
@RestController
@RequestMapping("/ai/patient")
@RequiredArgsConstructor
public class AiPatientController {

    private final PatientReportExplainCapability reportExplainCapability;

    private final PatientFeeExplainCapability feeExplainCapability;

    @Operation(summary = "报告解读（患者版大白话）")
    @PostMapping("/reportExplain")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientReportExplainVO> reportExplain(@Valid @RequestBody PatientReportExplainDTO dto) {
        return Result.success(reportExplainCapability.execute(dto));
    }

    @Operation(summary = "费用解释（这笔钱怎么算的）")
    @PostMapping("/feeExplain")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PatientFeeExplainVO> feeExplain(@Valid @RequestBody PatientFeeExplainDTO dto) {
        return Result.success(feeExplainCapability.execute(dto));
    }
}
