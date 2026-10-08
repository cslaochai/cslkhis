package com.his.ai.controller;

import com.his.ai.dto.Icd10PredictDTO;
import com.his.ai.dto.Icd10SelectListDTO;
import com.his.ai.service.Icd10Capability;
import com.his.ai.service.Icd10RecallService;
import com.his.ai.vo.Icd10PredictResultVO;
import com.his.ai.vo.Icd10SelectListVO;
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

import java.util.List;

/**
 * ICD-10 智能编码接口。
 */
@Tag(name = "AI 能力-ICD10 智能编码")
@RestController
@RequestMapping("/ai/icd10")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class AiIcd10Controller {

    private final Icd10Capability icd10Capability;

    private final Icd10RecallService icd10RecallService;

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "推荐 ICD-10 编码（码表封闭集合内选择 + 规则降级回落）")
    @PostMapping("/predict")
    public Result<Icd10PredictResultVO> predict(@Valid @RequestBody Icd10PredictDTO predictDTO) {
        return Result.success(icd10Capability.predict(predictDTO));
    }

    @Operation(summary = "检索 ICD-10 编码下拉选项（纯字典查询，不调用模型）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<Icd10SelectListVO>> selectList(@Valid @RequestBody Icd10SelectListDTO selectListDTO) {
        return Result.success(icd10RecallService.selectOptions(selectListDTO));
    }
}
