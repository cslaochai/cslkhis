package com.his.ai.controller;

import com.his.ai.service.AiHealthService;
import com.his.ai.vo.AiHealthVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 能力运维接口。
 * <p>
 * <b>healthCheck 刻意不发起模型调用</b>：这是运维每 10 秒可能要打一次的探针，
 * 每次真调一次大模型既烧钱又慢。它回答的问题是「配置齐不齐、能力开没开、熔断没有」，
 * 而不是「模型今天心情好不好」。
 * 想验证端到端连通性，直接调一次 {@code /ai/icd10/predict} 更实在。
 * <p>
 * <b>配置本体在 application.yml 的 {@code ai.*} 段</b>，启动时绑定、改值需重启，
 * 因此这里没有任何「刷新 AI 配置」的入口 —— 原因见 {@link AiHealthService#refreshCodeCache()}。
 */
@Tag(name = "AI 能力-运维")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'opd:emergency:list', 'medtech:laboratoryWorkstation:list')")
public class AiHealthController {

    private final AiHealthService aiHealthService;

    @Operation(summary = "AI 运行时状态（只读配置快照，不产生模型调用）")
    @GetMapping("/healthCheck")
    public Result<AiHealthVO> healthCheck() {
        return Result.success(aiHealthService.runtimeStatus());
    }

    @PreAuthorize("hasAuthority('patient:profile:edit')")
    @Operation(summary = "刷新 ICD 码表缓存（AI 连接配置在 application.yml，改动需重启服务）")
    @PostMapping("/configRefresh")
    public Result<Void> configRefresh() {
        aiHealthService.refreshCodeCache();
        return Result.success("码表缓存已刷新；AI 连接配置在 application.yml，改动需重启服务", null);
    }
}
