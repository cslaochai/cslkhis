package com.his.schedule.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.schedule.dto.DutyCheckDTO;
import com.his.schedule.dto.DutyRosterPageQueryDTO;
import com.his.schedule.dto.DutyRosterUpsertDTO;
import com.his.schedule.dto.DutySubstituteDTO;
import com.his.schedule.dto.DutyWorkloadDTO;
import com.his.schedule.service.DutyRosterService;
import com.his.schedule.vo.DutyCheckVO;
import com.his.schedule.vo.DutyOfficerVO;
import com.his.schedule.vo.DutyRosterVO;
import com.his.schedule.vo.DutyWorkloadVO;
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
    public Result<DutyRosterVO> dutyRosterUpsert(@RequestBody @Valid DutyRosterUpsertDTO upsertDTO) {
        DutyRosterVO row = dutyRosterService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "登记成功" : "修改成功", row);
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
    /**
     * 提交审核：草稿 → 待审核。值班排班人排完后提交，等审核人发布。
     */
    @Operation(summary = "提交审核（草稿→待审核）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestParam Long id) {
        dutyRosterService.submit(id);
        return Result.success("已提交审核", null);
    }

    /**
     * 审核发布：待审核 → 已发布。审核通过后该值守才进「今日值班表」与「此刻谁负责」。
     */
    @Operation(summary = "审核发布（待审核→已发布，此后才对外展示）")
    @PreAuthorize("hasAuthority('org:duty:audit')")
    @PostMapping("/auditPublish")
    public Result<Void> auditPublish(@RequestParam Long id) {
        dutyRosterService.auditPublish(id);
        return Result.success("已发布", null);
    }

    /**
     * 撤回：任意态 → 草稿。撤回后不再对外展示。
     */
    @Operation(summary = "撤回（任意态→草稿，不再对外展示）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/recall")
    public Result<Void> recall(@RequestParam Long id) {
        dutyRosterService.recall(id);
        return Result.success("已撤回", null);
    }

    /**
     * 进入调班：已发布/已变更 → 调班中（锁定）。改完保存自动转「已变更」直接生效。
     */
    @Operation(summary = "进入调班（已发布/已变更→调班中，锁定）")
    @PreAuthorize("hasAuthority('org:duty:edit')")
    @PostMapping("/adjustStart")
    public Result<Void> adjustStart(@RequestParam Long id) {
        dutyRosterService.adjustStart(id);
        return Result.success("已进入调班", null);
    }

    /**
     * 删除一条值守排班。用于总值班排班页操作列删除按钮。
     */
    @Operation(summary = "删除排班（唯一键不含 del_flag → 物理删）")
    @PreAuthorize("hasAuthority('org:duty:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyRosterService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 月度值班工时：一人一行，白班/夜班/主班/副班次数与总时长（按实际值班人归并）。
     * 用于「统计校验」页签的工时表。
     */
    @Operation(summary = "月度值班工时统计")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/monthWorkload")
    public Result<DutyWorkloadVO> monthWorkload(@Valid @RequestBody DutyWorkloadDTO dto) {
        return Result.success(dutyRosterService.monthWorkload(dto));
    }

    /**
     * 区间规则校验：连续值班 / 值班频次 / 主班漏排。只列明细不阻断保存。
     * 用于「统计校验」页签的规则校验表。
     */
    @Operation(summary = "区间规则校验（连续值班/值班频次/主班漏排）")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/check")
    public Result<DutyCheckVO> check(@Valid @RequestBody DutyCheckDTO dto) {
        return Result.success(dutyRosterService.check(dto));
    }
}
