package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.dto.Icd10PredictDTO;
import com.his.system.service.Icd10Service;
import com.his.system.vo.Icd10PredictVO;
import com.his.system.vo.SysIcd10SelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * ICD-10 编码管理
 *
 * <p>控制器只做入参/出参转换，检索排序与打分逻辑都在 {@link Icd10Service}。</p>
 */
@Tag(name = "ICD-10编码管理")
@RestController
@RequestMapping("/system/icd10")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class Icd10Controller {

    private final Icd10Service icd10Service;

    @Operation(summary = "ICD-10下拉候选（按相关性排序，keyword/limit 可选）")
    @GetMapping("/selectList")
    public Result<List<SysIcd10SelectListVO>> selectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(icd10Service.selectOptions(keyword, limit));
    }

    @Operation(summary = "智能预测ICD-10编码（规则版，不调用模型）")
    @PostMapping("/predict")
    public Result<List<Icd10PredictVO>> predict(@RequestBody Icd10PredictDTO predictDTO) {
        return Result.success(icd10Service.predict(predictDTO));
    }
}
