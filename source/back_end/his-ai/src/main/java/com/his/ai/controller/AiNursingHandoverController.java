package com.his.ai.controller;

import com.his.ai.dto.NursingHandoverDTO;
import com.his.ai.service.NursingHandoverCapability;
import com.his.ai.vo.WardHandoverVO;
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
 * AI 护理交接班摘要（G-13）。
 * <p>病区×班次事实聚合 + 模型拟 SBAR 摘要草稿，护士编辑终审、不写库；
 * 消费端是护士工作站（菜单 303），权限沿用入口码，不新增按钮码。</p>
 */
@Tag(name = "AI 护理交接班摘要")
@RestController
@RequestMapping("/ai/nursingHandover")
@RequiredArgsConstructor
public class AiNursingHandoverController {

    private final NursingHandoverCapability nursingHandoverCapability;

    @Operation(summary = "按病区+班次聚合护理事实并拟交接班摘要草稿（source=1-模型 2-规则，degraded 必显）")
    @PreAuthorize("hasAuthority('ipd:nurse:list')")
    @PostMapping("/compose")
    public Result<WardHandoverVO> compose(@Valid @RequestBody NursingHandoverDTO dto) {
        return Result.success(nursingHandoverCapability.compose(dto));
    }
}
