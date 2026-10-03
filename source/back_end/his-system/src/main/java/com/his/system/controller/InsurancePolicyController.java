package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.InsurancePolicyQueryPageDTO;
import com.his.system.dto.InsurancePolicyUpsertDTO;
import com.his.system.service.InsurancePolicyService;
import com.his.system.vo.InsurancePolicyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 医保政策配置控制器
 *
 * <p>维护医保政策配置：门诊/住院结算时「统筹比例、乙类自付比例」的依据。</p>
 */
@Tag(name = "医保政策配置")
@RestController
@RequestMapping("/system/insurancePolicy")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:insurancePolicy:list')")
public class InsurancePolicyController {

    private final InsurancePolicyService insurancePolicyService;

    @Operation(summary = "分页查询医保政策")
    @PostMapping("/listPage")
    public Result<PageResult<InsurancePolicyVO>> listPage(@RequestBody InsurancePolicyQueryPageDTO queryDTO) {
        return Result.success(insurancePolicyService.queryPolicyPage(queryDTO));
    }

    @Operation(summary = "获取医保政策详情")
    @GetMapping("/getById")
    public Result<InsurancePolicyVO> getById(@RequestParam Long id) {
        return Result.success(insurancePolicyService.getPolicyInfo(id));
    }

    @PreAuthorize("hasAuthority('finance:insurancePolicy:add')")
    @Operation(summary = "新增或修改医保政策")
    @PostMapping("/insurancePolicyUpsert")
    public Result<Void> insurancePolicyUpsert(@Valid @RequestBody InsurancePolicyUpsertDTO upsertDTO) {
        return Result.success(insurancePolicyService.upsertPolicy(upsertDTO), null);
    }

    @PreAuthorize("hasAuthority('finance:insurancePolicy:delete')")
    @Operation(summary = "删除医保政策")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        insurancePolicyService.removePolicy(id);
        return Result.success("删除成功", null);
    }
}
