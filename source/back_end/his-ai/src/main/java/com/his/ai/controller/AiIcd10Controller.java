package com.his.ai.controller;

import com.his.ai.dto.Icd10PredictDTO;
import com.his.ai.dto.Icd10SelectListDTO;
import com.his.ai.service.Icd10Capability;
import com.his.ai.service.Icd10RecallService;
import com.his.ai.vo.Icd10PredictVO;
import com.his.ai.vo.Icd10SelectListVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ICD-10 智能编码接口。
 * <p>
 * <b>与 {@code /system/icd10/predict} 的关系 —— 刻意共存，不是重复建设</b>：
 * 既有接口是纯关键词打分版本（候选集之外还会顺手给出 DRG 权重与费用估算，
 * 这两项是按 ICD 大类硬编码估算的，不是真实分组结果）。
 * 本接口把「选哪个编码」这一步升级为「码表封闭集合内的大模型重排」，
 * 但<b>不删除</b>旧接口：
 * <ul>
 *   <li>旧接口行为不变，未接入 AI 的调用方零改动；</li>
 *   <li>新接口的降级路径就是旧的规则算法，所以迁移没有功能倒退风险；</li>
 *   <li>his-system 不能反过来依赖 his-ai（会形成循环依赖），
 *       因此只能并存，不能把旧接口改成转发。</li>
 * </ul>
 * 前端迁移建议：优先调 {@code /ai/icd10/predict}，当返回 {@code degraded=true}
 * 且 {@code predictions} 为空时再回落旧接口。
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
    public Result<Icd10PredictVO> predict(@RequestBody Icd10PredictDTO predictDTO) {
        return Result.success(icd10Capability.predict(predictDTO));
    }

    @Operation(summary = "检索 ICD-10 编码下拉选项（纯字典查询，不调用模型）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/selectList")
    public Result<List<Icd10SelectListVO>> selectList(@RequestBody Icd10SelectListDTO selectListDTO) {
        return Result.success(icd10RecallService.selectOptions(selectListDTO));
    }
}
