package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.CriticalValueHandleDTO;
import com.his.medicaltech.dto.CriticalValuePageQueryDTO;
import com.his.medicaltech.dto.CriticalValueReceiveDTO;
import com.his.medicaltech.service.CriticalValueService;
import com.his.medicaltech.vo.BizCriticalValueVO;
import com.his.medicaltech.vo.CriticalValueStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 检验危急值控制器：危急值上报记录的查询统计、接收处置与超时升级的接口入口。
 */
@Tag(name = "医技管理-危急值")
@RestController
@RequestMapping("/medicaltech/criticalValue")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:criticalValue:list')")
public class CriticalValueController {

    private final CriticalValueService criticalValueService;

    /**
     * 危急值分页：按闭环状态、类型、关键词与报告日期区间检索；「超时」是查询时拿处理时限与当前时刻比出来的，不是库里的状态。
     * 用于危急值管理页的查询表格。
     */
    @Operation(summary = "分页查询危急值")
    @PostMapping("/listPage")
    public Result<PageResult<BizCriticalValueVO>> listPage(@Valid @RequestBody CriticalValuePageQueryDTO queryDTO) {
        return Result.success(criticalValueService.listPage(queryDTO));
    }

    /**
     * 按 id 取单条危急值详情，含结果与参考区间、处理时限与超时标记、接收与处置留痕。
     * 用于危急值详情弹框自取数据，也用于站内信处理弹框跳转后定位到那条危急值。
     */
    @Operation(summary = "根据ID获取危急值详情")
    @GetMapping("/getById")
    public Result<BizCriticalValueVO> getById(@RequestParam Long criticalValueId) {
        return Result.success(criticalValueService.getById(criticalValueId));
    }

    /**
     * 危急值汇总：本月总数、待接收、已接收待处置、已处置、超时未处置与及时处置率。
     * 用于危急值管理页顶部统计卡，随列表一起刷新。
     *
     * @return 及时处置率在没有可统计记录时为空，不填 0（0 会被读成「全都超时」）
     */
    @Operation(summary = "危急值统计（列表页卡片）")
    @GetMapping("/stats")
    public Result<CriticalValueStatsVO> stats() {
        return Result.success(criticalValueService.stats());
    }

    /**
     * 确认接收危急值：登记接收人与接收时刻，只允许待接收状态的记录进入已接收。
     * 用于危急值管理页列表行与详情弹框的「接收」动作。
     */
    @PreAuthorize("hasAuthority('medtech:criticalValue:edit')")
    @Operation(summary = "确认接收危急值")
    @PostMapping("/receive")
    public Result<Boolean> receive(@Valid @RequestBody CriticalValueReceiveDTO dto) {
        return Result.success(criticalValueService.receive(dto));
    }

    /**
     * 记录危急值处置措施并闭环，同时联动关闭这条危急值的站内信待办；未接收直接处置时服务端补上接收人。
     * 用于危急值管理页与站内信处理弹框的「处置」提交。
     */
    @PreAuthorize("hasAuthority('medtech:criticalValue:edit')")
    @Operation(summary = "记录危急值处置措施")
    @PostMapping("/handle")
    public Result<Boolean> handle(@Valid @RequestBody CriticalValueHandleDTO dto) {
        return Result.success(criticalValueService.handle(dto));
    }

    /**
     * 手工补跑一轮超时升级：把已超时仍未处置的危急值二次通知并上报上级，每条只升级一次。
     * 定时任务已按周期自动扫描，本入口供验证与运维当场触发，不等下一轮；重跑影响 0 条。
     *
     * @return 本次完成升级的条数
     */
    @PreAuthorize("hasAuthority('medtech:criticalValue:edit')")
    @Operation(summary = "超时升级补跑（验证/运维用；定时任务每 5 分钟自动扫）")
    @PostMapping("/escalateOverdue")
    public Result<Integer> escalateOverdue() {
        return Result.success(criticalValueService.escalateOverdue());
    }

    /**
     * 删除危急值记录（逻辑删除）。
     * 供清理误上报或重复的危急值，避免其一直挂在待处理统计里。
     */
    @PreAuthorize("hasAuthority('medtech:criticalValue:delete')")
    @Operation(summary = "删除危急值")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long criticalValueId) {
        return Result.success(criticalValueService.deleteById(criticalValueId));
    }
}
