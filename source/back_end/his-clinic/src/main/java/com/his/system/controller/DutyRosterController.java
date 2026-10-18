package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DutyRosterPageQueryDTO;
import com.his.system.dto.DutyRosterUpsertDTO;
import com.his.system.dto.DutySubstituteDTO;
import com.his.system.service.DutyRosterService;
import com.his.system.vo.DutyOfficerVO;
import com.his.system.vo.DutyRosterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 全院总值班排班控制器：值班点位排班登记、临时换班与当班负责人查询的接口入口（sql/169，菜单 806）。
 */
@Tag(name = "全院总值班排班")
@RestController
@RequestMapping("/system/dutyRoster")
@RequiredArgsConstructor
public class DutyRosterController {

    private final DutyRosterService dutyRosterService;

    /**
     * 查此刻全院谁负责值班（当天没排班时返回未命中，前端必须显式告警而不是留白）。
     * 用于贴在急诊墙与床位协调场景的值班条，只要求登录——半夜找兜底人不该被菜单权限挡住。
     */
    @Operation(summary = "当前总值班（此刻全院谁负责）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current")
    public Result<DutyOfficerVO> current() {
        return Result.success(dutyRosterService.current());
    }

    /**
     * 今日排班明细：白班/夜班 × 主班/副班。
     * 供值班交班与当日班次核对在岗人员时取数。
     */
    @Operation(summary = "今日排班（白班/夜班 × 主班/副班）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/todayList")
    public Result<List<DutyRosterVO>> todayList() {
        return Result.success(dutyRosterService.todayList());
    }

    /**
     * 总值班排班分页：按日期区间、班次、点位筛选。
     * 用于「总值班排班」页查询卡与排班表格。
     */
    @Operation(summary = "分页查询总值班排班")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DutyRosterVO>> listPage(@Valid @RequestBody DutyRosterPageQueryDTO queryDTO) {
        return Result.success(dutyRosterService.listPage(queryDTO));
    }

    /**
     * 登记或修改总值班排班：同一天 + 班次 + 角色重复提交即改，不产生第二条。
     * 用于「总值班排班」页排班登记弹框保存。
     */
    @Operation(summary = "新增或修改总值班排班（同一天+班次+角色重复提交 = 改）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/dutyRosterUpsert")
    public Result<Long> dutyRosterUpsert(@RequestBody @Valid DutyRosterUpsertDTO upsertDTO) {
        Long id = dutyRosterService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "登记成功" : "修改成功", id);
    }

    /**
     * 临时换班：只写换班人，原排班人保留在记录里以便追责与回滚。
     * 用于「总值班排班」页换班弹框提交。
     */
    @Operation(summary = "临时换班（写换班人，不覆盖原排班人）")
    @PreAuthorize("hasAuthority('org:duty:substitute')")
    @PostMapping("/dutySubstitute")
    public Result<Void> dutySubstitute(@RequestBody @Valid DutySubstituteDTO dto) {
        dutyRosterService.substitute(dto);
        return Result.success("换班成功，原排班人保留在记录中", null);
    }

    /**
     * 撤回换班：清除换班人恢复原排班人。
     * 用于「总值班排班」页换班行的行内撤回操作。
     */
    @Operation(summary = "撤回换班（恢复原排班人）")
    @PreAuthorize("hasAuthority('org:duty:substitute')")
    @PostMapping("/dutySubstituteCancel")
    public Result<Void> dutySubstituteCancel(@RequestParam Long id) {
        dutyRosterService.cancelSubstitute(id);
        return Result.success("已撤回换班", null);
    }

    /**
     * 删除排班行（唯一键不含删除标志，走物理删）。
     * 用于「总值班排班」页操作列删除。
     */
    @Operation(summary = "删除排班（唯一键不含 del_flag → 物理删）")
    @PreAuthorize("hasAuthority('org:duty:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyRosterService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
