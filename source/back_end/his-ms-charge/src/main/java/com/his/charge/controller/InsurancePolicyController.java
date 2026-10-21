package com.his.charge.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.charge.dto.InsurancePolicyPageQueryDTO;
import com.his.charge.dto.InsurancePolicyUpsertDTO;
import com.his.charge.service.InsurancePolicyService;
import com.his.charge.vo.InsurancePolicyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医保政策配置控制器：按医保类型维护统筹比例、乙类自付比例与启用状态的接口入口。
 */
@Tag(name = "医保政策配置")
@RestController
@RequestMapping("/system/insurancePolicy")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:insurancePolicy:list')")
public class InsurancePolicyController {

    private final InsurancePolicyService insurancePolicyService;

    /**
     * 医保政策分页：按政策名称、医保类型、结算方式、启用状态筛选。
     * 用于「医保政策配置」页查询卡与政策表格。
     */
    @Operation(summary = "分页查询医保政策")
    @PostMapping("/listPage")
    public Result<PageResult<InsurancePolicyVO>> listPage(@Valid @RequestBody InsurancePolicyPageQueryDTO queryDTO) {
        return Result.success(insurancePolicyService.queryPolicyPage(queryDTO));
    }

    /**
     * 单条医保政策详情：含比例与生效状态全字段。
     * 供政策调整前整条参数回读、比对当前口径与拟改口径时取数。
     */
    @Operation(summary = "获取医保政策详情")
    @GetMapping("/getById")
    public Result<InsurancePolicyVO> getById(@RequestParam Long id) {
        return Result.success(insurancePolicyService.getPolicyInfo(id));
    }

    /**
     * 新增或修改医保政策：改比例即改变后续结算的报销口径，只影响之后的算费。
     * 用于「医保政策配置」页新增/编辑弹框保存。
     */
    @PreAuthorize("hasAuthority('finance:insurancePolicy:add')")
    @Operation(summary = "新增或修改医保政策")
    @PostMapping("/insurancePolicyUpsert")
    public Result<Void> insurancePolicyUpsert(@Valid @RequestBody InsurancePolicyUpsertDTO upsertDTO) {
        return Result.success(insurancePolicyService.upsertPolicy(upsertDTO), null);
    }

    /**
     * 删除医保政策。
     * 用于「医保政策配置」页操作列删除已停用的政策。
     */
    @PreAuthorize("hasAuthority('finance:insurancePolicy:delete')")
    @Operation(summary = "删除医保政策")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        insurancePolicyService.removePolicy(id);
        return Result.success("删除成功", null);
    }
}
