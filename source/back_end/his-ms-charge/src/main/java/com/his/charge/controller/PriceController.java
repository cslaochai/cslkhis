package com.his.charge.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.charge.dto.PriceChangeDTO;
import com.his.charge.dto.PriceHistoryPageQueryDTO;
import com.his.charge.dto.PricePageQueryDTO;
import com.his.charge.service.PriceService;
import com.his.charge.vo.PriceChangeHistoryVO;
import com.his.charge.vo.PriceItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 价格管理控制器
 */
@Tag(name = "价格管理")
@RestController
@RequestMapping("/system/price")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:price:list')")
public class PriceController {

    private final PriceService priceService;

    /**
     * 按项目类型分页查询价表行：关键字命中项目编码或名称，可按启停状态过滤。
     * 用于价格管理页各类型页签下的价表列表与查询卡。
     */
    @Operation(summary = "分页查询价格（按项目类型）")
    @PostMapping("/listPage")
    public Result<PageResult<PriceItemVO>> listPage(@Valid @RequestBody PricePageQueryDTO queryDTO) {
        return Result.success(priceService.listPage(queryDTO));
    }

    /**
     * 调价：改写项目当前价，并留一条含前后价、调价原因与操作人的历史。
     * 用于价格管理页行内「调价」弹框保存。
     */
    @PreAuthorize("hasAuthority('finance:price:edit')")
    @Operation(summary = "调价（更新价格并记录调价历史）")
    @PostMapping("/changePrice")
    public Result<PriceItemVO> changePrice(@Valid @RequestBody PriceChangeDTO changeDTO) {
        return Result.success(priceService.changePrice(changeDTO));
    }

    /**
     * 调价历史分页：按项目类型与关键字收口，时间倒序。
     * 用于价格管理页「调价历史」页签的追溯查询。
     */
    @Operation(summary = "分页查询调价历史")
    @PostMapping("/historyListPage")
    public Result<PageResult<PriceChangeHistoryVO>> historyListPage(@Valid @RequestBody PriceHistoryPageQueryDTO queryDTO) {
        return Result.success(priceService.historyListPage(queryDTO));
    }

    /**
     * 五类价表（药品、耗材、检查、检验、治疗）行数的合计数。
     * 用于价格管理页进入时读取价表规模。
     */
    @Operation(summary = "价表总条数（5 类合计）")
    @GetMapping("/summary")
    public Result<Long> summary() {
        return Result.success(priceService.countAll());
    }
}
