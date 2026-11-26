package com.his.iot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.iot.dto.IotEventHandleDTO;
import com.his.iot.dto.IotEventPageQueryDTO;
import com.his.iot.service.IotEventService;
import com.his.iot.vo.IotEventVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 物联网设备事件（告警）查询与处理闭环。
 * <p>
 * 权限同遥测：护士站/设备科/运维共用，按钮码未铺底前只要求登录。
 */
@Tag(name = "物联网设备事件")
@RestController
@RequestMapping("/iot/event")
@RequiredArgsConstructor
public class IotEventController {

    private final IotEventService iotEventService;

    /**
     * 监护大屏的科室告警长连接：设备事件落库即下推一帧，页面按级别发声。
     * 断线后由页面的 10 秒轮询兜底（轮询仍能发现新的阻塞告警，只是慢一拍）。
     */
    @Operation(summary = "科室设备告警 SSE 实时流（alarm/sse）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/alarm/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter alarmSse(@RequestParam(required = false) Long deptId) {
        return iotEventService.subscribeAlarm(deptId);
    }

    @Operation(summary = "设备事件分页（listPage）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/listPage")
    public Result<IPage<IotEventVO>> listPage(@Valid @RequestBody IotEventPageQueryDTO query) {
        return Result.success(iotEventService.listPage(query));
    }

    @Operation(summary = "处理设备事件（handle）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody IotEventHandleDTO dto) {
        iotEventService.handle(dto);
        return Result.success();
    }
}
