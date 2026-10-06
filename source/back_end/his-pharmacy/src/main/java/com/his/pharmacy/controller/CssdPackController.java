package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.service.CssdService;
import com.his.pharmacy.vo.CssdPackVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * CSSD 消毒供应追溯控制器。
 *
 * <p>流程：回收登记（建包+回收节点）→ 清洗 → 打包 → 灭菌（必填锅次/批次）→ 储存（不合格退回清洗）→ 发放。
 * 追溯节点只增不改。
 */
@Tag(name = "CSSD消毒供应追溯")
@RestController
@RequestMapping("/cssd/pack")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('asset:cssd:list')")
public class CssdPackController {

    private final CssdService cssdService;

    @PreAuthorize("hasAuthority('asset:cssd:edit')")
    @Operation(summary = "回收登记（新建器械包并记录回收节点）")
    @PostMapping("/receive")
    public Result<CssdPackVO> receive(@Valid @RequestBody CssdDTO.Receive dto) {
        return Result.success("回收登记成功", cssdService.receive(dto));
    }

    @PreAuthorize("hasAuthority('asset:cssd:edit')")
    @Operation(summary = "流转到下一追溯节点")
    @PostMapping("/advance")
    public Result<CssdPackVO> advance(@Valid @RequestBody CssdDTO.Advance dto) {
        return Result.success("流转成功", cssdService.advance(dto));
    }

    @Operation(summary = "器械包分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<CssdPackVO>> listPage(@Valid @RequestBody CssdDTO.QueryPage dto) {
        var page = cssdService.listPage(dto == null ? new CssdDTO.QueryPage() : dto);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    @Operation(summary = "器械包详情（含全量追溯链）")
    @GetMapping("/getDetailById")
    public Result<CssdPackVO> getDetailById(@RequestParam Long packId) {
        return Result.success(cssdService.getDetailById(packId));
    }
}
