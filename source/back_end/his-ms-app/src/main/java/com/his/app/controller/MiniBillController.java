package com.his.app.controller;

import com.his.app.service.MiniBillService;
import com.his.app.vo.MiniBillDetailVO;
import com.his.app.vo.MiniBillListVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 患者端-费用明细（按就诊人查账单与逐项明细）。
 */
@Tag(name = "患者端-费用明细")
@RestController
@RequestMapping("/miniapp/bill")
@RequiredArgsConstructor
public class MiniBillController {

    private final MiniBillService miniBillService;

    @Operation(summary = "我的账单列表")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/list")
    public Result<List<MiniBillListVO>> list(@RequestParam Long patientId,
                                                  @RequestParam(defaultValue = "50") int limit) {
        return Result.success(miniBillService.myBills(patientId, limit));
    }

    @Operation(summary = "账单详情（含逐项明细）")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/detail")
    public Result<MiniBillDetailVO> detail(@RequestParam Long patientId, @RequestParam Long billId) {
        return Result.success(miniBillService.billDetail(patientId, billId));
    }
}
