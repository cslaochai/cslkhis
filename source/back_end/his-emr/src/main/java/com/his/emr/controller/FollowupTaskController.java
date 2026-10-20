package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.FollowupTaskService;
import com.his.spi.vo.BizFollowupTaskVO;
import com.his.emr.vo.FollowupStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 出院随访任务控制器：随访计划的建单与生成、看板统计、随访执行、复诊号落地与电话外呼的接口入口。
 */
@Tag(name = "随访任务")
@RestController
@RequestMapping("/charge/followup")
@RequiredArgsConstructor
public class FollowupTaskController {

    private final FollowupTaskService followupTaskService;

    /**
     * 分页查询随访任务，科室范围按登录岗位收口。
     * 用于「出院随访」页随访任务列表的查询卡。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:list')")
    @Operation(summary = "分页查询随访任务（科室按登录岗位收口）")
    @PostMapping("/listPage")
    public Result<PageResult<BizFollowupTaskVO>> listPage(@Valid @RequestBody FollowupQueryDTO queryDTO) {
        return Result.success(followupTaskService.listPage(queryDTO));
    }

    /**
     * 汇总今日应访、逾期、完成率与科室待办的随访看板数字。
     * 用于「出院随访」页顶部指标卡与统计页签；逾期按应访时间现算，不受当前页筛选影响。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:list')")
    @Operation(summary = "出院随访任务看板（今日应访/逾期/完成率/科室待办，服务端聚合）")
    @GetMapping("/stat")
    public Result<FollowupStatVO> stat() {
        return Result.success(followupTaskService.stat());
    }

    /**
     * 新建或修改一条随访任务。
     * 用于「出院随访」页的「新建随访」与行内编辑弹框；已进入随访及之后的任务不许再改。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:add')")
    @Operation(summary = "新建 / 修改随访任务（修改仅待随访可改）")
    @PostMapping("/upsert")
    public Result<BizFollowupTaskVO> upsert(@Valid @RequestBody FollowupTaskDTO.Upsert dto) {
        return Result.success("随访任务已保存", followupTaskService.upsertTask(dto));
    }

    /**
     * 依据一张出院记录一键生成随访计划。
     * 用于「出院随访」页的「按出院记录生成」弹框；同一出院记录重复提交只得到原任务。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:add')")
    @Operation(summary = "按出院记录一键生成随访计划（幂等）")
    @PostMapping("/createFromDischarge")
    public Result<BizFollowupTaskVO> createFromDischarge(@Valid @RequestBody FollowupTaskDTO.FromDischarge dto) {
        return Result.success("随访计划已生成", followupTaskService.createTaskFromDischarge(dto));
    }

    /**
     * 按任务主键取随访任务全量信息，手机号为明文。
     * 用于「出院随访」页的详情弹框与编辑弹框回显；明文只服务表单回填，列表展示仍走脱敏字段。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:list')")
    @Operation(summary = "获取随访任务详情（编辑回显，返回明文手机号）")
    @GetMapping("/getById")
    public Result<BizFollowupTaskVO> getById(@RequestParam Long id) {
        return Result.success(followupTaskService.getFollowupTaskDetail(id));
    }

    /**
     * 把一条待随访任务置为随访中并记录执行人。
     * 用于「出院随访」页列表行的「开始随访」。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "开始随访")
    @PostMapping("/startFollowup")
    public Result<Void> startFollowup(@Valid @RequestBody FollowupStartDTO actionDTO) {
        followupTaskService.startFollowup(actionDTO.getId(), actionDTO.getExecutorId(), actionDTO.getExecutorName());
        return Result.success("开始随访", null);
    }

    /**
     * 登记随访结果并结束任务，同时按启用中的问卷发放满意度评价单。
     * 用于「出院随访」页的「完成随访」弹框。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "完成随访（同时自动发放满意度问卷）")
    @PostMapping("/completeFollowup")
    public Result<Void> completeFollowup(@Valid @RequestBody FollowupCompleteDTO actionDTO) {
        followupTaskService.completeFollowup(actionDTO.getId(), actionDTO.getResult());
        return Result.success("随访完成", null);
    }

    /**
     * 由随访任务挂一个复诊号，走与门诊加号同一条占号与收费链路。
     * 用于「出院随访」页的复诊登记动作；一条任务只允许挂一个有效复诊号。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "由随访任务生成复诊号（复诊来源 4-随访计划复诊）")
    @PostMapping("/createRevisitAppoint")
    public Result<BizFollowupTaskVO> createRevisitAppoint(@Valid @RequestBody FollowupTaskDTO.CreateRevisit dto) {
        return Result.success("复诊号已生成", followupTaskService.createRevisitAppoint(dto));
    }

    /**
     * 作废一条尚未完成的随访任务并记录取消原因。
     * 用于「出院随访」页列表行的「取消随访」；已取消任务不计入完成率分母。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:delete')")
    @Operation(summary = "取消随访")
    @PostMapping("/cancelFollowup")
    public Result<Void> cancelFollowup(@Valid @RequestBody FollowupCancelDTO actionDTO) {
        followupTaskService.cancelFollowup(actionDTO.getId(), actionDTO.getReason());
        return Result.success("已取消", null);
    }

    /**
     * 登记一次电话外呼并回带患者明文联系电话供拨号。
     * 用于「出院随访」页的外呼弹框打开时刻；外呼通道未接通前由随访员人工登记。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "登记电话外呼（返回明文电话供拨号；mock 通道=人工登记待呼）")
    @PostMapping("/callRegister")
    public Result<BizFollowupTaskVO> callRegister(@Valid @RequestBody FollowupCallRegisterDTO dto) {
        return Result.success("外呼已登记", followupTaskService.registerCall(dto.getId()));
    }

    /**
     * 回填外呼的接通情况与备注。
     * 用于随访员在「出院随访」外呼弹框挂机后的提交；接通且任务仍待随访时任务转为随访中。
     */
    @PreAuthorize("hasAuthority('inpatient:followup:edit')")
    @Operation(summary = "回填电话外呼结果（接通且任务待随访时自动转随访中）")
    @PostMapping("/callResult")
    public Result<Void> callResult(@Valid @RequestBody FollowupCallResultDTO dto) {
        followupTaskService.recordCallResult(dto);
        return Result.success("外呼结果已回填", null);
    }
}
