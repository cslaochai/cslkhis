-- ============================================================
-- 243-知识库文档版本与来源元数据.sql
-- 目的：sys_knowledge_doc 补充元数据（版本、来源名称）
--   - version（版本号）、source_name（来源名称）
-- 注：sys_knowledge_chunk 已在 244 中随表 DROP（切块事实源迁移至 Milvus），不再加列
-- 注意：一次性变更脚本，按编号顺序执行；老数据两列为 NULL，前端展示为空即可
-- ============================================================

-- sys_knowledge_doc.version  版本号（如 v1.0 / 2026.09）
ALTER TABLE `sys_knowledge_doc`
    ADD COLUMN `version`     varchar(50)  NULL COMMENT '版本号' AFTER `source_type`;

-- sys_knowledge_doc.source_name  内容来源（诊疗指南/论坛/院内制度/药品说明书等）
ALTER TABLE `sys_knowledge_doc`
    ADD COLUMN `source_name` varchar(100) NULL COMMENT '来源名称（诊疗指南/论坛/院内制度等）' AFTER `version`;
