package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizExamImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizExamImageMapper extends BaseMapper<BizExamImage> {

    /**
     * 物理删帧。
     *
     * <p>不能用 removeById：BaseEntity 的 del_flag 带 @TableLogic，那是软删，会留下
     * 「磁盘文件已删、界面看不见、行还在」的三不像（AGENTS §3）。留痕由审计日志承担。
     */
    @Delete("DELETE FROM biz_exam_image WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
