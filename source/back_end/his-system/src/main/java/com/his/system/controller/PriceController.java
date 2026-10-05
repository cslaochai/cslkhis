package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.PriceChangeDTO;
import com.his.system.dto.PriceHistoryQueryPageDTO;
import com.his.system.dto.PriceQueryPageDTO;
import com.his.system.service.PriceService;
import com.his.system.vo.PriceChangeHistoryVO;
import com.his.system.vo.PriceItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 价格管理控制器
 *
 * <p>药品、耗材、检查、检验、治疗五类价表的统一价格查询与调价留痕。
 * 各类型的维护入口仍在各自模块（药品=DrugController、检查/检验=MedicalItemController），
 * 这里只做跨表统一视图 + 调价。</p>
 */
@Tag(name = "价格管理")
@RestController
@RequestMapping("/system/price")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:price:list')")
public class PriceController {

    private final PriceService priceService;

    @Operation(summary = "分页查询价格（按项目类型）")
    @PostMapping("/listPage")
    public Result<PageResult<PriceItemVO>> listPage(@Valid @RequestBody PriceQueryPageDTO queryDTO) {
        return Result.success(priceService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('finance:price:edit')")
    @Operation(summary = "调价（更新价格并记录调价历史）")
    @PostMapping("/changePrice")
    public Result<PriceItemVO> changePrice(@Valid @RequestBody PriceChangeDTO changeDTO) {
        return Result.success(priceService.changePrice(changeDTO));
    }

    @Operation(summary = "分页查询调价历史")
    @PostMapping("/historyListPage")
    public Result<PageResult<PriceChangeHistoryVO>> historyListPage(@Valid @RequestBody PriceHistoryQueryPageDTO queryDTO) {
        return Result.success(priceService.historyListPage(queryDTO));
    }

    @Operation(summary = "价表总条数（5 类合计）")
    @GetMapping("/summary")
    public Result<Long> summary() {
        return Result.success(priceService.countAll());
    }
}
