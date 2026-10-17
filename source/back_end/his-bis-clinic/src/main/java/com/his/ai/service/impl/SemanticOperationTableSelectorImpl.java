package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.OperationQaRouteLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.OperationQaConfigService;
import com.his.ai.service.OperationTableSelection;
import com.his.ai.service.OperationTableSelector;
import com.his.ai.support.OperationSchemaCatalog;
import com.his.ai.vo.OperationQaRoutePromptVariablesVO;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营问数选表实现：语义配置命中优先，未命中时模型从表目录兜底选表，
 * 仍失败则返回空（由生成段兜底全量，回到未做按需加载时的行为）。
 *
 * <p>语义/白名单/表目录一律按需查库（{@link OperationQaConfigService}），无内存快照。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticOperationTableSelectorImpl implements OperationTableSelector {

    private static final String ROUTE_TEMPLATE_NAME = "operation-qa-route";

    private static final int ROUTE_MAX_TOKENS = 128;

    private static final String BIZ_TYPE = "operation_qa";

    private final AiExecutionService aiExecutionService;

    private final OperationQaConfigService operationQaConfigService;

    @Override
    public OperationTableSelection select(String question) {
        // 图谱边（join 权威键），用于命中表集合的图遍历补维度
        List<OperationSchemaCatalog.RelationDef> relations = operationQaConfigService.relationDefs();

        // 1. 语义命中（配置驱动，无额外调用）
        List<OperationSchemaCatalog.SemanticDef> hits = matchSemantics(question);
        if (!hits.isEmpty()) {
            Set<String> tables = hits.stream()
                    .map(OperationSchemaCatalog.SemanticDef::tableName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new OperationTableSelection(expandWithRelations(tables, relations), hits, "SEMANTIC");
        }

        // 2. 模型兜底选表（仅语义未命中时触发，控制成本与失败面）
        Optional<OperationQaRouteLlmOutputDTO> routed = callRoute(question);
        if (routed.isPresent()) {
            Set<String> allowed = operationQaConfigService.enabledTableNames();
            Set<String> modelTables = routed.get().getTables().stream()
                    .filter(TextUtil::hasText)
                    .map(String::trim)
                    .map(t -> t.toLowerCase(Locale.ROOT))
                    .filter(allowed::contains)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!modelTables.isEmpty()) {
                return new OperationTableSelection(expandWithRelations(modelTables, relations), List.of(), "MODEL");
            }
        }
        // 3. 未选出 → 调用方兜底全量
        return OperationTableSelection.fallback();
    }

    /**
     * 图遍历补维度（第三层图谱）：沿关系边把关联表补进命中集合（BFS 闭包）。
     *
     * <p>如命中「手术」补出 sys_department（按科室）+ biz_admission（按入院科室分组）、
     * 命中「药品销售」先补 biz_prescription（明细归处方）再经其 dept_id 补 sys_department
     * —— 模型写 JOIN 时不再需要现场想维度表，join 键也由关系边注入提示词。
     * 只补出边（source→target），不补入边（避免把明细表反向拉进集合造成膨胀）；
     * 图仅白名单内十余张表、十余条边，BFS 闭包代价可忽略。
     */
    private Set<String> expandWithRelations(Set<String> tables,
                                            List<OperationSchemaCatalog.RelationDef> relations) {
        if (relations.isEmpty()) {
            return tables;
        }
        Set<String> expanded = new LinkedHashSet<>(tables);
        boolean changed;
        do {
            changed = false;
            for (OperationSchemaCatalog.RelationDef relation : relations) {
                if (expanded.contains(relation.sourceTable().toLowerCase(Locale.ROOT))
                        && expanded.add(relation.targetTable().toLowerCase(Locale.ROOT))) {
                    changed = true;
                }
            }
        } while (changed);
        return expanded;
    }

    /**
     * 遍历启用语义（按需查库）：包含匹配（问题包含 term）/ 精确匹配（归一化后全等），按优先级降序。
     */
    private List<OperationSchemaCatalog.SemanticDef> matchSemantics(String question) {
        return operationQaConfigService.semanticDefs().stream()
                .filter(s -> s.matchMode() == null
                        || s.matchMode() == 1
                        ? question.contains(s.term())
                        : normalized(question).equals(normalized(s.term())))
                .sorted(Comparator.comparingInt(
                        (OperationSchemaCatalog.SemanticDef s) -> s.priority() == null ? 0 : s.priority())
                        .reversed())
                .collect(Collectors.toList());
    }

    private static String normalized(String text) {
        return text == null ? "" : text.trim();
    }

    private Optional<OperationQaRouteLlmOutputDTO> callRoute(String question) {
        OperationQaRoutePromptVariablesVO variables = new OperationQaRoutePromptVariablesVO();
        variables.setCatalog(OperationSchemaCatalog.catalogText(operationQaConfigService.tableDefs(null)));
        variables.setQuestion(question);
        variables.setToday(LocalDate.now().format(DateFormats.DATE));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.OPERATION_QA)
                .templateName(ROUTE_TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .inputDigest(question)
                .useLiteModel(true)
                .maxTokens(ROUTE_MAX_TOKENS)
                .temperature(0D)
                .build();
        return aiExecutionService.call(call, OperationQaRouteLlmOutputDTO.class);
    }
}
