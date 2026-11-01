package com.his.clinical.controller;

import com.his.clinical.dto.*;
import com.his.clinical.dto.SignaturePageQueryDTO;
import com.his.clinical.service.EmrSignatureService;
import com.his.clinical.service.SignatureCenterService;
import com.his.clinical.vo.MyPendingSignVO;
import com.his.clinical.vo.ObjectSignatureVO;
import com.his.clinical.vo.SignatureOptionsVO;
import com.his.clinical.vo.SignatureSummaryVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.utils.UserUtils;
import com.his.spi.dto.*;
import com.his.spi.service.SignCertFacadeService;
import com.his.spi.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子签名（P5.5）。
 */
@Tag(name = "电子签名")
@RestController
@RequiredArgsConstructor
public class SignatureController {

    private final EmrSignatureService emrSignatureService;
    private final SignatureCenterService signatureCenterService;
    private final SignCertFacadeService signCertFacadeService;

    // 签名记录

    @Operation(summary = "签名记录分页（可按对象类型/场景/签名人/状态/验签结果/时间过滤）")
    @PreAuthorize("hasAnyAuthority('sign:verify:list', 'sign:audit:list', 'pharmacy:prescriptionAudit:list')")
    @GetMapping("/emr/signature/listPage")
    public Result<PageResult<SignatureVO>> listPage(@Valid SignaturePageQueryDTO query) {
        var page = emrSignatureService.listPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    /**
     * 单条签名详情：含被签内容快照全文、摘要与签名证书。
     * 用于签名验签页「签名记录」签点开一行后的「签名详情与验签」抽屉。
     */
    @Operation(summary = "签名详情（含被签内容快照全文）")
    @PreAuthorize("hasAnyAuthority('sign:verify:list', 'sign:audit:list', 'pharmacy:prescriptionAudit:list')")
    @GetMapping("/emr/signature/getById")
    public Result<SignatureVO> getById(@RequestParam Long id) {
        return Result.success(emrSignatureService.getById(id));
    }

    /**
     * 一个业务对象的签名链：按签名序号升序列出全部签名，含已作废的历史签。
     * 供被签文书/处方回看「谁在何时签过、后续为何作废」的证据核查环节取数。
     */
    @Operation(summary = "某个对象的签名链（含已作废的，按 chain_no 升序）")
    @PreAuthorize("hasAnyAuthority('sign:verify:list', 'sign:audit:list', 'pharmacy:prescriptionAudit:list')")
    @GetMapping("/emr/signature/listByBiz")
    public Result<List<SignatureVO>> listByBiz(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.success(emrSignatureService.listByBiz(bizType, bizId));
    }

    /**
     * 一个业务对象的签名情况：当前锚点状态、签名链，以及能否补签与不能补签的原因。
     * 供发起补签前先判断该对象还缺哪一环签名的核查节点使用。
     */
    @Operation(summary = "某对象的签名情况（当前锚点状态 / 签名链 / 能否补签及原因）")
    @PreAuthorize("hasAnyAuthority('sign:verify:list', 'sign:audit:list', 'pharmacy:prescriptionAudit:list')")
    @GetMapping("/emr/signature/objectStatus")
    public Result<ObjectSignatureVO> objectStatus(@RequestParam Integer bizType, @RequestParam Long bizId) {
        return Result.success(emrSignatureService.objectStatus(bizType, bizId));
    }

    /**
     * 签名概览：有效/作废/未校验/验签失败的计数与各对象类型的签名覆盖率，并附证书信任级别说明。
     * 用于签名验签页顶部的概览卡片区。
     */
    @Operation(summary = "签名概览（有效/作废/未校验/验签失败 + 各类型覆盖率）")
    @PreAuthorize("hasAuthority('sign:verify:list')")
    @GetMapping("/emr/signature/summary")
    public Result<SignatureSummaryVO> summary() {
        return Result.success(emrSignatureService.summary());
    }

    /**
     * 签名域下拉选项：对象类型、场景、签名状态、验签状态等码值一次取回。
     * 用于签名验签查询卡与表格的码值翻译；审方等其他岗位同样要读，故只要求登录。
     */
    @Operation(summary = "下拉选项（对象类型/场景/签名状态/验签状态）")
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
    @PreAuthorize("hasAuthority('sign:verify:edit')")
    @PostMapping("/emr/signature/verify")
    public Result<SignVerifyVO> verify(@Valid @RequestBody SignatureVerifyDTO dto) {
        return Result.success(emrSignatureService.verify(dto.getSignId()));
    }

    /**
     * 按业务对象批量验签：一次核完该对象名下全部签名，含已作废的。
     * 用于门诊药房处方审核的「处方详情」抽屉里核对处方签名。
     */
    @Operation(summary = "按对象批量验签（含已作废签名）")
    @PreAuthorize("hasAnyAuthority('sign:verify:edit', 'pharmacy:prescriptionAudit:list')")
    @PostMapping("/emr/signature/verifyByBiz")
    public Result<List<SignVerifyVO>> verifyByBiz(@Valid @RequestBody SignatureVerifyByBizDTO dto) {
        return Result.success(emrSignatureService.verifyByBiz(dto.getBizType(), dto.getBizId()));
    }

    /**
     * 补签：管理员发起，签名人固定取当前登录用户，必须写明补签原因，场景默认记为补签。
     * 供签名核查发现缺签或原签名失效后追加一环签名的处理节点使用。
     */
    @PreAuthorize("hasAuthority('sign:verify:edit')")
    @Operation(summary = "补签（管理员发起；签名人为当前登录用户，必须写明原因）")
    @PostMapping("/emr/signature/sign")
    public Result<SignatureVO> sign(@Valid @RequestBody SignatureSignDTO dto) {
        return Result.success(signatureCenterService.sign(dto, dto.getPin()));
    }

    /**
     * 医生「我的待补签」：只列当前登录医生本人书写、尚未电子签名的门诊病历/处方/检查检验申请单。
     * <p>只要求登录不收员工ID：医生岗位拿不到签名核查的管理岗权限码，自助入口必须是本人接口。
     */
    @Operation(summary = "我的待补签（仅当前登录医生本人的未签对象）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/emr/signature/myPending")
    public Result<List<MyPendingSignVO>> myPending() {
        return Result.success(signatureCenterService.listMyPendingSigns());
    }

    /**
     * 医生自助补签：签名人强制为当前登录医生本人，对象必须属于本人，必须写补签原因并输入签名 PIN；
     * 成功与失败都写审计日志。
     */
    @Operation(summary = "医生自助补签（限本人对象；必须写原因 + 签名PIN）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/emr/signature/myMakeup")
    public Result<SignatureVO> myMakeup(@Valid @RequestBody MyMakeupSignDTO dto) {
        return Result.success(signatureCenterService.makeupSign(dto));
    }

    /**
     * 作废签名：必须写理由；历史行不动，只追加作废信息并解除被签内容的锁定。
     * 用于「签名详情与验签」抽屉里的作废动作。
     */
    @PreAuthorize("hasAuthority('sign:verify:edit')")
    @Operation(summary = "作废签名（必须写理由；不改历史行，只追加作废信息并解除内容锁定）")
    @PostMapping("/emr/signature/invalidate")
    public Result<SignatureVO> invalidate(@Valid @RequestBody SignatureInvalidateDTO dto) {
        return Result.success(signatureCenterService.invalidate(dto));
    }

    // ---------- 签名证书（CA 在线申请：生成密钥对 → 自动提交 → 签发后自动回填；可转线下） ----------

    /**
     * CA 通道状态：在线申请是否接入、模板与算法是否匹配；未接入时提示线下兜底。
     */
    @Operation(summary = "签名 CA 通道状态（在线申请接入自检）")
    @PreAuthorize("hasAnyAuthority('sign:cert:list', 'sign:cert:edit', 'org:employee:list')")
    @GetMapping("/emr/signCert/caStatus")
    public Result<SignCaStatusVO> signCertCaStatus() {
        return Result.success(signCertFacadeService.caStatus());
    }

    /**
     * 证书台账分页（按持有人、状态、签发方式与关键字过滤）。
     */
    @Operation(summary = "签名证书台账分页")
    @PreAuthorize("hasAnyAuthority('sign:cert:list', 'org:employee:list')")
    @GetMapping("/emr/signCert/listPage")
    public Result<PageResult<SignCertVO>> signCertListPage(@Valid SignCertPageQueryDTO query) {
        var page = signCertFacadeService.listCertPage(query);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getPages(), page.getRecords()));
    }

    /**
     * 单张证书详情：含公钥 PEM、指纹与 CA 签发信息。
     */
    @Operation(summary = "签名证书详情（含公钥 PEM 与指纹）")
    @PreAuthorize("hasAnyAuthority('sign:cert:list', 'org:employee:list')")
    @GetMapping("/emr/signCert/getById")
    public Result<SignCertVO> signCertDetail(@RequestParam Long id) {
        return Result.success(signCertFacadeService.getCertDetail(id));
    }

    /**
     * 一张证书的 CA 申请流水：第几次申请、走到哪一步、提交/审核/签发/回填各阶段的时刻与 CA 说明。
     */
    @Operation(summary = "签名证书 CA 申请记录（时间线）")
    @PreAuthorize("hasAnyAuthority('sign:cert:list', 'org:employee:list')")
    @GetMapping("/emr/signCert/caApplyList")
    public Result<List<SignCaApplyRecordVO>> signCertCaApplyList(@RequestParam Long certId) {
        return Result.success(signCertFacadeService.listCaApplies(certId));
    }

    /**
     * 按员工取当前有效证书：员工管理「签名设置」弹框用（只读，人事侧也可看）。
     */
    @Operation(summary = "按员工取当前有效签名证书（只读）")
    @PreAuthorize("hasAnyAuthority('sign:cert:list', 'org:employee:list')")
    @GetMapping("/emr/signCert/getByEmp")
    public Result<SignCertVO> signCertByEmp(@RequestParam Long empId) {
        return Result.success(signCertFacadeService.getActiveCertByEmp(empId));
    }

    /**
     * 本人签名预检：当前登录员工这次能不能完成电子签名、需不需要输入签名 PIN。
     * 用于医生站结诊前决定「无签结诊强提示」与 PIN 弹框。
     *
     * <p>只要求登录且不收员工ID：证书台账的权限码是管理岗口径，普通医生拿不到；
     * 「本人看自己」的签名状态必须能查得到，否则提示整段静默失效。
     */
    @Operation(summary = "本人签名预检（能否电子签名 / 是否需要输入签名PIN）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/emr/signCert/preCheck")
    public Result<SignPreCheckVO> signCertPreCheck() {
        return Result.success(signCertFacadeService.preCheckForSign(UserUtils.getCurrentUser().getEmployeeId()));
    }

    /**
     * 本人换签名 PIN：当前口令校验通过才允许换成新口令。
     * 用于首次软签前把建号下发的初始口令换成签名人自己的口令；证书行取自登录态，不收员工ID，
     * 与预检同理——管理岗权限码普通医生拿不到，改口令的入口必须是本人接口。
     */
    @Operation(summary = "本人修改签名PIN")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/emr/signCert/changePin")
    public Result<Void> signCertChangePin(@Valid @RequestBody SignPinChangeDTO dto) {
        signCertFacadeService.changeOwnPin(dto);
        return Result.success();
    }

    /**
     * 生成签名密钥对：私钥本地生成并加密托管，接入 CA 在线申请时立即提交申请；
     * 未接入或提交失败时返回线下送 CA 的申请单（Subject DN + 公钥 PEM）。
     */
    @PreAuthorize("hasAuthority('sign:cert:edit')")
    @Operation(summary = "生成签名密钥对（在线自动提交 CA 申请；未接入时返回线下申请单）")
    @PostMapping("/emr/signCert/keygen")
    public Result<SignCaApplyVO> signCertKeygen(@Valid @RequestBody SignCertKeygenDTO dto) {
        return Result.success(signCertFacadeService.keygenCert(dto));
    }

    /**
     * 提交 / 重试 CA 在线申请：已提交待审核时等价于刷新状态，不重复建单。
     */
    @PreAuthorize("hasAuthority('sign:cert:edit')")
    @Operation(summary = "提交或重试 CA 在线申请")
    @PostMapping("/emr/signCert/caSubmit")
    public Result<SignCaApplyVO> signCertCaSubmit(@Valid @RequestBody SignCertCaDTO dto) {
        return Result.success(signCertFacadeService.submitCaApply(dto.getCertId()));
    }

    /**
     * 刷新 CA 申请状态：CA 已签发则自动下载证书链、校验公钥并回填生效。
     */
    @PreAuthorize("hasAuthority('sign:cert:edit')")
    @Operation(summary = "刷新 CA 申请状态（已签发自动下载回填）")
    @PostMapping("/emr/signCert/caRefresh")
    public Result<SignCaApplyVO> signCertCaRefresh(@Valid @RequestBody SignCertCaDTO dto) {
        return Result.success(signCertFacadeService.refreshCaApply(dto.getCertId()));
    }

    /**
     * 回填 CA 线下签发的证书：校验证书公钥与本地密钥对一致后转为有效状态。
     */
    @PreAuthorize("hasAuthority('sign:cert:edit')")
    @Operation(summary = "回填 CA 签发的证书")
    @PostMapping("/emr/signCert/bind")
    public Result<SignCertVO> signCertBind(@Valid @RequestBody SignCertBindDTO dto) {
        return Result.success(signCertFacadeService.bindCert(dto));
    }

    /**
     * 吊销证书：必须写明理由；吊销不删行，历史签名仍要靠它的公钥验通。
     *
     * <p>吊销是独立处置权（sign:cert:revoke），不与证书办理（sign:cert:edit）混用：
     * 能办证的人不该能随手吊销，处置权只授医务科。无签结诊预警也发给持本权限的人。
     */
    @PreAuthorize("hasAuthority('sign:cert:revoke')")
    @Operation(summary = "吊销签名证书（必须写理由）")
    @PostMapping("/emr/signCert/revoke")
    public Result<SignCertVO> signCertRevoke(@Valid @RequestBody SignCertRevokeDTO dto) {
        signCertFacadeService.revokeCert(dto);
        return Result.success(signCertFacadeService.getCertDetail(dto.getCertId()));
    }
}
