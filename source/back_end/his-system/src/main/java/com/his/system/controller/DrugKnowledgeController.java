package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.DoseLimitQueryPageDTO;
import com.his.system.dto.DoseLimitUpsertDTO;
import com.his.system.dto.DrugInteractionQueryPageDTO;
import com.his.system.dto.DrugInteractionUpsertDTO;
import com.his.system.service.DrugKnowledgeService;
import com.his.system.vo.DoseLimitVO;
import com.his.system.vo.DrugInteractionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 合理用药知识库（药物相互作用 × 剂量上限）维护
 */
@Tag(name = "合理用药知识库")
@RestController
@RequestMapping("/system/drugKnowledge")
@RequiredArgsConstructor
public class DrugKnowledgeController {

    private final DrugKnowledgeService drugKnowledgeService;

    @Operation(summary = "分页查询药物相互作用")
    @PreAuthorize("hasAuthority('system:drugKnowledge:list')")
    @PostMapping("/interactionListPage")
    public Result<PageResult<DrugInteractionVO>> interactionListPage(@Valid @RequestBody DrugInteractionQueryPageDTO queryDTO) {
        return Result.success(drugKnowledgeService.interactionListPage(queryDTO));
    }

    @Operation(summary = "新增/修改药物相互作用（返回保存后的整条，成分对顺序以服务端归一化结果为准）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:add')")
    @PostMapping("/interactionUpsert")
    public Result<DrugInteractionVO> interactionUpsert(@Valid @RequestBody DrugInteractionUpsertDTO upsertDTO) {
        return Result.success(drugKnowledgeService.interactionUpsert(upsertDTO));
    }

    @Operation(summary = "删除药物相互作用（物理删）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:delete')")
    @DeleteMapping("/interactionDeleteById")
    public Result<Void> interactionDeleteById(@RequestParam Long id) {
        drugKnowledgeService.interactionDeleteById(id);
        return Result.success();
    }

    @Operation(summary = "分页查询剂量上限")
    @PreAuthorize("hasAuthority('system:drugKnowledge:list')")
    @PostMapping("/doseListPage")
    public Result<PageResult<DoseLimitVO>> doseListPage(@Valid @RequestBody DoseLimitQueryPageDTO queryDTO) {
        return Result.success(drugKnowledgeService.doseLimitListPage(queryDTO));
    }

    @Operation(summary = "新增/修改剂量上限（返回保存后的整条）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:add')")
    @PostMapping("/doseUpsert")
    public Result<DoseLimitVO> doseUpsert(@Valid @RequestBody DoseLimitUpsertDTO upsertDTO) {
        return Result.success(drugKnowledgeService.doseLimitUpsert(upsertDTO));
    }

    @Operation(summary = "删除剂量上限（物理删）")
    @PreAuthorize("hasAuthority('system:drugKnowledge:delete')")
    @DeleteMapping("/doseDeleteById")
    public Result<Void> doseDeleteById(@RequestParam Long id) {
        drugKnowledgeService.doseLimitDeleteById(id);
        return Result.success();
    }
}
