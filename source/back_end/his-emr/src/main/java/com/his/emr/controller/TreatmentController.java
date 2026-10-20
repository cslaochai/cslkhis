package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.TreatmentDTO;
import com.his.emr.service.TreatmentService;
import com.his.emr.vo.TreatmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门诊治疗站控制器：治疗疗程开单、按次打卡执行与计费补记的接口入口。
 */
@Tag(name = "门诊治疗站")
@RestController
@RequestMapping("/emr/treatment")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:treatmentStation:list')")
public class TreatmentController {

    private final TreatmentService treatmentService;

    // 疗程（申请单）

    @PreAuthorize("hasAuthority('opd:treatmentStation:add')")
    @Operation(summary = "分页查询治疗申请（疗程）")
    @PostMapping("/applyListPage")
    public Result<PageResult<TreatmentVO.ApplyVO>> applyListPage(@Valid @RequestBody TreatmentDTO.ApplyQuery query) {
        return Result.success(treatmentService.listPageApplies(query));
    }

    /**
     * 疗程详情：一次开单的项目、排期进度连同全部按次执行流水。
     * 用于「治疗开单」页签疗程列表行点开详情弹框时取数。
     */
    @Operation(summary = "疗程详情（含全部按次流水）")
    @GetMapping("/getDetailById")
    public Result<TreatmentVO.ApplyDetailVO> getDetailById(@RequestParam Long applyId) {
        return Result.success(treatmentService.getDetail(applyId));
    }

    /**
     * 治疗项目下拉：按关键字取可开的治疗项目及其单次价格。
     * 用于门诊治疗站开单弹框选项目，临床路径模板配置治疗节点时同源取数。
     */
    @Operation(summary = "治疗项目下拉（开单选项目用，带单价）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/item/selectList")
    public Result<List<TreatmentVO.ItemSelectListVO>> itemSelectList(@RequestParam(required = false) String keyword,
                                                                     @RequestParam(required = false) Integer limit) {
        return Result.success(treatmentService.itemSelectList(keyword, limit));
    }

    /**
     * 新开疗程或整体重排疗程：按次数与间隔天数一次落完整排期，已经打过卡的疗程不许重排。
     * 用于门诊治疗站开单与重排弹框保存时提交。
     */
    @PreAuthorize("hasAuthority('opd:treatmentStation:add')")
    @Operation(summary = "开单/重排疗程（仅在一次卡都没打过时允许重排）")
    @PostMapping("/applyUpsert")
    public Result<TreatmentVO.ApplyDetailVO> applyUpsert(@Valid @RequestBody TreatmentDTO.ApplyUpsertDTO dto) {
        TreatmentVO.ApplyDetailVO detail = treatmentService.upsertApply(dto);
        return Result.success("疗程已排期：" + detail.getApply().getItemName()
                + " 共 " + detail.getApply().getTotalTimes() + " 次", detail);
    }

    /**
     * 取消疗程：连带取消尚未执行的排期次，已经收过的费不在这里退。
     * 用于「治疗开单」页签疗程列表行的取消操作，患者停止后续治疗时登记原因后提交。
     */
    @PreAuthorize("hasAuthority('opd:treatmentStation:delete')")
    @Operation(summary = "取消疗程（连带取消未执行的次数，不退已计费的账）")
    @PostMapping("/cancelApply")
    public Result<Integer> cancelApply(@Valid @RequestBody TreatmentDTO.ApplyCancel dto) {
        int n = treatmentService.cancelApply(dto);
        return Result.success("疗程已取消，连带取消未执行 " + n + " 次", n);
    }

    /**
     * 删除疗程：连排期行一起清掉，只允许一次都没执行过的疗程。
     * 用于「治疗开单」页签疗程列表行的删除操作，误开且未执行时收回。
     */
    @PreAuthorize("hasAuthority('opd:treatmentStation:delete')")
    @Operation(summary = "删除疗程（仅一次都未执行时允许）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long applyId) {
        treatmentService.deleteApply(applyId);
        return Result.success("疗程及其排期已删除", true);
    }

    // 按次流水

    @Operation(summary = "分页查询按次执行流水（治疗台/台账共用）")
    @PostMapping("/execListPage")
    public Result<PageResult<TreatmentVO.ExecVO>> execListPage(@Valid @RequestBody TreatmentDTO.ExecQuery query) {
        return Result.success(treatmentService.listPageExecs(query));
    }

    /**
     * 按次流水的状态分布计数：与流水列表同一筛选口径，只统计被筛掉的那一维。
     * 用于门诊治疗站治疗台与执行计费台账流水表上方的状态筛选片。
     */
    @Operation(summary = "流水状态分布（与分页同口径，但不带上被统计的那一维）")
    @PostMapping("/execStatusCount")
    public Result<List<TreatmentVO.StatusCountVO>> execStatusCount(@Valid @RequestBody TreatmentDTO.ExecQuery query) {
        return Result.success(treatmentService.statusCount(query));
    }

    /**
     * 治疗台看板汇总：在办疗程数、今日应做与已做、逾期未打卡次数与今日治疗费合计。
     * 用于「今日治疗台」页签顶部统计卡。
     */
    @Operation(summary = "治疗台看板统计")
    @GetMapping("/stats")
    public Result<TreatmentVO.StatsVO> stats() {
        return Result.success(treatmentService.stats());
    }

    /**
     * 单次改期：只挪还没打的那一次排期到指定日期，已执行的历史日期不动。
     * 用于「今日治疗台」流水行的改期弹框，患者临时有事顺延后续治疗时提交。
     */
    @PreAuthorize("hasAuthority('opd:treatmentStation:edit')")
    @Operation(summary = "单次改期（只挪未执行的那一次）")
    @PostMapping("/execReschedule")
    public Result<TreatmentVO.ExecVO> execReschedule(@Valid @RequestBody TreatmentDTO.ExecReschedule dto) {
        TreatmentVO.ExecVO vo = treatmentService.rescheduleExec(dto);
        return Result.success("第 " + vo.getExecSeq() + " 次计划日期已改为 " + vo.getPlanDate(), vo);
    }

    /**
     * 按次打卡：确认这一次治疗做完，随后按次记账；记账失败只留提示，打卡本身不回滚。
     * 用于治疗台或台账流水行的执行弹框确认时提交，同一疗程必须按次序打。
     */
    @PreAuthorize("hasAuthority('opd:treatmentStation:edit')")
    @Operation(summary = "按次打卡（打卡成功即按次计费；计费失败只留痕，不回滚打卡）")
    @PostMapping("/execExecute")
    public Result<TreatmentVO.ExecVO> execExecute(@Valid @RequestBody TreatmentDTO.ExecExecute dto) {
        TreatmentVO.ExecVO vo = treatmentService.executeExec(dto);
        return Result.success(vo.getChargeNotice(), vo);
    }

    /**
     * 计费补记：给已打卡却没记上账的那一次重试一次记账。
     * 用于「今日治疗台」与「执行与计费台账」流水行的补记按钮；已经记上账的再补会被拒绝，不出双份账。
     */
    @PreAuthorize("hasAuthority('opd:treatmentStation:edit')")
    @Operation(summary = "计费补记（已执行但没记上账的行重试一次）")
    @PostMapping("/execRetryCharge")
    public Result<TreatmentVO.ExecVO> execRetryCharge(@Valid @RequestBody TreatmentDTO.ExecRetryCharge dto) {
        TreatmentVO.ExecVO vo = treatmentService.retryCharge(dto.getRecordId());
        return Result.success(vo.getChargeNotice(), vo);
    }
}
