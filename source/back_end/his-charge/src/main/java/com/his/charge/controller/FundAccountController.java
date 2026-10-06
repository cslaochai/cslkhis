package com.his.charge.controller;


import com.his.charge.dto.FundAccountQueryPageDTO;
import com.his.charge.dto.FundTxnQueryPageDTO;
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
 * 资金账户台账（L3）：门诊余额与住院预交金的统一账本查询。
 *
 * <p>只读：账户与流水由充值/抵扣/退差各动作写入，这里不提供任何改账口子 ——
 * 余额永远等于 SUM(流水)，能直接改余额就等于账本作废。
 * 充值/退款动作在住院账户（{@code /charge/inpatient/account/prepay/save}）与
 * 收款链（余额抵扣）里，不在本控制器。
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
    public Result<PageResult<FundAccountListVO>> listPage(@Valid @RequestBody FundAccountQueryPageDTO query) {
        return Result.success(fundAccountService.accountListPage(query));
    }

    @Operation(summary = "分页查询账户流水（按账户或按患者跨账户）")
    @PreAuthorize("hasAuthority('finance:fundAccount:list')")
    @PostMapping("/txnListPage")
    public Result<PageResult<FundAccountTxnListVO>> txnListPage(@Valid @RequestBody FundTxnQueryPageDTO query) {
        return Result.success(fundAccountService.txnListPage(query));
    }

    @Operation(summary = "某账户的全部有效流水（账户详情弹框一次给全）")
    @PreAuthorize("hasAuthority('finance:fundAccount:list')")
    @GetMapping("/listByAccount")
    public Result<List<FundAccountTxnListVO>> listByAccount(@RequestParam Long accountId) {
        return Result.success(fundAccountService.listTxnByAccount(accountId));
    }
}
