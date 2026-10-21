package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.AdverseEventActionDTO;
import com.his.clinical.dto.AdverseEventPageQueryDTO;
import com.his.clinical.dto.AdverseEventUpsertDTO;
import com.his.clinical.service.AdverseEventService;
import com.his.clinical.vo.AdverseEventStatsVO;
import com.his.clinical.vo.AdverseEventVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 不良事件控制器
 */
@Tag(name = "不良事件上报")
@RestController
@RequestMapping("/emr/adverseEvent")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('emr:adverseEvent:list')")
public class AdverseEventController {

    private final AdverseEventService adverseEventService;

    /**
     * 不良事件分页：按科室、等级、状态等条件筛出上报过的事件清单。
     * 用于「不良事件」页查询卡下方的事件台账表格。
     */
    @Operation(summary = "分页查询不良事件")
    @PostMapping("/listPage")
    public Result<PageResult<AdverseEventVO>> listPage(@Valid @RequestBody AdverseEventPageQueryDTO queryDTO) {
        return Result.success(adverseEventService.page(queryDTO));
    }

    /**
     * 单个事件详情：取上报内容、处理与整改经过的完整记录。
     * 用于事件列表行点开详情弹框时按事件编号取数。
     */
    @Operation(summary = "事件详情")
    @GetMapping("/getDetailById")
    public Result<AdverseEventVO> getDetailById(@RequestParam Long id) {
        return Result.success(adverseEventService.getDetailById(id));
    }

    /**
     * 上报新事件或改写尚未处理的事件：带编号走修改，编号为空走上报。
     * 用于「不良事件」页新增/修改弹框保存时提交。
     */
    @PreAuthorize("hasAuthority('emr:adverseEvent:add')")
    @Operation(summary = "上报事件 / 修改待处理事件")
    @PostMapping("/upsert")
    public Result<AdverseEventVO> upsert(@Valid @RequestBody AdverseEventUpsertDTO dto) {
        Long id = adverseEventService.upsert(dto);
        // 创建必须回 VO（Result<Long> 只装 count 是全仓口径）
        return Result.success("上报成功", adverseEventService.getDetailById(id));
    }

    /**
     * 事件处理：把「已上报待处理」推进到「处理中」并记录处置措施。
     * 用于质控或责任科室在事件列表对未处理事件填写处理意见。
     */
    @PreAuthorize("hasAuthority('emr:adverseEvent:edit')")
    @Operation(summary = "处理（1→2）")
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody AdverseEventActionDTO dto) {
        adverseEventService.handle(dto);
        return Result.success("处理成功", null);
    }

    /**
     * 事件整改：把「处理中」推进到「已整改」并记录整改结果。
     * 用于责任科室完成整改措施后在事件列表回写。
     */
    @PreAuthorize("hasAuthority('emr:adverseEvent:edit')")
    @Operation(summary = "整改（2→3）")
    @PostMapping("/rectify")
    public Result<Void> rectify(@Valid @RequestBody AdverseEventActionDTO dto) {
        adverseEventService.rectify(dto);
        return Result.success("整改成功", null);
    }

    /**
     * 事件结案：把「已整改」推进到「已结案」，终态不可回退。
     * 用于质控复核整改到位后在事件列表终结该条上报。
     */
    @PreAuthorize("hasAuthority('emr:adverseEvent:edit')")
    @Operation(summary = "结案（3→4，不可逆）")
    @PostMapping("/close")
    public Result<Void> close(@Valid @RequestBody AdverseEventActionDTO dto) {
        adverseEventService.close(dto);
        return Result.success("结案成功", null);
    }

    /**
     * 撤销上报：删除还停在「已上报待处理」的事件，且只允许上报人本人操作。
     * 用于事件列表行的删除按钮，误报或重复上报时收回。
     */
    @PreAuthorize("hasAuthority('emr:adverseEvent:delete')")
    @Operation(summary = "删除待处理事件（仅上报人本人）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        adverseEventService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 本月事件汇总：上报量、待处理量、警讯事件量与已结案量。
     * 用于「不良事件」页顶部统计卡，随列表一同刷新。
     */
    @Operation(summary = "工作台统计（本月上报/待处理/警讯/已结案）")
    @GetMapping("/stats")
    public Result<AdverseEventStatsVO> stats() {
        return Result.success(adverseEventService.monthStats());
    }
}
