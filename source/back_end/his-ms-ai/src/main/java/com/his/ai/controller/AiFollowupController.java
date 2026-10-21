package com.his.ai.controller;

import com.his.ai.dto.FollowupComposeDTO;
import com.his.ai.service.FollowupComposeCapability;
import com.his.ai.vo.FollowupComposeVO;
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
 * AI 能力 - 随访
 */
@Tag(name = "AI能力-随访")
@RestController
@RequestMapping("/ai/followup")
@RequiredArgsConstructor
public class AiFollowupController {

    private final FollowupComposeCapability followupComposeCapability;

    /**
     * 按患者与随访类型拟一段随访话术初稿，模型不可用时回落该类型模板并标记降级。
     * 用于随访任务与电话随访编辑弹框的「AI 拟话术」，医护核对改完才随任务下发。
     */
    @Operation(summary = "AI 拟随访话术（医生终审后才可下发）")
    @PostMapping("/compose")
    @PreAuthorize("hasAuthority('inpatient:followup:add')")
    public Result<FollowupComposeVO> compose(@Valid @RequestBody FollowupComposeDTO dto) {
        return Result.success(followupComposeCapability.execute(dto));
    }
}
