package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.QualityIssuePageQueryDTO;
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
 * 数据质量报表控制器：全院数据完整性、一致性、及时性、唯一性、有效性五个维度的体检结果入口。
 */
@Tag(name = "数据质量报表")
@RestController
@RequestMapping("/report/quality")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('qc:dataQuality:list')")
public class QualityController {

    private final QualityService qualityService;

    /**
     * 五个质量维度的总览：每条规则带检查分母与命中数，合规率按维度加权算出。
     * 用于「数据质量报表」页的维度卡与规则台账。
     */
    @Operation(summary = "五维度总览（含每条规则的分母与命中数）")
    @GetMapping("/getSummary")
    public Result<QualitySummaryVO> getSummary() {
        return Result.success(qualityService.getSummary());
    }

    /**
     * 质量规则清单：可按单个维度收口，含每条规则的判定口径。
     * 用于质量报表把某维度下各规则逐条摊开核对的环节。
     */
    @Operation(summary = "规则清单（可按维度过滤）")
    @GetMapping("/getRuleList")
    public Result<List<QualityRuleVO>> getRuleList(String dimension) {
        return Result.success(qualityService.getRuleList(dimension));
    }

    /**
     * 质量维度字典：五个维度的编码与名称。
     * 用于质量报表按维度过滤规则与问题清单时的候选来源。
     */
    @Operation(summary = "维度字典")
    @GetMapping("/dimensionDict")
    public Result<List<QualityDimensionSelectListVO>> dimensionDict() {
        return Result.success(qualityService.dimensionDict());
    }

    /**
     * 质量问题清单分页：一行一条命中记录，可定位到具体业务记录与患者。
     * 用于「数据质量报表」页点某条规则后下钻的问题明细。
     */
    @Operation(summary = "问题清单（分页，可定位到具体记录）")
    @GetMapping("/listIssuePage")
    public Result<PageResult<QualityIssueVO>> listIssuePage(@Valid QualityIssuePageQueryDTO dto) {
        return Result.success(qualityService.listIssuePage(dto));
    }
}
