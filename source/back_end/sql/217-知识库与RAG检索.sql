-- 217 知识库与 RAG 检索
-- 开发环境方案：文本落这两张表，向量存内存（启动时从 chunk 表重建索引）。
-- 唯一键均含 del_flag，无"不含 del_flag 的唯一键"，因此整表替换走 MP 默认软删即可，无需物理删。
-- 将来接 Milvus 2.5 时，sys_knowledge_chunk.content 仍保留原文，向量迁移到向量库。

CREATE TABLE IF NOT EXISTS sys_knowledge_doc (
  id           BIGINT       NOT NULL                COMMENT '文档ID（雪花）',
  title        VARCHAR(200) NOT NULL                COMMENT '文档标题',
  category     VARCHAR(50)  DEFAULT ''              COMMENT '分类（就诊须知/科室介绍/检查注意事项/药品说明书）',
  source_type  TINYINT      DEFAULT 1               COMMENT '来源类型（1-内置示例 2-手工录入 3-文件导入）',
  content      LONGTEXT                              COMMENT '原始全文（便于回溯与重新切块）',
  chunk_count  INT          DEFAULT 0               COMMENT '切块数量',
  status       TINYINT      DEFAULT 0               COMMENT '状态（0-正常 1-停用）',
  create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
  create_time  DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
  update_time  DATETIME     DEFAULT NULL            COMMENT '更新时间',
  del_flag     TINYINT      DEFAULT 0               COMMENT '删除标志（0-正常 1-删除）',
  remark       VARCHAR(500) DEFAULT ''              COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

CREATE TABLE IF NOT EXISTS sys_knowledge_chunk (
  id           BIGINT       NOT NULL                COMMENT '切块ID（雪花）',
  doc_id       BIGINT       NOT NULL                COMMENT '所属文档ID',
  doc_title    VARCHAR(200) DEFAULT ''              COMMENT '文档标题（冗余，便于检索结果展示）',
  category     VARCHAR(50)  DEFAULT ''              COMMENT '分类（冗余）',
  chunk_index  INT          DEFAULT 0               COMMENT '块序号',
  content      LONGTEXT                              COMMENT '切块文本',
  create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
  create_time  DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
  update_time  DATETIME     DEFAULT NULL            COMMENT '更新时间',
  del_flag     TINYINT      DEFAULT 0               COMMENT '删除标志（0-正常 1-删除）',
  remark       VARCHAR(500) DEFAULT ''              COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库切块（向量在内存，文本在此）';
