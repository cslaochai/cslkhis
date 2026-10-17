package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizConsumableConsume;
import com.his.pharmacy.vo.BizConsumableConsumeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 耗材科室领用台账Mapper（台账自带耗材快照列，过滤直接走快照）
 */
@Mapper
public interface BizConsumableConsumeMapper extends BaseMapper<BizConsumableConsume> {

    /**
     * 领用台账分页（耗材名/领用单号模糊 + 科室ID过滤）
     * <p>{@code scopeDeptIds} 是科室数据权限（M6）的服务端收敛集合，前端不可见；
     * 与 {@code deptId} 显式筛选叠加（deptId 已先经越权校验）。
     */
    @Select("<script>" +
            "SELECT * FROM biz_consumable_consume WHERE del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (consumable_name LIKE CONCAT('%', #{keyword}, '%') OR consume_no LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='deptId != null'> AND dept_id = #{deptId} </if> " +
            "<if test='scopeDeptIds != null and scopeDeptIds.size() > 0'> AND dept_id IN <foreach collection='scopeDeptIds' item='sd' open='(' separator=',' close=')'>#{sd}</foreach> </if> " +
            "ORDER BY consume_time DESC, id DESC" +
            "</script>")
    Page<BizConsumableConsumeVO> selectConsumePage(Page<BizConsumableConsumeVO> page,
                                                   @Param("keyword") String keyword,
                                                   @Param("deptId") Long deptId,
                                                   @Param("scopeDeptIds") java.util.List<Long> scopeDeptIds);
}
