package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.service.DrugTraceService;
import com.his.pharmacy.vo.DrugTraceReconcileVO;
import com.his.pharmacy.vo.DrugTraceScanVO;
import com.his.pharmacy.vo.DrugTraceUploadResultVO;
import com.his.pharmacy.vo.DrugTraceVO;
import com.his.common.exception.BusinessException;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品追溯码采集与核对控制器
 *
 * <p>医保局口径：入库扫码采集、发药扫码核销，两个动作都要上传。
 * 三个页面共用一套接口：采集窗口（scan+collect）、发药窗口（scan+verifyDispense）、
 * 上传与对账（upload+reconcileStats）。
 * <br>操作人一律服务端取登录态（铁律：不信任前端传的姓名）。
 */
@Tag(name = "药品追溯码")
@RestController
@RequestMapping("/drugTrace")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:drugTrace:list')")
public class DrugTraceController {

    private final DrugTraceService drugTraceService;

    @Operation(summary = "扫码解析（含采集/核销闸门结论，不落库）")
    @PostMapping("/scan")
    public Result<DrugTraceScanVO> scan(@Valid @RequestBody DrugTraceScanDTO scanDTO) {
        return Result.success(drugTraceService.scan(scanDTO));
    }

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

    @Operation(summary = "追溯码台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<DrugTraceVO>> listPage(@Valid @RequestBody DrugTraceQueryPageDTO queryDTO) {
        return Result.success(drugTraceService.page(queryDTO));
    }

    @Operation(summary = "追溯码详情")
    @GetMapping("/getDetailById")
    public Result<DrugTraceVO> getDetailById(@RequestParam Long id) {
        return Result.success(drugTraceService.getDetailById(id));
    }

    @Operation(summary = "对账统计（码状态分布 / 上传分布 / 未核销发药行数）")
    @GetMapping("/reconcileStats")
    public Result<DrugTraceReconcileVO> reconcileStats() {
        return Result.success(drugTraceService.reconcileStats());
    }

    @PreAuthorize("hasAuthority('pharmacy:drugTrace:delete')")
    @Operation(summary = "删除误采记录（仅在库且未上传可删，物理删）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        drugTraceService.deleteById(id);
        return Result.success();
    }

}
