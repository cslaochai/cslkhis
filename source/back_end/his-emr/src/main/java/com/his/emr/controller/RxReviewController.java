package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.RxReviewService;
import com.his.emr.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 处方点评（事后专项点评 + 超常处方公示 + 医师约谈）。
 */
@Tag(name = "处方点评")
@RestController
@RequestMapping("/rxReview")
@RequiredArgsConstructor
public class RxReviewController {

    private final RxReviewService rxReviewService;

    @Operation(summary = "点评批次分页")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @PostMapping("/batchListPage")
    public Result<PageResult<RxReviewBatchVO>> batchListPage(@Valid @RequestBody RxReviewBatchQueryPageDTO query) {
        return Result.success(rxReviewService.batchListPage(query));
    }

    @Operation(summary = "建批（抽样生成明细）/ 改批次名称与专项主题")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/batchUpsert")
    public Result<RxReviewBatchVO> batchUpsert(@Valid @RequestBody RxReviewBatchUpsertDTO dto) {
        return Result.success(rxReviewService.batchUpsert(dto));
    }

    @Operation(summary = "完成批次（进行中 → 已完成归档）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/completeBatch")
    public Result<Void> completeBatch(@RequestParam Long id) {
        rxReviewService.completeBatch(id);
        return Result.success(null);
    }

    @Operation(summary = "删除批次（已有已点评明细则拒绝；整批物理删）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:delete')")
    @DeleteMapping("/batchDeleteById")
    public Result<Void> batchDeleteById(@RequestParam Long id) {
        rxReviewService.batchDeleteById(id);
        return Result.success(null);
    }

    @Operation(summary = "点评明细分页（附处方药品明细文本）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @PostMapping("/itemListPage")
    public Result<PageResult<RxReviewItemVO>> itemListPage(@Valid @RequestBody RxReviewItemPageDTO query) {
        return Result.success(rxReviewService.itemListPage(query));
    }

    @Operation(summary = "提交点评结论（不合理必填问题码与意见；已公示禁改）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/itemUpsert")
    public Result<Void> itemUpsert(@Valid @RequestBody RxReviewItemUpsertDTO dto) {
        rxReviewService.itemUpsert(dto);
        return Result.success(null);
    }

    @Operation(summary = "按处方号补录进批次")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/itemAddByNo")
    public Result<Void> itemAddByNo(@Valid @RequestBody RxReviewItemAddByNoDTO dto) {
        rxReviewService.itemAddByNo(dto.getBatchId(), dto.getPrescriptionNo());
        return Result.success(null);
    }

    @Operation(summary = "公示不合理处方（只增不可撤）；返回公示条数")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:publicity')")
    @PostMapping("/publicity")
    public Result<Long> publicity(@Valid @RequestBody RxReviewPublicityDTO dto) {
        return Result.success((long) rxReviewService.publicity(dto));
    }

    @Operation(summary = "已公示明细分页（公示页）")
    @PreAuthorize("hasAuthority('pharmacy:rxPublicity:list')")
    @PostMapping("/publicityListPage")
    public Result<PageResult<RxReviewItemVO>> publicityListPage(@Valid @RequestBody RxReviewItemPageDTO query) {
        return Result.success(rxReviewService.publicityListPage(query));
    }

    @Operation(summary = "公示页医师排名（超常≥3次标 needTalk）")
    @PreAuthorize("hasAuthority('pharmacy:rxPublicity:list')")
    @GetMapping("/publicityStats")
    public Result<List<RxPublicityDoctorVO>> publicityStats(
            @RequestParam(required = false) LocalDate dateStart,
            @RequestParam(required = false) LocalDate dateEnd) {
        return Result.success(rxReviewService.publicityStats(dateStart, dateEnd));
    }

    @Operation(summary = "月度点评统计（点评率/不合理率/超常数）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @GetMapping("/stats")
    public Result<RxReviewStatsVO> stats(@RequestParam(required = false) String month) {
        return Result.success(rxReviewService.stats(month));
    }

    @Operation(summary = "约谈记录分页")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @PostMapping("/talkListPage")
    public Result<PageResult<RxReviewTalkVO>> talkListPage(@Valid @RequestBody RxReviewTalkQueryPageDTO query) {
        return Result.success(rxReviewService.talkListPage(query));
    }

    @Operation(summary = "约谈新增/修改（医师已确认禁改）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:talk')")
    @PostMapping("/talkUpsert")
    public Result<RxReviewTalkVO> talkUpsert(@Valid @RequestBody RxReviewTalkUpsertDTO dto) {
        return Result.success(rxReviewService.talkUpsert(dto));
    }

    @Operation(summary = "医师确认签字（确认后禁改禁删）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:talk')")
    @PostMapping("/talkConfirm")
    public Result<Void> talkConfirm(@RequestParam Long id, @RequestParam String confirmBy) {
        rxReviewService.talkConfirm(id, confirmBy);
        return Result.success(null);
    }

    @Operation(summary = "删除约谈（医师已确认的拒绝；物理删）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:delete')")
    @DeleteMapping("/talkDeleteById")
    public Result<Void> talkDeleteById(@RequestParam Long id) {
        rxReviewService.talkDeleteById(id);
        return Result.success(null);
    }

    @Operation(summary = "导出点评明细台账 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:export')")
    @PostMapping("/itemExportCsv")
    public Result<String> itemExportCsv(@Valid @RequestBody RxReviewItemPageDTO query) {
        return Result.success(rxReviewService.itemExportCsv(query));
    }
}
