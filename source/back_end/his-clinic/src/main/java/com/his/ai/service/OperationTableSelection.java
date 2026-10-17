package com.his.ai.service;

import com.his.ai.support.OperationSchemaCatalog;

import java.util.List;
import java.util.Set;

/**
 * 运营问数选表结果。
 *
 * @param tables    选中的相关表集合；空集合表示未选出（调用方兜底全量）
 * @param semantics 命中的语义条目（注入生成段作为权威口径；可为空）
 * @param routeDesc 路由方式说明（SEMANTIC=语义命中 / MODEL=模型兜底 / FALLBACK=未选出）
 */
public record OperationTableSelection(
        Set<String> tables,
        List<OperationSchemaCatalog.SemanticDef> semantics,
        String routeDesc) {

    public static OperationTableSelection fallback() {
        return new OperationTableSelection(Set.of(), List.of(), "FALLBACK");
    }
}
