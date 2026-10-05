package com.his.appoint.controller;

import com.his.appoint.dto.DayEndSettleDTO;
import com.his.appoint.service.DayEndSettleService;
import com.his.appoint.vo.DayEndSettleResultVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日终结转控制器（手工补跑）。
 *
 */
@Tag(name = "日终结转")
@RestController
@RequestMapping("/appoint/dayEndSettle")
@PreAuthorize("hasAuthority('opd:todayVisits:list')")
@RequiredArgsConstructor
public class DayEndSettleController {

    private final DayEndSettleService dayEndSettleService;

    @Operation(summary = "执行日终结转：未签到→爽约 / 已签到未就诊→未就诊 / 队列行→已失效")
    @PostMapping("/run")
    @PreAuthorize("hasAuthority('opd:todayVisits:list')")
    public Result<DayEndSettleResultVO> run(@Valid @RequestBody DayEndSettleDTO dto) {
        // 结论放在 message、明细放在 data（Result.success 的签名是 (message, data)）
        DayEndSettleResultVO vo = dayEndSettleService.settle(dto);
        return Result.success(vo.getMessage(), vo);
    }
}
