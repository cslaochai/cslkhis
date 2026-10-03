package com.his.operation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.operation.dto.OperationApplyQueryPageDTO;
import com.his.operation.dto.OperationApplyUpsertDTO;
import com.his.operation.dto.OperationCancelDTO;
import com.his.operation.dto.OperationFinishDTO;
import com.his.operation.dto.OperationPreopCheckDTO;
import com.his.operation.dto.OperationScheduleDTO;
import com.his.operation.service.OperationApplyService;
import com.his.operation.vo.OperationApplyVO;
import com.his.operation.vo.OperationScheduleMatrixVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 住院手术闭环端点（P4.3）。
 *
 * <p>路径与会诊/转科保持同一套命名：查询一律 {@code GET} + 驼峰 URL，
 * 写操作一律 {@code POST}，只返回一个 ID 用 {@code Result<String>}（雪花 ID 超 JS 精度）。
 *
 * <p>路径刻意用 {@code operationApply} 而不是 {@code operation}：既有的
 * 病案首页手术明细是**病案首页手术明细**（表单侧），
 * 本控制器管的是**手术闭环主单**（申请→排台→核对→完成）。两个概念不能共用一个路径前缀。
 */
@Tag(name = "住院手术闭环")
@RestController
@RequestMapping("/patient/inpatient/operationApply")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ipd:anesthesia:list', 'ipd:surgery:list', 'ipd:operationCount:list')")
public class OperationApplyController {

    private final OperationApplyService operationApplyService;

    @Operation(summary = "手术申请分页（状态/术式/主刀/手术间/日期范围/关键字）")
    @GetMapping("/listPage")
    public Result<IPage<OperationApplyVO>> listPage(OperationApplyQueryPageDTO query) {
        return Result.success(operationApplyService.listPage(query));
    }

    @Operation(summary = "手术申请详情")
    @GetMapping("/getDetailById")
    public Result<OperationApplyVO> getDetailById(@RequestParam Long applyId) {
        return Result.success(operationApplyService.getDetailById(applyId));
    }

    @Operation(summary = "某次住院的全部手术申请（按发生顺序升序）")
    @GetMapping("/listByAdmission")
    public Result<List<OperationApplyVO>> listByAdmission(@RequestParam Long admissionId) {
        return Result.success(operationApplyService.listByAdmission(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:surgery:add')")
    @Operation(summary = "发起/修改手术申请（返回手术申请单号；修改仅限「待排期」）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid OperationApplyUpsertDTO dto) {
        return Result.success("手术申请已提交（等待手术室排台）", operationApplyService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:surgery:edit')")
    @Operation(summary = "排台（待排期→已排期；已排期可改期；同手术间时段重叠会被拒）")
    @PostMapping("/schedule")
    public Result<Void> schedule(@RequestBody @Valid OperationScheduleDTO dto) {
        operationApplyService.schedule(dto);
        return Result.success("已排台", null);
    }

    @PreAuthorize("hasAuthority('ipd:surgery:edit')")
    @Operation(summary = "术前核对（已排期→术前核对完成；4 项必核项缺一不可）")
    @PostMapping("/preopCheck")
    public Result<Void> preopCheck(@RequestBody @Valid OperationPreopCheckDTO dto) {
        operationApplyService.preopCheck(dto);
        return Result.success("术前核对已完成", null);
    }

    @PreAuthorize("hasAuthority('ipd:surgery:edit')")
    @Operation(summary = "手术完成（回写病案首页手术明细 + 手术记录病历）")
    @PostMapping("/finish")
    public Result<Void> finish(@RequestBody @Valid OperationFinishDTO dto) {
        operationApplyService.finish(dto);
        return Result.success("手术已完成（已回写病案首页手术明细与手术记录病历）", null);
    }

    @PreAuthorize("hasAuthority('ipd:surgery:delete')")
    @Operation(summary = "取消手术（仅「待排期/已排期」可取消；已核对不可取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid OperationCancelDTO dto) {
        operationApplyService.cancel(dto);
        return Result.success("手术申请已取消", null);
    }

    @Operation(summary = "未完成手术数（工作台角标）")
    @GetMapping("/countUnfinished")
    public Result<Long> countUnfinished(@RequestParam(required = false) Long admissionId) {
        return Result.success(operationApplyService.countUnfinished(admissionId));
    }

    @Operation(summary = "排台总表（某天 × 手术间矩阵；date 为空按今天）")
    @GetMapping("/scheduleMatrix")
    public Result<OperationScheduleMatrixVO> scheduleMatrix(
            @RequestParam(required = false) String date) {
        return Result.success(operationApplyService.scheduleMatrix(date));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "已用过的手术间（下拉候选）")
    @GetMapping("/room/selectList")
    public Result<List<String>> selectRoomList() {
        return Result.success(operationApplyService.roomList());
    }

    @Operation(summary = "术前核对要点字典（前端渲染勾选框）")
    @GetMapping("/checkItemList")
    public Result<List<OperationApplyVO.CheckItem>> checkItemList() {
        return Result.success(operationApplyService.checkItems());
    }
}
