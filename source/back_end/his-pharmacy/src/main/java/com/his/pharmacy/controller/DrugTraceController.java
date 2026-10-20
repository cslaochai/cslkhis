package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.service.DrugTraceService;
import com.his.pharmacy.vo.DrugTraceReconcileVO;
import com.his.pharmacy.vo.DrugTraceScanVO;
import com.his.pharmacy.vo.DrugTraceUploadResultVO;
import com.his.pharmacy.vo.DrugTraceVO;
import com.his.common.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品追溯码采集与核对控制器
 */
@Tag(name = "药品追溯码")
@RestController
@RequestMapping("/drugTrace")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:drugTrace:list')")
public class DrugTraceController {

    private final DrugTraceService drugTraceService;

    /**
     * 解析追溯码原文，识别码制与命中药品/批次，并给出这张码当前能不能采集或核销。
     * 用于追溯码页「采集与核对」两个表单的扫码预检，只回结论不落库，确认后才真正写入。
     */
    @Operation(summary = "扫码解析（含采集/核销闸门结论，不落库）")
    @PostMapping("/scan")
    public Result<DrugTraceScanVO> scan(@Valid @RequestBody DrugTraceScanDTO scanDTO) {
        return Result.success(drugTraceService.scan(scanDTO));
    }

    /**
     * 写入一条追溯码采集记录，挂到指定库存批次上；来源区分入库验收扫码与存量补采。
     * 用于药品收货验收时的扫码与历史批次的补录；码解析未命中药品时需人工指定药品再采集。
     */
    @PreAuthorize("hasAuthority('pharmacy:drugTrace:add')")
    @Operation(summary = "入库采集/存量补采")
    @PostMapping("/collect")
    public Result<DrugTraceVO> collect(@Valid @RequestBody DrugTraceCollectDTO collectDTO) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(drugTraceService.collect(collectDTO, operatorUser.getRealName()));
    }

    /**
     * 把扫到的追溯码核销到一条已发药记录上，建立「这盒药发给了谁」的对应关系。
     * 用于药房侧发药后的追溯核对环节，未发药的记录核销不掉。
     */
    @PreAuthorize("hasAuthority('pharmacy:drugTrace:edit')")
    @Operation(summary = "发药核销（扫追溯码绑定已发药记录）")
    @PostMapping("/verifyDispense")
    public Result<DrugTraceVO> verifyDispense(@Valid @RequestBody DrugTraceDispenseDTO dispenseDTO) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(drugTraceService.verifyDispense(dispenseDTO, operatorUser.getRealName()));
    }

    /**
     * 作废一枚已采集的追溯码，按退药、报损或召回登记原因，作废后该码不再参与核销与上传。
     * 用于追溯码页的作废弹框；退药场景对应药房侧的患者退药回库。
     */
    @PreAuthorize("hasAuthority('pharmacy:drugTrace:edit')")
    @Operation(summary = "作废（1-退药 2-报损 3-召回）")
    @PostMapping("/void")
    public Result<DrugTraceVO> voidTrace(@Valid @RequestBody DrugTraceVoidDTO voidDTO) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(drugTraceService.voidTrace(voidDTO, operatorUser.getRealName()));
    }

    /**
     * 批量把已采集的追溯码上报医保局，回写上传结果；不指定记录时按上限抓取待上传与上次失败的码。
     * 用于追溯码页「上传与对账」页签的批量报码，失败行留在台账里可重试。
     */
    @PreAuthorize("hasAuthority('pharmacy:drugTrace:export')")
    @Operation(summary = "批量上传医保局（ids 为空=上传全部待上传/失败）")
    @PostMapping("/upload")
    public Result<DrugTraceUploadResultVO> upload(@Valid @RequestBody DrugTraceUploadDTO uploadDTO) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return Result.success(drugTraceService.upload(uploadDTO, operatorUser.getRealName()));
    }

    /**
     * 追溯码台账分页，可按码状态、上传状态、码制与采集来源筛。
     * 用于药品追溯码页「追溯码台账」页签的查询卡与码清单表格。
     */
    @Operation(summary = "追溯码台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<DrugTraceVO>> listPage(@Valid @RequestBody DrugTracePageQueryDTO queryDTO) {
        return Result.success(drugTraceService.page(queryDTO));
    }

    /**
     * 按台账主键取单枚追溯码的完整档案（采集来源、挂靠批次、核销与上传结果）。
     * 供追溯码单条核对与医保报码排查时按ID取数。
     */
    @Operation(summary = "追溯码详情")
    @GetMapping("/getDetailById")
    public Result<DrugTraceVO> getDetailById(@RequestParam Long id) {
        return Result.success(drugTraceService.getDetailById(id));
    }

    /**
     * 汇总追溯码的对账口径：码状态分布、上传进度分布，以及近30天已发药却没核销码的发药行数。
     * 用于追溯码页「上传与对账」页签的统计卡，报码漏采时靠这组数定位缺口。
     */
    @Operation(summary = "对账统计（码状态分布 / 上传分布 / 未核销发药行数）")
    @GetMapping("/reconcileStats")
    public Result<DrugTraceReconcileVO> reconcileStats() {
        return Result.success(drugTraceService.reconcileStats());
    }

    /**
     * 删掉一条采错的追溯码记录，只在库且未上报医保的码才允许删。
     * 用于追溯码台账行内清理重复扫码或错挂批次的采集行；已核销、已上传、已作废的码一律删不掉，只能保留留档。
     */
    @PreAuthorize("hasAuthority('pharmacy:drugTrace:delete')")
    @Operation(summary = "删除误采记录（仅在库且未上传可删，物理删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        drugTraceService.deleteById(id);
        return Result.success();
    }

}
