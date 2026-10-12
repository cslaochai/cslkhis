-- ============================================================
-- 246-AI运营问数列类型列.sql
-- 前置：245 已执行（sys_ai_qa_column 已建）
-- 说明：重写刷新（扫描 information_schema）需要保存真实列类型，提示词输出类型帮助模型生成 SQL
-- 幂等：用 information_schema 判断，重复执行不报错
-- ============================================================

-- 1. sys_ai_qa_column 加 column_type（幂等）
SET @db = DATABASE();
SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_ai_qa_column'
              AND COLUMN_NAME = 'column_type'
        ),
        'SELECT 1',
        'ALTER TABLE sys_ai_qa_column ADD COLUMN column_type varchar(64) NULL COMMENT ''列类型（来自 information_schema data_type）'' AFTER column_desc'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
