package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizRxDoctorTalk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizRxDoctorTalkMapper extends BaseMapper<BizRxDoctorTalk> {

    /**
     * 同日约谈编号最大值（生成 YT+yyyyMMdd+4 位序号用）
     */
    @Select("SELECT MAX(talk_no) FROM biz_rx_doctor_talk WHERE talk_no LIKE CONCAT('YT', #{day}, '%')")
    String selectMaxTalkNo(@Param("day") String day);
}
