package com.his.appoint.controller;

import com.his.appoint.dto.RevisitFeePolicyQueryPageDTO;
import com.his.appoint.dto.RevisitFeePolicyUpsertDTO;
import com.his.appoint.service.RevisitFeePolicyService;
import com.his.appoint.vo.RevisitFeePolicyVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 复诊收费策略配置控制器
 *
 * <p>这张表决定「复诊号收不收挂号费/诊查费」，改一次全站生效，
 * 所以权限码只给管理岗与收费/医保口（菜单 2340，见 sql/121）。
 *
 * <p>注解一律标在方法上，不标类：类级 {@code @PreAuthorize} 会静默罩住
 * 所有没写自己注解的方法（{@code /system/menu/userMenus} 被医生站权限罩住导致
 * 非管理岗一进「切换岗位」就被踢回登录页，就是这么来的）。
 */
@Tag(name = "复诊收费策略配置")
@RestController
@RequestMapping("/appoint/revisitFeePolicy")
@RequiredArgsConstructor
public class RevisitFeePolicyController {

    private final RevisitFeePolicyService revisitFeePolicyService;

    @PreAuthorize("hasAuthority('opd:revisitPolicy:list')")
    @Operation(summary = "分页查询复诊收费策略")
    @PostMapping("/listPage")
    public Result<PageResult<RevisitFeePolicyVO>> listPage(@Valid @RequestBody RevisitFeePolicyQueryPageDTO queryDTO) {
        return Result.success(revisitFeePolicyService.listPage(queryDTO));
    }

    @PreAuthorize("hasAuthority('opd:revisitPolicy:list')")
    @Operation(summary = "获取策略详情")
    @GetMapping("/getById")
    public Result<RevisitFeePolicyVO> getById(@RequestParam Long id) {
        return Result.success(revisitFeePolicyService.detail(id));
    }

    @PreAuthorize("hasAuthority('opd:revisitPolicy:add')")
    @Operation(summary = "新增或修改策略")
    @PostMapping("/revisitFeePolicyUpsert")
    public Result<Void> revisitFeePolicyUpsert(@Valid @RequestBody RevisitFeePolicyUpsertDTO upsertDTO) {
        revisitFeePolicyService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", null);
    }

    @PreAuthorize("hasAuthority('opd:revisitPolicy:delete')")
    @Operation(summary = "删除策略")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        revisitFeePolicyService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
