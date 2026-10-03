package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.EcgAuditDTO;
import com.his.medicaltech.dto.EcgCollectWaveDTO;
import com.his.medicaltech.dto.EcgHolterUpsertDTO;
import com.his.medicaltech.dto.EcgMeasureUpsertDTO;
import com.his.medicaltech.dto.EcgQueryPageDTO;
import com.his.medicaltech.dto.EcgReportUpsertDTO;
import com.his.medicaltech.dto.EcgSimulateDTO;
import com.his.medicaltech.dto.EcgTemplateUpsertDTO;
import com.his.medicaltech.service.EcgService;
import com.his.medicaltech.vo.EcgDetailVO;
import com.his.medicaltech.vo.EcgListVO;
import com.his.medicaltech.vo.EcgTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心电工作站（sql/173，菜单 417）。
 *
 * <p>权限口径与「分岗」直接对应（与放射 sql/138 同款）：
 * <ul>
 *   <li>进页面（listPage/getDetail）→ {@code medtech:ecg:list}；</li>
 *   <li>签到/波形采集（checkIn/collectWave/simulateWave）→ {@code medtech:ecg:collect}
 *       —— 采集是技师岗；</li>
 *   <li>测量/Holter 分析/写报告（saveMeasure/saveHolter/saveDraft/submit）→ {@code medtech:ecg:write}
 *       —— 测量参数与 Holter 分析是报告的证据链，随报告权限一并放给书写岗；</li>
 *   <li>审核/退回（audit/reject）→ {@code medtech:ecg:audit}；</li>
 *   <li>发布（publish）→ {@code medtech:ecg:publish}。</li>
 * </ul>
 * 按钮码只是第一道门，服务端另有硬闸门（item_type=3 / 波形存在 / Holter 已分析 /
 * 不能自审 / 发布必须已审核），见 {@code EcgServiceImpl}。
 */
@Tag(name = "心电工作站")
@RestController
@RequestMapping("/medicaltech/ecg")
@RequiredArgsConstructor
public class EcgController {

    private final EcgService ecgService;

    // 工作台

    @Operation(summary = "工作台分页列表（检查记录 + 波形/测量/Holter/报告，未采集未书写的也在）")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('medtech:ecg:list')")
    public Result<PageResult<EcgListVO>> listPage(@Valid @RequestBody EcgQueryPageDTO query) {
        return Result.success(ecgService.listPage(query));
    }

    @Operation(summary = "按检查记录取详情（含波形 JSON，还没采集时也能取）")
    @GetMapping("/getDetailByRecordId")
    @PreAuthorize("hasAuthority('medtech:ecg:list')")
    public Result<EcgDetailVO> getDetailByRecordId(@RequestParam Long recordId) {
        return Result.success(ecgService.getDetailByRecordId(recordId));
    }

    // 采集（技师岗）

    @Operation(summary = "签到（已登记 → 已签到；心电检查的排队叫号入口）")
    @PostMapping("/checkIn")
    @PreAuthorize("hasAuthority('medtech:ecg:collect')")
    public Result<EcgDetailVO> checkIn(@RequestParam Long recordId) {
        return Result.success("已签到", ecgService.checkIn(recordId));
    }

    @Operation(summary = "波形采集（设备推送路径）：waveData JSON 落库并置记录为已出结果")
    @PostMapping("/collectWave")
    @PreAuthorize("hasAuthority('medtech:ecg:collect')")
    public Result<EcgDetailVO> collectWave(@Valid @RequestBody EcgCollectWaveDTO dto) {
        return Result.success("波形已入库", ecgService.collectWave(dto));
    }

    @Operation(summary = "模拟采集（联调/演示路径）：服务端按节律合成 12 导联波形")
    @PostMapping("/simulateWave")
    @PreAuthorize("hasAuthority('medtech:ecg:collect')")
    public Result<EcgDetailVO> simulateWave(@Valid @RequestBody EcgSimulateDTO dto) {
        return Result.success("波形已入库", ecgService.simulateWave(dto));
    }

    // 书写（医师岗）

    @Operation(summary = "保存测量参数（心率/PR/QRS/QT/电轴等，upsert）")
    @PostMapping("/saveMeasure")
    @PreAuthorize("hasAuthority('medtech:ecg:write')")
    public Result<EcgDetailVO> saveMeasure(@Valid @RequestBody EcgMeasureUpsertDTO dto) {
        return Result.success("测量参数已保存", ecgService.saveMeasure(dto));
    }

    @Operation(summary = "保存 Holter 分析（心搏统计/心律失常事件/小时心率，upsert）")
    @PostMapping("/saveHolter")
    @PreAuthorize("hasAuthority('medtech:ecg:write')")
    public Result<EcgDetailVO> saveHolter(@Valid @RequestBody EcgHolterUpsertDTO dto) {
        return Result.success("Holter 分析已保存", ecgService.saveHolter(dto));
    }

    @Operation(summary = "保存报告草稿")
    @PostMapping("/saveDraft")
    @PreAuthorize("hasAuthority('medtech:ecg:write')")
    public Result<EcgDetailVO> saveDraft(@Valid @RequestBody EcgReportUpsertDTO dto) {
        return Result.success("草稿已保存", ecgService.saveDraft(dto));
    }

    @Operation(summary = "提交报告待审核（前置：已有波形；Holter 还需已分析；同时完成报告医师签名）")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('medtech:ecg:write')")
    public Result<EcgDetailVO> submit(@Valid @RequestBody EcgReportUpsertDTO dto) {
        return Result.success("已提交审核", ecgService.submit(dto));
    }

    @Operation(summary = "审核通过（不能自审；同时完成审核医师签名）")
    @PostMapping("/audit")
    @PreAuthorize("hasAuthority('medtech:ecg:audit')")
    public Result<EcgDetailVO> audit(@Valid @RequestBody EcgAuditDTO dto) {
        return Result.success("审核通过", ecgService.audit(dto));
    }

    @Operation(summary = "退回重写（原因必填，报告回到草稿并清签名）")
    @PostMapping("/reject")
    @PreAuthorize("hasAuthority('medtech:ecg:audit')")
    public Result<EcgDetailVO> reject(@Valid @RequestBody EcgAuditDTO dto) {
        return Result.success("已退回，报告回到草稿", ecgService.reject(dto));
    }

    @Operation(summary = "发布报告（只有已审核的能发布，发布后推送申请医师）")
    @PostMapping("/publish")
    @PreAuthorize("hasAuthority('medtech:ecg:publish')")
    public Result<EcgDetailVO> publish(@RequestParam Long reportId) {
        return Result.success("报告已发布", ecgService.publish(reportId));
    }

    // 报告模板

    @Operation(summary = "模板下拉（书写弹框用；按心电类型过滤，通用模板任何类型都能选）")
    @GetMapping("/template/selectList")
    @PreAuthorize("hasAuthority('medtech:ecg:list')")
    public Result<List<EcgTemplateVO>> templateSelectList(@RequestParam(required = false) Integer ecgType) {
        return Result.success(ecgService.templateSelectList(ecgType));
    }

    @Operation(summary = "模板列表（维护用，含停用）")
    @GetMapping("/template/list")
    @PreAuthorize("hasAuthority('medtech:ecg:tplEdit')")
    public Result<List<EcgTemplateVO>> templateList() {
        return Result.success(ecgService.templateList());
    }

    @Operation(summary = "新增/修改模板")
    @PostMapping("/template/upsert")
    @PreAuthorize("hasAuthority('medtech:ecg:tplEdit')")
    public Result<EcgTemplateVO> templateUpsert(@Valid @RequestBody EcgTemplateUpsertDTO dto) {
        return Result.success("已保存", ecgService.templateUpsert(dto));
    }

    @Operation(summary = "删除模板（物理删：uk_ecg_tpl_code 不含 del_flag，软删会让同编码再也建不出来）")
    @DeleteMapping("/template/deleteById")
    @PreAuthorize("hasAuthority('medtech:ecg:tplEdit')")
    public Result<Void> templateDeleteById(@RequestParam Long id) {
        boolean ok = ecgService.templateDeleteById(id);
        return ok ? Result.success("已删除", null) : Result.error("模板不存在或已被删除");
    }
}
