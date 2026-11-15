package com.his.workflow.service;

import com.his.common.base.PageResult;
import com.his.workflow.dto.FlowDefinitionPageQueryDTO;
import com.his.workflow.dto.FlowDefinitionUpsertDTO;
import com.his.workflow.vo.FlowDefinitionVO;

import java.util.List;

/**
 * 流程定义管理。
 */
public interface FlowDefinitionService {

    PageResult<FlowDefinitionVO> listPage(FlowDefinitionPageQueryDTO query);

    FlowDefinitionVO getDetailById(Long id);

    /**
     * 整图保存（元信息 + 节点 + 流转线），返回流程定义 ID。
     */
    Long upsert(FlowDefinitionUpsertDTO dto);

    /**
     * 发布：校验图完整性，状态置已发布、版本号递增。
     */
    void publish(Long id);

    /**
     * 删除（仅草稿；本表无 del_flag，走物理删）。
     */
    void deleteById(Long id);

    /**
     * 下拉：已发布流程，供意图路由候选与手工启动。
     */
    List<FlowDefinitionVO> selectList();
}
