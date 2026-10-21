package com.his.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.app.entity.BizServiceMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者端留言读写。
 */
@Mapper
public interface MiniServiceMessageMapper extends BaseMapper<BizServiceMessage> {
}
