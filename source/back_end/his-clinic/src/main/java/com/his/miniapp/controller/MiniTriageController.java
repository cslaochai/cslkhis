package com.his.miniapp.controller;

import com.his.common.base.Result;
import com.his.miniapp.dto.TriageRecommendDTO;
import com.his.miniapp.service.MiniTriageService;
import com.his.miniapp.vo.MiniTriageDeptVO;
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
public class MiniTriageController {

    private final MiniTriageService miniTriageService;

    /**
     * 按主诉推荐科室：拿症状描述做规则匹配给出候选科室，急症科室排在最前。
     * 用于小程序「智能导诊」页患者填写主诉后的推荐结果。
     */
    @Operation(summary = "按主诉推荐科室（规则匹配，急症置顶）")
    @PostMapping("/recommend")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<MiniTriageDeptVO>> recommend(@RequestBody @Valid TriageRecommendDTO dto) {
        return Result.success(miniTriageService.recommend(dto.getDescription()));
    }

    /**
     * 常见症状快捷标签：高频主诉词清单，供一键填入代替手工输入。
     * 用于小程序「智能导诊」页输入框下方的快捷选择。
     */
    @Operation(summary = "常见症状快捷标签")
    @GetMapping("/hotSymptoms")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<List<TriageSymptomVO>> hotSymptoms() {
        return Result.success(miniTriageService.hotSymptoms());
    }
}
