package com.his.schedule.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.schedule.dto.ShiftPageQueryDTO;
import com.his.schedule.dto.ShiftUpsertDTO;
import com.his.schedule.service.ShiftService;
import com.his.schedule.vo.ShiftSelectListVO;
import com.his.schedule.vo.ShiftVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班次字典控制器（排班/周模板「标准班次」下拉的数据源）
 */
@Tag(name = "班次字典")
@RestController
@RequestMapping("/shift")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    /**
     * 班次下拉候选：可按科室（该科室适用 + 全院通用）、状态、使用范围（门诊排班/病区护理）收口，
     * 防止护理班次串进门诊下拉。
     * 用于排班面板、排班与周模板、值班岗位、人力配置标准等表单里选班次。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "班次列表（下拉取数，只要求登录；deptId 传了=该科室适用+全院通用；useScope 传了=只列该册班次）")
    @GetMapping("/selectList")
    public Result<List<ShiftSelectListVO>> selectList(@RequestParam(required = false) Long deptId,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) Integer useScope) {
        return Result.success(shiftService.selectListVO(deptId, status, useScope));
    }

    /**
     * 班次字典分页：名称模糊查询，按开始时间升序、同一开始时间按主键稳定排序。
     * 用于「班次字典」独立维护页（排班中心下）的表格。
     */
    @PreAuthorize("hasAuthority('shift:dict:list')")
    @Operation(summary = "班次分页查询（关键词=名称模糊；排序 start_time + id 二级键）")
    @PostMapping("/listPage")
    public Result<PageResult<ShiftVO>> listPage(@Valid @RequestBody ShiftPageQueryDTO dto) {
        return Result.success(shiftService.pageVO(dto));
    }

    /**
     * 新增或修改班次：完整建条，时长由起止时间推导、同科室重名与跨零点在 service 拦下。
     * 用于「班次字典」独立维护页新增班次，以及需要连带改适用范围的整条覆盖修改。
     */
    @PreAuthorize("hasAuthority('shift:dict:add')")
    @Operation(summary = "新增/修改班次（时长按起止时间重算，同名同科室防重）")
    @PostMapping("/shiftUpsert")
    public Result<Void> shiftUpsert(@Valid @RequestBody ShiftUpsertDTO dto) {
        shiftService.upsertShift(dto);
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", null);
    }

    /**
     * 删除班次：逻辑删，历史排班上已落的班次名不受影响。
     * 供班次字典维护作废不再使用的班次；界面当前只给停用，未接该动作。
     */
    @PreAuthorize("hasAuthority('shift:dict:delete')")
    @Operation(summary = "删除班次（逻辑删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        return shiftService.deleteShift(id) ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 班次改名：只动名称一列，起止时间/适用科室/班次类型等铺底口径不可改；名字未变按幂等成功。
     * 用于「班次字典」独立维护页已有班次的编辑弹框。
     */
    @PreAuthorize("hasAuthority('shift:dict:edit')")
    @Operation(summary = "班次改名（维护界面：只改名称，时间/科室/类型不可改）")
    @PostMapping("/rename")
    public Result<Void> rename(@RequestParam Long id, @RequestParam String shiftName) {
        String error = shiftService.renameShift(id, shiftName);
        return error == null ? Result.success("改名成功", null) : Result.error(error);
    }

    /**
     * 班次启用/停用：只动状态一列，重复提交同一状态按幂等成功。
     * 用于「班次字典」独立维护页编辑弹框的状态开关；停用后该班次不再参与新排班，历史行保留。
     */
    @PreAuthorize("hasAuthority('shift:dict:edit')")
    @Operation(summary = "班次启用/停用（维护界面：只改状态，名称/时间/科室/类型不可改）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        String error = shiftService.updateStatus(id, status);
        return error == null ? Result.success("状态更新成功", null) : Result.error(error);
    }
}
