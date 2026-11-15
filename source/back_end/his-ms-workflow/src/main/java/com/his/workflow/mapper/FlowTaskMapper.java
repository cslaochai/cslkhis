package com.his.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.workflow.entity.FlowTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FlowTaskMapper extends BaseMapper<FlowTask> {

    /**
     * 审批通过：PENDING → PASS，条件更新保证同一待办只能被处理一次。
     */
    @Update("UPDATE biz_flow_task SET task_status = 'PASS', assignee_user_id = #{userId}, assignee_name = #{userName}, "
            + "update_time = NOW() WHERE id = #{id} AND task_status = 'PENDING'")
    int passTask(@Param("id") Long id, @Param("userId") Long userId, @Param("userName") String userName);

    /**
     * 驳回：PENDING → REJECT。
     */
    @Update("UPDATE biz_flow_task SET task_status = 'REJECT', assignee_user_id = #{userId}, assignee_name = #{userName}, "
            + "update_time = NOW() WHERE id = #{id} AND task_status = 'PENDING'")
    int rejectTask(@Param("id") Long id, @Param("userId") Long userId, @Param("userName") String userName);

    /**
     * 作废同实例其余待办（驳回/撤回时调用）。
     */
    @Update("UPDATE biz_flow_task SET task_status = 'CANCELED', update_time = NOW() "
            + "WHERE instance_id = #{instanceId} AND task_status = 'PENDING'")
    int cancelPendingByInstance(@Param("instanceId") Long instanceId);
}
