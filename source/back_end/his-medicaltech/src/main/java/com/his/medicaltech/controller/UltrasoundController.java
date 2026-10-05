package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.UltrasoundDTO;
import com.his.medicaltech.service.UltrasoundService;
import com.his.medicaltech.vo.UltrasoundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 超声亚专业接口（URL 前缀 /medicaltech/ultrasound）
 */
@Tag(name = "超声管理")
@RestController
@RequestMapping("/medicaltech/ultrasound")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:ultrasound:list')")
public class UltrasoundController {

    private final UltrasoundService ultrasoundService;

    @Operation(summary = "分页查询超声检查记录")
    @PostMapping("/listPage")
    public Result<PageResult<UltrasoundVO.ListVO>> listPage(@Valid @RequestBody UltrasoundDTO.Query query) {
        return Result.success(ultrasoundService.pageVO(query));
    }

    @Operation(summary = "超声统计")
    @GetMapping("/stats")
    public Result<UltrasoundVO.StatsVO> stats() {
        return Result.success(ultrasoundService.stats());
    }

    @Operation(summary = "超声检查详情（含结构化测量值）")
    @GetMapping("/getDetailById")
    public Result<UltrasoundVO.DetailVO> getDetailById(@RequestParam Long recordId) {
        return Result.success(ultrasoundService.getDetail(recordId));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:add')")
    @Operation(summary = "登记/修改超声检查")
    @PostMapping("/recordUpsert")
    public Result<UltrasoundVO.DetailVO> recordUpsert(@Valid @RequestBody UltrasoundDTO.RecordUpsert dto) {
        UltrasoundVO.DetailVO vo = ultrasoundService.upsertRecord(dto);
        return Result.success("超声检查 " + vo.getRecordNo() + " 已保存", vo);
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    @Operation(summary = "签到")
    @PostMapping("/checkIn")
    public Result<UltrasoundVO.DetailVO> checkIn(@Valid @RequestBody UltrasoundDTO.IdOnly dto) {
        ultrasoundService.checkIn(dto.getRecordId());
        return Result.success("已签到", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    @Operation(summary = "执行检查")
    @PostMapping("/execute")
    public Result<UltrasoundVO.DetailVO> execute(@Valid @RequestBody UltrasoundDTO.Execute dto) {
        ultrasoundService.execute(dto);
        return Result.success("检查已执行", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:add')")
    @Operation(summary = "保存结构化测量值（整单覆盖，异常标志服务端判定）")
    @PostMapping("/saveMeasures")
    public Result<UltrasoundVO.DetailVO> saveMeasures(@Valid @RequestBody UltrasoundDTO.MeasureSave dto) {
        int n = ultrasoundService.saveMeasures(dto);
        return Result.success("已保存 " + n + " 条测量值", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    @Operation(summary = "出具报告（超声所见 + 提示）")
    @PostMapping("/report")
    public Result<UltrasoundVO.DetailVO> report(@Valid @RequestBody UltrasoundDTO.Report dto) {
        ultrasoundService.report(dto);
        return Result.success("报告已出具", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    @Operation(summary = "审核（审核人不得是报告人本人）")
    @PostMapping("/audit")
    public Result<UltrasoundVO.DetailVO> audit(@Valid @RequestBody UltrasoundDTO.Audit dto) {
        ultrasoundService.audit(dto);
        return Result.success("审核通过", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    @Operation(summary = "发布报告")
    @PostMapping("/publish")
    public Result<UltrasoundVO.DetailVO> publish(@Valid @RequestBody UltrasoundDTO.Publish dto) {
        ultrasoundService.publish(dto.getRecordId());
        return Result.success("报告已发布", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:delete')")
    @Operation(summary = "取消检查")
    @PostMapping("/cancel")
    public Result<UltrasoundVO.DetailVO> cancel(@Valid @RequestBody UltrasoundDTO.Cancel dto) {
        ultrasoundService.cancel(dto);
        return Result.success("检查已取消", ultrasoundService.getDetail(dto.getRecordId()));
    }
}
