package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysKnowledgeDocHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档历史版本 Mapper。历史只追加不删除，无需 purge。
 */
@Mapper
public interface SysKnowledgeDocHistoryMapper extends BaseMapper<SysKnowledgeDocHistory> {
}
