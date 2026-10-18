package com.his.charge.controller;


import com.his.charge.dto.FundAccountPageQueryDTO;
import com.his.charge.dto.FundTxnPageQueryDTO;
import com.his.charge.service.FundAccountService;
import com.his.charge.vo.FundAccountListVO;
import com.his.charge.vo.FundAccountTxnListVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资金账户台账：门诊余额与住院预交金的统一账本查询。
 */
@Tag(name = "资金账户")
@RestController
@RequestMapping("/charge/fundAccount")
@RequiredArgsConstructor
public class FundAccountController {

    private final FundAccountService fundAccountService;

    @Operation(summary = "分页查询资金账户（门诊余额 / 住院预交金）")
    @PreAuthorize("hasAuthority('finance:fundAccount:list')")
    @PostMapping("/listPage")
    public Result<PageResult<FundAccountListVO>> listPage(@Valid @RequestBody FundAccountPageQueryDTO query) {
        return Result.success(fundAccountService.accountListPage(query));
    }

    @Operation(summary = "分页查询账户流水（按账户或按患者跨账户）")
    @PreAuthorize("hasAuthority('finance:fundAccount:list')")
    @PostMapping("/txnListPage")
    public Result<PageResult<FundAccountTxnListVO>> txnListPage(@Valid @RequestBody FundTxnPageQueryDTO query) {
        return Result.success(fundAccountService.txnListPage(query));
    }

    @Operation(summary = "某账户的全部有效流水（账户详情弹框一次给全）")
    @PreAuthorize("hasAuthority('finance:fundAccount:list')")
    @GetMapping("/listByAccount")
    public Result<List<FundAccountTxnListVO>> listByAccount(@RequestParam Long accountId) {
        return Result.success(fundAccountService.listTxnByAccount(accountId));
    }
}
