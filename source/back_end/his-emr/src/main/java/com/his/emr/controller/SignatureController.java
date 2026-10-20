package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.EmrSignatureService;
import com.his.emr.service.SignCertService;
import com.his.emr.service.SignatureCenterService;
import com.his.emr.service.TsaService;
import com.his.emr.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子签名与时间戳（P5.5）。
 */
@Tag(name = "电子签名与时间戳")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('sign:center:list', 'pharmacy:prescriptionAudit:list')")
public class SignatureController {

    private final EmrSignatureService emrSignatureService;
    private final SignCertService signCertService;
    private final TsaService tsaService;
    private final SignatureCenterService signatureCenterService;

    // 签名记录

    @Operation(summary = "签名记录分页（可按对象类型/场景/签名人/状态/验签结果/时间过滤）")
    @GetMapping("/emr/signature/listPage")
    public Result<PageResult<SignatureVO>> listPage(@Valid SignaturePageQueryDTO query) {
        var page = emrSignatureService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    /**
     * 单条签名详情：含被签内容快照全文、摘要、签名证书与时间戳来源。
     * 用于签名中心「签名记录」签点开一行后的「签名详情与验签」抽屉。
     */
    @Operation(summary = "签名详情（含被签内容快照全文）")
    @GetMapping("/emr/signature/getById")
    public Result<SignatureVO> getById(@RequestParam Long id) {
        return Result.success(emrSignatureService.getById(id));
    }

    /**
     * 一个业务对象的签名链：按签名序号升序列出全部签名，含已作废的历史签。
     * 供被签文书/处方回看「谁在何时签过、后续为何作废」的证据核查环节取数。
     */
    @Operation(summary = "某个对象的签名链（含已作废的，按 chain_no 升序）")
    @GetMapping("/emr/signature/listByBiz")
    public Result<List<SignatureVO>> listByBiz(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.success(emrSignatureService.listByBiz(bizType, bizId));
    }

    /**
     * 一个业务对象的签名情况：当前锚点状态、签名链，以及能否补签与不能补签的原因。
     * 供发起补签前先判断该对象还缺哪一环签名的核查节点使用。
     */
    @Operation(summary = "某对象的签名情况（当前锚点状态 / 签名链 / 能否补签及原因）")
    @GetMapping("/emr/signature/objectStatus")
    public Result<ObjectSignatureVO> objectStatus(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.success(emrSignatureService.objectStatus(bizType, bizId));
    }

    /**
     * 签名概览：有效/作废/未校验/验签失败的计数与各对象类型的签名覆盖率，并附时间来源与证书信任级别说明。
     * 用于签名中心页面顶部的概览卡片区。
     */
    @Operation(summary = "签名概览（有效/作废/未校验/验签失败 + 各类型覆盖率 + 时间来源与证书信任级别说明）")
    @GetMapping("/emr/signature/summary")
    public Result<SignatureSummaryVO> summary() {
        return Result.success(emrSignatureService.summary());
    }

    /**
     * 签名域下拉选项：对象类型、场景、签名状态、验签状态、时间来源等码值一次取回。
     * 用于签名中心查询卡与表格的码值翻译；审方等其他岗位同样要读，故只要求登录。
     */
    @Operation(summary = "下拉选项（对象类型/场景/签名状态/验签状态/时间来源）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/emr/signature/queryOptions")
    public Result<SignatureOptionsVO> queryOptions() {
        return Result.success(signatureCenterService.queryOptions());
    }

    /**
     * 单条验签：签名值能否验通、当前内容与签名时摘要是否一致，两个结论各自独立，并把核查结果回写留痕。
     * 用于「签名详情与验签」抽屉上的验签按钮。
     */
    @Operation(summary = "按 ID 验签（返回签名值校验与内容比对两个独立结论）")
    @PostMapping("/emr/signature/verify")
    public Result<SignVerifyVO> verify(@Valid @RequestBody SignatureVerifyDTO dto) {
        return Result.success(emrSignatureService.verify(dto.getSignId()));
    }

    /**
     * 按业务对象批量验签：一次核完该对象名下全部签名，含已作废的。
     * 用于门诊药房处方审核的「处方详情」抽屉里核对处方签名。
     */
    @Operation(summary = "按对象批量验签（含已作废签名）")
    @PostMapping("/emr/signature/verifyByBiz")
    public Result<List<SignVerifyVO>> verifyByBiz(@Valid @RequestBody SignatureVerifyByBizDTO dto) {
        return Result.success(emrSignatureService.verifyByBiz(dto.getBizType(), dto.getBizId()));
    }

    /**
     * 补签：管理员发起，签名人固定取当前登录用户，必须写明补签原因，场景默认记为补签。
     * 供签名核查发现缺签或原签名失效后追加一环签名的处理节点使用。
     */
    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "补签（管理员发起；签名人为当前登录用户，必须写明原因）")
    @PostMapping("/emr/signature/sign")
    public Result<SignatureVO> sign(@Valid @RequestBody SignatureSignDTO dto) {
        return Result.success(signatureCenterService.sign(dto));
    }

    /**
     * 作废签名：必须写理由；历史行不动，只追加作废信息并解除被签内容的锁定。
     * 用于「签名详情与验签」抽屉里的作废动作。
     */
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

    /**
     * 时间戳令牌台账分页：只增不改的签发流水，可按序列号精确查、按内容摘要关键字模糊查。
     * 用于签名中心「时间戳台账」签的表格。
     */
    @Operation(summary = "时间戳令牌台账分页（只增不改的签发流水，可按序列号精确查）")
    @GetMapping("/emr/tsa/tokenListPage")
    public Result<PageResult<TsaTokenVO>> tsaTokenListPage(@Valid TsaTokenPageQueryDTO query) {
        var page = tsaService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    /**
     * 时间戳服务启停：停用即不再签发新令牌，新签名自动降级用本机时钟，历史令牌仍可验；返回操作后的最新状态。
     * 用于「时间戳台账」签的服务开关，开关结果同屏刷新可用性。
     */
    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "TSA 服务启停（G6b；停用=不再签发新令牌，签名自动降级本机时钟，历史令牌仍可验）")
    @PostMapping("/emr/tsa/updateStatus")
    public Result<TsaStatusVO> tsaUpdateStatus(@Valid @RequestBody TsaStatusUpsertDTO dto) {
        return Result.success(tsaService.updateStatus(dto.getTsaStatus()));
    }

    /**
     * 切换签名时间来源：只允许本机时钟与可信时间戳两档，院内授时未实现会被拒；配置值与生效值可能不一致。
     * 用于「时间戳台账」签的时间来源切换，切完同屏对比配置值与生效值。
     */
    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "切换签名时间来源（G6b；只允许 1 本机时钟 / 3 可信时间戳，2 未实现拒绝）")
    @PostMapping("/emr/tsa/timeSource")
    public Result<TsaStatusVO> tsaTimeSource(@Valid @RequestBody TsaTimeSourceDTO dto) {
        return Result.success(tsaService.updateTimeSource(dto.getTimeSource()));
    }

    /**
     * 台账令牌复验：对一枚令牌重验签名值、摘要与时刻，只读不写留痕，不通过时给出人能看懂的原因。
     * 用于「时间戳台账」表格行上的复验动作。
     */
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

    /**
     * 证书机构外发探针：用一次性密钥对构造证书签名请求提交给签发适配器，只回显外发报文，不留任何库表痕迹。
     * 供对接外部证书机构前验证外发通道与报文形态的联调环节使用。
     */
    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "CA 外发探针（M8 留口子；用一次性密钥对构造 CSR 提交给适配器，控制台打印外发内容，不留任何库表痕迹）")
    @PostMapping("/emr/signCa/probeOutbound")
    public Result<SignCaProbeOutboundVO> signCaProbeOutbound() {
        return Result.success(signatureCenterService.probeCaOutbound());
    }

    // 证书
    @Operation(summary = "签名证书分页")
    @GetMapping("/emr/signCert/listPage")
    public Result<PageResult<SignCertVO>> certListPage(@Valid SignCertPageQueryDTO query) {
        var page = signCertService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    /**
     * 单张证书详情：含公钥 PEM 与指纹，可核对持有人、算法与有效期。
     * 用于签名中心「签名证书」签点开一张证书后的详情弹框。
     */
    @Operation(summary = "签名证书详情（含公钥 PEM 与指纹）")
    @GetMapping("/emr/signCert/getById")
    public Result<SignCertVO> certGetById(@RequestParam Long id) {
        return Result.success(signCertService.getById(id));
    }

    /**
     * 有效证书下拉：按员工姓名或证书号模糊筛选，只列当前可用的证书。
     * 供签名、审方等需要指定签名证书的环节选人用；证书是跨岗位参照数据，不配页面权限码。
     */
    @Operation(summary = "有效证书下拉（按员工姓名/证书号模糊查）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/emr/signCert/selectList")
    public Result<List<SignCertSelectListVO>> certSelectList(@RequestParam(required = false) String keyword) {
        // 只要求登录：证书是跨岗位参照数据（审方、签名中心等都会选），类级权限码会把其他岗位拦成 403
        return Result.success(signatureCenterService.certOptions(keyword));
    }

    /**
     * 人工签发证书：同一员工名下已有有效证书时直接拒绝，需先吊销旧证再发；返回新证书详情。
     * 用于「签名证书」签的签发弹框，签发人取当前登录用户。
     */
    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "人工签发证书（同一员工已有有效证书时拒绝，需先吊销）")
    @PostMapping("/emr/signCert/issue")
    public Result<SignCertVO> certIssue(@Valid @RequestBody SignCertIssueDTO dto) {
        return Result.success(signatureCenterService.issueCert(dto));
    }

    /**
     * 吊销证书：必须写理由；不删行，历史签名仍可拿它的公钥验签。
     * 用于「签名证书」签表格行上的吊销动作。
     */
    @PreAuthorize("hasAuthority('sign:center:edit')")
    @Operation(summary = "吊销证书（必须写理由；吊销不删行，历史签名仍可用其公钥验签）")
    @PostMapping("/emr/signCert/revoke")
    public Result<SignCertVO> certRevoke(@Valid @RequestBody SignCertRevokeDTO dto) {
        return Result.success(signatureCenterService.revokeCert(dto));
    }
}
