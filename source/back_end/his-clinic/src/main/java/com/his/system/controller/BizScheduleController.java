package com.his.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.NurseScheduleDTO;
import com.his.system.dto.NurseScheduleLogQueryDTO;
import com.his.system.service.BizScheduleService;
import com.his.system.service.NurseScheduleLogService;
import com.his.system.vo.NurseScheduleLogVO;
import com.his.system.vo.NurseScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病区护理排班
 */
@Tag(name = "病区护理排班")
@RestController
@RequestMapping("/nursing/schedule")
@RequiredArgsConstructor
public class BizScheduleController {

    private final BizScheduleService bizScheduleService;

    private final NurseScheduleLogService nurseScheduleLogService;

    /**
     * 病区候选下拉，可按关键词过滤。
     * 用于全院岗位排班页登记排班时选择病区类型的排班单元。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "病区下拉")
    @GetMapping("/wardSelectList")
    public Result<List<NurseScheduleVO.Ward>> wardSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(bizScheduleService.wardSelectList(keyword));
    }

    /**
     * 护理排班单元候选下拉，可按关键词过滤。
     * 用于病区护理排班页顶部的排班单元切换。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "护理排班单元下拉")
    @GetMapping("/unitSelectList")
    public Result<List<NurseScheduleVO.Ward>> unitSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(bizScheduleService.unitSelectList(keyword));
    }

    /**
     * 查询排班单元所属科室的在册护士（护士/护师）候选下拉。
     * 供护理排班点格登记环节选择护士时取候选。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "护士下拉（该排班单元所属科室的在册护士：护士/护师）")
    @GetMapping("/nurseSelectList")
    public Result<List<NurseScheduleVO.Nurse>> nurseSelectList(@RequestParam(required = false) Integer unitType,
                                                               @RequestParam Long unitId,
                                                               @RequestParam(required = false) String keyword) {
        return Result.success(bizScheduleService.nurseSelectList(unitType, unitId, keyword));
    }

    /**
     * 查询本周排班矩阵（行=护士、列=周一至周日），附本周告警与每日在岗人数对照。
     * 用于病区护理排班页主视图取数。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "周矩阵（行=护士 列=周一至周日，附本周告警与每日在岗人数对照）")
    @PostMapping("/weekMatrix")
    public Result<NurseScheduleVO.Matrix> weekMatrix(@RequestBody @Valid NurseScheduleDTO.MatrixQuery dto) {
        return Result.success(bizScheduleService.weekMatrix(dto));
    }

    /**
     * 护理排班台账分页，跨病区回看历史排班行。
     * 用于病区护理排班页排班台账页签取数。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "排班台账分页（跨病区回看）")
    @PostMapping("/listPage")
    public Result<PageResult<NurseScheduleVO.Row>> listPage(@Valid @RequestBody NurseScheduleDTO.PageQuery dto) {
        return Result.success(bizScheduleService.listPage(dto));
    }

    /**
     * 对区间内排班跑人力配置规则校验，返回告警明细、不阻断登记。
     * 用于病区护理排班页保存前后的规则自查提示。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "规则校验（区间告警明细，不阻断保存）")
    @PostMapping("/check")
    public Result<NurseScheduleVO.CheckResult> check(@RequestBody @Valid NurseScheduleDTO.CheckQuery dto) {
        return Result.success(bizScheduleService.check(dto));
    }

    /**
     * 统计护士月度工时与出勤，整段未排班的人也在名单内。
     * 用于病区护理排班页月度工时统计视图取数。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "月度工时统计（含整段未排班的人）")
    @PostMapping("/monthWorkload")
    public Result<NurseScheduleVO.MonthWorkload> monthWorkload(@Valid @RequestBody NurseScheduleDTO.WorkloadQuery dto) {
        return Result.success(bizScheduleService.monthWorkload(dto));
    }

    /**
     * 排班或修改一格（一人一天一条；非上班状态由服务端清空班次与工时）。
     * 用于病区护理排班页周矩阵点格编辑弹框保存。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "点格排班/改格（一人一天一条；非上班状态由服务端清空班次与工时）")
    @PostMapping("/upsert")
    public Result<NurseScheduleVO.SaveResult> upsert(@Valid @RequestBody NurseScheduleDTO.CellUpsert dto) {
        return Result.success("已排班", bizScheduleService.upsert(dto));
    }

    /**
     * 提交审核：草稿 → 待审核。排班人排完后提交，等审核人发布。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "提交审核（草稿→待审核）")
    @PostMapping("/submit")
    public Result<Void> submit(@RequestParam Long id) {
        bizScheduleService.submit(id);
        return Result.success("已提交审核", null);
    }

    /**
     * 审核发布：待审核 → 已发布。审核通过后该排班才正式生效、计入在岗。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:audit')")
    @Operation(summary = "审核发布（待审核→已发布，此后才计在岗）")
    @PostMapping("/auditPublish")
    public Result<Void> auditPublish(@RequestParam Long id) {
        bizScheduleService.auditPublish(id);
        return Result.success("已发布", null);
    }

    /**
     * 整周提交审核：本周本病区/科室的草稿一次性 → 待审核（整周批量，不碰已发布/调班中行）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "整周提交审核（本周草稿→待审核）")
    @PostMapping("/submitWeek")
    public Result<Integer> submitWeek(@RequestBody @Valid NurseScheduleDTO.MatrixQuery dto) {
        int moved = bizScheduleService.submitWeek(dto);
        return Result.success("已提交审核 " + moved + " 条", moved);
    }

    /**
     * 整周审核发布：本周待审核一次性 → 已发布，此后计入在岗（整周批量，不碰草稿/调班中行）。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:audit')")
    @Operation(summary = "整周审核发布（本周待审核→已发布，此后计在岗）")
    @PostMapping("/auditWeek")
    public Result<Integer> auditWeek(@RequestBody @Valid NurseScheduleDTO.MatrixQuery dto) {
        int moved = bizScheduleService.auditWeek(dto);
        return Result.success("已发布 " + moved + " 条", moved);
    }

    /**
     * 撤回：任意态 → 草稿。撤回后不计在岗。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "撤回（任意态→草稿，不计在岗）")
    @PostMapping("/recall")
    public Result<Void> recall(@RequestParam Long id) {
        bizScheduleService.recall(id);
        return Result.success("已撤回", null);
    }

    /**
     * 进入调班：已发布/已变更 → 调班中（锁定）。改完保存自动转「已变更」直接生效。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "进入调班（已发布/已变更→调班中，锁定）")
    @PostMapping("/adjustStart")
    public Result<Void> adjustStart(@RequestParam Long id) {
        bizScheduleService.adjustStart(id);
        return Result.success("已进入调班", null);
    }

    /**
     * 批量铺班：共用「状态+班次」× 选中日期 × 多选护士笛卡尔展开，只填空缺格、已排的跳过不覆盖，单事务。
     * 用于病区护理排班页「批量铺班」弹框提交；前端禁止循环调用单格接口。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "批量铺班（共用状态/班次×选中日×多选护士，只填空缺格不覆盖）")
    @PostMapping("/batchUpsert")
    public Result<NurseScheduleVO.BatchResult> batchUpsert(@Valid @RequestBody NurseScheduleDTO.BatchUpsert dto) {
        return Result.success("批量铺班完成", bizScheduleService.batchUpsert(dto));
    }

    /**
     * 把上周排班复制到目标周，只填空缺格、已排的不覆盖。
     * 用于病区护理排班页周矩阵的「复制上周」动作。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "复制上周（只填目标周空缺格，已排的不覆盖）")
    @PostMapping("/copyWeek")
    public Result<NurseScheduleVO.CopyResult> copyWeek(@Valid @RequestBody NurseScheduleDTO.CopyWeek dto) {
        NurseScheduleVO.CopyResult result = bizScheduleService.copyWeek(dto);
        return Result.success(result.getMessage(), result);
    }

    /**
     * 删除一格护理排班。
     * 用于病区护理排班页周矩阵的清空格子动作；删除走物理删——软删会让重排同一人同一天撞唯一键。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:delete')")
    @Operation(summary = "删除一格（物理删：唯一键不含 del_flag，软删会让重排同一人同一天撞键）")
    @DeleteMapping("/deleteById")
    public Result<NurseScheduleVO.DeleteResult> deleteById(@RequestParam Long id) {
        return Result.success("已删除", bizScheduleService.deleteById(id));
    }

    /**
     * 护理排班审批与变更留痕分页：谁在何时对哪位护士哪天做了提交/发布/撤回/调班/改格/删格、为什么。
     * 用于护理排班页「审批记录」查看入口的审计查询。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "护理排班审批与变更留痕分页（审计：谁在何时对哪位护士哪天做了什么、为什么）")
    @PostMapping("/logPage")
    public Result<IPage<NurseScheduleLogVO>> logPage(@Valid @RequestBody NurseScheduleLogQueryDTO queryDTO) {
        return Result.success(nurseScheduleLogService.pageByQuery(queryDTO));
    }

    /**
     * 查询排班单元的人力配置标准，含停用行（看得见为什么不生效）。
     * 用于病区护理排班页人力配置标准页签取数。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "人力配置标准（含停用行，看得见为什么不生效）")
    @GetMapping("/ruleList")
    public Result<List<NurseScheduleVO.Rule>> ruleList(@RequestParam(required = false) Integer unitType,
                                                       @RequestParam Long unitId) {
        return Result.success(bizScheduleService.ruleList(unitType, unitId));
    }

    /**
     * 保存一条人力配置标准（病区级行承载工时/连班上限，班次级行按班次配置）。
     * 用于病区护理排班页标准编辑弹框保存。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "保存人力配置标准（shiftId=0 为病区级行：工时/连班上限只在这一行有效）")
    @PostMapping("/ruleUpsert")
    public Result<Void> ruleUpsert(@Valid @RequestBody NurseScheduleDTO.RuleUpsert dto) {
        bizScheduleService.ruleUpsert(dto);
        return Result.success("标准已保存", null);
    }

    /**
     * 删除一条人力配置标准。
     * 用于病区护理排班页标准页签的行删除动作；删除走物理删。
     */
    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "删除人力配置标准（物理删）")
    @DeleteMapping("/ruleDeleteById")
    public Result<Void> ruleDeleteById(@RequestParam Long id) {
        bizScheduleService.ruleDeleteById(id);
        return Result.success("已删除", null);
    }
}
