package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysKnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库切块 Mapper。向量不落库，这里只存原文。
 */
@Mapper
public interface SysKnowledgeChunkMapper extends BaseMapper<SysKnowledgeChunk> {
}
