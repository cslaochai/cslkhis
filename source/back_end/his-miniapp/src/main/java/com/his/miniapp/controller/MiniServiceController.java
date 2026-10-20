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

    /**
     * 提交客服留言：新建一张工单，归属由登录态决定，不接受前端传就诊人。
     * 用于小程序「客服中心」页常见问题没解决后转人工的发起留言。
     */
    @Operation(summary = "提交留言（归属由登录态决定）")
    @PostMapping("/messageUpsert")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<String> messageUpsert(@RequestBody @Valid ServiceMessageUpsertDTO dto) {
        return Result.success(miniServiceMessageService.submit(dto));
    }

    /**
     * 我的工单分页：本人提交的客服工单及其处理状态、受理人，按提交时间倒序。
     * 用于小程序「客服中心」页的我的工单列表。
     */
    @Operation(summary = "我的工单（分页，含处理状态与受理人）")
    @PostMapping("/myMessages")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PageResult<MiniServiceMessageListVO>> myMessages(@Valid @RequestBody MessagePageDTO dto) {
        return Result.success(miniServiceMessageService.myPage(dto));
    }

    /**
     * 工单详情：取本人一张工单的问答往来与流转时间轴，内部备注不出现在出参里。
     * 用于小程序「客服中心」页进入某张工单后的对话视图。
     */
    @Operation(summary = "工单详情（含流转时间轴，只能看自己的）")
    @GetMapping("/ticketDetail")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<MiniServiceDetailVO> ticketDetail(@RequestParam Long id) {
        return Result.success(miniServiceMessageService.myDetail(id));
    }

    /**
     * 补充留言：在已有工单上追加患者一侧的内容，已办结的单补充后自动重开。
     * 用于小程序「客服中心」页工单对话视图的继续追问。
     */
    @Operation(summary = "补充留言（已办结的单补充会自动重开）")
    @PostMapping("/ticketAppend")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> ticketAppend(@RequestBody @Valid ServiceTicketAppendDTO dto) {
        miniServiceMessageService.append(dto);
        return Result.success(1);
    }

    /**
     * 工单患者动作：对本人工单执行撤单、确认解决或重开，推动工单状态机流转。
     * 用于小程序「客服中心」页工单对话视图底部的操作按钮。
     */
    @Operation(summary = "患者动作：撤单 / 确认解决 / 重开")
    @PostMapping("/ticketAction")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> ticketAction(@RequestBody @Valid ServiceTicketActionDTO dto) {
        miniServiceMessageService.patientAction(dto);
        return Result.success(1);
    }

    /**
     * 客服页行为埋点：记录患者在客服台的浏览、检索、反馈与转人工等事件，用于运营分析常见问题效果。
     * 用于小程序「客服中心」页各交互节点的事后上报，上报失败不影响业务动作本身。
     */
    @Operation(summary = "客服页行为埋点（失败不影响业务）")
    @PostMapping("/trace")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> trace(@RequestBody @Valid ServiceTraceDTO dto) {
        miniServiceTraceService.record(dto);
        return Result.success(1);
    }

}
