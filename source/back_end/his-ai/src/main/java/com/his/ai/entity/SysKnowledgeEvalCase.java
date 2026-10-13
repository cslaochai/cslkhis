package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库评测用例（软删，继承逻辑删除由 del_flag 承担）。
 */
@Data
@TableName("sys_knowledge_eval_case")
public class SysKnowledgeEvalCase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 评测问题 */
    private String question;

    /** 期望命中文档ID（分号分隔） */
    private String expectedDocIds;

    /** 期望命中文档标题（分号分隔，展示用） */
    private String expectedDocTitles;

    /** 说明 */
    private String remark;

    /** 逻辑删除（0-正常 1-删除） */
    private Integer delFlag;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;
}
