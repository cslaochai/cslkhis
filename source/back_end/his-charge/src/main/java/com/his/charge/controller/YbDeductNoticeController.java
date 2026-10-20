package com.his.charge.controller;


import com.his.charge.dto.*;
import com.his.charge.service.YbDeductNoticeService;
import com.his.charge.vo.DeductNoticeDetailVO;
import com.his.charge.vo.DeductNoticeListVO;
import com.his.charge.vo.DeductSummaryVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医保扣款通知单（申诉 → 确认追责 → 缴回闭环，菜单 1010，sql/163）。
 */
@Tag(name = "医保扣款与飞检-扣款通知")
@RestController
@RequestMapping("/charge/ybDeduct")
@RequiredArgsConstructor
public class YbDeductNoticeController {

    private final YbDeductNoticeService ybDeductNoticeService;

    /**
     * 分页查询医保扣款通知单，附带是否超期的展示态。
     * 用于医保扣款与飞检页按状态、时间检索待处理的扣款单。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "扣款通知分页（附超期展示态）")
    @GetMapping("/listPage")
    public Result<PageResult<DeductNoticeListVO>> listPage(@Valid DeductNoticePageQueryDTO queryDTO) {
        return Result.success(ybDeductNoticeService.listPage(queryDTO));
    }

    /**
     * 取单张扣款通知详情，含申诉、确认、缴回的全过程留痕。
     * 用于扣款单详情弹框与处理弹框回显当前进度。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "扣款通知详情（含全过程留痕）")
    @GetMapping("/getDetailById")
    public Result<DeductNoticeDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(ybDeductNoticeService.getDetailById(id));
    }

    /**
     * 汇总扣款台账：待确认/申诉中/待缴/已缴回/超期单数与金额合计。
     * 用于医保扣款与飞检页顶部统计卡。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:list')")
    @Operation(summary = "台账汇总（待确认/申诉中/待缴/已缴回/超期/金额）")
    @GetMapping("/summary")
    public Result<DeductSummaryVO> summary() {
        return Result.success(ybDeductNoticeService.summary());
    }

    /**
     * 新增或修改扣款通知单，仅待确认状态可改。
     * 用于医保收到扣款通知后登记入册或纠正待确认单据。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:add')")
    @Operation(summary = "通知单新增/修改（仅待确认可改）")
    @PostMapping("/upsert")
    public Result<DeductNoticeListVO> upsert(@Valid @RequestBody DeductNoticeUpsertDTO dto) {
        return Result.success(ybDeductNoticeService.upsert(dto));
    }

    /**
     * 对扣款通知发起申诉，单据由待确认流转为申诉中。
     * 用于院方认为扣款不当时启动申诉环节。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "发起申诉（待确认 → 申诉中）")
    @PostMapping("/appeal")
    public Result<Void> appeal(@Valid @RequestBody DeductAppealDTO dto) {
        ybDeductNoticeService.appeal(dto);
        return Result.success(null);
    }

    /**
     * 录入医保方申诉结果，申诉成功则撤销、否则转入待缴。
     * 用于申诉环节返回结论后推进单据状态。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "录入申诉结果（申诉中 → 申诉成功 / 待缴）")
    @PostMapping("/appealResult")
    public Result<Void> appealResult(@Valid @RequestBody DeductAppealResultDTO dto) {
        ybDeductNoticeService.appealResult(dto);
        return Result.success(null);
    }

    /**
     * 确认扣款并登记追责对象，单据流转为维持扣款待缴。
     * 用于放弃申诉或申诉失败后确认承担扣款的环节。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "确认扣款并追责（→ 维持扣款待缴）")
    @PostMapping("/confirm")
    public Result<Void> confirm(@Valid @RequestBody DeductConfirmDTO dto) {
        ybDeductNoticeService.confirm(dto);
        return Result.success(null);
    }

    /**
     * 录入扣款缴回，单据由待缴流转为已缴回，缴回金额须等于扣款金额。
     * 用于扣款闭环的最后一步确认款项已上缴。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "录入缴回（待缴 → 已缴回，金额须等于扣款金额）")
    @PostMapping("/payback")
    public Result<Void> payback(@Valid @RequestBody DeductPaybackDTO dto) {
        ybDeductNoticeService.payback(dto);
        return Result.success(null);
    }

    /**
     * 作废扣款通知单，仅待确认状态可作废。
     * 用于通知登记错误或重复时撤销该单。
     */
    @PreAuthorize("hasAuthority('finance:insuranceDeduct:edit')")
    @Operation(summary = "作废（仅待确认可作废）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody YbCancelDTO dto) {
        ybDeductNoticeService.cancel(dto);
        return Result.success(null);
    }
}
