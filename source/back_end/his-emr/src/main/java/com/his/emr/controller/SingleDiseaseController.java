package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.SingleDiseaseDTO;
import com.his.emr.service.SingleDiseaseService;
import com.his.emr.vo.SingleDiseaseAutoEnrollStatVO;
import com.his.emr.vo.SingleDiseaseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 单病种质控（M4）：病种目录（ICD 前缀纳入）→ 病例纳入（首页快照）→ 质控判级 → 上报打标 → 病种指标。
 *
 * <p>@PreAuthorize 全部标到方法（类级注解会罩住未标注方法）。
 */
@Tag(name = "单病种质控")
@RestController
@RequestMapping("/qc/singleDisease")
@RequiredArgsConstructor
public class SingleDiseaseController {

    private final SingleDiseaseService singleDiseaseService;

    // 目录

    @PreAuthorize("hasAuthority('qc:singleDisease:list')")
    @Operation(summary = "病种目录（含已纳入例数）")
    @GetMapping("/diseaseList")
    public Result<List<SingleDiseaseVO.Disease>> diseaseList() {
        return Result.success(singleDiseaseService.diseaseList());
    }

    @PreAuthorize("hasAuthority('qc:singleDisease:add')")
    @Operation(summary = "病种目录新增/修改（编码唯一）")
    @PostMapping("/diseaseUpsert")
    public Result<SingleDiseaseVO.Disease> diseaseUpsert(@Valid @RequestBody SingleDiseaseDTO.DiseaseUpsert dto) {
        return Result.success(singleDiseaseService.diseaseUpsert(dto));
    }

    @PreAuthorize("hasAuthority('qc:singleDisease:add')")
    @Operation(summary = "病种目录删除（已纳入病例的病种拒绝删除；物理删）")
    @PostMapping("/diseaseDelete/{id}")
    public Result<Void> diseaseDelete(@PathVariable Long id) {
        singleDiseaseService.diseaseDelete(id);
        return Result.success(null);
    }

    // 纳入

    @PreAuthorize("hasAuthority('qc:singleDisease:edit')")
    @Operation(summary = "手工纳入病例（首页快照 + 诊断前缀校验 + 唯一校验）")
    @PostMapping("/enroll")
    public Result<SingleDiseaseVO.Case> enroll(@Valid @RequestBody SingleDiseaseDTO.Enroll dto) {
        return Result.success(singleDiseaseService.enroll(dto));
    }

    @PreAuthorize("hasAuthority('qc:singleDisease:edit')")
    @Operation(summary = "自动扫描纳入（按 ICD 前缀扫出院首页，返回扫描/纳入/跳过计数）")
    @PostMapping("/autoEnroll")
    public Result<SingleDiseaseAutoEnrollStatVO> autoEnroll(@Valid @RequestBody SingleDiseaseDTO.AutoEnroll dto) {
        return Result.success(singleDiseaseService.autoEnroll(dto));
    }

    // 质控 / 上报

    @PreAuthorize("hasAuthority('qc:singleDisease:edit')")
    @Operation(summary = "质控判级（首页完整性校验；已上报不可再改）")
    @PostMapping("/qc")
    public Result<SingleDiseaseVO.Case> qc(@Valid @RequestBody SingleDiseaseDTO.Qc dto) {
        return Result.success(singleDiseaseService.qc(dto));
    }

    @PreAuthorize("hasAuthority('qc:singleDisease:edit')")
    @Operation(summary = "上报打标（质控通过才可上报）")
    @PostMapping("/report/{id}")
    public Result<SingleDiseaseVO.Case> report(@PathVariable Long id) {
        return Result.success(singleDiseaseService.report(id));
    }

    // 查询

    @PreAuthorize("hasAuthority('qc:singleDisease:list')")
    @Operation(summary = "病例分页")
    @PostMapping("/caseListPage")
    public Result<PageResult<SingleDiseaseVO.Case>> caseListPage(@RequestBody SingleDiseaseDTO.CaseQuery query) {
        return Result.success(singleDiseaseService.casePage(query));
    }

    @PreAuthorize("hasAuthority('qc:singleDisease:list')")
    @Operation(summary = "病种指标（治愈率/死亡率/平均住院日/平均费用，服务端复算）")
    @GetMapping("/metrics")
    public Result<List<SingleDiseaseVO.Metric>> metrics() {
        return Result.success(singleDiseaseService.metrics());
    }
}
