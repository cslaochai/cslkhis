package com.his.report.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.report.dto.StatReportDTO;
import com.his.report.service.StatReportService;
import com.his.report.vo.StatReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病案统计上报控制器（L9 打印预留）。
 *
 * <p>generate 从住院数据聚合卫统风格 JSON 报文落库留痕；submit 即"上报埋点"——
 * 现在只冻结留痕并支持打印，真实对接时把这一步换成 http 上报。
 */
@Tag(name = "病案统计上报（打印预留）")
@RestController
@RequestMapping("/report/statReport")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:statReport:list')")
public class StatReportController {

    private final StatReportService statReportService;

    @Operation(summary = "生成上报报文（按期间聚合并落库留痕，不对外发送）")
    @PreAuthorize("hasAuthority('report:statReport:add')")
    @PostMapping("/generate")
    public Result<StatReportVO.Detail> generate(@Valid @RequestBody StatReportDTO.Generate dto) {
        return Result.success("上报报文已生成（打印预留，尚未报出）", statReportService.generate(dto));
    }

    @Operation(summary = "报出（打印预留：冻结报文留痕，真实对接时此处换为 http 上报）")
    @PreAuthorize("hasAuthority('report:statReport:submit')")
    @PostMapping("/submit")
    public Result<StatReportVO.Row> submit(@Valid @RequestBody StatReportDTO.Submit dto) {
        return Result.success("已报出（打印预留：报文冻结留痕，未对接外部平台）",
                statReportService.submit(dto.getId()));
    }

    @Operation(summary = "作废（释放同期槽位，报文留痕不删）")
    @PreAuthorize("hasAuthority('report:statReport:void')")
    @PostMapping("/void")
    public Result<StatReportVO.Row> voidReport(@Valid @RequestBody StatReportDTO.VoidReq dto) {
        return Result.success("台账已作废", statReportService.voidReport(dto.getId(), dto.getReason()));
    }

    @Operation(summary = "上报台账分页（不含报文大字段）")
    @PostMapping("/listPage")
    public Result<PageResult<StatReportVO.Row>> listPage(@RequestBody StatReportDTO.QueryPage dto) {
        var page = statReportService.listPage(dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "台账明细（含报文原文，预览/打印用）")
    @GetMapping("/getDetailById")
    public Result<StatReportVO.Detail> getDetailById(@RequestParam Long id) {
        return Result.success(statReportService.getDetailById(id));
    }
}
