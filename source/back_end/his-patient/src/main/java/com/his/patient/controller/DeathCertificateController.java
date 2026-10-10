package com.his.patient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.patient.dto.DeathCertificateDTO;
import com.his.patient.dto.DeathRegistrationDTO;
import com.his.patient.service.DeathCertificateService;
import com.his.patient.service.DeathRegistrationService;
import com.his.patient.vo.DeathCertificateVO;
import com.his.patient.vo.DeathRegisterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 死亡证明与死亡登记
 */
@Tag(name = "死亡证明与死亡登记")
@RestController
@RequestMapping("/patient/death")
@RequiredArgsConstructor
public class DeathCertificateController {

    private final DeathCertificateService deathCertificateService;
    private final DeathRegistrationService deathRegistrationService;

    // 死亡证明

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "证明台账分页（含上报台账，overdue=1 只看逾期未报）")
    @PostMapping("/cert/listPage")
    public Result<PageResult<DeathCertificateVO.Row>> certListPage(@Valid @RequestBody DeathCertificateDTO.QueryPage dto) {
        return Result.success(deathCertificateService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "待开证榜（已办死亡离院但无有效证明，欠账榜）")
    @PostMapping("/cert/pendingListPage")
    public Result<PageResult<DeathCertificateVO.PendingRow>> pendingListPage(@Valid @RequestBody DeathCertificateDTO.QueryPage dto) {
        return Result.success(deathCertificateService.pendingListPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "证明详情＝编辑回显（一般项目明文 + 死因链 + 上报报文）")
    @GetMapping("/cert/getDetailById")
    public Result<DeathCertificateVO.Detail> certGetDetailById(@RequestParam Long id) {
        return Result.success(deathCertificateService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "开证底稿（按住院带出死者快照与死亡离院时间）")
    @GetMapping("/cert/admissionBase")
    public Result<DeathCertificateVO.PatientSnapshot> admissionBase(@RequestParam Long admissionId) {
        return Result.success(deathCertificateService.admissionBase(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "统计卡（四态 + 上报三态 + 待开证/待登记欠账）")
    @GetMapping("/cert/stats")
    public Result<DeathCertificateVO.Stats> stats() {
        return Result.success(deathCertificateService.stats());
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:add')")
    @Operation(summary = "填写/修改证明（草稿与已审核可改；死因链整体替换，根本死因服务端按链尾校验）")
    @PostMapping("/cert/upsert")
    public Result<String> certUpsert(@Valid @RequestBody DeathCertificateDTO.Upsert dto) {
        // 雪花 ID 19 位，裸 Long 出 JSON number 会在前端丢精度（回查即「不存在」），统一字符串出参
        return Result.success("证明已保存", String.valueOf(deathCertificateService.upsert(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:edit')")
    @Operation(summary = "审核（草稿→已审核）")
    @PostMapping("/cert/audit")
    public Result<Void> certAudit(@Valid @RequestBody DeathCertificateDTO.Audit dto) {
        deathCertificateService.audit(dto);
        return Result.success("审核通过", null);
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:edit')")
    @Operation(summary = "签发（已审核→已开具；须该住院已办死亡离院且死亡时间与出院同一时点）")
    @PostMapping("/cert/issue")
    public Result<Void> certIssue(@Valid @RequestBody DeathCertificateDTO.Issue dto) {
        deathCertificateService.issue(dto);
        return Result.success("已签发", null);
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:edit')")
    @Operation(summary = "作废（必填原因，内容不再可改）")
    @PostMapping("/cert/voidById")
    public Result<Void> certVoid(@Valid @RequestBody DeathCertificateDTO.VoidCert dto) {
        deathCertificateService.voidCert(dto);
        return Result.success("已作废", null);
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:edit')")
    @Operation(summary = "重开（按被作废原证复制新草稿，orig_cert_id 指向原证）")
    @PostMapping("/cert/reissue")
    public Result<String> certReissue(@RequestParam Long origCertId) {
        return Result.success("已按原证生成新草稿", String.valueOf(deathCertificateService.reissue(origCertId)));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:print')")
    @Operation(summary = "四联打印回执（打印一次计数一次，只有已开具能打印）")
    @PostMapping("/cert/print")
    public Result<Void> certPrint(@Valid @RequestBody DeathCertificateDTO.Print dto) {
        deathCertificateService.print(dto);
        return Result.success("已记录打印", null);
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:report')")
    @Operation(summary = "死因监测上报（组装标准报文落库；真实对接时此点替换为上报客户端）")
    @PostMapping("/cert/report")
    public Result<String> certReport(@RequestParam Long id) {
        return Result.success("上报完成", deathCertificateService.report(id));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:report')")
    @Operation(summary = "逾期催报（定时+手工补跑双路径，按天幂等；返回发送条数）")
    @PostMapping("/cert/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        return Result.success("催报完成", deathCertificateService.notifyOverdue());
    }

    // 死亡登记

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "死亡登记簿分页")
    @PostMapping("/register/listPage")
    public Result<PageResult<DeathRegisterVO.Row>> registerListPage(@Valid @RequestBody DeathRegistrationDTO.QueryPage dto) {
        return Result.success(deathRegistrationService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "登记详情＝编辑回显")
    @GetMapping("/register/getDetailById")
    public Result<DeathRegisterVO.Detail> registerGetDetailById(@RequestParam Long id) {
        return Result.success(deathRegistrationService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "登记底稿（按住院带出死者与有效证明摘要）")
    @GetMapping("/register/base")
    public Result<DeathRegisterVO.Base> registerBase(@RequestParam Long admissionId) {
        return Result.success(deathRegistrationService.base(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:deathCertificate:list')")
    @Operation(summary = "可登记候选（已办死亡离院的住院）")
    @GetMapping("/register/admissions")
    public Result<List<DeathRegisterVO.Base>> registerAdmissions(@RequestParam(required = false) String keyword,
                                                                 @RequestParam(required = false) Integer limit) {
        return Result.success(deathRegistrationService.admissionCandidates(keyword, limit));
    }

    @PreAuthorize("hasAuthority('ipd:deathRegister:add')")
    @Operation(summary = "填写/修改登记（草稿可改；已登记只能作废重登）")
    @PostMapping("/register/upsert")
    public Result<String> registerUpsert(@Valid @RequestBody DeathRegistrationDTO.Upsert dto) {
        return Result.success("登记已保存", String.valueOf(deathRegistrationService.upsert(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:deathRegister:edit')")
    @Operation(summary = "确认登记（非疾病死亡/死因不明必须已报公安，否则拒绝）")
    @PostMapping("/register/confirm")
    public Result<Void> registerConfirm(@Valid @RequestBody DeathRegistrationDTO.Confirm dto) {
        deathRegistrationService.confirm(dto);
        return Result.success("已登记", null);
    }

    @PreAuthorize("hasAuthority('ipd:deathRegister:edit')")
    @Operation(summary = "登记作废（必填原因，作废后可重登）")
    @PostMapping("/register/voidById")
    public Result<Void> registerVoid(@Valid @RequestBody DeathRegistrationDTO.VoidRegister dto) {
        deathRegistrationService.voidRegister(dto);
        return Result.success("已作废", null);
    }
}
