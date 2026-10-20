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
 * 处方点评控制器：点评批次建批与归档、明细结论提交、超常处方公示、医师约谈的接口入口。
 */
@Tag(name = "处方点评")
@RestController
@RequestMapping("/rxReview")
@RequiredArgsConstructor
public class RxReviewController {

    private final RxReviewService rxReviewService;

    /**
     * 点评批次分页：按状态、批次号筛出建批记录，带抽样数与已点评数。
     * 用于「处方点评」页「点评批次」页签的批次台账表格。
     */
    @Operation(summary = "点评批次分页")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @PostMapping("/batchListPage")
    public Result<PageResult<RxReviewBatchVO>> batchListPage(@Valid @RequestBody RxReviewBatchPageQueryDTO query) {
        return Result.success(rxReviewService.batchListPage(query));
    }

    /**
     * 新建点评批次并按处方日期区间抽样生成待点评明细；带批次标识时只改批次名称与专项主题。
     * 用于「处方点评」页「点评批次」页签的新增/修改弹框保存。
     */
    @Operation(summary = "建批（抽样生成明细）/ 改批次名称与专项主题")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/batchUpsert")
    public Result<RxReviewBatchVO> batchUpsert(@Valid @RequestBody RxReviewBatchUpsertDTO dto) {
        return Result.success(rxReviewService.batchUpsert(dto));
    }

    /**
     * 把进行中的点评批次归档收尾，归档后不再接收新的点评明细。
     * 用于「点评批次」页签操作列的完成批次动作。
     */
    @Operation(summary = "完成批次（进行中 → 已完成归档）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/completeBatch")
    public Result<Void> completeBatch(@RequestParam Long id) {
        rxReviewService.completeBatch(id);
        return Result.success(null);
    }

    /**
     * 删除点评批次并连带清掉名下明细，已产生点评结论的批次拒绝删除。
     * 用于「点评批次」页签操作列的删除。
     */
    @Operation(summary = "删除批次（已有已点评明细则拒绝；整批物理删）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:delete')")
    @DeleteMapping("/batchDeleteById")
    public Result<Void> batchDeleteById(@RequestParam Long id) {
        rxReviewService.batchDeleteById(id);
        return Result.success(null);
    }

    /**
     * 点评明细分页：按批次、点评结论、公示状态筛出处方清单，并附处方药品明细文本。
     * 用于「处方点评」页「点评明细」页签的台账表格；约谈登记弹框也按医师名从这里取名下已点评处方。
     */
    @Operation(summary = "点评明细分页（附处方药品明细文本）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @PostMapping("/itemListPage")
    public Result<PageResult<RxReviewItemVO>> itemListPage(@Valid @RequestBody RxReviewItemPageQueryDTO query) {
        return Result.success(rxReviewService.itemListPage(query));
    }

    /**
     * 提交单张处方的点评结论与点评人，判为不合理时必须带问题码与意见，已公示的禁改。
     * 用于「点评明细」页签的点评填写弹框保存。
     */
    @Operation(summary = "提交点评结论（不合理必填问题码与意见；已公示禁改）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/itemUpsert")
    public Result<Void> itemUpsert(@Valid @RequestBody RxReviewItemUpsertDTO dto) {
        rxReviewService.itemUpsert(dto);
        return Result.success(null);
    }

    /**
     * 按处方号把抽样漏掉的处方补录进指定批次，成为待点评明细。
     * 用于「点评明细」页签的按处方号补录弹框。
     */
    @Operation(summary = "按处方号补录进批次")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:add')")
    @PostMapping("/itemAddByNo")
    public Result<Void> itemAddByNo(@Valid @RequestBody RxReviewItemAddByNoDTO dto) {
        rxReviewService.itemAddByNo(dto.getBatchId(), dto.getPrescriptionNo());
        return Result.success(null);
    }

    /**
     * 把选中的不合理处方登记为超常公示并返回实际公示条数，公示只增不可撤。
     * 用于「点评明细」页签的单条公示与批量公示，公示结果是医师约谈的发起依据。
     */
    @Operation(summary = "公示不合理处方（只增不可撤）；返回公示条数")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:publicity')")
    @PostMapping("/publicity")
    public Result<Long> publicity(@Valid @RequestBody RxReviewPublicityDTO dto) {
        return Result.success((long) rxReviewService.publicity(dto));
    }

    /**
     * 已公示处分明细分页：只出登记进公示的清单，按医师、科室、日期筛。
     * 用于「处方点评」页「超常处方公示」页签的公示台账表格。
     */
    @Operation(summary = "已公示明细分页（公示页）")
    @PreAuthorize("hasAuthority('pharmacy:rxPublicity:list')")
    @PostMapping("/publicityListPage")
    public Result<PageResult<RxReviewItemVO>> publicityListPage(@Valid @RequestBody RxReviewItemPageQueryDTO query) {
        return Result.success(rxReviewService.publicityListPage(query));
    }

    /**
     * 公示区间内的医师超常处方排名，超常次数达阈值的标记为需要约谈。
     * 用于「超常处方公示」页签的医师排名区，为发起约谈提供名单。
     */
    @Operation(summary = "公示页医师排名（超常≥3次标 needTalk）")
    @PreAuthorize("hasAuthority('pharmacy:rxPublicity:list')")
    @GetMapping("/publicityStats")
    public Result<List<RxPublicityDoctorVO>> publicityStats(
            @RequestParam(required = false) LocalDate dateStart,
            @RequestParam(required = false) LocalDate dateEnd) {
        return Result.success(rxReviewService.publicityStats(dateStart, dateEnd));
    }

    /**
     * 月度点评口径统计：期内处方总数、点评率、不合理率、超常与已公示量一次算齐。
     * 用于「处方点评」页顶部「处方点评统计」卡，随月份切换刷新。
     */
    @Operation(summary = "月度点评统计（点评率/不合理率/超常数）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @GetMapping("/stats")
    public Result<RxReviewStatsVO> stats(@RequestParam(required = false) String month) {
        return Result.success(rxReviewService.stats(month));
    }

    /**
     * 医师约谈记录分页：按约谈类型、整改状态与被约谈医师筛出台账。
     * 用于「处方点评」页「医师约谈」页签的约谈记录表格。
     */
    @Operation(summary = "约谈记录分页")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:list')")
    @PostMapping("/talkListPage")
    public Result<PageResult<RxReviewTalkVO>> talkListPage(@Valid @RequestBody RxReviewTalkPageQueryDTO query) {
        return Result.success(rxReviewService.talkListPage(query));
    }

    /**
     * 登记或修改一条约谈记录并挂上关联的不合理处方，医师已确认的禁改。
     * 用于「医师约谈」页签的约谈登记弹框保存，被约谈人取自超常处方公示排名。
     */
    @Operation(summary = "约谈新增/修改（医师已确认禁改）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:talk')")
    @PostMapping("/talkUpsert")
    public Result<RxReviewTalkVO> talkUpsert(@Valid @RequestBody RxReviewTalkUpsertDTO dto) {
        return Result.success(rxReviewService.talkUpsert(dto));
    }

    /**
     * 补记医师确认人，确认后该条约谈进入禁改禁删状态。
     * 用于「医师约谈」页签操作列的医师确认签字。
     */
    @Operation(summary = "医师确认签字（确认后禁改禁删）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:talk')")
    @PostMapping("/talkConfirm")
    public Result<Void> talkConfirm(@RequestParam Long id, @RequestParam String confirmBy) {
        rxReviewService.talkConfirm(id, confirmBy);
        return Result.success(null);
    }

    /**
     * 删除约谈记录，医师已确认的拒绝删除。
     * 用于「医师约谈」页签操作列的删除。
     */
    @Operation(summary = "删除约谈（医师已确认的拒绝；物理删）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:delete')")
    @DeleteMapping("/talkDeleteById")
    public Result<Void> talkDeleteById(@RequestParam Long id) {
        rxReviewService.talkDeleteById(id);
        return Result.success(null);
    }

    /**
     * 按当前查询条件导出点评明细台账（CSV 带 BOM，单次上限 5000 行）。
     * 用于「点评明细」页签的导出台账按钮，供药学部门留档与上报。
     */
    @Operation(summary = "导出点评明细台账 CSV（BOM，上限 5000 行）")
    @PreAuthorize("hasAuthority('pharmacy:rxReview:export')")
    @PostMapping("/itemExportCsv")
    public Result<String> itemExportCsv(@Valid @RequestBody RxReviewItemPageQueryDTO query) {
        return Result.success(rxReviewService.itemExportCsv(query));
    }
}
