package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.StatReportDTO;
import com.his.medicaltech.service.StatReportService;
import com.his.medicaltech.vo.StatReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病案统计上报控制器：上报台账生成、报出、作废与查询的接口入口；当前为打印预留形态，报文只留痕不外发。
 */
@Tag(name = "病案统计上报（打印预留）")
@RestController
@RequestMapping("/report/statReport")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:statReport:list')")
public class StatReportController {

    private final StatReportService statReportService;

    /**
     * 按期间聚合数据生成上报报文并落库留痕，不对外发送。
     * 用于病案统计上报页对某统计期间发起生成。
     */
    @Operation(summary = "生成上报报文（按期间聚合并落库留痕，不对外发送）")
    @PreAuthorize("hasAuthority('report:statReport:add')")
    @PostMapping("/generate")
    public Result<StatReportVO.Detail> generate(@Valid @RequestBody StatReportDTO.Generate dto) {
        return Result.success("上报报文已生成（打印预留，尚未报出）", statReportService.generate(dto));
    }

    /**
     * 将生成的报文冻结留痕完成报出登记；对接外部平台时此环节改为真实上报。
     * 用于病案统计上报页对已生成待报的台账执行报出。
     */
    @Operation(summary = "报出（打印预留：冻结报文留痕，真实对接时此处换为 http 上报）")
    @PreAuthorize("hasAuthority('report:statReport:submit')")
    @PostMapping("/submit")
    public Result<StatReportVO.Row> submit(@Valid @RequestBody StatReportDTO.Submit dto) {
        return Result.success("已报出（打印预留：报文冻结留痕，未对接外部平台）",
                statReportService.submit(dto.getId()));
    }

    /**
     * 作废上报台账并释放该期间的上报名额，报文内容保留不删。
     * 用于病案统计上报页对报错台账发起作废后重新生成。
     */
    @Operation(summary = "作废（释放同期槽位，报文留痕不删）")
    @PreAuthorize("hasAuthority('report:statReport:void')")
    @PostMapping("/void")
    public Result<StatReportVO.Row> voidReport(@Valid @RequestBody StatReportDTO.VoidReq dto) {
        return Result.success("台账已作废", statReportService.voidReport(dto.getId(), dto.getReason()));
    }

    /**
     * 上报台账分页列表：轻量行，不带报文原文大字段。
     * 用于病案统计上报页查询卡。
     */
    @Operation(summary = "上报台账分页（不含报文大字段）")
    @PostMapping("/listPage")
    public Result<PageResult<StatReportVO.Row>> listPage(@Valid @RequestBody StatReportDTO.PageQuery dto) {
        var page = statReportService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 台账全量明细，含报文原文。
     * 用于上报台账预览与打印前核对报文内容。
     */
    @Operation(summary = "台账明细（含报文原文，预览/打印用）")
    @GetMapping("/getDetailById")
    public Result<StatReportVO.Detail> getDetailById(@RequestParam Long id) {
        return Result.success(statReportService.getDetailById(id));
    }
}
