package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 知识库切块。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_knowledge_chunk")
public class SysKnowledgeChunk extends BaseEntity {

    /**
     * 所属文档ID
     */
    private Long docId;

    /**
     * 文档标题（冗余，便于检索结果展示）
     */
    private String docTitle;

    /**
     * 分类（冗余）
     */
    private String category;

    /**
     * 块序号
     */
    private Integer chunkIndex;

    /**
     * 切块文本
     */
    private String content;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
