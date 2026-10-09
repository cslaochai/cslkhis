package com.his.appoint.controller;

import com.his.appoint.dto.*;
import com.his.appoint.service.BizAppointService;
import com.his.appoint.vo.AppointStatusCountVO;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.appoint.vo.RevisitFeePreviewVO;
import com.his.appoint.vo.RevisitRecordSelectVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 挂号管理控制器
 */
@Tag(name = "挂号管理")
@RestController
@RequestMapping("/appoint")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:appointments:list', 'opd:doctorWorkstation:list', 'opd:todayVisits:list')")
public class BizAppointController {

    private final BizAppointService bizAppointService;

    @Operation(summary = "分页查询挂号记录")
    @GetMapping("/listPage")
    public Result<PageResult<BizAppointInfoListVO>> listPage(@Valid AppointQueryDTO queryDTO) {
        PageResult<BizAppointInfoListVO> result = bizAppointService.listPage(queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "挂号状态统计：六格状态卡")
    @GetMapping("/statusCount")
    public Result<AppointStatusCountVO> statusCount(@Valid AppointQueryDTO queryDTO) {
        return Result.success(bizAppointService.statusCount(queryDTO));
    }

    @Operation(summary = "预约看板：一次取整段区间的全部挂号")
    @PostMapping("/boardList")
    public Result<List<BizAppointInfoListVO>> boardList(@Valid @RequestBody AppointBoardQueryDTO queryDTO) {
        return Result.success(bizAppointService.boardList(queryDTO));
    }

    @PreAuthorize("hasAuthority('opd:appointments:add')")
    @Operation(summary = "患者挂号或编辑挂号（新增/修改合一）")
    @PostMapping("/appointUpsert")
    public Result<BizAppointInfoListVO> appointUpsert(@Valid @RequestBody AppointUpsertDTO upsertDTO) {
        return Result.success(upsertDTO.getId() == null ? "挂号成功" : "修改成功", bizAppointService.appointUpsert(upsertDTO));
    }

    /**
     * 医生站建复诊号（只放来源 1-当日回诊、2-医嘱复诊预约）。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "医生站建复诊号（当日回诊 / 医嘱复诊预约）")
    @PostMapping("/revisitUpsert")
    public Result<BizAppointInfoListVO> revisitUpsert(@Valid @RequestBody AppointUpsertDTO upsertDTO) {
        return Result.success("复诊号已创建", bizAppointService.revisitUpsert(upsertDTO));
    }

    /**
     * 复诊费用预估
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "复诊费用预估：提交挂号前问一次这张号收多少钱")
    @PostMapping("/revisitFeePreview")
    public Result<RevisitFeePreviewVO> revisitFeePreview(@RequestBody @Valid RevisitFeePreviewDTO previewDTO) {
        return Result.success(bizAppointService.revisitFeePreviewScoped(previewDTO));
    }

    /**
     * 复诊「原病历」候选列表（窗口/医生站共用）。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "复诊原病历候选列表（按就诊日倒序）")
    @GetMapping("/revisitRecordSelectList")
    public Result<List<RevisitRecordSelectVO>> revisitRecordSelectList(@RequestParam Long patientId) {
        return Result.success(bizAppointService.revisitRecordSelectListScoped(patientId));
    }

    @PreAuthorize("hasAuthority('opd:appointments:delete')")
    @Operation(summary = "退号")
    @PostMapping("/cancelRegist")
    public Result<Void> cancelRegist(@RequestBody @Valid AppointCancelDTO appointCancelDTO) {
        bizAppointService.cancelRegist(appointCancelDTO);
        return Result.success();
    }

    @Operation(summary = "获取挂号详情")
    @GetMapping("/getDetail")
    public Result<BizAppointInfoListVO> getDetail(@Valid AppointQueryDTO appointQueryDTO) {
        return Result.success(bizAppointService.getDetail(appointQueryDTO));
    }

    @PreAuthorize("hasAuthority('opd:appointments:edit')")
    @Operation(summary = "更新挂号状态")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody AppointStatusUpsertDTO appointStatusDTO) {
        boolean success = bizAppointService.updateStatus(appointStatusDTO.getRegistId(), appointStatusDTO.getStatus());
        return success ? Result.success() : Result.error("更新状态失败");
    }

}
