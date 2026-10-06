package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.QualityIssueQueryPageDTO;
import com.his.medicaltech.service.QualityService;
import com.his.medicaltech.vo.QualityDimensionSelectListVO;
import com.his.medicaltech.vo.QualityIssueVO;
import com.his.medicaltech.vo.QualityRuleVO;
import com.his.medicaltech.vo.QualitySummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据质量报表（P5.3）。
 *
 * <p>挂 {@code /report/quality} 而不是复用 {@code /report}（运营报表）：运营报表回答
 * "这个月收了多少"，数据质量回答"这些数据能不能信"，前者的使用者是院长，
 * 后者是信息科与质控科，混在一个控制器里权限没法分。
 *
 * <p>全部用 GET：这几个接口只读，会被前端轮询与看板定时刷新，GET 语义更准，
 * 也便于用浏览器直接打开核对数字。
 */
@Tag(name = "数据质量报表")
@RestController
@RequestMapping("/report/quality")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('qc:dataQuality:list')")
public class QualityController {

    private final QualityService qualityService;

    @Operation(summary = "五维度总览（含每条规则的分母与命中数）")
    @GetMapping("/getSummary")
    public Result<QualitySummaryVO> getSummary() {
        return Result.success(qualityService.getSummary());
    }

    @Operation(summary = "规则清单（可按维度过滤）")
    @GetMapping("/getRuleList")
    public Result<List<QualityRuleVO>> getRuleList(String dimension) {
        return Result.success(qualityService.getRuleList(dimension));
    }

    @Operation(summary = "维度字典")
    @GetMapping("/dimensionDict")
    public Result<List<QualityDimensionSelectListVO>> dimensionDict() {
        return Result.success(qualityService.dimensionDict());
    }

    @Operation(summary = "问题清单（分页，可定位到具体记录）")
    @GetMapping("/listIssuePage")
    public Result<PageResult<QualityIssueVO>> listIssuePage(@Valid QualityIssueQueryPageDTO dto) {
        return Result.success(qualityService.listIssuePage(dto));
    }
}
