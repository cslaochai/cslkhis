package com.his.charge.controller;

import com.his.charge.dto.YbCatalogRuleImportBatchDTO;
import com.his.charge.service.YbCatalogRuleService;
import com.his.charge.vo.YbImportResultVO;
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
 * 医保目录报销规则控制器。
 *
 * <p>规则表决定统筹金额（比目录表更敏感：目录错了是对不上码，规则错了是直接多报/少报钱），
 * 所以权限挂在编辑权上，且只开批量导入这一个写入口 —— 逐条手改规则没有业务场景。
 */
@Tag(name = "医保目录对照-报销规则")
@RestController
@RequestMapping("/charge/ybCatalogRule")
@RequiredArgsConstructor
public class YbCatalogRuleController {

    private final YbCatalogRuleService ybCatalogRuleService;

    /**
     * 规则批量导入：按规则编号幂等，已存在即更新。
     * 用于拿到省医保局下发的待遇政策后一次性刷新本地规则表（目录能批量导，规则以前只能写 SQL，不对称）。
     */
    @PreAuthorize("hasAuthority('finance:insuranceMapping:edit')")
    @Operation(summary = "报销规则批量导入（按 rule_no 幂等，存在即更新）")
    @PostMapping("/importBatch")
    public Result<YbImportResultVO> importBatch(@Valid @RequestBody YbCatalogRuleImportBatchDTO batchDTO) {
        return Result.success(ybCatalogRuleService.importBatch(batchDTO.getItems()));
    }
}