package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizSurveyAnswerItem;
import com.his.emr.vo.SurveyAnswerItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 满意度逐题答案 Mapper。
 */
@Mapper
public interface BizSurveyAnswerItemMapper extends BaseMapper<BizSurveyAnswerItem> {

    /**
     * 重填前物理删旧明细：唯一键 uk_survey_answer_item(answer_id,item_id) 不含 del_flag，
     * 软删留下的行仍占着题目，重写同一题必然 Duplicate entry。
     */
    @Delete("DELETE FROM biz_survey_answer_item WHERE answer_id = #{answerId}")
    int purgeByAnswer(@Param("answerId") Long answerId);

    /**
     * 答卷明细（按题号升序，详情与重算都用它）
     */
    @Select("SELECT * FROM biz_survey_answer_item WHERE del_flag = 0 AND answer_id = #{answerId} "
            + "ORDER BY seq_no ASC, id ASC")
    List<SurveyAnswerItemVO> selectByAnswer(@Param("answerId") Long answerId);
}
