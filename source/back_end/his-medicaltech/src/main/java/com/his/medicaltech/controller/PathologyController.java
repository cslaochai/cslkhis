package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.PathologyDTO;
import com.his.medicaltech.service.PathologyService;
import com.his.medicaltech.vo.PathologyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病理管理控制器：病理单登记、标本接收、蜡块流转与初诊-审核-发布这条病理亚专业链的接口入口。
 */
@Tag(name = "病理管理")
@RestController
@RequestMapping("/medicaltech/pathology")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:pathology:list')")
public class PathologyController {

    private final PathologyService pathologyService;

    /**
     * 病理单分页。
     * 用于病理管理页的病理单查询表格。
     */
    @Operation(summary = "分页查询病理单")
    @PostMapping("/listPage")
    public Result<PageResult<PathologyVO.ListVO>> listPage(@Valid @RequestBody PathologyDTO.Query query) {
        return Result.success(pathologyService.pageVO(query));
    }

    /**
     * 病理工作量统计：待接收、处理中、待审核、已发布与今日冰冻的单据数。
     * 用于病理管理页顶部的统计卡。
     */
    @Operation(summary = "病理统计（待接收/处理中/待审核/已发布/今日冰冻）")
    @GetMapping("/stats")
    public Result<PathologyVO.StatsVO> stats() {
        return Result.success(pathologyService.stats());
    }

    /**
     * 病理单详情，含名下蜡块明细。
     * 用于病理管理页的详情弹框与初诊弹框回显。
     */
    @Operation(summary = "病理单详情（含蜡块明细）")
    @GetMapping("/getDetailById")
    public Result<PathologyVO.DetailVO> getDetailById(@RequestParam Long orderId) {
        return Result.success(pathologyService.getDetail(orderId));
    }

    /**
     * 新增或修改病理单，仅已登记状态可改。
     * 用于病理管理页的病理单登记与修改。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:add')")
    @Operation(summary = "新增/修改病理单（仅已登记可改）")
    @PostMapping("/orderUpsert")
    public Result<PathologyVO.DetailVO> orderUpsert(@Valid @RequestBody PathologyDTO.OrderUpsert dto) {
        PathologyVO.DetailVO vo = pathologyService.upsertOrder(dto);
        return Result.success("病理单 " + vo.getOrderNo() + " 已保存", vo);
    }

    /**
     * 接收送检标本，登记接收信息并把病理单推进到已接收。
     * 用于病理管理页待接收标本的接收动作。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "标本接收")
    @PostMapping("/receive")
    public Result<PathologyVO.DetailVO> receive(@Valid @RequestBody PathologyDTO.Receive dto) {
        pathologyService.receive(dto);
        return Result.success("标本已接收", pathologyService.getDetail(dto.getOrderId()));
    }

    /**
     * 推进病理主单流程到已取材或已制片。
     * 供大体取材与制片环节回写主单状态。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "主单流程推进（3 已取材 / 4 已制片）")
    @PostMapping("/process")
    public Result<PathologyVO.DetailVO> process(@Valid @RequestBody PathologyDTO.Process dto) {
        pathologyService.process(dto);
        return Result.success("流程已推进", pathologyService.getDetail(dto.getOrderId()));
    }

    /**
     * 为病理单登记蜡块。
     * 用于病理管理页的蜡块补录。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:add')")
    @Operation(summary = "登记蜡块")
    @PostMapping("/blockUpsert")
    public Result<PathologyVO.DetailVO> blockUpsert(@Valid @RequestBody PathologyDTO.BlockUpsert dto) {
        pathologyService.addBlock(dto);
        return Result.success("蜡块已登记", pathologyService.getDetail(dto.getOrderId()));
    }

    /**
     * 推进单个蜡块的流转状态（取材、包埋、切片）。
     * 用于病理单详情弹框内蜡块行的流转操作。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "蜡块流转（1 取材 2 包埋 3 切片）")
    @PostMapping("/blockAction")
    public Result<PathologyVO.DetailVO> blockAction(@Valid @RequestBody PathologyDTO.BlockAction dto) {
        Long orderId = pathologyService.blockAction(dto);
        return Result.success("蜡块状态已更新", pathologyService.getDetail(orderId));
    }

    /**
     * 提交病理初诊：填写镜下所见与病理诊断。
     * 用于病理管理页的初诊报告录入。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "初诊（填写镜下所见与病理诊断）")
    @PostMapping("/report")
    public Result<PathologyVO.DetailVO> report(@Valid @RequestBody PathologyDTO.Report dto) {
        pathologyService.report(dto);
        return Result.success("初诊已提交", pathologyService.getDetail(dto.getOrderId()));
    }

    /**
     * 审核病理初诊，审核人不得是初诊人本人。
     * 用于病理管理页初诊完成后的上级审核。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "审核（审核人不得是初诊人本人）")
    @PostMapping("/audit")
    public Result<PathologyVO.DetailVO> audit(@Valid @RequestBody PathologyDTO.Audit dto) {
        pathologyService.audit(dto);
        return Result.success("审核通过", pathologyService.getDetail(dto.getOrderId()));
    }

    /**
     * 发布已审核的病理报告，使其对临床可见。
     * 用于病理管理页审核通过后的发布动作。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "发布报告（已审核 → 已发布）")
    @PostMapping("/publish")
    public Result<PathologyVO.DetailVO> publish(@Valid @RequestBody PathologyDTO.Publish dto) {
        pathologyService.publish(dto.getOrderId());
        return Result.success("报告已发布", pathologyService.getDetail(dto.getOrderId()));
    }

    /**
     * 取消病理单，已发布的报告不可取消。
     * 用于病理管理页作废录错或重复的病理单。
     */
    @PreAuthorize("hasAuthority('medtech:pathology:delete')")
    @Operation(summary = "取消病理单（已发布不可取消）")
    @PostMapping("/cancel")
    public Result<PathologyVO.DetailVO> cancel(@Valid @RequestBody PathologyDTO.Cancel dto) {
        pathologyService.cancel(dto);
        return Result.success("病理单已取消", pathologyService.getDetail(dto.getOrderId()));
    }
}
