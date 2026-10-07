package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.*;
import com.his.patient.service.InpatientRecordService;
import com.his.patient.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院病历文书（P2：结构化率 80% 的载体）。
 *
 * <p>约定：查询一律 GET（驼峰 URL + 查询参数），写操作一律 POST，分页 {@code listPage}。
 *
 * <p>两条要记住的口径：
 * <ol>
 *   <li>{@code /save} 是<u>传什么覆盖什么</u>（不传 = 置空），配合逐字段 diff 留痕；
 *       这跟"只更新非空字段"是相反的语义，前端必须把编辑态完整回传。</li>
 *   <li>{@code /qualityStat} 的分母<b>按文书类型分别算</b>（病程才有病程正文），
 *       所以它的 {@code elementTotal} ≠ 份数 × 26 —— 这不是 bug。</li>
 * </ol>
 */
@Tag(name = "住院病历文书")
@RestController
@RequestMapping("/patient/inpatient/record")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ipd:record:list', 'ipd:order:list')")
public class InpatientRecordController {

    private final InpatientRecordService inpatientRecordService;

    @Operation(summary = "病历文书分页（列表行不带长文本，只给摘要与结构化率）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientRecordVO>> listPage(@Valid InpatientRecordQueryPageDTO query) {
        return Result.success(inpatientRecordService.listPage(query));
    }

    @Operation(summary = "病历文书详情（含逐要素明细与缺失项）")
    @GetMapping("/detail")
    public Result<InpatientRecordDetailVO> detail(@RequestParam Long id) {
        return Result.success(inpatientRecordService.detail(id));
    }

    @PreAuthorize("hasAuthority('ipd:record:add')")
    @Operation(summary = "新增/修改病历文书（传什么覆盖什么；修改逐字段留痕，已归档拒改）")
    @PostMapping("/save")
    public Result<InpatientRecordDetailVO> save(@Valid @RequestBody InpatientRecordUpsertDTO dto) {
        return Result.success("病历文书已保存", inpatientRecordService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:record:add')")
    @Operation(summary = "提交病历文书（草稿 → 已提交；提交后仍可修改，但每次修改留痕）")
    @PostMapping("/submit")
    public Result<Integer> submit(@RequestBody @Valid InpatientRecordSubmitDTO dto) {
        return Result.success("病历文书已提交", inpatientRecordService.submit(dto));
    }

    @PreAuthorize("hasAuthority('ipd:record:edit')")
    @Operation(summary = "归档病历文书（已提交 → 已归档；单向门，归档后禁改，说明必填）")
    @PostMapping("/archive")
    public Result<Integer> archive(@RequestBody @Valid InpatientRecordArchiveDTO dto) {
        return Result.success("病历文书已归档", inpatientRecordService.archive(dto));
    }

    @Operation(summary = "修改日志分页（可按单据查，也可按 admissionId 查本次住院全部文书的修改轨迹）")
    @GetMapping("/logs")
    public Result<IPage<InpatientRecordLogVO>> logs(@Valid InpatientRecordLogQueryPageDTO query) {
        return Result.success(inpatientRecordService.logPage(query));
    }

    @Operation(summary = "某份文书的全部修改日志（按时间升序，不分页；docType=1病历 2护理）")
    @GetMapping("/logList")
    public Result<List<InpatientRecordLogVO>> logList(@RequestParam Integer docType,
                                                      @RequestParam Long recordId) {
        return Result.success(inpatientRecordService.logList(docType, recordId));
    }

    @Operation(summary = "结构化率统计（总体 + 分组 + 逐要素 + 逐份文书；不达标能定位到具体记录）")
    @GetMapping("/qualityStat")
    public Result<RecordQualityStatVO> qualityStat(@RequestParam Long admissionId) {
        return Result.success(inpatientRecordService.qualityStat(admissionId));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "病历文书类型下拉（码值由后端给，前端不写死）")
    @GetMapping("/type/selectList")
    public Result<List<CodeOptionVO>> typeOptions() {
        return Result.success(inpatientRecordService.typeOptions());
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "文书状态下拉")
    @GetMapping("/status/selectList")
    public Result<List<CodeOptionVO>> statusOptions() {
        return Result.success(inpatientRecordService.statusOptions());
    }
}
