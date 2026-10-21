package com.his.clinical.controller;

import com.his.spi.vo.SignVerifyVO;
import com.his.spi.vo.SignatureVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.*;
import com.his.clinical.service.EmrSignatureService;
import com.his.clinical.service.SignatureCenterService;
import com.his.clinical.vo.*;
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
}
