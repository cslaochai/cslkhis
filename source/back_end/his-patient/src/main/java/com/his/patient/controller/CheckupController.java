package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.CheckupDTO;
import com.his.patient.service.CheckupService;
import com.his.patient.vo.CheckupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 体检控制器（套餐 / 登记 / 结果 / 总检）。
 */
@Tag(name = "体检管理")
@RestController
@RequestMapping("/patient/checkup")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('checkup:manage:list')")
public class CheckupController {

    private final CheckupService checkupService;

    /**
     * 新增或修改体检套餐，项目明细按整单替换写入。
     * 用于体检套餐页「新增/修改套餐」弹框保存。
     */
    @PreAuthorize("hasAuthority('checkup:manage:add')")
    @Operation(summary = "套餐保存（新增/更新，整单替换明细）")
    @PostMapping("/package/save")
    public Result<CheckupVO.PackageVO> savePackage(@Valid @RequestBody CheckupDTO.PackageSave dto) {
        return Result.success("套餐已保存", checkupService.savePackage(dto));
    }

    /**
     * 体检套餐分页查询。
     * 用于体检套餐页查询表格，也用于体检登记时拉可选套餐清单。
     */
    @Operation(summary = "套餐分页")
    @PostMapping("/package/listPage")
    public Result<PageResult<CheckupVO.PackageVO>> packagePage(@Valid @RequestBody CheckupDTO.PackageQuery dto) {
        var page = checkupService.packagePage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 取单个套餐及其项目明细。
     * 用于体检套餐编辑弹框的回显取数。
     */
    @Operation(summary = "套餐详情")
    @GetMapping("/package/getDetailById")
    public Result<CheckupVO.PackageVO> getPackage(@RequestParam Long id) {
        return Result.success(checkupService.getPackage(id));
    }

    /**
     * 停用一份体检套餐，停用后不再出现在登记可选清单。
     * 用于体检套餐页操作列的停用动作。
     */
    @PreAuthorize("hasAuthority('checkup:manage:edit')")
    @Operation(summary = "套餐停用")
    @PostMapping("/package/disable")
    public Result<Void> disablePackage(@RequestParam Long id) {
        checkupService.disablePackage(id);
        return Result.success("套餐已停用", null);
    }

    /**
     * 为患者建立一次体检登记，并按所选套餐项目预生成待录入的结果空行。
     * 用于体检管理页「新增登记」弹框保存。
     */
    @PreAuthorize("hasAuthority('checkup:manage:add')")
    @Operation(summary = "体检登记（按套餐项目预生成结果空行）")
    @PostMapping("/record/create")
    public Result<CheckupVO.RecordVO> createRecord(@Valid @RequestBody CheckupDTO.RecordCreate dto) {
        return Result.success("体检登记成功", checkupService.createRecord(dto));
    }

    /**
     * 体检登记分页查询。
     * 用于体检管理页查询卡与登记表格。
     */
    @Operation(summary = "体检登记分页")
    @PostMapping("/record/listPage")
    public Result<PageResult<CheckupVO.RecordVO>> recordPage(@Valid @RequestBody CheckupDTO.RecordQuery dto) {
        var page = checkupService.recordPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 取一次体检登记及其全部结果明细；读取时顺带把明细已录完的登记状态推进为已完成。
     * 用于体检详情弹框与总检弹框的取数。
     */
    @Operation(summary = "体检登记详情（含结果明细；顺带判定明细录完→已完成）")
    @GetMapping("/record/getDetailById")
    public Result<CheckupVO.RecordVO> getRecord(@RequestParam Long recordId) {
        return Result.success(checkupService.refreshFinishStatus(recordId));
    }

    /**
     * 把登记为已登记的受检者置为检查中，状态机的前进动作。
     * 用于体检管理页操作列「开始体检」。
     */
    @PreAuthorize("hasAuthority('checkup:manage:edit')")
    @Operation(summary = "开始体检（1→2）")
    @PostMapping("/record/start")
    public Result<Void> start(@RequestParam Long recordId) {
        checkupService.startCheckup(recordId);
        return Result.success("已开始体检", null);
    }

    /**
     * 保存体检登记下单个项目的检查结果。
     * 用于体检详情弹框的逐项结果录入。
     */
    @PreAuthorize("hasAuthority('checkup:manage:add')")
    @Operation(summary = "单项结果录入")
    @PostMapping("/result/save")
    public Result<CheckupVO.ResultVO> saveResult(@Valid @RequestBody CheckupDTO.ResultSave dto) {
        return Result.success("结果已保存", checkupService.saveResult(dto));
    }

    /**
     * 出具总检结论并把登记推进到已出报告。
     * 用于体检详情的「总检出报告」弹框提交。
     */
    @PreAuthorize("hasAuthority('checkup:manage:edit')")
    @Operation(summary = "总检出报告（3→4）")
    @PostMapping("/record/conclude")
    public Result<CheckupVO.RecordVO> conclude(@Valid @RequestBody CheckupDTO.Conclusion dto) {
        return Result.success("体检报告已出", checkupService.conclude(dto));
    }

    /**
     * 删除一次体检登记，只有尚未开始检查的登记可删。
     * 用于体检管理页操作列的删除动作。
     */
    @PreAuthorize("hasAuthority('checkup:manage:delete')")
    @Operation(summary = "删除登记（仅已登记可删）")
    @PostMapping("/record/deleteById")
    public Result<Void> deleteRecord(@RequestParam Long recordId) {
        checkupService.deleteRecord(recordId);
        return Result.success("登记已删除", null);
    }
}
