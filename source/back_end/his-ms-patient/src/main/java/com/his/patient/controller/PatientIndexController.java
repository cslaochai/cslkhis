package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.PatientIndexQueryDTO;
import com.his.patient.dto.PatientMergeDTO;
import com.his.patient.dto.PatientMergeRevertDTO;
import com.his.patient.service.PatientIndexService;
import com.his.patient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者主索引控制器：一人一档的唯一性判定、疑似重复检测与档案合并撤销的接口入口。
 */
@Tag(name = "患者主索引(EMPI)")
@RestController
@RequestMapping("/patient/patientIndex")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:empi:list')")
public class PatientIndexController {

    private final PatientIndexService patientIndexService;

    /**
     * 患者主索引分页，逐行带出档案完整度与业务数据量，默认不含已并入主档的影子档案。
     * 用于「患者主索引」页的档案列表标签卡。
     */
    @Operation(summary = "患者主索引分页（含档案完整度与业务数据量）")
    @PostMapping("/listPage")
    public Result<PageResult<PatientIndexVO>> listPage(@Valid @RequestBody PatientIndexQueryDTO queryDTO) {
        return Result.success(patientIndexService.selectIndexPage(queryDTO));
    }

    /**
     * 疑似重复档案检测：按匹配强度分级成组返回，只提示「值得看一眼」，不构成可自动合并的结论。
     * 用于「患者主索引」页的疑似重复检测标签卡，是人工发起合并的前置环节。
     */
    @Operation(summary = "疑似重复档案检测（分级；返回的是'值得看一眼'而非'应该合并'）")
    @PostMapping("/duplicateList")
    public Result<List<PatientDuplicateGroupVO>> duplicateList(@Valid @RequestBody PatientIndexQueryDTO queryDTO) {
        return Result.success(patientIndexService.detectDuplicates(queryDTO));
    }

    /**
     * 单份档案的主索引详情，含其所属主档下的其他档案与各自的业务数据量。
     * 用于档案列表与疑似重复检测两处打开的档案详情抽屉。
     */
    @Operation(summary = "患者主索引详情（含同主档下的其他档案）")
    @GetMapping("/getDetailById")
    public Result<PatientIndexVO> getDetailById(@RequestParam Long patientId) {
        return Result.success(patientIndexService.getIndexDetail(patientId));
    }

    /**
     * 合并档案：只把被并档案指向主档、不搬业务数据，匹配置信级别由服务端按当前档案重算，理由长度随级别加压。
     * 用于疑似重复检测结果中人工指定保留方后的合并提交，返回本次合并审计。
     */
    @PreAuthorize("hasAuthority('patient:empi:edit')")
    @Operation(summary = "合并档案（服端重新判定匹配级别；返回合并审计）")
    @PostMapping("/merge")
    public Result<PatientMergeLogVO> merge(@RequestBody @Valid PatientMergeDTO dto) {
        return Result.success(patientIndexService.merge(dto));
    }

    /**
     * 撤销一次合并：按合并审计快照还原指向，被并档案重新在册。
     * 用于「患者主索引」页合并历史标签卡对已生效合并的撤销，撤销理由必填。
     */
    @PreAuthorize("hasAuthority('patient:empi:delete')")
    @Operation(summary = "撤销合并（按审计快照还原）")
    @PostMapping("/revert")
    public Result<PatientMergeLogVO> revert(@RequestBody @Valid PatientMergeRevertDTO dto) {
        return Result.success(patientIndexService.revert(dto));
    }

    /**
     * 合并历史分页：合并单号、主档与被并档案、匹配级别、理由、操作人及当前状态。
     * 用于「患者主索引」页的合并历史标签卡，也是发起撤销的取数来源。
     */
    @PreAuthorize("hasAuthority('patient:empi:edit')")
    @Operation(summary = "合并历史分页")
    @PostMapping("/mergeLogListPage")
    public Result<PageResult<PatientMergeLogVO>> mergeLogListPage(@Valid @RequestBody PatientIndexQueryDTO queryDTO) {
        return Result.success(patientIndexService.selectMergeLogPage(queryDTO));
    }

    /**
     * 主索引概览指标：档案唯一性与完整性两组数。
     * 用于「患者主索引」页顶部统计卡片，合并与撤销完成后随列表一并刷新。
     */
    @Operation(summary = "EMPI 概览指标（唯一性/完整性）")
    @GetMapping("/stats")
    public Result<PatientIndexStatVO> stats() {
        return Result.success(patientIndexService.stats());
    }

    /**
     * 主索引字典：匹配级别清单与参与匹配的档案关键字段清单，与后端判定规则同源。
     * 用于「患者主索引」页挂载时取筛选下拉与列头文案，前端不自行硬编码。
     */
    @Operation(summary = "字典：匹配级别 / 档案关键字段清单")
    @GetMapping("/dict")
    public Result<PatientIndexDictVO> dict() {
        return Result.success(patientIndexService.dict());
    }
}
