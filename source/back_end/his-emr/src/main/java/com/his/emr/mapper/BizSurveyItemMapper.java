package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizSurveyItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 满意度问卷题目 Mapper。
 */
@Mapper
public interface BizSurveyItemMapper extends BaseMapper<BizSurveyItem> {

    /**
     * 整卷覆盖前物理删旧题。
     *
     * <p>必须物理删：唯一键 uk_survey_item(template_id,seq_no) 不含 del_flag，
     * MP 的 remove(wrapper) 是软删，留下的行仍占着题号，「先清再插」的第二步必然
     * Duplicate entry（L12 踩过的坑，配置页必现 500）。题目没有留档价值，不要舍不得删。
     */
    @Delete("DELETE FROM biz_survey_item WHERE template_id = #{templateId}")
    int purgeByTemplate(@Param("templateId") Long templateId);

    /**
     * 模板下的题目（按题号升序，出题与回收都按它排）
     */
    @Select("SELECT * FROM biz_survey_item WHERE del_flag = 0 AND template_id = #{templateId} "
            + "ORDER BY seq_no ASC, id ASC")
    List<BizSurveyItem> selectByTemplate(@Param("templateId") Long templateId);

    /**
     * 一次捞多张卷的题目数（列表页那一列，避免逐行 count 的 N+1）
     */
    @Select("""
            <script>
            SELECT template_id AS t, COUNT(*) AS c FROM biz_survey_item
             WHERE del_flag = 0 AND template_id IN
             <foreach collection="templateIds" item="tid" open="(" separator="," close=")">#{tid}</foreach>
             GROUP BY template_id
            </script>
            """)
    List<Map<String, Object>> countByTemplates(@Param("templateIds") List<Long> templateIds);
}
