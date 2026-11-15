package com.his.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.FlowAiActionEnum;
import com.his.common.enums.FlowDefinitionStatusEnum;
import com.his.common.enums.FlowInstanceStatusEnum;
import com.his.common.enums.FlowNodeTypeEnum;
import com.his.common.enums.FlowSkipTypeEnum;
import com.his.common.enums.FlowTaskStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.spi.dto.FlowAiNodeRequestDTO;
import com.his.spi.dto.FlowIntentOptionDTO;
import com.his.spi.dto.FlowIntentRouteRequestDTO;
import com.his.spi.service.FlowAiGatewayService;
import com.his.spi.vo.FlowAiNodeResultVO;
import com.his.spi.vo.FlowIntentRouteResultVO;
import com.his.workflow.dto.FlowDispatchDTO;
import com.his.workflow.dto.FlowStartDTO;
import com.his.workflow.dto.FlowTaskHandleDTO;
import com.his.workflow.dto.FlowTaskPageQueryDTO;
import com.his.workflow.entity.FlowDefinition;
import com.his.workflow.entity.FlowHisTask;
import com.his.workflow.entity.FlowInstance;
import com.his.workflow.entity.FlowNode;
import com.his.workflow.entity.FlowSkip;
import com.his.workflow.entity.FlowTask;
import com.his.workflow.mapper.FlowDefinitionMapper;
import com.his.workflow.mapper.FlowHisTaskMapper;
import com.his.workflow.mapper.FlowInstanceMapper;
import com.his.workflow.mapper.FlowNodeMapper;
import com.his.workflow.mapper.FlowSkipMapper;
import com.his.workflow.mapper.FlowTaskMapper;
import com.his.workflow.service.FlowRuntimeService;
import com.his.workflow.support.FlowConditionEvaluator;
import com.his.workflow.vo.FlowDispatchResultVO;
import com.his.workflow.vo.FlowHistoryVO;
import com.his.workflow.vo.FlowInstanceVO;
import com.his.workflow.vo.FlowTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowRuntimeServiceImpl implements FlowRuntimeService {

    /**
     * 单次推进最多走多少步：防止配置成环把请求拖死
     */
    private static final int MAX_STEPS = 20;

    /**
     * 意图路由最低置信度，低于则拒绝启动（宁可让患者补充描述，也不猜）
     */
    private static final double ROUTE_MIN_CONFIDENCE = 0.5D;

    /**
     * AI 节点默认能力标识，与 his-ms-ai 的 AiCapabilityKeys.FLOW_AI_NODE 一致
     */
    private static final String DEFAULT_AI_CAPABILITY = "flow_ai_node";

    /**
     * 意图路由默认能力标识，与 AiCapabilityKeys.FLOW_INTENT_ROUTE 一致
     */
    private static final String ROUTE_CAPABILITY = "flow_intent_route";

    private static final String ROUTE_TEMPLATE = "flow-intent-route";

    private static final String VAR_TEXT = "_text";

    private static final String VAR_CHANNEL = "_channel";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final FlowDefinitionMapper definitionMapper;

    private final FlowNodeMapper nodeMapper;

    private final FlowSkipMapper skipMapper;

    private final FlowInstanceMapper instanceMapper;

    private final FlowTaskMapper taskMapper;

    private final FlowHisTaskMapper hisTaskMapper;

    private final FlowConditionEvaluator conditionEvaluator;

    private final FlowAiGatewayService flowAiGatewayService;

    // ---------------------------------------------------------------- 启动与分发

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowInstanceVO start(FlowStartDTO dto) {
        return toInstanceVO(doStart(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowDispatchResultVO dispatch(FlowDispatchDTO dto) {
        String flowCode = dto.getFlowCode();
        FlowIntentRouteResultVO route = null;
        if (!TextUtil.hasText(flowCode)) {
            route = routeByText(dto.getText(), dto.getChannel());
            if (route == null || !TextUtil.hasText(route.getFlowCode())) {
                throw new BusinessException("没能识别出对应流程，请补充一句更具体的描述");
            }
            double confidence = route.getConfidence() == null ? 0D : route.getConfidence();
            if (confidence > 0D && confidence < ROUTE_MIN_CONFIDENCE) {
                throw new BusinessException("识别结果不确定（置信度 " + confidence + "），请补充一句更具体的描述");
            }
            flowCode = route.getFlowCode();
        }

        ObjectNode variables = (ObjectNode) conditionEvaluator.parse(dto.getVariableJson());
        variables.put(VAR_TEXT, dto.getText());
        if (TextUtil.hasText(dto.getChannel())) {
            variables.put(VAR_CHANNEL, dto.getChannel());
        }

        FlowStartDTO start = new FlowStartDTO();
        start.setFlowCode(flowCode);
        start.setBusinessId(dto.getBusinessId());
        start.setBizType(dto.getBizType());
        start.setVariableJson(conditionEvaluator.write(variables));
        start.setStartedByUserId(dto.getStartedByUserId());
        start.setStartedByName(dto.getStartedByName());

        FlowInstance instance = doStart(start);
        JsonNode finalVariables = conditionEvaluator.parse(instance.getVariableJson());

        FlowDispatchResultVO result = new FlowDispatchResultVO();
        result.setFlowCode(instance.getFlowCode());
        result.setFlowName(instance.getFlowName());
        result.setInstanceId(instance.getId());
        result.setNodeCode(instance.getNodeCode());
        result.setNodeName(instance.getNodeName());
        result.setNodeType(instance.getNodeType());
        result.setFlowStatus(instance.getFlowStatus());
        result.setStatusLabel(FlowInstanceStatusEnum.getLabel(instance.getFlowStatus()));
        result.setVariableJson(instance.getVariableJson());
        result.setAnswer(finalVariables.hasNonNull("answer") ? finalVariables.get("answer").asText() : "");
        if (route != null) {
            result.setRouteReason(route.getReason());
            result.setRouteConfidence(route.getConfidence());
        }
        return result;
    }

    private FlowIntentRouteResultVO routeByText(String text, String channel) {
        List<FlowDefinitionVOForRoute> published = definitionMapper.selectList(
                        new LambdaQueryWrapper<FlowDefinition>()
                                .eq(FlowDefinition::getStatus, FlowDefinitionStatusEnum.PUBLISHED.getCode())
                                .orderByAsc(FlowDefinition::getFlowCode))
                .stream().map(d -> new FlowDefinitionVOForRoute(d.getFlowCode(), d.getFlowName(), d.getFlowDesc()))
                .toList();
        if (published.isEmpty()) {
            throw new BusinessException("没有已发布的工作流，无法分发");
        }
        FlowIntentRouteRequestDTO request = new FlowIntentRouteRequestDTO();
        request.setText(text);
        request.setChannel(channel);
        request.setOptions(published.stream().map(p -> {
            FlowIntentOptionDTO option = new FlowIntentOptionDTO();
            option.setFlowCode(p.code());
            option.setFlowName(p.name());
            option.setDescription(TextUtil.hasText(p.desc()) ? p.desc() : p.name());
            return option;
        }).toList());
        return flowAiGatewayService.routeIntent(request);
    }

    private FlowInstance doStart(FlowStartDTO dto) {
        FlowDefinition definition = definitionMapper.selectOne(new LambdaQueryWrapper<FlowDefinition>()
                .eq(FlowDefinition::getFlowCode, dto.getFlowCode().trim())
                .eq(FlowDefinition::getStatus, FlowDefinitionStatusEnum.PUBLISHED.getCode())
                .last("LIMIT 1"));
        if (definition == null) {
            throw new BusinessException("流程不存在或未发布：" + dto.getFlowCode());
        }
        FlowNode startNode = nodeMapper.selectOne(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getDefinitionId, definition.getId())
                .eq(FlowNode::getNodeType, FlowNodeTypeEnum.START.getCode())
                .last("LIMIT 1"));
        if (startNode == null) {
            throw new BusinessException("流程缺少开始节点：" + definition.getFlowCode());
        }

        Long starterId = dto.getStartedByUserId();
        String starterName = dto.getStartedByName();
        if (starterId == null) {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                throw new BusinessException("无法确定流程发起人");
            }
            starterId = user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
            starterName = user.getRealName();
        }
        if (!TextUtil.hasText(starterName)) {
            throw new BusinessException("无法确定流程发起人姓名");
        }

        FlowInstance instance = new FlowInstance();
        instance.setDefinitionId(definition.getId());
        instance.setFlowCode(definition.getFlowCode());
        instance.setFlowName(definition.getFlowName());
        instance.setBusinessId(dto.getBusinessId());
        instance.setBizType(dto.getBizType());
        instance.setStartedByUserId(starterId);
        instance.setStartedByName(starterName);
        instance.setNodeCode(startNode.getNodeCode());
        instance.setNodeName(startNode.getNodeName());
        instance.setNodeType(startNode.getNodeType());
        instance.setFlowStatus(FlowInstanceStatusEnum.RUNNING.getCode());
        instance.setActivityStatus(1);
        instance.setVariableJson(conditionEvaluator.write(conditionEvaluator.parse(dto.getVariableJson())));
        instance.setCreateTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        insertHis(instance, null, startNode, "START", starterId, starterName, "发起流程");
        advance(instance, startNode, starterId, starterName);
        return instance;
    }

    // ---------------------------------------------------------------- 推进

    /**
     * 从当前节点往后推进，直到停在审批节点或结束。
     */
    private void advance(FlowInstance instance, FlowNode fromNode, Long operatorId, String operatorName) {
        FlowNode current = fromNode;
        for (int guard = 0; guard < MAX_STEPS; guard++) {
            int type = current.getNodeType() == null ? -1 : current.getNodeType();
            if (type == FlowNodeTypeEnum.END.getCode()) {
                finishInstance(instance, FlowInstanceStatusEnum.APPROVED, operatorId, operatorName, "流程结束");
                return;
            }
            if (type == FlowNodeTypeEnum.APPROVAL.getCode()) {
                createTasks(instance, current);
                return;
            }

            JsonNode variables = conditionEvaluator.parse(instance.getVariableJson());
            FlowNode next;
            if (type == FlowNodeTypeEnum.AUTO.getCode()) {
                variables = runAiNode(instance, current, variables, operatorId, operatorName);
                instance.setVariableJson(conditionEvaluator.write(variables));
                next = passTarget(instance, current);
            } else if (type == FlowNodeTypeEnum.GATEWAY.getCode()) {
                if (Objects.equals(FlowAiActionEnum.ROUTE.getCode(), current.getAiAction())) {
                    variables = runRouteNode(instance, current, variables, operatorId, operatorName);
                    instance.setVariableJson(conditionEvaluator.write(variables));
                }
                next = pickByCondition(instance, current, variables);
            } else {
                next = passTarget(instance, current);
            }

            if (next == null) {
                throw new BusinessException("流程配置不完整：节点「" + current.getNodeName() + "」没有可用出口");
            }
            move(instance, current, next);
            current = next;
        }
        throw new BusinessException("流程推进超过 " + MAX_STEPS + " 步仍未结束，请检查是否存在环");
    }

    private FlowNode passTarget(FlowInstance instance, FlowNode node) {
        FlowSkip skip = firstSkip(instance.getDefinitionId(), node.getNodeCode(), FlowSkipTypeEnum.PASS.getCode());
        return skip == null ? null : nodeByCode(instance.getDefinitionId(), skip.getNextNodeCode());
    }

    private FlowNode pickByCondition(FlowInstance instance, FlowNode node, JsonNode variables) {
        List<FlowSkip> skips = skipMapper.selectList(new LambdaQueryWrapper<FlowSkip>()
                .eq(FlowSkip::getDefinitionId, instance.getDefinitionId())
                .eq(FlowSkip::getNowNodeCode, node.getNodeCode())
                .eq(FlowSkip::getSkipType, FlowSkipTypeEnum.COND.getCode())
                .orderByAsc(FlowSkip::getSortNo));
        FlowSkip fallback = null;
        for (FlowSkip skip : skips) {
            if (conditionEvaluator.matches(skip.getConditionExpr(), variables)) {
                return nodeByCode(instance.getDefinitionId(), skip.getNextNodeCode());
            }
            if (fallback == null && !TextUtil.hasText(skip.getConditionExpr())) {
                fallback = skip;
            }
        }
        return fallback == null ? null : nodeByCode(instance.getDefinitionId(), fallback.getNextNodeCode());
    }

    private FlowSkip firstSkip(Long definitionId, String nodeCode, String skipType) {
        List<FlowSkip> skips = skipMapper.selectList(new LambdaQueryWrapper<FlowSkip>()
                .eq(FlowSkip::getDefinitionId, definitionId)
                .eq(FlowSkip::getNowNodeCode, nodeCode)
                .eq(FlowSkip::getSkipType, skipType)
                .orderByAsc(FlowSkip::getSortNo));
        return skips.isEmpty() ? null : skips.get(0);
    }

    private void move(FlowInstance instance, FlowNode from, FlowNode to) {
        String variableJson = instance.getVariableJson();
        int rows = instanceMapper.moveNode(instance.getId(), from.getNodeCode(), to.getNodeCode(),
                to.getNodeName(), to.getNodeType(), variableJson);
        if (rows == 0) {
            throw new BusinessException("流程已被其他操作推进，请刷新后重试");
        }
        instance.setNodeCode(to.getNodeCode());
        instance.setNodeName(to.getNodeName());
        instance.setNodeType(to.getNodeType());
        instance.setUpdateTime(LocalDateTime.now());
    }

    private void finishInstance(FlowInstance instance, FlowInstanceStatusEnum status,
                                Long operatorId, String operatorName, String message) {
        int rows = instanceMapper.finish(instance.getId(), status.getCode());
        if (rows == 0) {
            throw new BusinessException("流程已结束，请勿重复操作");
        }
        instance.setFlowStatus(status.getCode());
        instance.setActivityStatus(0);
        instance.setUpdateTime(LocalDateTime.now());
        insertHis(instance, null, currentNode(instance), status.getCode(),
                operatorId, operatorName, message);
    }

    private FlowNode currentNode(FlowInstance instance) {
        FlowNode node = nodeByCode(instance.getDefinitionId(), instance.getNodeCode());
        return node == null ? new FlowNode() : node;
    }

    // ---------------------------------------------------------------- AI 节点

    private JsonNode runAiNode(FlowInstance instance, FlowNode node, JsonNode variables,
                               Long operatorId, String operatorName) {
        ObjectNode input = (ObjectNode) conditionEvaluator.parse(node.getAiInputJson());
        input.set("variables", variables);

        FlowAiNodeRequestDTO request = new FlowAiNodeRequestDTO();
        request.setCapabilityKey(TextUtil.hasText(node.getAiCapabilityKey())
                ? node.getAiCapabilityKey() : DEFAULT_AI_CAPABILITY);
        request.setTemplateName(node.getAiTemplateName());
        request.setBizType(instance.getBizType());
        request.setBizId(parseLong(instance.getBusinessId()));
        request.setInputJson(conditionEvaluator.write(input));

        FlowAiNodeResultVO result = flowAiGatewayService.executeNode(request);
        if (result == null || !result.isSucceeded()) {
            String reason = result == null ? "AI 节点无返回" : result.getDegradeReason();
            insertHis(instance, null, node, "AUTO", operatorId, operatorName, "AI 节点降级：" + reason);
            return variables;
        }
        JsonNode merged = conditionEvaluator.mergeNodeOutput(variables, node.getNodeCode(), result.getPayload());
        insertHis(instance, null, node, "AUTO", operatorId, operatorName,
                TextUtil.cut(result.getPayload(), 200, ""));
        return merged;
    }

    private JsonNode runRouteNode(FlowInstance instance, FlowNode node, JsonNode variables,
                                  Long operatorId, String operatorName) {
        List<FlowIntentOptionDTO> options = new ArrayList<>();
        List<FlowSkip> condSkips = skipMapper.selectList(new LambdaQueryWrapper<FlowSkip>()
                .eq(FlowSkip::getDefinitionId, instance.getDefinitionId())
                .eq(FlowSkip::getNowNodeCode, node.getNodeCode())
                .eq(FlowSkip::getSkipType, FlowSkipTypeEnum.COND.getCode())
                .orderByAsc(FlowSkip::getSortNo));
        for (FlowSkip skip : condSkips) {
            FlowNode target = nodeByCode(instance.getDefinitionId(), skip.getNextNodeCode());
            if (target == null || !TextUtil.hasText(skip.getConditionExpr())
                    || "DEFAULT".equalsIgnoreCase(skip.getConditionExpr().trim())) {
                continue;
            }
            FlowIntentOptionDTO option = new FlowIntentOptionDTO();
            option.setFlowCode(target.getNodeCode());
            option.setFlowName(target.getNodeName());
            option.setDescription(target.getNodeName());
            options.add(option);
        }
        if (options.isEmpty()) {
            return variables;
        }

        FlowIntentRouteRequestDTO request = new FlowIntentRouteRequestDTO();
        request.setText(variables.hasNonNull(VAR_TEXT) ? variables.get(VAR_TEXT).asText() : "");
        request.setChannel(variables.hasNonNull(VAR_CHANNEL) ? variables.get(VAR_CHANNEL).asText() : "");
        request.setOptions(options);

        FlowIntentRouteResultVO result = flowAiGatewayService.routeIntent(request);
        if (result == null || !TextUtil.hasText(result.getFlowCode())) {
            insertHis(instance, null, node, "AUTO", operatorId, operatorName, "意图未识别，走兜底分支");
            return variables;
        }
        ObjectNode output = MAPPER.createObjectNode();
        output.put("flowCode", result.getFlowCode());
        output.put("confidence", result.getConfidence() == null ? 0D : result.getConfidence());
        output.put("reason", result.getReason());
        JsonNode merged = conditionEvaluator.mergeNodeOutput(variables, node.getNodeCode(),
                conditionEvaluator.write(output));
        insertHis(instance, null, node, "AUTO", operatorId, operatorName,
                "意图路由：" + result.getFlowCode() + "，" + TextUtil.cut(result.getReason(), 120, ""));
        return merged;
    }

    // ---------------------------------------------------------------- 待办

    private void createTasks(FlowInstance instance, FlowNode node) {
        String flag = node.getPermissionFlag();
        if (!TextUtil.hasText(flag)) {
            throw new BusinessException("审批节点「" + node.getNodeName() + "」未配置审批人");
        }
        int ratio = parseInt(node.getNodeRatio());
        List<String> assignees = new ArrayList<>();
        if (ratio <= 0) {
            // 或签：一个待办，任意候选人处理即完成
            assignees.add(flag.trim());
        } else {
            for (String entry : flag.split("@@")) {
                if (TextUtil.hasText(entry)) {
                    assignees.add(entry.trim());
                }
            }
        }
        for (String assignee : assignees) {
            FlowTask task = new FlowTask();
            task.setDefinitionId(instance.getDefinitionId());
            task.setInstanceId(instance.getId());
            task.setFlowCode(instance.getFlowCode());
            task.setFlowName(instance.getFlowName());
            task.setBusinessId(instance.getBusinessId());
            task.setNodeCode(node.getNodeCode());
            task.setNodeName(node.getNodeName());
            task.setNodeType(node.getNodeType());
            task.setTaskStatus(FlowTaskStatusEnum.PENDING.getCode());
            task.setAssigneeExpr(assignee);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.insert(task);
        }
        insertHis(instance, null, node, "TASK", null, null, "生成待办 " + assignees.size() + " 条");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(FlowTaskHandleDTO dto) {
        FlowTask task = mustPendingTask(dto.getTaskId());
        CurrentUser user = requireUser();
        assertCanHandle(task, user);
        if (taskMapper.passTask(task.getId(), operatorId(user), user.getRealName()) == 0) {
            throw new BusinessException("待办已被处理，请刷新");
        }

        FlowInstance instance = mustRunningInstance(task.getInstanceId());
        FlowNode node = mustNode(task.getDefinitionId(), task.getNodeCode());
        insertHis(instance, task, node, "PASS", operatorId(user), user.getRealName(),
                TextUtil.cut(dto.getMessage(), 200, ""));

        int passed = countTasks(task.getInstanceId(), task.getNodeCode(), FlowTaskStatusEnum.PASS);
        if (passed >= requiredPassCount(task, node)) {
            advance(instance, node, operatorId(user), user.getRealName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(FlowTaskHandleDTO dto) {
        FlowTask task = mustPendingTask(dto.getTaskId());
        CurrentUser user = requireUser();
        assertCanHandle(task, user);
        if (taskMapper.rejectTask(task.getId(), operatorId(user), user.getRealName()) == 0) {
            throw new BusinessException("待办已被处理，请刷新");
        }
        FlowInstance instance = mustRunningInstance(task.getInstanceId());
        taskMapper.cancelPendingByInstance(instance.getId());
        FlowNode node = mustNode(task.getDefinitionId(), task.getNodeCode());
        insertHis(instance, task, node, "REJECT", operatorId(user), user.getRealName(),
                TextUtil.cut(dto.getMessage(), 200, ""));

        // 驳回即终止：实例置已驳回，后续在业务侧发起新一轮流程
        int rows = instanceMapper.finish(instance.getId(), FlowInstanceStatusEnum.REJECTED.getCode());
        if (rows == 0) {
            throw new BusinessException("流程已结束，请勿重复操作");
        }
        instance.setFlowStatus(FlowInstanceStatusEnum.REJECTED.getCode());
        instance.setActivityStatus(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long instanceId) {
        FlowInstance instance = mustRunningInstance(instanceId);
        CurrentUser user = requireUser();
        if (!Objects.equals(instance.getStartedByUserId(), operatorId(user))) {
            throw new BusinessException("只有发起人可以撤回");
        }
        taskMapper.cancelPendingByInstance(instanceId);
        FlowNode node = currentNode(instance);
        insertHis(instance, null, node, "CANCELED", operatorId(user), user.getRealName(), "发起人撤回");
        if (instanceMapper.finish(instanceId, FlowInstanceStatusEnum.CANCELED.getCode()) == 0) {
            throw new BusinessException("流程已结束，请勿重复操作");
        }
        instance.setFlowStatus(FlowInstanceStatusEnum.CANCELED.getCode());
        instance.setActivityStatus(0);
    }

    private FlowTask mustPendingTask(Long taskId) {
        FlowTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("待办不存在");
        }
        if (!Objects.equals(FlowTaskStatusEnum.PENDING.getCode(), task.getTaskStatus())) {
            throw new BusinessException("待办已处理");
        }
        return task;
    }

    private FlowInstance mustRunningInstance(Long instanceId) {
        FlowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("流程实例不存在");
        }
        if (!Objects.equals(FlowInstanceStatusEnum.RUNNING.getCode(), instance.getFlowStatus())) {
            throw new BusinessException("流程已结束");
        }
        return instance;
    }

    private FlowNode mustNode(Long definitionId, String nodeCode) {
        FlowNode node = nodeByCode(definitionId, nodeCode);
        if (node == null) {
            throw new BusinessException("节点不存在：" + nodeCode);
        }
        return node;
    }

    private FlowNode nodeByCode(Long definitionId, String nodeCode) {
        return nodeMapper.selectOne(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getDefinitionId, definitionId)
                .eq(FlowNode::getNodeCode, nodeCode)
                .last("LIMIT 1"));
    }

    /**
     * 候选人表达式匹配：user 侧同时认员工ID与账号ID，role 只认本次生效角色。
     */
    private void assertCanHandle(FlowTask task, CurrentUser user) {
        if (!canHandle(task.getAssigneeExpr(), user)) {
            throw new BusinessException("该待办不属于当前用户");
        }
    }

    private boolean canHandle(String assigneeExpr, CurrentUser user) {
        if (!TextUtil.hasText(assigneeExpr) || user == null) {
            return false;
        }
        String userId = user.getUserId() == null ? "" : user.getUserId().toString();
        String employeeId = user.getEmployeeId() == null ? "" : user.getEmployeeId().toString();
        String role = user.getCurrentRole() == null ? "" : user.getCurrentRole();
        for (String entry : assigneeExpr.split("@@")) {
            String item = entry.trim();
            if (item.startsWith("user:") && (item.substring(5).equals(userId)
                    || item.substring(5).equals(employeeId))) {
                return true;
            }
            if (item.startsWith("role:") && item.substring(5).equals(role)) {
                return true;
            }
        }
        return false;
    }

    private int requiredPassCount(FlowTask task, FlowNode node) {
        int total = countTasks(task.getInstanceId(), task.getNodeCode(), null);
        int ratio = parseInt(node.getNodeRatio());
        if (ratio >= 100) {
            return total;
        }
        if (ratio <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(total * ratio / 100.0));
    }

    /**
     * @param status 为 null 时统计该节点全部待办
     */
    private int countTasks(Long instanceId, String nodeCode, FlowTaskStatusEnum status) {
        LambdaQueryWrapper<FlowTask> wrapper = new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getInstanceId, instanceId)
                .eq(FlowTask::getNodeCode, nodeCode);
        if (status != null) {
            wrapper.eq(FlowTask::getTaskStatus, status.getCode());
        }
        return taskMapper.selectCount(wrapper).intValue();
    }

    // ---------------------------------------------------------------- 查询

    @Override
    public PageResult<FlowTaskVO> listMyTasks(FlowTaskPageQueryDTO query) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录");
        }
        LambdaQueryWrapper<FlowTask> wrapper = new LambdaQueryWrapper<FlowTask>()
                .eq(FlowTask::getTaskStatus, FlowTaskStatusEnum.PENDING.getCode())
                .orderByDesc(FlowTask::getCreateTime);
        if (TextUtil.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(FlowTask::getFlowName, kw).or().like(FlowTask::getNodeName, kw));
        }
        Page<FlowTask> page = taskMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        // 候选人表达式要先按人过滤再分页，否则页内条数不稳；待办量按人收口后可控
        List<FlowTaskVO> mine = page.getRecords().stream()
                .filter(task -> canHandle(task.getAssigneeExpr(), user))
                .map(this::toTaskVO)
                .toList();
        return PageResult.of(mine.size(), page.getCurrent(), page.getSize(), page.getPages(), mine);
    }

    @Override
    public FlowInstanceVO getInstance(Long id) {
        FlowInstance instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw new BusinessException("流程实例不存在");
        }
        return toInstanceVO(instance);
    }

    @Override
    public List<FlowHistoryVO> listHistory(Long instanceId) {
        return hisTaskMapper.selectList(new LambdaQueryWrapper<FlowHisTask>()
                        .eq(FlowHisTask::getInstanceId, instanceId)
                        .orderByAsc(FlowHisTask::getCreateTime))
                .stream().map(this::toHistoryVO).toList();
    }

    @Override
    public List<FlowInstanceVO> listByBusinessId(String businessId) {
        if (!TextUtil.hasText(businessId)) {
            throw new BusinessException("业务单据ID不能为空");
        }
        return instanceMapper.selectList(new LambdaQueryWrapper<FlowInstance>()
                        .eq(FlowInstance::getBusinessId, businessId)
                        .orderByDesc(FlowInstance::getCreateTime))
                .stream().map(this::toInstanceVO).toList();
    }

    // ---------------------------------------------------------------- 公共

    private CurrentUser requireUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录");
        }
        return user;
    }

    private Long operatorId(CurrentUser user) {
        return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
    }

    private int parseInt(String text) {
        if (!TextUtil.hasText(text)) {
            return 0;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private Long parseLong(String text) {
        if (!TextUtil.hasText(text) || text.trim().length() > 19) {
            return null;
        }
        try {
            return Long.valueOf(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void insertHis(FlowInstance instance, FlowTask task, FlowNode node, String action,
                           Long operatorId, String operatorName, String message) {
        FlowHisTask his = new FlowHisTask();
        his.setDefinitionId(instance.getDefinitionId());
        his.setInstanceId(instance.getId());
        his.setTaskId(task == null ? null : task.getId());
        his.setBusinessId(instance.getBusinessId());
        his.setFlowCode(instance.getFlowCode());
        his.setNodeCode(node.getNodeCode());
        his.setNodeName(node.getNodeName());
        his.setNodeType(node.getNodeType());
        his.setAction(action);
        his.setApproverId(operatorId);
        his.setApproverName(operatorName);
        his.setMessage(message);
        his.setCreateTime(LocalDateTime.now());
        hisTaskMapper.insert(his);
    }

    private FlowInstanceVO toInstanceVO(FlowInstance instance) {
        FlowInstanceVO vo = new FlowInstanceVO();
        vo.setId(instance.getId());
        vo.setFlowCode(instance.getFlowCode());
        vo.setFlowName(instance.getFlowName());
        vo.setBusinessId(instance.getBusinessId());
        vo.setBizType(instance.getBizType());
        vo.setStartedByUserId(instance.getStartedByUserId());
        vo.setStartedByName(instance.getStartedByName());
        vo.setNodeCode(instance.getNodeCode());
        vo.setNodeName(instance.getNodeName());
        vo.setNodeType(instance.getNodeType());
        vo.setFlowStatus(instance.getFlowStatus());
        vo.setStatusLabel(FlowInstanceStatusEnum.getLabel(instance.getFlowStatus()));
        vo.setVariableJson(instance.getVariableJson());
        vo.setCreateTime(instance.getCreateTime());
        vo.setUpdateTime(instance.getUpdateTime());
        return vo;
    }

    private FlowTaskVO toTaskVO(FlowTask task) {
        FlowTaskVO vo = new FlowTaskVO();
        vo.setTaskId(task.getId());
        vo.setInstanceId(task.getInstanceId());
        vo.setFlowCode(task.getFlowCode());
        vo.setFlowName(task.getFlowName());
        vo.setBusinessId(task.getBusinessId());
        vo.setNodeCode(task.getNodeCode());
        vo.setNodeName(task.getNodeName());
        vo.setNodeType(task.getNodeType());
        vo.setTaskStatus(task.getTaskStatus());
        vo.setStatusLabel(FlowTaskStatusEnum.getLabel(task.getTaskStatus()));
        vo.setCreateTime(task.getCreateTime());
        return vo;
    }

    private FlowHistoryVO toHistoryVO(FlowHisTask his) {
        FlowHistoryVO vo = new FlowHistoryVO();
        vo.setNodeCode(his.getNodeCode());
        vo.setNodeName(his.getNodeName());
        vo.setNodeType(his.getNodeType());
        vo.setNodeTypeLabel(FlowNodeTypeEnum.getLabel(his.getNodeType()));
        vo.setAction(his.getAction());
        vo.setApproverId(his.getApproverId());
        vo.setApproverName(his.getApproverName());
        vo.setMessage(his.getMessage());
        vo.setCreateTime(his.getCreateTime());
        return vo;
    }

    /**
     * 意图候选的只读投影：避免在 router 里直接抓实体。
     */
    private record FlowDefinitionVOForRoute(String code, String name, String desc) {
    }
}
