package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysKnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper。唯一键不含 del_flag 的字段，整表替换走 MP 默认软删即可。
 */
@Mapper
public interface SysKnowledgeDocMapper extends BaseMapper<SysKnowledgeDoc> {
}
