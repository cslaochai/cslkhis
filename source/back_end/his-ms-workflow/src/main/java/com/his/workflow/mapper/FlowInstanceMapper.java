package com.his.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.workflow.entity.FlowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FlowInstanceMapper extends BaseMapper<FlowInstance> {

    /**
     * 前进一步：带上「当前节点」做条件更新，两个线程同时推进只有一条能改成功。
     */
    @Update("UPDATE biz_flow_instance SET node_code = #{nextCode}, node_name = #{nextName}, node_type = #{nextType}, "
            + "variable_json = #{variableJson}, update_time = NOW() "
            + "WHERE id = #{id} AND node_code = #{fromCode} AND flow_status = 'RUNNING'")
    int moveNode(@Param("id") Long id,
                 @Param("fromCode") String fromCode,
                 @Param("nextCode") String nextCode,
                 @Param("nextName") String nextName,
                 @Param("nextType") Integer nextType,
                 @Param("variableJson") String variableJson);

    /**
     * 结束实例：RUNNING → 终态，重复调用返回 0。
     */
    @Update("UPDATE biz_flow_instance SET flow_status = #{status}, activity_status = 0, update_time = NOW() "
            + "WHERE id = #{id} AND flow_status = 'RUNNING'")
    int finish(@Param("id") Long id, @Param("status") String status);

    /**
     * 只更新变量（自动节点写回输出，不推进节点）。
     */
    @Update("UPDATE biz_flow_instance SET variable_json = #{variableJson}, update_time = NOW() "
            + "WHERE id = #{id} AND flow_status = 'RUNNING'")
    int saveVariable(@Param("id") Long id, @Param("variableJson") String variableJson);
}
