package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.StaffScheduleCopyDTO;
import com.his.system.dto.StaffSchedulePageQueryDTO;
import com.his.system.dto.StaffScheduleSwapDTO;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.service.ScheduleChangeLogService;
import com.his.system.service.StaffScheduleService;
import com.his.system.vo.ScheduleChangeLogVO;
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
 */
@Tag(name = "全院岗位排班")
@RestController
@RequestMapping("/system/staffSchedule")
@RequiredArgsConstructor
public class StaffScheduleController {

    private final StaffScheduleService staffScheduleService;
    private final ScheduleChangeLogService scheduleChangeLogService;

    /**
     * 按日期区间、单元、岗位类别、出勤状态与姓名/工号关键词分页查排班行。
     * 用于「全院岗位排班」页查询卡下方的排班表格。
     */
    @Operation(summary = "分页查询排班（日期区间 + 单元 + 岗位类别 + 出勤状态 + 姓名/工号关键词）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @PostMapping("/listPage")
    public Result<PageResult<StaffScheduleVO>> listPage(@Valid @RequestBody StaffSchedulePageQueryDTO queryDTO) {
        return Result.success(staffScheduleService.pageVO(queryDTO));
    }

    /**
     * 登记或修改一条排班，上下班时间与工时由所选班次带出。
     * 用于该页「登记排班」弹框保存；超出单元人力上限时照样保存，只把提示语带回给操作者。
     */
    @Operation(summary = "新增/修改排班（时间与工时由班次带出；超出人力上限只提示不拦）")
    @PreAuthorize("hasAuthority('staff:schedule:add')")
    @PostMapping("/staffScheduleUpsert")
    public Result<Void> staffScheduleUpsert(@Valid @RequestBody StaffScheduleUpsertDTO upsertDTO) {
        String warning = staffScheduleService.upsert(upsertDTO);
        return warning == null ? Result.success("排班成功", null) : Result.success(warning, null);
    }

    /**
     * 按主键删除一条排班。
     * 用于该页表格行的删除动作；删完低于该单元最低在岗人数会当场拦下并回滚。
     */
    @Operation(summary = "删除排班（物理删；删后低于最低在岗会当场拦下并回滚）")
    @PreAuthorize("hasAuthority('staff:schedule:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        staffScheduleService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 换班（两条同天排班互换）或代班（单向换人承接），双方都要重过时间与岗位校验。
     * 用于该页「换班」弹框；原排班人的变动会留痕，供留痕抽屉回看。
     */
    @Operation(summary = "换班（两条同天排班互换）/ 代班（单向换人承接），双方都要重过时间与岗位校验")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/swap")
    public Result<Void> swap(@Valid @RequestBody StaffScheduleSwapDTO dto) {
        staffScheduleService.swap(dto);
        return Result.success("换班成功，原排班人已留痕", null);
    }

    /**
     * 按星期把已排好的整周模式复制到目标周，返回补上的行数，已排过的人跳过。
     * 用于该页「整周复制」弹框，避免新的一周逐条重登同样的班。
     */
    @Operation(summary = "按星期整周复制排班（返回补上的行数，已排过的人自动跳过）")
    @PreAuthorize("hasAuthority('staff:schedule:add')")
    @PostMapping("/copyRange")
    public Result<Integer> copyRange(@Valid @RequestBody StaffScheduleCopyDTO dto) {
        int copied = staffScheduleService.copyRange(dto);
        return Result.success("已复制 " + copied + " 条排班", copied);
    }

    /**
     * 取指定时刻在岗的人员名单，跨零点班归到开始日，所以按昨天加今天两天解析。
     * 用于该页顶部「此刻在岗」卡片——回答「现在打电话给谁」；任何登录岗位都要看，不挂排班管理权限。
     */
    @Operation(summary = "此刻在岗名单（跨零点班归开始日，按昨天+今天两天解析）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/onDuty")
    public Result<List<StaffScheduleVO>> onDuty(@RequestParam(required = false) Integer orgType,
                                                @RequestParam(required = false) Long orgId,
                                                @RequestParam(required = false) Integer staffType) {
        return Result.success(staffScheduleService.onDutyAt(LocalDateTime.now(), orgType, orgId, staffType));
    }

    /**
     * 取一条排班的历次变更留痕（换班/代班/停班/加减号），最新在前。
     * 用于该页行内「留痕」抽屉，追查这条班是谁在什么时候改成的。
     */
    @Operation(summary = "某条排班的变更留痕（换班/代班/停班/加减号，最新在前）")
    @PreAuthorize("hasAuthority('staff:schedule:list')")
    @GetMapping("/changeLogList")
    public Result<List<ScheduleChangeLogVO>> changeLogList(@RequestParam Long staffScheduleId) {
        return Result.success(scheduleChangeLogService.listBySchedule(staffScheduleId));
    }
}
