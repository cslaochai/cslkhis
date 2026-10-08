package com.his.miniapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.miniapp.entity.BizServiceMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 患者端留言读写。
 */
@Mapper
public interface MiniServiceMessageMapper extends BaseMapper<BizServiceMessage> {

    /**
     * 当天已用的最大单号。
     *
     * @param prefix 形如 MSG20261003
     */
    @Select("SELECT MAX(message_no) FROM biz_service_message WHERE message_no LIKE CONCAT(#{prefix}, '%')")
    String maxMessageNo(@Param("prefix") String prefix);
}
