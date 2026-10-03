package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DutyLogHandoverDTO;
import com.his.system.dto.DutyLogQueryPageDTO;
import com.his.system.dto.DutyLogUpsertDTO;
import com.his.system.service.DutyLogService;
import com.his.system.vo.DutyLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 值班日志 / 交班本（sql/170，挂在菜单 806「总值班排班」的第二个 Tab）。
 *
 * <p>权限全部标在方法上（类级 {@code @PreAuthorize} 会静默覆盖没写注解的方法，见 AGENTS.md §4）。
 *
 * <p>{@code /pendingMine} 只要求登录：接班人要能一眼看到"昨夜留了什么事给我"，
 * 这不是院办专属信息 —— 按菜单权限收口，等于让人第二天自己翻页面撞运气。
 */
@Tag(name = "全院总值班值班日志")
@RestController
@RequestMapping("/system/dutyLog")
@RequiredArgsConstructor
public class DutyLogController {

    private final DutyLogService dutyLogService;

    @Operation(summary = "分页查询值班日志")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DutyLogVO>> listPage(@RequestBody DutyLogQueryPageDTO queryDTO) {
        return Result.success(dutyLogService.listPage(queryDTO));
    }

    @Operation(summary = "待我签收的遗留事项（交班对象 = 当前登录人）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/pendingMine")
    public Result<List<DutyLogVO>> pendingMine() {
        return Result.success(dutyLogService.pendingMine());
    }

    @Operation(summary = "登记或修改值班日志（值班人留空 = 当前总值班）")
    @PreAuthorize("hasAuthority('org:duty:log:edit')")
    @PostMapping("/dutyLogUpsert")
    public Result<Long> dutyLogUpsert(@RequestBody @Valid DutyLogUpsertDTO upsertDTO) {
        Long id = dutyLogService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "登记成功" : "修改成功", id);
    }

    @Operation(summary = "交班（接班人留空 = 下一班总值班）")
    @PreAuthorize("hasAuthority('org:duty:log:handover')")
    @PostMapping("/dutyLogHandover")
    public Result<Void> dutyLogHandover(@RequestBody @Valid DutyLogHandoverDTO dto) {
        dutyLogService.handover(dto);
        return Result.success("已交班，接班人会收到待办", null);
    }

    @Operation(summary = "签收（只有接班人本人能签收）")
    @PreAuthorize("hasAuthority('org:duty:log:handover')")
    @PostMapping("/dutyLogAck")
    public Result<Void> dutyLogAck(@RequestParam Long id) {
        dutyLogService.ack(id);
        return Result.success("已签收，交班闭环", null);
    }

    @Operation(summary = "删除值班日志（已签收的不能删）")
    @PreAuthorize("hasAuthority('org:duty:log:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyLogService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
