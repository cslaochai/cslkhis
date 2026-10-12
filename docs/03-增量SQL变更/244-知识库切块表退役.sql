-- ============================================================
-- 244-知识库切块表退役.sql
-- 目的：sys_knowledge_chunk 表删除（切块事实源迁移至 Milvus）
-- 背景：知识库向量索引已由进程内存升级为 Milvus 持久化（his.ai.rag.vector-store=milvus，
--       集合 his_knowledge_chunk，文本+向量+元数据整体入库，重启不丢）。
--       sys_knowledge_chunk 原为"向量在内存、文本在 MySQL"时代的文本事实源；
--       迁移后文本留档由 sys_knowledge_doc.content 承担（重建从 doc 全文重新切分，同口径），
--       该表不再有任何读写方，故整体 DROP。
-- 注意：一次性变更脚本，按编号顺序执行；需在 243（doc 加列）之后执行。
--       执行前确认 his-ai 已升级到"Milvus 持久化 + rebuild 从 doc 重建"的代码版本，
--       否则旧代码仍会读写本表导致运行时错误。
-- ============================================================

DROP TABLE IF EXISTS `sys_knowledge_chunk`;
