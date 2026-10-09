package com.his.appoint.controller;

import com.his.appoint.dto.*;
import com.his.appoint.service.BizEmergencyService;
import com.his.appoint.vo.*;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.vo.BedVO;
import com.his.patient.vo.WardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 急诊管理控制器
 */
@Tag(name = "急诊管理")
@RestController
@RequestMapping("/emergency")
@RequiredArgsConstructor
public class BizEmergencyController {

    private final BizEmergencyService bizEmergencyService;

    @Operation(summary = "分页查询急诊记录（出参含候诊时长/超时档位/派单方式）")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('opd:emergency:list')")
    public Result<PageResult<BizEmergencyVO>> list(@Valid @RequestBody EmergencyQueryDTO queryDTO) {
        return Result.success(bizEmergencyService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('opd:emergency:add')")
    @Operation(summary = "急诊登记")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid BizEmergencyUpsertDTO upsertDTO) {
        boolean success = bizEmergencyService.register(upsertDTO);
        return success ? Result.success("登记成功", null) : Result.error("登记失败");
    }

    @PreAuthorize("hasAuthority('opd:emergency:edit')")
    @Operation(summary = "更新急诊状态（接诊/留观/离院/死亡）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestBody @Valid EmergencyStatusUpsertDTO statusDTO) {
        boolean success = bizEmergencyService.updateStatus(statusDTO);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    @PreAuthorize("hasAuthority('opd:emergency:edit')")
    @Operation(summary = "急诊转住院（办理入院登记，入院途径=急诊）")
    @PostMapping("/admit")
    public Result<String> admit(@RequestBody @Valid EmergencyAdmitDTO admitDTO) {
        Long admissionId = bizEmergencyService.admit(admitDTO);
        // 雪花ID必须字符串出参，前端 Number 会丢精度
        return Result.success("转住院成功，入院登记已办理", String.valueOf(admissionId));
    }

    @Operation(summary = "病区下拉（转住院/留观选床用，参照数据不猜权限）")
    @GetMapping("/wardSelectList")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WardVO>> wardSelectList() {
        return Result.success(bizEmergencyService.wardSelectList());
    }

    @Operation(summary = "床位下拉（按病区）")
    @GetMapping("/bedSelectList")
    @PreAuthorize("isAuthenticated()")
    public Result<List<BedVO>> bedSelectList(@RequestParam Long wardId,
                                             @RequestParam(required = false) Integer bedStatus) {
        return Result.success(bizEmergencyService.bedSelectList(wardId, bedStatus));
    }

    @Operation(summary = "此刻在岗的值班医生（登记表单选医生用，参照数据不猜权限）")
    @GetMapping("/dutySelectList")
    @PreAuthorize("isAuthenticated()")
    public Result<List<EmergencyDutyVO>> dutySelectList(@RequestParam Long deptId) {
        return Result.success(bizEmergencyService.dutySelectList(deptId));
    }

    @Operation(summary = "急诊统计")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('opd:emergency:list')")
    public Result<EmergencyStatsVO> stats() {
        return Result.success(bizEmergencyService.getStats());
    }

    @Operation(summary = "超时候诊升级补跑（定时任务同一入口，返回本次发出的待办条数）")
    @PostMapping("/escalateOverdue")
    @PreAuthorize("hasAuthority('opd:emergency:edit')")
    public Result<Integer> escalateOverdue() {
        return Result.success("升级扫描完成", bizEmergencyService.escalateOverdue());
    }

    @Operation(summary = "留观超时限催办补跑（定时任务同一入口，返回本次发出的待办条数）")
    @PostMapping("/escalateObservation")
    @PreAuthorize("hasAuthority('opd:emergency:edit')")
    public Result<Integer> escalateObservation() {
        return Result.success("留观超时限催办完成", bizEmergencyService.escalateObservation());
    }

    @Operation(summary = "待交班清单（本科室未闭环中「无人指派」+「挂我名下」的行，交班弹框数据源）")
    @GetMapping("/handoverPendingList")
    @PreAuthorize("hasAuthority('opd:emergency:list')")
    public Result<List<EmergencyHandoverPendingVO>> handoverPendingList(@RequestParam(required = false) Long deptId) {
        return Result.success(bizEmergencyService.handoverPendingList(deptId));
    }

    @Operation(summary = "接班人候选（当前在岗优先，其次本科室在职员工；参照数据不猜权限）")
    @GetMapping("/handoverTakeList")
    @PreAuthorize("isAuthenticated()")
    public Result<List<EmergencyTakeCandidateVO>> handoverTakeList(@RequestParam(required = false) Long deptId) {
        return Result.success(bizEmergencyService.handoverTakeList(deptId));
    }

    @Operation(summary = "提交交班（逐条点名，漏一条即拒绝；返回交班单ID，雪花按字符串出参）")
    @PostMapping("/handoverSave")
    @PreAuthorize("hasAuthority('opd:emergency:handover')")
    public Result<String> handoverSave(@RequestBody @Valid EmergencyHandoverUpsertDTO submitDTO) {
        return Result.success("交班完成，未闭环清单已清零", String.valueOf(bizEmergencyService.submitHandover(submitDTO)));
    }

    @Operation(summary = "交班台账分页")
    @PostMapping("/handoverListPage")
    @PreAuthorize("hasAuthority('opd:emergency:list')")
    public Result<PageResult<EmergencyHandoverVO>> handoverListPage(@Valid @RequestBody EmergencyHandoverQueryPageDTO queryDTO) {
        return Result.success(bizEmergencyService.handoverListPage(queryDTO));
    }

    @Operation(summary = "交班单详情（抬头 + 逐条明细凭证）")
    @GetMapping("/handoverGetDetailById")
    @PreAuthorize("hasAuthority('opd:emergency:list')")
    public Result<EmergencyHandoverDetailVO> handoverGetDetailById(@RequestParam Long id) {
        return Result.success(bizEmergencyService.handoverDetailById(id));
    }
}
