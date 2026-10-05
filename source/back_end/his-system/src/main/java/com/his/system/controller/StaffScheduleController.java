package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.StaffScheduleCopyDTO;
import com.his.system.dto.StaffScheduleQueryPageDTO;
import com.his.system.dto.StaffScheduleSwapDTO;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.service.ScheduleChangeLogService;
import com.his.system.service.StaffScheduleService;
import com.his.system.vo.ScheduleChangeLogVO;
import com.his.system.vo.StaffOnDutyVO;
import com.his.system.vo.StaffScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 全院岗位排班（谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤）。
 *
 * <p>{@code /onDuty} 与 {@code /changeLogList} 只要求登录：「此刻谁在岗」是分诊、急诊、
 * 收费处都要拿来打电话的公共信息，按菜单权限收口等于让人半夜找不到人（AGENTS.md §4）。
 */
@Tag(name = "全院岗位排班")
@RestController
@RequestMapping("/system/staffSchedule")
@RequiredArgsConstructor
public class StaffScheduleController {

    private final StaffScheduleService staffScheduleService;
    private final ScheduleChangeLogService scheduleChangeLogService;

    @Operation(summary = "分页查询排班（日期区间 + 单元 + 岗位类别 + 出勤状态 + 姓名/工号关键词）")
    @PreAuthorize("hasAuthority('org:schedule:list')")
    @PostMapping("/listPage")
    public Result<PageResult<StaffScheduleVO>> listPage(@Valid @RequestBody StaffScheduleQueryPageDTO queryDTO) {
        return Result.success(staffScheduleService.pageVO(queryDTO));
    }

    @Operation(summary = "新增/修改排班（时间与工时由班次带出；超出人力上限只提示不拦）")
    @PreAuthorize("hasAuthority('org:schedule:add')")
    @PostMapping("/staffScheduleUpsert")
    public Result<Void> staffScheduleUpsert(@Valid @RequestBody StaffScheduleUpsertDTO upsertDTO) {
        String warning = staffScheduleService.upsert(upsertDTO);
        return warning == null ? Result.success("排班成功", null) : Result.success(warning, null);
    }

    @Operation(summary = "删除排班（物理删；删后低于最低在岗会当场拦下并回滚）")
    @PreAuthorize("hasAuthority('org:schedule:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        staffScheduleService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "换班（两条同天排班互换）/ 代班（单向换人承接），双方都要重过时间与岗位校验")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @PostMapping("/swap")
    public Result<Void> swap(@Valid @RequestBody StaffScheduleSwapDTO dto) {
        staffScheduleService.swap(dto);
        return Result.success("换班成功，原排班人已留痕", null);
    }

    @Operation(summary = "按星期整周复制排班（返回补上的行数，已排过的人自动跳过）")
    @PreAuthorize("hasAuthority('org:schedule:add')")
    @PostMapping("/copyRange")
    public Result<Integer> copyRange(@Valid @RequestBody StaffScheduleCopyDTO dto) {
        int copied = staffScheduleService.copyRange(dto);
        return Result.success("已复制 " + copied + " 条排班", copied);
    }

    @Operation(summary = "此刻在岗名单（跨零点班归开始日，按昨天+今天两天解析）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/onDuty")
    public Result<List<StaffOnDutyVO>> onDuty(@RequestParam(required = false) Integer orgType,
                                              @RequestParam(required = false) Long orgId,
                                              @RequestParam(required = false) Integer staffType) {
        return Result.success(staffScheduleService.onDutyAt(LocalDateTime.now(), orgType, orgId, staffType));
    }

    @Operation(summary = "某条排班的变更留痕（换班/代班/停班/加减号，最新在前）")
    @PreAuthorize("hasAuthority('org:schedule:list')")
    @GetMapping("/changeLogList")
    public Result<List<ScheduleChangeLogVO>> changeLogList(@RequestParam Long staffScheduleId) {
        return Result.success(scheduleChangeLogService.listBySchedule(staffScheduleId));
    }
}
