package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.dto.*;
import com.his.common.service.EmrSignatureService;
import com.his.common.service.SignCertService;
import com.his.common.service.TsaService;
import com.his.common.vo.*;
import com.his.emr.service.SignatureCenterService;
import com.his.emr.vo.SignCaProbeOutboundVO;
import com.his.emr.vo.SignCaStatusVO;
import com.his.emr.vo.SignCertSelectListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子签名与时间戳（P5.5）。
 *
 * <p><b>为什么控制器放在 his-emr 而不是 his-common</b>：
 * 能力（签名/验签/证书）在 his-common，业务接入在 his-patient 与 his-emr。
 * 控制器只暴露"签名中心"这一块运维/审计界面，属于病历管理域，放这里最自然；
 * his-common 保持"零 Controller、零业务表"的纯能力层定位。
 *
 * <p><b>读写分工</b>：查询一律 GET（可被看板轮询、可直接贴进浏览器核对数字）；
 * 只有「补签 / 验签 / 作废 / 签发证书 / 吊销证书」这类确实改变状态或产生留痕的用 POST。
 *
 * <p><b>签名人一律取当前登录用户</b>，不从入参接收 —— 允许前端指定签名人，
 * 等于把"谁签的"交给前端填，签名的不可否认性当场归零。
 */
@Tag(name = "电子签名与时间戳")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('sign:center:list', 'pharmacy:prescriptionAudit:list')")
public class EmrSignatureController {

    private final EmrSignatureService emrSignatureService;
    private final SignCertService signCertService;
    private final TsaService tsaService;
    private final SignatureCenterService signatureCenterService;

    // 签名记录

    @Operation(summary = "签名记录分页（可按对象类型/场景/签名人/状态/验签结果/时间过滤）")
    @GetMapping("/emr/signature/listPage")
    public Result<PageResult<SignatureVO>> listPage(@Valid SignatureQueryPageDTO query) {
        var page = emrSignatureService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    @Operation(summary = "签名详情（含被签内容快照全文）")
    @GetMapping("/emr/signature/getById")
    public Result<SignatureVO> getById(@RequestParam Long id) {
        return Result.success(emrSignatureService.getById(id));
    }

    @Operation(summary = "某个对象的签名链（含已作废的，按 chain_no 升序）")
    @GetMapping("/emr/signature/listByBiz")
    public Result<List<SignatureVO>> listByBiz(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.success(emrSignatureService.listByBiz(bizType, bizId));
    }

    @Operation(summary = "某对象的签名情况（当前锚点状态 / 签名链 / 能否补签及原因）")
    @GetMapping("/emr/signature/objectStatus")
    public Result<ObjectSignatureVO> objectStatus(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.success(emrSignatureService.objectStatus(bizType, bizId));
    }

    @Operation(summary = "签名概览（有效/作废/未校验/验签失败 + 各类型覆盖率 + 时间来源与证书信任级别说明）")
    @GetMapping("/emr/signature/summary")
    public Result<SignatureSummaryVO> summary() {
        return Result.success(emrSignatureService.summary());
    }

    @Operation(summary = "下拉选项（对象类型/场景/签名状态/验签状态/时间来源）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/emr/signature/queryOptions")
    public Result<SignatureOptionsVO> queryOptions() {
        return Result.success(signatureCenterService.queryOptions());
    }

    @Operation(summary = "按 ID 验签（返回签名值校验与内容比对两个独立结论）")
    @PostMapping("/emr/signature/verify")
    public Result<SignVerifyVO> verify(@Valid @RequestBody SignatureVerifyDTO dto) {
        return Result.success(emrSignatureService.verify(dto.getSignId()));
    }

    @Operation(summary = "按对象批量验签（含已作废签名）")
    @PostMapping("/emr/signature/verifyByBiz")
    public Result<List<SignVerifyVO>> verifyByBiz(@Valid @RequestBody SignatureVerifyByBizDTO dto) {
        return Result.success(emrSignatureService.verifyByBiz(dto.getBizType(), dto.getBizId()));
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "补签（管理员发起；签名人为当前登录用户，必须写明原因）")
    @PostMapping("/emr/signature/sign")
    public Result<SignatureVO> sign(@Valid @RequestBody SignatureSignDTO dto) {
        return Result.success(signatureCenterService.sign(dto));
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "作废签名（必须写理由；不改历史行，只追加作废信息并解除内容锁定）")
    @PostMapping("/emr/signature/invalidate")
    public Result<SignatureVO> invalidate(@Valid @RequestBody SignatureInvalidateDTO dto) {
        return Result.success(signatureCenterService.invalidate(dto));
    }

    // 可信时间戳（TSA）

    @Operation(summary = "TSA 服务状态（适配器在线 / 配置与生效的时间来源 / 台账计数；纯查询无副作用）")
    @GetMapping("/emr/tsa/status")
    public Result<TsaStatusVO> tsaStatus() {
        return Result.success(tsaService.status());
    }

    @Operation(summary = "时间戳令牌台账分页（只增不改的签发流水，可按序列号精确查）")
    @GetMapping("/emr/tsa/tokenListPage")
    public Result<PageResult<TsaTokenVO>> tsaTokenListPage(@Valid TsaTokenQueryPageDTO query) {
        var page = tsaService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "TSA 服务启停（G6b；停用=不再签发新令牌，签名自动降级本机时钟，历史令牌仍可验）")
    @PostMapping("/emr/tsa/updateStatus")
    public Result<TsaStatusVO> tsaUpdateStatus(@Valid @RequestBody TsaStatusUpsertDTO dto) {
        return Result.success(tsaService.updateStatus(dto.getTsaStatus()));
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "切换签名时间来源（G6b；只允许 1 本机时钟 / 3 可信时间戳，2 未实现拒绝）")
    @PostMapping("/emr/tsa/timeSource")
    public Result<TsaStatusVO> tsaTimeSource(@Valid @RequestBody TsaTimeSourceDTO dto) {
        return Result.success(tsaService.updateTimeSource(dto.getTimeSource()));
    }

    @Operation(summary = "时间戳令牌复验（G6b；对台账一枚令牌重验签名值/摘要/时刻，只读不落留痕）")
    @PostMapping("/emr/tsa/verifyToken")
    public Result<TsaTokenVerifyVO> tsaVerifyToken(@Valid @RequestBody TsaTokenVerifyDTO dto) {
        return Result.success(tsaService.verifyToken(dto.getId()));
    }

    // 证书签发信任根（M8 留口子：内部自签 / 外部 CA）

    @Operation(summary = "CA 签发模式状态（M8；internal=院内自签，external=外部CA适配器当前形态）")
    @GetMapping("/emr/signCa/status")
    public Result<SignCaStatusVO> signCaStatus() {
        return Result.success(signatureCenterService.signCaStatus());
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "CA 外发探针（M8 留口子；用一次性密钥对构造 CSR 提交给适配器，控制台打印外发内容，不留任何库表痕迹）")
    @PostMapping("/emr/signCa/probeOutbound")
    public Result<SignCaProbeOutboundVO> signCaProbeOutbound() {
        return Result.success(signatureCenterService.probeCaOutbound());
    }

    // 证书

    @Operation(summary = "签名证书分页")
    @GetMapping("/emr/signCert/listPage")
    public Result<PageResult<SignCertVO>> certListPage(@Valid SignCertQueryPageDTO query) {
        var page = signCertService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    @Operation(summary = "签名证书详情（含公钥 PEM 与指纹）")
    @GetMapping("/emr/signCert/getById")
    public Result<SignCertVO> certGetById(@RequestParam Long id) {
        return Result.success(signCertService.getById(id));
    }

    @Operation(summary = "有效证书下拉（按员工姓名/证书号模糊查）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/emr/signCert/selectList")
    public Result<List<SignCertSelectListVO>> certSelectList(@RequestParam(required = false) String keyword) {
        // 只要求登录：证书是跨岗位参照数据（审方、签名中心等都会选），类级权限码会把其他岗位拦成 403
        return Result.success(signatureCenterService.certOptions(keyword));
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "人工签发证书（同一员工已有有效证书时拒绝，需先吊销）")
    @PostMapping("/emr/signCert/issue")
    public Result<SignCertVO> certIssue(@Valid @RequestBody SignCertIssueDTO dto) {
        return Result.success(signatureCenterService.issueCert(dto));
    }

    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "吊销证书（必须写理由；吊销不删行，历史签名仍可用其公钥验签）")
    @PostMapping("/emr/signCert/revoke")
    public Result<SignCertVO> certRevoke(@Valid @RequestBody SignCertRevokeDTO dto) {
        return Result.success(signatureCenterService.revokeCert(dto));
    }
}
