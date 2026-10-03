package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.RadioReportAuditDTO;
import com.his.medicaltech.dto.RadioReportQueryPageDTO;
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
 * 放射诊断工作站 · 报告书写台（sql/138，菜单 414）。
 *
 * <p>权限口径与「分岗」直接对应：
 * <ul>
 *   <li>进页面（list/getDetail）→ {@code medtech:radioDiagnosis:list}：检查技师没有这个码，
 *       所以他在侧边栏根本看不到这个页面；</li>
 *   <li>写报告（saveDraft/submit）→ {@code medtech:radioDiagnosis:write}；</li>
 *   <li>审核/退回（audit/reject）→ {@code medtech:radioDiagnosis:audit}；</li>
 *   <li>发布（publish）→ {@code medtech:radioDiagnosis:publish}。</li>
 * </ul>
 * 按钮码只是第一道门，服务端另有三道硬闸门（是不是放射项目 / 报告是否已发布 / 不能自审），
 * 见 {@code RadiologyReportServiceImpl} —— 光靠前端藏按钮的分岗不是分岗。
 */
@Tag(name = "放射诊断工作站")
@RestController
@RequestMapping("/medicaltech/radiology/report")
@RequiredArgsConstructor
public class RadiologyReportController {

    private final RadiologyReportService radiologyReportService;

    @Operation(summary = "报告工作台分页列表（检查记录 + 其报告，未写报告的也在）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<PageResult<RadioReportListVO>> listPage(@Valid @RequestBody RadioReportQueryPageDTO query) {
        return Result.success(radiologyReportService.listPage(query));
    }

    @Operation(summary = "按检查记录取报告详情（还没写报告时也能取，用来打开书写台）")
    @GetMapping("/getDetailByRecordId")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<RadioReportDetailVO> getDetailByRecordId(@RequestParam Long recordId) {
        return Result.success(radiologyReportService.getDetailByRecordId(recordId));
    }

    @Operation(summary = "按报告ID取报告详情")
    @GetMapping("/getDetailByReportId")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:list')")
    public Result<RadioReportDetailVO> getDetailByReportId(@RequestParam Long reportId) {
        return Result.success(radiologyReportService.getDetailByReportId(reportId));
    }

    @Operation(summary = "保存报告草稿")
    @PostMapping("/saveDraft")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:write')")
    public Result<RadioReportDetailVO> saveDraft(@Valid @RequestBody RadioReportUpsertDTO dto) {
        return Result.success("草稿已保存", radiologyReportService.saveDraft(dto));
    }

    @Operation(summary = "提交报告待审核（同时完成报告医师签名）")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:write')")
    public Result<RadioReportDetailVO> submit(@Valid @RequestBody RadioReportUpsertDTO dto) {
        return Result.success("已提交审核", radiologyReportService.submit(dto));
    }

    @Operation(summary = "审核通过（同时完成审核医师签名）")
    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:audit')")
    public Result<RadioReportDetailVO> audit(@Valid @RequestBody RadioReportAuditDTO dto) {
        return Result.success("审核通过", radiologyReportService.audit(dto));
    }

    @Operation(summary = "退回重写（原因必填，报告回到草稿并允许重新签名）")
    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:audit')")
    public Result<RadioReportDetailVO> reject(@Valid @RequestBody RadioReportAuditDTO dto) {
        return Result.success("已退回，报告回到草稿", radiologyReportService.reject(dto));
    }

    @Operation(summary = "发布报告（只有已审核的能发布）")
    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('medtech:radioDiagnosis:publish')")
    public Result<RadioReportDetailVO> publish(@RequestParam Long reportId) {
        return Result.success("报告已发布", radiologyReportService.publish(reportId));
    }
}
