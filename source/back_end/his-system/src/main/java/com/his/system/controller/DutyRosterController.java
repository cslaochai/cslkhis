package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DutyRosterQueryPageDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 全院总值班排班（sql/169，菜单 806）。
 *
 * <p><b>{@code /current} 只要求登录</b>，不挂 {@code org:duty:list}：
 * 「今天全院谁负责」是贴在急诊墙上的公共信息 —— 急诊护士、分诊台、收费处都要能一眼看到，
 * 按菜单权限收口等于让人半夜到处打电话问总值班是谁。排/改/删才要 {@code org:duty:*}。
 *
 * <p>权限注解一律标在方法上（类级 {@code @PreAuthorize} 会静默覆盖没写注解的方法，
 * 见 AGENTS.md §4）。
 */
@Tag(name = "全院总值班排班")
@RestController
@RequestMapping("/system/dutyRoster")
@RequiredArgsConstructor
public class DutyRosterController {

    private final DutyRosterService dutyRosterService;

    @Operation(summary = "当前总值班（此刻全院谁负责）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current")
    public Result<DutyOfficerVO> current() {
        return Result.success(dutyRosterService.current());
    }

    @Operation(summary = "今日排班（白班/夜班 × 主班/副班）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/todayList")
    public Result<List<DutyRosterVO>> todayList() {
        return Result.success(dutyRosterService.todayList());
    }

    @Operation(summary = "分页查询总值班排班")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DutyRosterVO>> listPage(@RequestBody DutyRosterQueryPageDTO queryDTO) {
        return Result.success(dutyRosterService.listPage(queryDTO));
    }

    @Operation(summary = "新增或修改总值班排班（同一天+班次+角色重复提交 = 改）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/dutyRosterUpsert")
    public Result<Long> dutyRosterUpsert(@RequestBody @Valid DutyRosterUpsertDTO upsertDTO) {
        Long id = dutyRosterService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "登记成功" : "修改成功", id);
    }

    @Operation(summary = "临时换班（写换班人，不覆盖原排班人）")
    @PreAuthorize("hasAuthority('org:duty:substitute')")
    @PostMapping("/dutySubstitute")
    public Result<Void> dutySubstitute(@RequestBody @Valid DutySubstituteDTO dto) {
        dutyRosterService.substitute(dto);
        return Result.success("换班成功，原排班人保留在记录中", null);
    }

    @Operation(summary = "撤回换班（恢复原排班人）")
    @PreAuthorize("hasAuthority('org:duty:substitute')")
    @PostMapping("/dutySubstituteCancel")
    public Result<Void> dutySubstituteCancel(@RequestParam Long id) {
        dutyRosterService.cancelSubstitute(id);
        return Result.success("已撤回换班", null);
    }

    @Operation(summary = "删除排班（唯一键不含 del_flag → 物理删）")
    @PreAuthorize("hasAuthority('org:duty:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyRosterService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
