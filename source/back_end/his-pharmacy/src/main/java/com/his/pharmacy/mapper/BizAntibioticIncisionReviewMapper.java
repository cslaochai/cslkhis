package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizAntibioticIncisionReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizAntibioticIncisionReviewMapper extends BaseMapper<BizAntibioticIncisionReview> {

    /** 同日点评编号最大值（生成 KQI+yyyyMMdd+4位序号用） */
    @Select("SELECT MAX(review_no) FROM biz_antibiotic_incision_review WHERE review_no LIKE CONCAT('KQI', #{day}, '%')")
    String selectMaxReviewNo(@Param("day") String day);
}
