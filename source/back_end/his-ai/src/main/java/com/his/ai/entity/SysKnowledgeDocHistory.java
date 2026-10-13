package com.his.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档历史版本（留痕快照）。
 * <p>不继承 BaseEntity：本表无 del_flag，历史不可删，只追加；每次保存（新建/编辑/回退）落一条快照。
 */
@Data
@TableName("sys_knowledge_doc_history")
public class SysKnowledgeDocHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文档ID
     */
    private Long docId;

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
     * 版本号（如 v1.0 / 2026.09）
     */
    private String version;

    /**
     * 来源名称（诊疗指南/论坛/院内制度/药品说明书等）
     */
    private String sourceName;

    /**
     * 内容快照
     */
    private String content;

    /**
     * 变更类型（1-新建 2-编辑 3-回退）
     */
    private Integer changeType;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}
