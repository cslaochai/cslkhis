package com.his.charge.controller;

import com.his.spi.dto.FeeBookDTO;
import com.his.charge.dto.FeeRecordPageQueryDTO;
import com.his.charge.dto.FeeReverseDTO;
import com.his.charge.service.FeeRecordService;
import com.his.charge.vo.BizFeeRecordDetailVO;
import com.his.charge.vo.BizFeeRecordVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 费用记账台账。
 */
@Tag(name = "费用记账")
@RestController
@RequestMapping("/charge/feeRecord")
@RequiredArgsConstructor
public class FeeRecordController {

    private final FeeRecordService feeRecordService;

    /**
     * 分页查询 L1 记账行（谁该付多少、来自哪张临床单据）。
     * 用于费用记账台账页按患者、单据、状态检索已记账费用。
     */
    @Operation(summary = "分页查询记账行")
    @PreAuthorize("hasAuthority('finance:feeRecord:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizFeeRecordVO>> listPage(@Valid @RequestBody FeeRecordPageQueryDTO query) {
        return Result.success(feeRecordService.selectPage(query));
    }

    /**
     * 取单条记账行详情，含其名下红冲负行串成的红冲链。
     * 用于台账页点开某行核对原始记账与历次冲正净额。
     */
    @Operation(summary = "记账行详情（含红冲链）")
    @PreAuthorize("hasAuthority('finance:feeRecord:list')")
    @GetMapping("/getDetailById")
    public Result<BizFeeRecordDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(feeRecordService.getDetailById(id));
    }

    /**
     * 手工补记一笔 L1 费用，是临床漏计/错计费用唯一的补录入口，按幂等键防重。
     * 用于费用记账台账页补录自动化链路未记到的费用。
     */
    @Operation(summary = "手工补记账（错漏费用的唯一录入口，幂等）")
    @PreAuthorize("hasAuthority('finance:feeRecord:add')")
    @PostMapping("/book")
    public Result<BizFeeRecordVO> book(@Valid @RequestBody FeeBookDTO dto) {
        return Result.success(feeRecordService.bookVO(dto));
    }

    /**
     * 红冲一笔记账行：记账行不可改，错账只能以负数行冲正。数量为空整行冲，给了数量部分冲减。
     * 用于费用记账台账页纠正错账，原行保留、冲正行回指原行。
     */
    @Operation(summary = "红冲：数量为空=整行冲，给了数量=部分冲减")
    @PreAuthorize("hasAuthority('finance:feeRecord:edit')")
    @PostMapping("/reverse")
    public Result<BizFeeRecordVO> reverse(@Valid @RequestBody FeeReverseDTO dto) {
        return Result.success(feeRecordService.reverseVO(dto));
    }
}
