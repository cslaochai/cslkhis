package com.his.ai.controller;

import com.his.ai.dto.OperationQaColumnQueryPageDTO;
import com.his.ai.dto.OperationQaColumnUpsertDTO;
import com.his.ai.dto.OperationQaSchemaQueryPageDTO;
import com.his.ai.dto.OperationQaSchemaUpsertDTO;
import com.his.ai.dto.OperationQaSemanticQueryPageDTO;
import com.his.ai.dto.OperationQaSemanticUpsertDTO;
import com.his.ai.service.OperationQaConfigService;
import com.his.ai.vo.OperationQaColumnListVO;
import com.his.ai.vo.OperationQaSchemaListVO;
import com.his.ai.vo.OperationQaSchemaSelectListVO;
import com.his.ai.vo.OperationQaSemanticListVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 运营问数配置管理接口（数据表/列/语义映射维护）。
 */
@Tag(name = "AI 能力-运营问数配置")
@RestController
@RequestMapping("/ai/operationQa/config")
@RequiredArgsConstructor
public class AiOperationQaConfigController {

    private final OperationQaConfigService operationQaConfigService;

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "数据表配置分页查询")
    @PostMapping("/schemaListPage")
    public Result<PageResult<OperationQaSchemaListVO>> schemaListPage(@RequestBody @Valid OperationQaSchemaQueryPageDTO dto) {
        return Result.success(operationQaConfigService.listSchemaPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "数据表配置新增/修改")
    @PostMapping("/schemaUpsert")
    public Result<Void> schemaUpsert(@RequestBody @Valid OperationQaSchemaUpsertDTO dto) {
        operationQaConfigService.upsertSchema(dto);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "删除数据表配置（级联删该表列配置）")
    @DeleteMapping("/schemaDeleteById")
    public Result<Void> schemaDeleteById(@RequestParam Long id) {
        operationQaConfigService.deleteSchemaById(id);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "数据列配置分页查询（按表）")
    @PostMapping("/columnListPage")
    public Result<PageResult<OperationQaColumnListVO>> columnListPage(@RequestBody @Valid OperationQaColumnQueryPageDTO dto) {
        return Result.success(operationQaConfigService.listColumnPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "数据列配置新增/修改")
    @PostMapping("/columnUpsert")
    public Result<Void> columnUpsert(@RequestBody @Valid OperationQaColumnUpsertDTO dto) {
        operationQaConfigService.upsertColumn(dto);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "删除数据列配置")
    @DeleteMapping("/columnDeleteById")
    public Result<Void> columnDeleteById(@RequestParam Long id) {
        operationQaConfigService.deleteColumnById(id);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "语义映射分页查询")
    @PostMapping("/semanticListPage")
    public Result<PageResult<OperationQaSemanticListVO>> semanticListPage(@RequestBody @Valid OperationQaSemanticQueryPageDTO dto) {
        return Result.success(operationQaConfigService.listSemanticPage(dto));
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "语义映射新增/修改")
    @PostMapping("/semanticUpsert")
    public Result<Void> semanticUpsert(@RequestBody @Valid OperationQaSemanticUpsertDTO dto) {
        operationQaConfigService.upsertSemantic(dto);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "删除语义映射")
    @DeleteMapping("/semanticDeleteById")
    public Result<Void> semanticDeleteById(@RequestParam Long id) {
        operationQaConfigService.deleteSemanticById(id);
        return Result.success();
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "启用表下拉选项（配置弹框选表用）")
    @GetMapping("/schemaSelectList")
    public Result<List<OperationQaSchemaSelectListVO>> schemaSelectList() {
        return Result.success(operationQaConfigService.schemaSelectList());
    }

    @PreAuthorize("hasAuthority('ai:operationQa:config')")
    @Operation(summary = "重写刷新：扫描当前库全表结构，一次性覆盖表/列配置（表结构同步唯一入口）")
    @PostMapping("/rewriteSchema")
    public Result<String> rewriteSchema() {
        return Result.success(operationQaConfigService.rewriteSchema());
    }
}
