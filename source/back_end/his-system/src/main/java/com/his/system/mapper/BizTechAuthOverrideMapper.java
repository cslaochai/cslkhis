package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizTechAuthOverride;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizTechAuthOverrideMapper extends BaseMapper<BizTechAuthOverride> {

    /**
     * 物理删：本表无 del_flag；此方法只给验证脚本清理夹具用（越权登记本身不允许业务侧删除）。
     */
    @Delete("DELETE FROM biz_tech_auth_override WHERE source_type = #{sourceType} AND source_id = #{sourceId}")
    int purgeBySource(@Param("sourceType") Integer sourceType, @Param("sourceId") Long sourceId);
}
