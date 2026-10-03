package com.his.fee.controller;

import com.his.fee.dto.FeeBookDTO;
import com.his.fee.dto.FeeRecordQueryPageDTO;
import com.his.fee.dto.FeeReverseDTO;
import com.his.fee.service.FeeRecordService;
import com.his.fee.vo.BizFeeRecordDetailVO;
import com.his.fee.vo.BizFeeRecordVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 费用记账台账（L1）。
 *
 * <p>只提供「查 + 补记账 + 红冲」：金额列没有任何编辑口子，改错必须走红冲留痕。
 * 临床单据（处方、医嘱、发药…）的计费由各模块的计费器直接调 {@code FeeRecordService.book}，
 * 不走这个 HTTP 入口。
 */
@Tag(name = "费用记账")
@RestController
@RequestMapping("/charge/feeRecord")
@RequiredArgsConstructor
public class FeeRecordController {

    private final FeeRecordService feeRecordService;

    @Operation(summary = "分页查询记账行")
    @PreAuthorize("hasAuthority('finance:feeRecord:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizFeeRecordVO>> listPage(@Valid @RequestBody FeeRecordQueryPageDTO query) {
        return Result.success(feeRecordService.selectPage(query));
    }

    @Operation(summary = "记账行详情（含红冲链）")
    @PreAuthorize("hasAuthority('finance:feeRecord:list')")
    @GetMapping("/getDetailById")
    public Result<BizFeeRecordDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(feeRecordService.getDetailById(id));
    }

    @Operation(summary = "手工补记账（错漏费用的唯一录入口，幂等）")
    @PreAuthorize("hasAuthority('finance:feeRecord:add')")
    @PostMapping("/book")
    public Result<BizFeeRecordVO> book(@Valid @RequestBody FeeBookDTO dto) {
        return Result.success(feeRecordService.bookVO(dto));
    }

    @Operation(summary = "红冲：数量为空=整行冲，给了数量=部分冲减")
    @PreAuthorize("hasAuthority('finance:feeRecord:edit')")
    @PostMapping("/reverse")
    public Result<BizFeeRecordVO> reverse(@Valid @RequestBody FeeReverseDTO dto) {
        return Result.success(feeRecordService.reverseVO(dto));
    }
}
