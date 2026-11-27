package com.his.iot.controller;

import com.his.common.base.Result;
import com.his.iot.dto.IotColdChainRecordQueryDTO;
import com.his.iot.dto.IotHistoryQueryDTO;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.dto.IotWaveformQueryDTO;
import com.his.iot.service.IotTelemetryService;
import com.his.iot.vo.IotColdChainRecordVO;
import com.his.iot.vo.IotColdChainSummaryVO;
import com.his.iot.vo.IotDeviceTypeVO;
import com.his.iot.vo.IotHistoryPointVO;
import com.his.iot.vo.IotLatestVO;
import com.his.iot.vo.IotWaveformVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物联网设备遥测查询。
 * <p>
 * 权限暂时只要求登录：设备总览要在护士站、设备科、运维多个岗位复用，而按钮级权限码还没在
 * {@code sys_menu} 铺底——先挂具体权限码会导致「码不存在 = 任何角色拿不到 = 接口永久 403」。
 * 等页面定稿、权限码入 {@code sql/100} 后再换成 {@code hasAuthority('iot:telemetry:view')}。
 */
@Tag(name = "物联网设备遥测")
@RestController
@RequestMapping("/iot/telemetry")
@RequiredArgsConstructor
public class IotTelemetryController {

    private final IotTelemetryService iotTelemetryService;

    /**
     * 设备类型下拉（设备总览页类型筛选）。返回 DeviceTypeSchema 注册表的类型码与中文名。
     */
    @Operation(summary = "设备类型下拉（selectDeviceTypes）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectDeviceTypes")
    public Result<List<IotDeviceTypeVO>> selectDeviceTypes() {
        return Result.success(iotTelemetryService.deviceTypes());
    }

    /**
     * 设备最新一行遥测（当前状态）。用于设备总览卡片、护士站"这床现在什么情况"。
     */
    @Operation(summary = "设备最新遥测（latestList）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/latestList")
    public Result<List<IotLatestVO>> latestList(@Valid @RequestBody IotLatestQueryDTO query) {
        return Result.success(iotTelemetryService.latestList(query));
    }

    /**
     * 单台设备的历史遥测点，按时间升序。用于趋势图、冷链温度回溯。
     */
    @Operation(summary = "设备历史遥测（historyList）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/historyList")
    public Result<List<IotHistoryPointVO>> historyList(@Valid @RequestBody IotHistoryQueryDTO query) {
        return Result.success(iotTelemetryService.historyList(query));
    }

    /**
     * 监护波形批量取窗。曲线只在内存缓冲不落库；大屏一轮轮询把本科室监护仪编号传入，
     * 拿回最近一段 ECG/脉搏波/呼吸波重绘。
     */
    @Operation(summary = "监护波形窗（waveformList）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/waveformList")
    public Result<List<IotWaveformVO>> waveformList(@RequestBody(required = false) IotWaveformQueryDTO query) {
        return Result.success(iotTelemetryService.waveformList(query));
    }

    /**
     * 冷链温湿度记录（5 分钟一轮，含最低/最高），GSP 检查导出用。
     */
    @Operation(summary = "冷链温湿度 GSP 记录（coldChainRecords）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/coldChainRecords")
    public Result<List<IotColdChainRecordVO>> coldChainRecords(@Valid @RequestBody IotColdChainRecordQueryDTO query) {
        return Result.success(iotTelemetryService.coldChainRecords(query));
    }

    /**
     * 冷链近 N 小时汇总。默认 24 小时，最多 7 天；卡片显示温度区间与归档完整度。
     */
    @Operation(summary = "冷链温度汇总（coldChainSummary）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/coldChainSummary")
    public Result<List<IotColdChainSummaryVO>> coldChainSummary(
            @RequestParam(defaultValue = "24") Integer hours) {
        return Result.success(iotTelemetryService.coldChainSummary(hours));
    }
}
