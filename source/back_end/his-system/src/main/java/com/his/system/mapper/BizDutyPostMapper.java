package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizDutyPost;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 值守点位 Mapper。
 */
@Mapper
public interface BizDutyPostMapper extends BaseMapper<BizDutyPost> {

    /**
     * 物理删单行：本表唯一键是点位编码且不含删除标志，
     * 软删后复用同一个编码必撞重复键。
     */
    @Delete("DELETE FROM biz_duty_post WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
