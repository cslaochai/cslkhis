package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_knowledge_doc")
public class SysKnowledgeDoc extends BaseEntity {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 分类（就诊须知/科室介绍/检查注意事项/药品说明书）
     */
    private String category;

    /**
     * 来源类型（1-内置示例 2-手工录入 3-文件导入）
     */
    private Integer sourceType;

    /**
     * 原始全文
     */
    private String content;

    /**
     * 切块数量
     */
    private Integer chunkCount;

    /**
     * 状态（0-正常 1-停用）
     */
    private Integer status;
}
