package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.*;
import com.his.miniapp.service.MiniServiceMessageService;
import com.his.miniapp.service.MiniServiceTraceService;
import com.his.miniapp.vo.MiniServiceMessageListVO;
import com.his.miniapp.vo.MiniServiceDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端客服台
 */
@Tag(name = "患者端-客服台")
@RestController
@RequestMapping("/miniapp/service")
@RequiredArgsConstructor
public class MiniServiceController {

    private final MiniServiceMessageService miniServiceMessageService;
    private final MiniServiceTraceService miniServiceTraceService;

    @Operation(summary = "提交留言（归属由登录态决定）")
    @PostMapping("/messageUpsert")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<String> messageUpsert(@RequestBody @Valid ServiceMessageUpsertDTO dto) {
        return Result.success(miniServiceMessageService.submit(dto));
    }

    @Operation(summary = "我的工单（分页，含处理状态与受理人）")
    @PostMapping("/myMessages")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PageResult<MiniServiceMessageListVO>> myMessages(@Valid @RequestBody MessagePageDTO dto) {
        return Result.success(miniServiceMessageService.myPage(dto));
    }

    @Operation(summary = "工单详情（含流转时间轴，只能看自己的）")
    @GetMapping("/ticketDetail")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<MiniServiceDetailVO> ticketDetail(@RequestParam Long id) {
        return Result.success(miniServiceMessageService.myDetail(id));
    }

    @Operation(summary = "补充留言（已办结的单补充会自动重开）")
    @PostMapping("/ticketAppend")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> ticketAppend(@RequestBody @Valid ServiceTicketAppendDTO dto) {
        miniServiceMessageService.append(dto);
        return Result.success(1);
    }

    @Operation(summary = "患者动作：撤单 / 确认解决 / 重开")
    @PostMapping("/ticketAction")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> ticketAction(@RequestBody @Valid ServiceTicketActionDTO dto) {
        miniServiceMessageService.patientAction(dto);
        return Result.success(1);
    }

    @Operation(summary = "客服页行为埋点（失败不影响业务）")
    @PostMapping("/trace")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> trace(@RequestBody @Valid ServiceTraceDTO dto) {
        miniServiceTraceService.record(dto);
        return Result.success(1);
    }

}
