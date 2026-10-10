package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.AdverseEventActionDTO;
import com.his.emr.dto.AdverseEventQueryPageDTO;
import com.his.emr.dto.AdverseEventUpsertDTO;
import com.his.emr.service.AdverseEventService;
import com.his.emr.vo.AdverseEventStatsVO;
import com.his.emr.vo.AdverseEventVO;
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

    @Operation(summary = "分页查询不良事件")
    @PostMapping("/listPage")
    public Result<PageResult<AdverseEventVO>> listPage(@Valid @RequestBody AdverseEventQueryPageDTO queryDTO) {
        return Result.success(adverseEventService.page(queryDTO));
    }

    @Operation(summary = "事件详情")
    @GetMapping("/getDetailById")
    public Result<AdverseEventVO> getDetailById(@RequestParam Long id) {
        return Result.success(adverseEventService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:adverseEvent:add')")
    @Operation(summary = "上报事件 / 修改待处理事件")
    @PostMapping("/upsert")
    public Result<AdverseEventVO> upsert(@Valid @RequestBody AdverseEventUpsertDTO dto) {
        Long id = adverseEventService.upsert(dto);
        // 创建必须回 VO（Result<Long> 只装 count 是全仓口径）
        return Result.success("上报成功", adverseEventService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:adverseEvent:edit')")
    @Operation(summary = "处理（1→2）")
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody AdverseEventActionDTO dto) {
        adverseEventService.handle(dto);
        return Result.success("处理成功", null);
    }

    @PreAuthorize("hasAuthority('emr:adverseEvent:edit')")
    @Operation(summary = "整改（2→3）")
    @PostMapping("/rectify")
    public Result<Void> rectify(@Valid @RequestBody AdverseEventActionDTO dto) {
        adverseEventService.rectify(dto);
        return Result.success("整改成功", null);
    }

    @PreAuthorize("hasAuthority('emr:adverseEvent:edit')")
    @Operation(summary = "结案（3→4，不可逆）")
    @PostMapping("/close")
    public Result<Void> close(@Valid @RequestBody AdverseEventActionDTO dto) {
        adverseEventService.close(dto);
        return Result.success("结案成功", null);
    }

    @PreAuthorize("hasAuthority('emr:adverseEvent:delete')")
    @Operation(summary = "删除待处理事件（仅上报人本人）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        adverseEventService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "工作台统计（本月上报/待处理/警讯/已结案）")
    @GetMapping("/stats")
    public Result<AdverseEventStatsVO> stats() {
        return Result.success(adverseEventService.monthStats());
    }
}
