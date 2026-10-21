package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.SingleDiseaseDTO;
import com.his.clinical.service.SingleDiseaseService;
import com.his.clinical.vo.SingleDiseaseAutoEnrollStatVO;
import com.his.clinical.vo.SingleDiseaseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单病种质控控制器：病种目录维护、病例纳入、质控判级与上报打标的接口入口。
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

    /**
     * 维护单病种纳入目录：新增与修改共用一个入口，病种编码全表唯一，ICD-10 前缀决定自动纳入的匹配范围。
     * 用于「单病种质控」页「病种目录」页签的新增/修改弹框保存。
     */
    @PreAuthorize("hasAuthority('qc:singleDisease:add')")
    @Operation(summary = "病种目录新增/修改（编码唯一）")
    @PostMapping("/diseaseUpsert")
    public Result<SingleDiseaseVO.Disease> diseaseUpsert(@Valid @RequestBody SingleDiseaseDTO.DiseaseUpsert dto) {
        return Result.success(singleDiseaseService.diseaseUpsert(dto));
    }

    /**
     * 从目录中移除病种，已纳入病例的病种拒绝删除。
     * 用于「病种目录」页签操作列的删除。
     */
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

    /**
     * 自动扫描纳入：按病种配置的 ICD-10 前缀扫出院首页批量入册，返回扫描、纳入与跳过计数；出院日期区间可空表示扫全部已出院首页。
     * 用于「病种目录」页签的自动扫描，可对单个病种反复执行以补漏。
     */
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

    /**
     * 给病例打上完成上报标记，质控通过的才允许上报，上报后判级不可再改。
     * 用于「病例列表」页签操作列的上报确认，满足上级单病种数据报送要求。
     */
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
    public Result<PageResult<SingleDiseaseVO.Case>> caseListPage(@Valid @RequestBody SingleDiseaseDTO.CaseQuery query) {
        return Result.success(singleDiseaseService.casePage(query));
    }

    /**
     * 病种级质控指标：治愈率、死亡率、平均住院日、平均费用由服务端从已纳入病例复算。
     * 用于「单病种质控」页「病种指标」页签，页面打开与病例增改后一同刷新。
     */
    @PreAuthorize("hasAuthority('qc:singleDisease:list')")
    @Operation(summary = "病种指标（治愈率/死亡率/平均住院日/平均费用，服务端复算）")
    @GetMapping("/metrics")
    public Result<List<SingleDiseaseVO.Metric>> metrics() {
        return Result.success(singleDiseaseService.metrics());
    }
}
