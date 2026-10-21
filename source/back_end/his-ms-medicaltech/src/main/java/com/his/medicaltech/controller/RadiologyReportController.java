package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.RadioReportAuditDTO;
import com.his.medicaltech.dto.RadioReportPageQueryDTO;
import com.his.medicaltech.dto.RadioReportUpsertDTO;
import com.his.medicaltech.service.RadiologyReportService;
import com.his.medicaltech.vo.RadioReportDetailVO;
import com.his.medicaltech.vo.RadioReportListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 放射诊断报告控制器：放射诊断工作站报告书写、审核与发布链的接口入口。
 */
@Tag(name = "放射诊断工作站")
@RestController
@RequestMapping("/medicaltech/radiology/report")
@RequiredArgsConstructor
public class RadiologyReportController {

    private final RadiologyReportService radiologyReportService;

    /**
     * 放射报告工作台分页：一次检查一行，尚未出报告的记录也在列，凭报告有无区分待书写。
     * 用于放射诊断工作站页待书写/待审核/已审核/已发布四栏查询卡。
     */
    @Operation(summary = "报告工作台分页列表（检查记录 + 其报告，未写报告的也在）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<PageResult<RadioReportListVO>> listPage(@Valid @RequestBody RadioReportPageQueryDTO query) {
        return Result.success(radiologyReportService.listPage(query));
    }

    /**
     * 按检查记录读取报告全量字段：报告尚未书写时同样可取，带回检查申请信息。
     * 用于放射诊断工作站打开书写台或审核弹框时的数据加载。
     */
    @Operation(summary = "按检查记录取报告详情（还没写报告时也能取，用来打开书写台）")
    @GetMapping("/getDetailByRecordId")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<RadioReportDetailVO> getDetailByRecordId(@RequestParam Long recordId) {
        return Result.success(radiologyReportService.getDetailByRecordId(recordId));
    }

    /**
     * 按报告标识回读已出报告的全量字段。
     * 供放射诊断环节复核、重打等按报告取数的出口。
     */
    @Operation(summary = "按报告ID取报告详情")
    @GetMapping("/getDetailByReportId")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<RadioReportDetailVO> getDetailByReportId(@RequestParam Long reportId) {
        return Result.success(radiologyReportService.getDetailByReportId(reportId));
    }

    /**
     * 落存报告所见与结论为草稿：不签名、不推进状态，可反复修改。
     * 用于诊断医师在书写台弹框边看图边记录时暂存。
     */
    @Operation(summary = "保存报告草稿")
    @PostMapping("/saveDraft")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:write')")
    public Result<RadioReportDetailVO> saveDraft(@Valid @RequestBody RadioReportUpsertDTO dto) {
        return Result.success("草稿已保存", radiologyReportService.saveDraft(dto));
    }

    /**
     * 报告从草稿送审：随提交完成报告医师签名，之后进入待审核。
     * 用于书写台弹框书写完毕后的送审动作。
     */
    @Operation(summary = "提交报告待审核（同时完成报告医师签名）")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:write')")
    public Result<RadioReportDetailVO> submit(@Valid @RequestBody RadioReportUpsertDTO dto) {
        return Result.success("已提交审核", radiologyReportService.submit(dto));
    }

    /**
     * 复核报告并加盖审核医师签名，报告状态推进为已审核。
     * 用于放射诊断工作站审核弹框的同意操作。
     */
    @Operation(summary = "审核通过（同时完成审核医师签名）")
    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:audit')")
    public Result<RadioReportDetailVO> audit(@Valid @RequestBody RadioReportAuditDTO dto) {
        return Result.success("审核通过", radiologyReportService.audit(dto));
    }

    /**
     * 将待审核报告打回草稿并可重新签名，退回原因必传。
     * 用于放射诊断工作站审核弹框驳回书写医师修改的操作。
     */
    @Operation(summary = "退回重写（原因必填，报告回到草稿并允许重新签名）")
    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:audit')")
    public Result<RadioReportDetailVO> reject(@Valid @RequestBody RadioReportAuditDTO dto) {
        return Result.success("已退回，报告回到草稿", radiologyReportService.reject(dto));
    }

    /**
     * 将已审核报告置为发布态，报告链路收口。
     * 用于放射诊断工作站对已审核行执行发布。
     */
    @Operation(summary = "发布报告（只有已审核的能发布）")
    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:publish')")
    public Result<RadioReportDetailVO> publish(@RequestParam Long reportId) {
        return Result.success("报告已发布", radiologyReportService.publish(reportId));
    }
}
