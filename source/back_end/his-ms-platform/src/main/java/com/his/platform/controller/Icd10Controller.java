package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.dto.Icd10PredictDTO;
import com.his.platform.service.Icd10Service;
import com.his.platform.vo.Icd10PredictVO;
import com.his.platform.vo.SysIcd10SelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ICD-10 诊断编码控制器：编码下拉候选与诊断文本编码预测的接口入口。
 */
@Tag(name = "ICD-10编码管理")
@RestController
@RequestMapping("/system/icd10")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class Icd10Controller {

    private final Icd10Service icd10Service;

    /**
     * 按关键词取 ICD-10 编码候选，按与关键词的相关性排序，返回条数由入参上限控制。
     * 用于门诊医生站与住院病历工作台填写诊断时的编码搜索下拉。
     */
    @Operation(summary = "ICD-10下拉候选（按相关性排序，keyword/limit 可选）")
    @GetMapping("/selectList")
    public Result<List<SysIcd10SelectListVO>> selectList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(icd10Service.selectOptions(keyword, limit));
    }

    /**
     * 由主诉/现病史/专科检查/已填诊断文本预测候选编码，走院内规则匹配不调模型。
     * 用于医生站病历填写时的「编码预测」与「AI 诊断建议」取候选。
     */
    @Operation(summary = "智能预测ICD-10编码（规则版，不调用模型）")
    @PostMapping("/predict")
    public Result<List<Icd10PredictVO>> predict(@Valid @RequestBody Icd10PredictDTO predictDTO) {
        return Result.success(icd10Service.predict(predictDTO));
    }
}
