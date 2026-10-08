package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.miniapp.dto.TriageRecommendDTO;
import com.his.miniapp.service.MiniappTriageService;
import com.his.miniapp.vo.TriageDeptVO;
import com.his.miniapp.vo.TriageSymptomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端智能导诊
 */
@Tag(name = "患者端-智能导诊")
@RestController
@RequestMapping("/miniapp/triage")
@RequiredArgsConstructor
public class MiniappTriageController {

    private final MiniappTriageService miniappTriageService;

    @Operation(summary = "按主诉推荐科室（规则匹配，急症置顶）")
    @PostMapping("/recommend")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<TriageDeptVO>> recommend(@RequestBody @Valid TriageRecommendDTO dto) {
        return Result.success(miniappTriageService.recommend(dto.getDescription()));
    }

    @Operation(summary = "常见症状快捷标签")
    @GetMapping("/hotSymptoms")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<TriageSymptomVO>> hotSymptoms() {
        return Result.success(miniappTriageService.hotSymptoms());
    }
}
