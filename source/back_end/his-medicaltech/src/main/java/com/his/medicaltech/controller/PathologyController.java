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
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 病理亚专业接口
 *
 * <p>URL 前缀 {@code /medicaltech/pathology}。所有状态流转校验在 PathologyService，
 * 这里只做入参校验与 VO 装配；操作人一律服务端取 {@code UserUtils.getCurrentUser()}，
 * **不接受前端传操作人姓名**（传了就等于谁都能替别人签名）。
 */
@Tag(name = "病理管理")
@RestController
@RequestMapping("/medicaltech/pathology")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:pathology:list')")
public class PathologyController {

    private final PathologyService pathologyService;

    @Operation(summary = "分页查询病理单")
    @PostMapping("/listPage")
    public Result<PageResult<PathologyVO.ListVO>> listPage(@RequestBody PathologyDTO.Query query) {
        return Result.success(pathologyService.pageVO(query));
    }

    @Operation(summary = "病理统计（待接收/处理中/待审核/已发布/今日冰冻）")
    @GetMapping("/stats")
    public Result<PathologyVO.StatsVO> stats() {
        return Result.success(pathologyService.stats());
    }

    @Operation(summary = "病理单详情（含蜡块明细）")
    @GetMapping("/getDetailById")
    public Result<PathologyVO.DetailVO> getDetailById(@RequestParam Long orderId) {
        return Result.success(pathologyService.getDetail(orderId));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:add')")
    @Operation(summary = "新增/修改病理单（仅已登记可改）")
    @PostMapping("/orderUpsert")
    public Result<PathologyVO.DetailVO> orderUpsert(@Valid @RequestBody PathologyDTO.OrderUpsert dto) {
        PathologyVO.DetailVO vo = pathologyService.upsertOrder(dto);
        return Result.success("病理单 " + vo.getOrderNo() + " 已保存", vo);
    }

    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "标本接收")
    @PostMapping("/receive")
    public Result<PathologyVO.DetailVO> receive(@Valid @RequestBody PathologyDTO.Receive dto) {
        pathologyService.receive(dto);
        return Result.success("标本已接收", pathologyService.getDetail(dto.getOrderId()));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "主单流程推进（3 已取材 / 4 已制片）")
    @PostMapping("/process")
    public Result<PathologyVO.DetailVO> process(@Valid @RequestBody PathologyDTO.Process dto) {
        pathologyService.process(dto);
        return Result.success("流程已推进", pathologyService.getDetail(dto.getOrderId()));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:add')")
    @Operation(summary = "登记蜡块")
    @PostMapping("/blockUpsert")
    public Result<PathologyVO.DetailVO> blockUpsert(@Valid @RequestBody PathologyDTO.BlockUpsert dto) {
        pathologyService.addBlock(dto);
        return Result.success("蜡块已登记", pathologyService.getDetail(dto.getOrderId()));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "蜡块流转（1 取材 2 包埋 3 切片）")
    @PostMapping("/blockAction")
    public Result<PathologyVO.DetailVO> blockAction(@Valid @RequestBody PathologyDTO.BlockAction dto) {
        Long orderId = pathologyService.blockAction(dto);
        return Result.success("蜡块状态已更新", pathologyService.getDetail(orderId));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "初诊（填写镜下所见与病理诊断）")
    @PostMapping("/report")
    public Result<PathologyVO.DetailVO> report(@Valid @RequestBody PathologyDTO.Report dto) {
        pathologyService.report(dto);
        return Result.success("初诊已提交", pathologyService.getDetail(dto.getOrderId()));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "审核（审核人不得是初诊人本人）")
    @PostMapping("/audit")
    public Result<PathologyVO.DetailVO> audit(@Valid @RequestBody PathologyDTO.Audit dto) {
        pathologyService.audit(dto);
        return Result.success("审核通过", pathologyService.getDetail(dto.getOrderId()));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:edit')")
    @Operation(summary = "发布报告（已审核 → 已发布）")
    @PostMapping("/publish")
    public Result<PathologyVO.DetailVO> publish(@Valid @RequestBody PathologyDTO.Publish dto) {
        pathologyService.publish(dto.getOrderId());
        return Result.success("报告已发布", pathologyService.getDetail(dto.getOrderId()));
    }

    @PreAuthorize("hasAuthority('medtech:pathology:delete')")
    @Operation(summary = "取消病理单（已发布不可取消）")
    @PostMapping("/cancel")
    public Result<PathologyVO.DetailVO> cancel(@Valid @RequestBody PathologyDTO.Cancel dto) {
        pathologyService.cancel(dto);
        return Result.success("病理单已取消", pathologyService.getDetail(dto.getOrderId()));
    }
}
