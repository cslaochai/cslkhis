package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.service.Icd9cm3Service;
import com.his.platform.vo.SysIcd9cm3SelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 手术操作编码控制器：ICD-9-CM-3 术式候选下拉的接口入口。
 */
@Tag(name = "手术操作编码")
@RestController
@RequestMapping("/system/icd9cm3")
@RequiredArgsConstructor
public class Icd9cm3Controller {

    private final Icd9cm3Service icd9cm3Service;

    /**
     * 按编码或名称片段取术式候选，前缀命中优先，返回条数由入参上限控制。
     * 用于手术申请填写拟施手术、住院医嘱选「手术」类项目时的项目下拉。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "ICD-9-CM-3 术式下拉候选（keyword/limit 可选）")
    @GetMapping("/selectList")
    public Result<List<SysIcd9cm3SelectListVO>> selectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(icd9cm3Service.selectOptions(keyword, limit));
    }
}
