package com.his.appoint.controller;

import com.his.appoint.dto.RevisitFeePolicyPageQueryDTO;
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
 * 复诊收费策略配置控制器：维护「复诊这趟按哪种方式收诊查费」的规则，是门诊收费台取档的唯一配置来源。
 */
@Tag(name = "复诊收费策略配置")
@RestController
@RequestMapping("/appoint/revisitFeePolicy")
@RequiredArgsConstructor
public class RevisitFeePolicyController {

    private final RevisitFeePolicyService revisitFeePolicyService;

    /**
     * 分页查询复诊收费策略。用于门诊「复诊收费策略」配置页表格。
     */
    @PreAuthorize("hasAuthority('opd:revisitPolicy:list')")
    @Operation(summary = "分页查询复诊收费策略")
    @PostMapping("/listPage")
    public Result<PageResult<RevisitFeePolicyVO>> listPage(@Valid @RequestBody RevisitFeePolicyPageQueryDTO queryDTO) {
        return Result.success(revisitFeePolicyService.listPage(queryDTO));
    }

    /**
     * 取单条复诊收费策略。用于配置页点行开详情弹框。
     */
    @PreAuthorize("hasAuthority('opd:revisitPolicy:list')")
    @Operation(summary = "获取策略详情")
    @GetMapping("/getById")
    public Result<RevisitFeePolicyVO> getById(@RequestParam Long id) {
        return Result.success(revisitFeePolicyService.detail(id));
    }

    /**
     * 新增或修改一条复诊收费策略。用于配置页「新增/编辑」弹框保存，同一接口按 id 分支。
     */
    @PreAuthorize("hasAuthority('opd:revisitPolicy:add')")
    @Operation(summary = "新增或修改策略")
    @PostMapping("/revisitFeePolicyUpsert")
    public Result<Void> revisitFeePolicyUpsert(@Valid @RequestBody RevisitFeePolicyUpsertDTO upsertDTO) {
        revisitFeePolicyService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "新增成功" : "修改成功", null);
    }

    /**
     * 停用删除一条复诊收费策略。用于配置页删除按钮，删后该档不再参与挂号取价。
     */
    @PreAuthorize("hasAuthority('opd:revisitPolicy:delete')")
    @Operation(summary = "删除策略")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        revisitFeePolicyService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
