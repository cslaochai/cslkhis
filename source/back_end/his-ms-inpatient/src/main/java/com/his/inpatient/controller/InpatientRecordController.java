package com.his.inpatient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.inpatient.dto.*;
import com.his.inpatient.service.InpatientRecordService;
import com.his.inpatient.vo.InpatientRecordVO;
import com.his.inpatient.vo.InpatientRecordDetailVO;
import com.his.inpatient.vo.InpatientRecordLogVO;
import com.his.inpatient.vo.RecordQualityStatVO;
import com.his.common.vo.CodeOptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院病历文书（P2：结构化率 80% 的载体）。
 */
@Tag(name = "住院病历文书")
@RestController
@RequestMapping("/patient/inpatient/record")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ipd:record:list', 'ipd:order:list')")
public class InpatientRecordController {

    private final InpatientRecordService inpatientRecordService;

    /**
     * 病历文书分页：列表行不带长正文，只给标题、摘要与结构化率。
     * 用于住院病历文书页查询卡（按在院患者圈定）。
     */
    @Operation(summary = "病历文书分页（列表行不带长文本，只给摘要与结构化率）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientRecordVO>> listPage(@Valid InpatientRecordPageQueryDTO query) {
        return Result.success(inpatientRecordService.listPage(query));
    }

    /**
     * 病历文书详情：含逐要素填写明细与缺失项。
     * 用于住院病历文书页的结构化要素明细弹框，也是编辑弹框的数据源。
     */
    @Operation(summary = "病历文书详情（含逐要素明细与缺失项）")
    @GetMapping("/detail")
    public Result<InpatientRecordDetailVO> detail(@RequestParam Long id) {
        return Result.success(inpatientRecordService.detail(id));
    }

    /**
     * 新增或修改病历文书：按提交的要素整体覆盖，修改逐字段留痕，已归档的拒改。
     * 用于住院病历文书页的新建/编辑弹框保存。
     */
    @PreAuthorize("hasAuthority('ipd:record:add')")
    @Operation(summary = "新增/修改病历文书（传什么覆盖什么；修改逐字段留痕，已归档拒改）")
    @PostMapping("/save")
    public Result<InpatientRecordDetailVO> save(@Valid @RequestBody InpatientRecordUpsertDTO dto) {
        return Result.success("病历文书已保存", inpatientRecordService.save(dto));
    }

    /**
     * 提交病历文书：草稿转为已提交，提交后仍可改但每次改动留痕。
     * 用于住院病历文书页列表行的提交操作。
     */
    @PreAuthorize("hasAuthority('ipd:record:add')")
    @Operation(summary = "提交病历文书（草稿 → 已提交；提交后仍可修改，但每次修改留痕）")
    @PostMapping("/submit")
    public Result<Integer> submit(@RequestBody @Valid InpatientRecordSubmitDTO dto) {
        return Result.success("病历文书已提交", inpatientRecordService.submit(dto, dto.getPin()));
    }

    /**
     * 归档病历文书：已提交转为已归档，单向门，归档后禁改且说明必填。
     * 用于住院病历文书页列表行的归档操作。
     */
    @PreAuthorize("hasAuthority('ipd:record:edit')")
    @Operation(summary = "归档病历文书（已提交 → 已归档；单向门，归档后禁改，说明必填）")
    @PostMapping("/archive")
    public Result<Integer> archive(@RequestBody @Valid InpatientRecordArchiveDTO dto) {
        return Result.success("病历文书已归档", inpatientRecordService.archive(dto, dto.getPin()));
    }

    /**
     * 修改日志分页：可按单份文书查，也可按住院次查本次住院全部文书的改动轨迹。
     * 用于住院病历文书页的修改留痕面板。
     */
    @Operation(summary = "修改日志分页（可按单据查，也可按 admissionId 查本次住院全部文书的修改轨迹）")
    @GetMapping("/logs")
    public Result<IPage<InpatientRecordLogVO>> logs(@Valid InpatientRecordLogPageQueryDTO query) {
        return Result.success(inpatientRecordService.logPage(query));
    }

    /**
     * 单份文书的全部修改日志，按时间升序且不分页。
     * 供文书留痕追溯环节一次取全某一份病历或护理记录的改动历史。
     */
    @Operation(summary = "某份文书的全部修改日志（按时间升序，不分页；docType=1病历 2护理）")
    @GetMapping("/logList")
    public Result<List<InpatientRecordLogVO>> logList(@RequestParam Integer docType,
                                                      @RequestParam Long recordId) {
        return Result.success(inpatientRecordService.logList(docType, recordId));
    }

    /**
     * 病历结构化率统计：总体、分组、逐要素与逐份文书四个层次。
     * 用于住院病历文书页的统计卡片区，不达标时定位到具体文书。
     */
    @Operation(summary = "结构化率统计（总体 + 分组 + 逐要素 + 逐份文书；不达标能定位到具体记录）")
    @GetMapping("/qualityStat")
    public Result<RecordQualityStatVO> qualityStat(@RequestParam Long admissionId) {
        return Result.success(inpatientRecordService.qualityStat(admissionId));
    }

    /**
     * 病历文书类型下拉选项。
     * 用于住院病历文书页查询卡与新建弹框的类型选择，码值由后端给。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "病历文书类型下拉（码值由后端给，前端不写死）")
    @GetMapping("/type/selectList")
    public Result<List<CodeOptionVO>> typeOptions() {
        return Result.success(inpatientRecordService.typeOptions());
    }

    /**
     * 病历文书状态下拉选项。
     * 用于住院病历文书页查询卡的状态筛选。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "文书状态下拉")
    @GetMapping("/status/selectList")
    public Result<List<CodeOptionVO>> statusOptions() {
        return Result.success(inpatientRecordService.statusOptions());
    }
}
