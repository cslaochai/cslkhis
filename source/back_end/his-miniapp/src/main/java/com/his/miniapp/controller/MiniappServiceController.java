package com.his.miniapp.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.miniapp.dto.ServiceMessageUpsertDTO;
import com.his.miniapp.dto.ServiceTicketActionDTO;
import com.his.miniapp.dto.ServiceTicketAppendDTO;
import com.his.miniapp.dto.ServiceTraceDTO;
import com.his.miniapp.service.MiniappServiceMessageService;
import com.his.miniapp.service.MiniappServiceTraceService;
import com.his.miniapp.vo.ServiceMessageListVO;
import com.his.miniapp.vo.ServiceTicketDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端客服台：工单（原「留言」）与埋点。
 *
 * <p>sql/221 把留言升级成<b>可受理工单</b>：患者提单后能看到进展时间轴
 * （受理 / 回复 / 办结 / 患者确认），而不是只知道一个干巴巴的状态码。
 * 受理端在 {@code /miniapp/service/admin/**}（权限 {@code service:ticket:*}）。
 */
@Tag(name = "患者端-客服台")
@RestController
@RequestMapping("/miniapp/service")
@RequiredArgsConstructor
public class MiniappServiceController {

    private final MiniappServiceMessageService messageService;
    private final MiniappServiceTraceService traceService;

    @Operation(summary = "提交留言（归属由登录态决定）")
    @PostMapping("/messageUpsert")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<String> messageUpsert(@RequestBody @Valid ServiceMessageUpsertDTO dto) {
        return Result.success(messageService.submit(dto));
    }

    @Operation(summary = "我的工单（分页，含处理状态与受理人）")
    @PostMapping("/myMessages")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<PageResult<ServiceMessageListVO>> myMessages(@RequestBody MessagePageDTO dto) {
        MessagePageDTO query = dto == null ? new MessagePageDTO() : dto;
        return Result.success(messageService.myPage(query.getPageNum(), query.getPageSize()));
    }

    @Operation(summary = "工单详情（含流转时间轴，只能看自己的）")
    @GetMapping("/ticketDetail")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<ServiceTicketDetailVO> ticketDetail(@RequestParam String id) {
        return Result.success(messageService.myDetail(parseId(id)));
    }

    @Operation(summary = "补充留言（已办结的单补充会自动重开）")
    @PostMapping("/ticketAppend")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> ticketAppend(@RequestBody @Valid ServiceTicketAppendDTO dto) {
        messageService.append(dto);
        return Result.success(1);
    }

    @Operation(summary = "患者动作：撤单 / 确认解决 / 重开")
    @PostMapping("/ticketAction")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> ticketAction(@RequestBody @Valid ServiceTicketActionDTO dto) {
        messageService.patientAction(dto);
        return Result.success(1);
    }

    private static Long parseId(String value) {
        if (!StringUtils.hasText(value) || !value.matches("\\d{1,20}")) {
            return null;
        }
        return Long.parseLong(value);
    }

    @Operation(summary = "客服页行为埋点（失败不影响业务）")
    @PostMapping("/trace")
    @PreAuthorize("hasAuthority('PATIENT')")
    public Result<Integer> trace(@RequestBody @Valid ServiceTraceDTO dto) {
        traceService.record(dto);
        return Result.success(1);
    }

    @Data
    public static class MessagePageDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }
}
