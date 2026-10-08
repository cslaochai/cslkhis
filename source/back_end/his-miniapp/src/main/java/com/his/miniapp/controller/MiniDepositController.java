package com.his.miniapp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.vo.PrepayBalanceVO;
import com.his.charge.vo.PrepayVO;
import com.his.common.base.Result;
import com.his.miniapp.service.MiniDepositService;
import com.his.miniapp.vo.MiniAdmiSelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端住院押金（读预交金，充值走支付口子；形态同 M7 打印——不接银联/对公）。
 */
@Tag(name = "患者端-住院押金")
@RestController
@RequestMapping("/miniapp/deposit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniDepositController {

    private final MiniDepositService miniDepositService;

    @Operation(summary = "我的住院记录（押金页选择入院单用）")
    @GetMapping("/myAdmissions")
    public Result<List<MiniAdmiSelectListVO>> myAdmissions(@RequestParam Long patientId) {
        return Result.success(miniDepositService.myAdmissions(patientId));
    }

    @Operation(summary = "押金余额（充值合计/退款合计/余额）")
    @GetMapping("/balance")
    public Result<PrepayBalanceVO> balance(@RequestParam Long admissionId) {
        return Result.success(miniDepositService.balance(admissionId));
    }

    @Operation(summary = "押金流水分页")
    @PostMapping("/listPage")
    public Result<IPage<PrepayVO>> listPage(@Valid @RequestBody PrepayQueryPageDTO query) {
        return Result.success(miniDepositService.prepayListPage(query));
    }
}
