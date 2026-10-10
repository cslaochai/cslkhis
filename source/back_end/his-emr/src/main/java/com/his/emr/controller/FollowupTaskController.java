package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.FollowupTaskService;
import com.his.emr.vo.BizFollowupTaskVO;
import com.his.emr.vo.FollowupStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 随访任务控制器。
 */
@Tag(name = "随访任务")
@RestController
@RequestMapping("/charge/followup")
@RequiredArgsConstructor
public class FollowupTaskController {

    private final FollowupTaskService followupTaskService;

    @PreAuthorize("hasAuthority('inpatient:followup:list')")
    @Operation(summary = "分页查询随访任务（科室按登录岗位收口）")
    @PostMapping("/listPage")
    public Result<PageResult<BizFollowupTaskVO>> listPage(@Valid @RequestBody FollowupQueryDTO queryDTO) {
        return Result.success(followupTaskService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('inpatient:followup:list')")
    @Operation(summary = "出院随访任务看板（今日应访/逾期/完成率/科室待办，服务端聚合）")
    @GetMapping("/stat")
    public Result<FollowupStatVO> stat() {
        return Result.success(followupTaskService.stat());
    }

    @PreAuthorize("hasAuthority('inpatient:followup:add')")
    @Operation(summary = "新建 / 修改随访任务（修改仅待随访可改）")
    @PostMapping("/upsert")
    public Result<BizFollowupTaskVO> upsert(@Valid @RequestBody FollowupTaskDTO.Upsert dto) {
        return Result.success("随访任务已保存", followupTaskService.upsertTask(dto));
    }

    @PreAuthorize("hasAuthority('inpatient:followup:add')")
    @Operation(summary = "按出院记录一键生成随访计划（幂等）")
    @PostMapping("/createFromDischarge")
    public Result<BizFollowupTaskVO> createFromDischarge(@Valid @RequestBody FollowupTaskDTO.FromDischarge dto) {
        return Result.success("随访计划已生成", followupTaskService.createTaskFromDischarge(dto));
    }

    @PreAuthorize("hasAuthority('inpatient:followup:list')")
    @Operation(summary = "获取随访任务详情（编辑回显，返回明文手机号）")
    @GetMapping("/getById")
    public Result<BizFollowupTaskVO> getById(@RequestParam Long id) {
        return Result.success(followupTaskService.getFollowupTaskDetail(id));
    }

    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "开始随访")
    @PostMapping("/startFollowup")
    public Result<Void> startFollowup(@Valid @RequestBody FollowupStartDTO actionDTO) {
        followupTaskService.startFollowup(actionDTO.getId(), actionDTO.getExecutorId(), actionDTO.getExecutorName());
        return Result.success("开始随访", null);
    }

    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "完成随访（同时自动发放满意度问卷）")
    @PostMapping("/completeFollowup")
    public Result<Void> completeFollowup(@Valid @RequestBody FollowupCompleteDTO actionDTO) {
        followupTaskService.completeFollowup(actionDTO.getId(), actionDTO.getResult());
        return Result.success("随访完成", null);
    }

    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "由随访任务生成复诊号（复诊来源 4-随访计划复诊）")
    @PostMapping("/createRevisitAppoint")
    public Result<BizFollowupTaskVO> createRevisitAppoint(@Valid @RequestBody FollowupTaskDTO.CreateRevisit dto) {
        return Result.success("复诊号已生成", followupTaskService.createRevisitAppoint(dto));
    }

    @PreAuthorize("hasAuthority('inpatient:followup:delete')")
    @Operation(summary = "取消随访")
    @PostMapping("/cancelFollowup")
    public Result<Void> cancelFollowup(@Valid @RequestBody FollowupCancelDTO actionDTO) {
        followupTaskService.cancelFollowup(actionDTO.getId(), actionDTO.getReason());
        return Result.success("已取消", null);
    }

    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "登记电话外呼（返回明文电话供拨号；mock 通道=人工登记待呼）")
    @PostMapping("/callRegister")
    public Result<BizFollowupTaskVO> callRegister(@Valid @RequestBody FollowupCallRegisterDTO dto) {
        return Result.success("外呼已登记", followupTaskService.registerCall(dto.getId()));
    }

    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "回填电话外呼结果（接通且任务待随访时自动转随访中）")
    @PostMapping("/callResult")
    public Result<Void> callResult(@Valid @RequestBody FollowupCallResultDTO dto) {
        followupTaskService.recordCallResult(dto);
        return Result.success("外呼结果已回填", null);
    }
}
