package com.his.miniapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.miniapp.entity.BizServiceMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者端留言读写。
 */
@Mapper
public interface MiniServiceMessageMapper extends BaseMapper<BizServiceMessage> {
}
