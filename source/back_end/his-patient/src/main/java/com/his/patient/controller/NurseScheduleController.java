package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.NurseScheduleDTO;
import com.his.patient.service.NurseScheduleService;
import com.his.patient.vo.NurseScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病区护理排班（菜单 331 / 路径 /nurse-schedule）。
 */
@Tag(name = "病区护理排班")
@RestController
@RequestMapping("/nursing/schedule")
@RequiredArgsConstructor
public class NurseScheduleController {

    private final NurseScheduleService nurseScheduleService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "病区下拉（只含当前岗位可见科室的启用病区，附在册护士数）")
    @GetMapping("/wardSelectList")
    public Result<List<NurseScheduleVO.Ward>> wardSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(nurseScheduleService.wardSelectList(keyword));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "护理排班单元下拉（病区 + 有护理编制的门诊科室，sql/209；附在册护士数）")
    @GetMapping("/unitSelectList")
    public Result<List<NurseScheduleVO.Ward>> unitSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(nurseScheduleService.unitSelectList(keyword));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "护士下拉（该排班单元所属科室的在册护士：护士/护师）")
    @GetMapping("/nurseSelectList")
    public Result<List<NurseScheduleVO.Nurse>> nurseSelectList(@RequestParam(required = false) Integer unitType,
                                                               @RequestParam Long unitId,
                                                               @RequestParam(required = false) String keyword) {
        return Result.success(nurseScheduleService.nurseSelectList(unitType, unitId, keyword));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "周矩阵（行=护士 列=周一至周日，附本周告警与每日在岗人数对照）")
    @PostMapping("/weekMatrix")
    public Result<NurseScheduleVO.Matrix> weekMatrix(@RequestBody @Valid NurseScheduleDTO.MatrixQuery dto) {
        return Result.success(nurseScheduleService.weekMatrix(dto));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "排班台账分页（跨病区回看）")
    @PostMapping("/listPage")
    public Result<PageResult<NurseScheduleVO.Row>> listPage(@Valid @RequestBody(required = false) NurseScheduleDTO.QueryPage dto) {
        return Result.success(nurseScheduleService.listPage(dto == null ? new NurseScheduleDTO.QueryPage() : dto));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "规则校验（区间告警明细，不阻断保存）")
    @PostMapping("/check")
    public Result<NurseScheduleVO.CheckResult> check(@RequestBody @Valid NurseScheduleDTO.CheckQuery dto) {
        return Result.success(nurseScheduleService.check(dto));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "月度工时统计（含整段未排班的人）")
    @PostMapping("/monthWorkload")
    public Result<NurseScheduleVO.MonthWorkload> monthWorkload(@Valid @RequestBody NurseScheduleDTO.WorkloadQuery dto) {
        return Result.success(nurseScheduleService.monthWorkload(dto));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "点格排班/改格（一人一天一条；非上班状态由服务端清空班次与工时）")
    @PostMapping("/upsert")
    public Result<NurseScheduleVO.SaveResult> upsert(@Valid @RequestBody NurseScheduleDTO.CellUpsert dto) {
        return Result.success("已排班", nurseScheduleService.upsert(dto));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:add')")
    @Operation(summary = "复制上周（只填目标周空缺格，已排的不覆盖）")
    @PostMapping("/copyWeek")
    public Result<NurseScheduleVO.CopyResult> copyWeek(@Valid @RequestBody NurseScheduleDTO.CopyWeek dto) {
        NurseScheduleVO.CopyResult result = nurseScheduleService.copyWeek(dto);
        return Result.success(result.getMessage(), result);
    }

    @PreAuthorize("hasAuthority('nursing:schedule:delete')")
    @Operation(summary = "删除一格（物理删：唯一键不含 del_flag，软删会让重排同一人同一天撞键）")
    @DeleteMapping("/deleteById")
    public Result<NurseScheduleVO.DeleteResult> deleteById(@RequestParam Long id) {
        return Result.success("已删除", nurseScheduleService.deleteById(id));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:list')")
    @Operation(summary = "人力配置标准（含停用行，看得见为什么不生效）")
    @GetMapping("/ruleList")
    public Result<List<NurseScheduleVO.Rule>> ruleList(@RequestParam(required = false) Integer unitType,
                                                       @RequestParam Long unitId) {
        return Result.success(nurseScheduleService.ruleList(unitType, unitId));
    }

    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "保存人力配置标准（shiftId=0 为病区级行：工时/连班上限只在这一行有效）")
    @PostMapping("/ruleUpsert")
    public Result<Void> ruleUpsert(@Valid @RequestBody NurseScheduleDTO.RuleUpsert dto) {
        nurseScheduleService.ruleUpsert(dto);
        return Result.success("标准已保存", null);
    }

    @PreAuthorize("hasAuthority('nursing:schedule:edit')")
    @Operation(summary = "删除人力配置标准（物理删）")
    @DeleteMapping("/ruleDeleteById")
    public Result<Void> ruleDeleteById(@RequestParam Long id) {
        nurseScheduleService.ruleDeleteById(id);
        return Result.success("已删除", null);
    }
}
