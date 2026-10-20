package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.InfectiousReportDTO;
import com.his.emr.dto.InfectiousReportPageQueryDTO;
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

    /**
     * 传染病报告卡分页：按卡状态、疾病类别、是否超时、关键字与填报日期区间筛选。
     * 用于「传染病报告卡」页查询卡。
     */
    @Operation(summary = "报卡分页")
    @PostMapping("/listPage")
    public Result<PageResult<InfectiousReportVO.Row>> listPage(@Valid @RequestBody InfectiousReportPageQueryDTO dto) {
        return Result.success(infectiousReportService.page(dto));
    }

    /**
     * 单张报卡详情，含已组装的直报报文预览。
     * 用于报卡详情弹框，审核与直报前据此核对填报内容与报文。
     */
    @Operation(summary = "报卡详情（含直报报文）")
    @GetMapping("/getDetailById")
    public Result<InfectiousReportVO.Detail> getDetailById(@RequestParam Long id) {
        return Result.success(infectiousReportService.getDetailById(id));
    }

    /**
     * 法定传染病病种下拉（只给启用的目录项，支持关键字搜索）。
     * 随填卡表单打开而加载，属公共参照数据，故只要求登录不挂本页权限码。
     */
    @Operation(summary = "病种下拉（法定目录，启用项）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/disease/selectList")
    public Result<List<InfectiousReportVO.DiseaseSelectListVO>> selectList(@RequestParam(required = false) String keyword) {
        return Result.success(infectiousReportService.selectList(keyword));
    }

    /**
     * 报卡统计卡：四态计数 + 超时未报 + 今日新增 + 甲类在办。
     * 用于「传染病报告卡」页顶部统计，计数走后端聚合、翻页不变。
     */
    @Operation(summary = "统计卡（四态+超时未报+今日新增+甲类在办）")
    @GetMapping("/stats")
    public Result<InfectiousReportVO.Stats> stats() {
        return Result.success(infectiousReportService.stats());
    }

    /**
     * 填卡与修改报卡（同一入口）；退报后重新报卡也走这里，报卡次数由服务端递增。
     * 用于报告卡页的填卡弹窗，门诊病历诊断命中法定病种后由医生发起。
     */
    @PreAuthorize("hasAuthority('emr:infectiousReport:add')")
    @Operation(summary = "填卡/修改（退报重报同入口，服务端递增报卡次数）")
    @PostMapping("/upsert")
    public Result<InfectiousReportVO.Row> upsert(@Valid @RequestBody InfectiousReportDTO.Upsert dto) {
        Long id = infectiousReportService.upsert(dto);
        InfectiousReportVO.Detail d = infectiousReportService.getDetailById(id);
        return Result.success("报卡已保存", d.getCard());
    }

    /**
     * 报告卡审核通过，待审核卡进入可直报状态。
     * 用于报告卡页操作列的审核动作，是直报前的关口。
     */
    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "审核（1→2）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody InfectiousReportDTO.Audit dto) {
        infectiousReportService.audit(dto);
        return Result.success("审核通过", null);
    }

    /**
     * 退报打回报卡并必填退报原因，卡片退回后医生可修改重报。
     * 用于报告卡页操作列的退报动作（已直报的卡是终态，不许退）。
     */
    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "退报（1/2→4，原因必填；修改后可重报）")
    @PostMapping("/returnCard")
    public Result<Void> returnCard(@Valid @RequestBody InfectiousReportDTO.ReturnCard dto) {
        infectiousReportService.returnCard(dto);
        return Result.success("已退报", null);
    }

    /**
     * 直报：组装法定传染病标准报文并留痕，卡状态推进为已直报。
     * 用于报告卡页审核通过后的直报动作，返回报文内容供页面提示。
     */
    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "直报（2→3，组装标准报文落库；真实对接时此点替换为疾控 http 客户端）")
    @PostMapping("/directReport")
    public Result<String> directReport(@RequestParam Long id) {
        return Result.success("直报完成", infectiousReportService.directReport(id));
    }

    /**
     * 超时限催报：扫出过了法定报告时限仍未审核的卡发站内提醒，返回发送条数。
     * 报告卡超时提醒页面的补跑入口，同一张卡按天幂等，可反复点击不重复打扰。
     */
    @PreAuthorize("hasAuthority('emr:infectiousReport:edit')")
    @Operation(summary = "超时限催报（定时+手工补跑双路径，按天幂等；返回发送条数）")
    @PostMapping("/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        return Result.success(infectiousReportService.notifyOverdue());
    }
}
