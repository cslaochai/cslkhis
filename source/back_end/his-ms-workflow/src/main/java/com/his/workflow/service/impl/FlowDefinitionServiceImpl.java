package com.his.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.FlowDefinitionStatusEnum;
import com.his.common.enums.FlowNodeTypeEnum;
import com.his.common.enums.FlowSkipTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.workflow.dto.FlowDefinitionPageQueryDTO;
import com.his.workflow.dto.FlowDefinitionUpsertDTO;
import com.his.workflow.dto.FlowNodeUpsertDTO;
import com.his.workflow.dto.FlowSkipUpsertDTO;
import com.his.workflow.entity.FlowDefinition;
import com.his.workflow.entity.FlowNode;
import com.his.workflow.entity.FlowSkip;
import com.his.workflow.mapper.FlowDefinitionMapper;
import com.his.workflow.mapper.FlowNodeMapper;
import com.his.workflow.mapper.FlowSkipMapper;
import com.his.workflow.service.FlowDefinitionService;
import com.his.workflow.vo.FlowDefinitionVO;
import com.his.workflow.vo.FlowNodeVO;
import com.his.workflow.vo.FlowSkipVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDefinitionServiceImpl extends ServiceImpl<FlowDefinitionMapper, FlowDefinition>
        implements FlowDefinitionService {

    private final FlowDefinitionMapper definitionMapper;
    private final FlowNodeMapper nodeMapper;
    private final FlowSkipMapper skipMapper;

    @Override
    public PageResult<FlowDefinitionVO> listPage(FlowDefinitionPageQueryDTO query) {
        LambdaQueryWrapper<FlowDefinition> wrapper = new LambdaQueryWrapper<FlowDefinition>()
                .eq(query.getStatus() != null, FlowDefinition::getStatus, query.getStatus())
                .orderByDesc(FlowDefinition::getUpdateTime);
        if (TextUtil.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(FlowDefinition::getFlowCode, kw)
                    .or().like(FlowDefinition::getFlowName, kw));
        }
        Page<FlowDefinition> page = definitionMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<FlowDefinitionVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public FlowDefinitionVO getDetailById(Long id) {
        FlowDefinition definition = definitionMapper.selectById(id);
        if (definition == null) {
            throw new BusinessException("流程定义不存在");
        }
        FlowDefinitionVO vo = toVO(definition);
        vo.setNodes(nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                        .eq(FlowNode::getDefinitionId, id)
                        .orderByAsc(FlowNode::getSortNo))
                .stream().map(this::toNodeVO).toList());
        vo.setSkips(skipMapper.selectList(new LambdaQueryWrapper<FlowSkip>()
                        .eq(FlowSkip::getDefinitionId, id)
                        .orderByAsc(FlowSkip::getSortNo))
                .stream().map(this::toSkipVO).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(FlowDefinitionUpsertDTO dto) {
        String flowCode = dto.getFlowCode().trim();
        FlowDefinition definition;
        if (dto.getId() == null) {
            assertFlowCodeUnique(flowCode, null);
            definition = new FlowDefinition();
            definition.setFlowCode(flowCode);
            definition.setStatus(FlowDefinitionStatusEnum.DRAFT.getCode());
            definition.setVersion(1);
            definition.setCreateTime(LocalDateTime.now());
        } else {
            definition = definitionMapper.selectById(dto.getId());
            if (definition == null) {
                throw new BusinessException("流程定义不存在");
            }
            assertFlowCodeUnique(flowCode, definition.getId());
        }
        definition.setFlowName(dto.getFlowName().trim());
        definition.setFlowDesc(dto.getFlowDesc());
        definition.setUpdateTime(LocalDateTime.now());
        definition.setModelJson(toModelJson(dto));
        if (dto.getId() == null) {
            definitionMapper.insert(definition);
        } else {
            definitionMapper.updateById(definition);
            // 图整体替换：节点/流转线无留档价值，走物理删（两表唯一键都不含 del_flag）
            nodeMapper.delete(new LambdaQueryWrapper<FlowNode>().eq(FlowNode::getDefinitionId, definition.getId()));
            skipMapper.delete(new LambdaQueryWrapper<FlowSkip>().eq(FlowSkip::getDefinitionId, definition.getId()));
        }
        saveNodes(definition.getId(), dto.getNodes());
        saveSkips(definition.getId(), dto.getSkips());
        return definition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        FlowDefinition definition = definitionMapper.selectById(id);
        if (definition == null) {
            throw new BusinessException("流程定义不存在");
        }
        List<FlowNode> nodes = nodeMapper.selectList(new LambdaQueryWrapper<FlowNode>()
                .eq(FlowNode::getDefinitionId, id));
        List<FlowSkip> skips = skipMapper.selectList(new LambdaQueryWrapper<FlowSkip>()
                .eq(FlowSkip::getDefinitionId, id));
        validateGraph(nodes, skips);

        definition.setStatus(FlowDefinitionStatusEnum.PUBLISHED.getCode());
        definition.setVersion(definition.getVersion() == null ? 1 : definition.getVersion() + 1);
        definition.setUpdateTime(LocalDateTime.now());
        definitionMapper.updateById(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        FlowDefinition definition = definitionMapper.selectById(id);
        if (definition == null) {
            return;
        }
        if (!Objects.equals(FlowDefinitionStatusEnum.DRAFT.getCode(), definition.getStatus())) {
            throw new BusinessException("仅草稿流程可删除");
        }
        nodeMapper.delete(new LambdaQueryWrapper<FlowNode>().eq(FlowNode::getDefinitionId, id));
        skipMapper.delete(new LambdaQueryWrapper<FlowSkip>().eq(FlowSkip::getDefinitionId, id));
        definitionMapper.deleteById(id);
    }

    @Override
    public List<FlowDefinitionVO> selectList() {
        return definitionMapper.selectList(new LambdaQueryWrapper<FlowDefinition>()
                        .eq(FlowDefinition::getStatus, FlowDefinitionStatusEnum.PUBLISHED.getCode())
                        .orderByAsc(FlowDefinition::getFlowCode))
                .stream().map(this::toVO).toList();
    }

    // ---------------------------------------------------------------- 校验

    private void assertFlowCodeUnique(String flowCode, Long excludeId) {
        FlowDefinition exists = definitionMapper.selectOne(new LambdaQueryWrapper<FlowDefinition>()
                .eq(FlowDefinition::getFlowCode, flowCode)
                .last("LIMIT 1"));
        if (exists != null && !Objects.equals(exists.getId(), excludeId)) {
            throw new BusinessException("流程编码已存在：" + flowCode);
        }
    }

    /**
     * 发布前校验：一张能跑的图必须「一进、至少一出、所有引用闭合」。
     */
    private void validateGraph(List<FlowNode> nodes, List<FlowSkip> skips) {
        List<FlowNode> starts = nodes.stream()
                .filter(n -> Objects.equals(FlowNodeTypeEnum.START.getCode(), n.getNodeType())).toList();
        List<FlowNode> ends = nodes.stream()
                .filter(n -> Objects.equals(FlowNodeTypeEnum.END.getCode(), n.getNodeType())).toList();
        if (starts.size() != 1) {
            throw new BusinessException("流程必须有且仅有一个开始节点");
        }
        if (ends.isEmpty()) {
            throw new BusinessException("流程至少需要一个结束节点");
        }
        for (FlowNode node : nodes) {
            if (Objects.equals(FlowNodeTypeEnum.END.getCode(), node.getNodeType())) {
                continue;
            }
            boolean hasOut = skips.stream().anyMatch(s -> node.getNodeCode().equals(s.getNowNodeCode()));
            if (!hasOut) {
                throw new BusinessException("节点「" + node.getNodeName() + "」没有出口");
            }
        }
        for (FlowSkip skip : skips) {
            boolean nowExists = nodes.stream().anyMatch(n -> n.getNodeCode().equals(skip.getNowNodeCode()));
            boolean nextExists = nodes.stream().anyMatch(n -> n.getNodeCode().equals(skip.getNextNodeCode()));
            if (!nowExists || !nextExists) {
                throw new BusinessException("流转线引用了不存在的节点：" + skip.getNowNodeCode()
                        + " → " + skip.getNextNodeCode());
            }
        }
    }

    // ---------------------------------------------------------------- 落库与转换

    private void saveNodes(Long definitionId, List<FlowNodeUpsertDTO> nodes) {
        List<FlowNodeUpsertDTO> list = nodes == null ? new ArrayList<>() : nodes;
        for (int i = 0; i < list.size(); i++) {
            FlowNodeUpsertDTO dto = list.get(i);
            FlowNode node = new FlowNode();
            node.setDefinitionId(definitionId);
            node.setNodeCode(dto.getNodeCode().trim());
            node.setNodeName(dto.getNodeName().trim());
            node.setNodeType(dto.getNodeType());
            node.setPermissionFlag(dto.getPermissionFlag());
            node.setNodeRatio(dto.getNodeRatio());
            node.setAiAction(dto.getAiAction());
            node.setAiCapabilityKey(dto.getAiCapabilityKey());
            node.setAiTemplateName(dto.getAiTemplateName());
            node.setAiInputJson(dto.getAiInputJson());
            node.setCoordinate(dto.getCoordinate());
            node.setSortNo(dto.getSortNo() == null ? i : dto.getSortNo());
            nodeMapper.insert(node);
        }
    }

    private void saveSkips(Long definitionId, List<FlowSkipUpsertDTO> skips) {
        List<FlowSkipUpsertDTO> list = skips == null ? new ArrayList<>() : skips;
        for (int i = 0; i < list.size(); i++) {
            FlowSkipUpsertDTO dto = list.get(i);
            FlowSkip skip = new FlowSkip();
            skip.setDefinitionId(definitionId);
            skip.setNowNodeCode(dto.getNowNodeCode().trim());
            skip.setNextNodeCode(dto.getNextNodeCode().trim());
            skip.setSkipType(dto.getSkipType());
            skip.setConditionExpr(dto.getConditionExpr());
            skip.setSortNo(dto.getSortNo() == null ? i : dto.getSortNo());
            skipMapper.insert(skip);
        }
    }

    /**
     * 整图快照：仅用于回看与比对，执行只认节点/流转线两张表。
     */
    private String toModelJson(FlowDefinitionUpsertDTO dto) {
        StringBuilder sb = new StringBuilder("{\"nodes\":[");
        for (int i = 0; i < dto.getNodes().size(); i++) {
            FlowNodeUpsertDTO n = dto.getNodes().get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"code\":\"").append(escape(n.getNodeCode()))
                    .append("\",\"name\":\"").append(escape(n.getNodeName()))
                    .append("\",\"type\":").append(n.getNodeType()).append('}');
        }
        sb.append("],\"skips\":[");
        for (int i = 0; i < dto.getSkips().size(); i++) {
            FlowSkipUpsertDTO s = dto.getSkips().get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"from\":\"").append(escape(s.getNowNodeCode()))
                    .append("\",\"to\":\"").append(escape(s.getNextNodeCode()))
                    .append("\",\"type\":\"").append(escape(s.getSkipType())).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private FlowDefinitionVO toVO(FlowDefinition definition) {
        FlowDefinitionVO vo = new FlowDefinitionVO();
        vo.setId(definition.getId());
        vo.setFlowCode(definition.getFlowCode());
        vo.setFlowName(definition.getFlowName());
        vo.setFlowDesc(definition.getFlowDesc());
        vo.setStatus(definition.getStatus());
        vo.setStatusLabel(FlowDefinitionStatusEnum.getLabel(definition.getStatus()));
        vo.setVersion(definition.getVersion());
        vo.setUpdateTime(definition.getUpdateTime());
        return vo;
    }

    private FlowNodeVO toNodeVO(FlowNode node) {
        FlowNodeVO vo = new FlowNodeVO();
        vo.setId(node.getId());
        vo.setNodeCode(node.getNodeCode());
        vo.setNodeName(node.getNodeName());
        vo.setNodeType(node.getNodeType());
        vo.setNodeTypeLabel(FlowNodeTypeEnum.getLabel(node.getNodeType()));
        vo.setPermissionFlag(node.getPermissionFlag());
        vo.setNodeRatio(node.getNodeRatio());
        vo.setAiAction(node.getAiAction());
        vo.setAiCapabilityKey(node.getAiCapabilityKey());
        vo.setAiTemplateName(node.getAiTemplateName());
        vo.setAiInputJson(node.getAiInputJson());
        vo.setCoordinate(node.getCoordinate());
        vo.setSortNo(node.getSortNo());
        return vo;
    }

    private FlowSkipVO toSkipVO(FlowSkip skip) {
        FlowSkipVO vo = new FlowSkipVO();
        vo.setId(skip.getId());
        vo.setNowNodeCode(skip.getNowNodeCode());
        vo.setNextNodeCode(skip.getNextNodeCode());
        vo.setSkipType(skip.getSkipType());
        vo.setConditionExpr(skip.getConditionExpr());
        vo.setSortNo(skip.getSortNo());
        return vo;
    }
}
