package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.InfectiousReportDTO;
import com.his.emr.dto.InfectiousReportQueryPageDTO;
import com.his.emr.service.InfectiousReportService;
import com.his.emr.vo.InfectiousReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 传染病报告卡控制器（G11）。
 */
@Tag(name = "传染病报告卡")
@RestController
@RequestMapping("/emr/infectious")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('emr:infectiousReport:list', 'finance:insurance:list')")
public class InfectiousReportController {

    private final InfectiousReportService infectiousReportService;

    @Operation(summary = "报卡分页")
    @PostMapping("/listPage")
    public Result<PageResult<InfectiousReportVO.Row>> listPage(@Valid @RequestBody InfectiousReportQueryPageDTO dto) {
        return Result.success(infectiousReportService.page(dto));
    }

    @Operation(summary = "报卡详情（含直报报文）")
    @GetMapping("/getDetailById")
    public Result<InfectiousReportVO.Detail> getDetailById(@RequestParam Long id) {
        return Result.success(infectiousReportService.getDetailById(id));
    }

    @Operation(summary = "病种下拉（法定目录，启用项）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/disease/selectList")
    public Result<List<InfectiousReportVO.DiseaseSelectListVO>> diseaseSelectList(@RequestParam(required = false) String keyword) {
        return Result.success(infectiousReportService.diseaseSelectList(keyword));
    }

    @Operation(summary = "统计卡（四态+超时未报+今日新增+甲类在办）")
    @GetMapping("/stats")
    public Result<InfectiousReportVO.Stats> stats() {
        return Result.success(infectiousReportService.stats());
    }

    @PreAuthorize("hasAuthority('emr:infectiousReport:add')")
    @Operation(summary = "填卡/修改（退报重报同入口，服务端递增报卡次数）")
    @PostMapping("/upsert")
    public Result<InfectiousReportVO.Row> upsert(@Valid @RequestBody InfectiousReportDTO.Upsert dto) {
        Long id = infectiousReportService.upsert(dto);
        InfectiousReportVO.Detail d = infectiousReportService.getDetailById(id);
        return Result.success("报卡已保存", d.getCard());
    }

    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "审核（1→2）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody InfectiousReportDTO.Audit dto) {
        infectiousReportService.audit(dto);
        return Result.success("审核通过", null);
    }

    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "退报（1/2→4，原因必填；修改后可重报）")
    @PostMapping("/returnCard")
    public Result<Void> returnCard(@Valid @RequestBody InfectiousReportDTO.ReturnCard dto) {
        infectiousReportService.returnCard(dto);
        return Result.success("已退报", null);
    }

    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "直报（2→3，组装标准报文落库；真实对接时此点替换为疾控 http 客户端）")
    @PostMapping("/directReport")
    public Result<String> directReport(@RequestParam Long id) {
        return Result.success("直报完成", infectiousReportService.directReport(id));
    }

    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "超时限催报（定时+手工补跑双路径，按天幂等；返回发送条数）")
    @PostMapping("/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        return Result.success(infectiousReportService.notifyOverdue());
    }
}
