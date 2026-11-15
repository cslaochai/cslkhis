package com.his.ai.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.FlowIntentRouteLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.vo.FlowPromptVariablesVO;
import com.his.common.enums.AiBizTypeEnum;
import com.his.common.util.TextUtil;
import com.his.spi.dto.FlowAiNodeRequestDTO;
import com.his.spi.dto.FlowIntentOptionDTO;
import com.his.spi.dto.FlowIntentRouteRequestDTO;
import com.his.spi.service.FlowAiGatewayService;
import com.his.spi.vo.FlowAiNodeResultVO;
import com.his.spi.vo.FlowIntentRouteResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 工作流 → AI 的端口实现（叶子适配器：只注入 AI 执行器，不牵业务编排）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowAiGatewayServiceImpl implements FlowAiGatewayService {

    /**
     * 节点输入上限：流程变量拼起来可能很长，超了截断，宁缺细节不撑爆提示词
     */
    private static final int INPUT_MAX = 6000;

    private static final int TEXT_MAX = 500;

    private final AiExecutionService aiExecutionService;

    @Override
    public FlowAiNodeResultVO executeNode(FlowAiNodeRequestDTO request) {
        FlowAiNodeResultVO result = new FlowAiNodeResultVO();
        if (!TextUtil.hasText(request.getTemplateName())) {
            result.setSucceeded(false);
            result.setDegraded(true);
            result.setDegradeReason("节点未配置提示词模板");
            return result;
        }
        String capabilityKey = TextUtil.hasText(request.getCapabilityKey())
                ? request.getCapabilityKey() : AiCapabilityKeys.FLOW_AI_NODE;

        FlowPromptVariablesVO variables = new FlowPromptVariablesVO();
        variables.setText("");
        variables.setChannel("");
        variables.setOptions("");
        variables.setInputJson(TextUtil.cut(request.getInputJson(), INPUT_MAX, "{}"));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(capabilityKey)
                .templateName(request.getTemplateName())
                .variables(variables)
                .bizType(TextUtil.hasText(request.getBizType())
                        ? request.getBizType() : AiBizTypeEnum.FLOW.getCode())
                .bizId(request.getBizId())
                .inputDigest(TextUtil.cut(request.getInputJson(), TEXT_MAX, ""))
                .temperature(0.2D)
                .build();

        Optional<JsonNode> output = aiExecutionService.call(call, JsonNode.class);
        if (output.isEmpty()) {
            result.setSucceeded(false);
            result.setDegraded(true);
            result.setDegradeReason(aiExecutionService.degradeReasonOf(capabilityKey));
            return result;
        }
        result.setSucceeded(true);
        result.setPayload(output.get().toString());
        return result;
    }

    @Override
    public FlowIntentRouteResultVO routeIntent(FlowIntentRouteRequestDTO request) {
        FlowIntentRouteResultVO result = new FlowIntentRouteResultVO();
        if (!TextUtil.hasText(request.getText()) || request.getOptions() == null || request.getOptions().isEmpty()) {
            result.setDegraded(true);
            result.setDegradeReason("缺少文本或候选流程");
            return result;
        }

        FlowPromptVariablesVO variables = new FlowPromptVariablesVO();
        variables.setText(TextUtil.cut(request.getText(), TEXT_MAX, ""));
        variables.setChannel(TextUtil.hasText(request.getChannel()) ? request.getChannel() : "");
        variables.setOptions(buildOptionsText(request));
        variables.setInputJson("");

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.FLOW_INTENT_ROUTE)
                .templateName("flow-intent-route")
                .variables(variables)
                .bizType(AiBizTypeEnum.FLOW.getCode())
                .inputDigest(TextUtil.cut(request.getText(), 60, ""))
                .useLiteModel(true)
                .maxTokens(256)
                .build();

        Optional<FlowIntentRouteLlmOutputDTO> output =
                aiExecutionService.call(call, FlowIntentRouteLlmOutputDTO.class);
        if (output.isEmpty()) {
            result.setDegraded(true);
            result.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.FLOW_INTENT_ROUTE));
            return result;
        }
        FlowIntentRouteLlmOutputDTO dto = output.get();
        String flowCode = TextUtil.trim(dto.getFlowCode());
        boolean inOptions = request.getOptions().stream()
                .anyMatch(option -> flowCode != null && flowCode.equals(option.getFlowCode()));
        if (!inOptions) {
            // 模型自造编码一律丢弃：这不是"多选了一个"，是没有识别出来
            result.setDegraded(true);
            result.setDegradeReason("模型返回的流程编码不在候选清单内");
            result.setReason(TextUtil.cut(dto.getReason(), 120, ""));
            return result;
        }
        result.setFlowCode(flowCode);
        result.setConfidence(dto.getConfidence());
        result.setReason(TextUtil.cut(dto.getReason(), 120, ""));
        return result;
    }

    private String buildOptionsText(FlowIntentRouteRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        for (FlowIntentOptionDTO option : request.getOptions()) {
            sb.append(option.getFlowCode()).append(" | ")
                    .append(option.getFlowName()).append(" | ")
                    .append(TextUtil.hasText(option.getDescription()) ? option.getDescription() : option.getFlowName())
                    .append('\n');
        }
        return sb.toString();
    }
}
