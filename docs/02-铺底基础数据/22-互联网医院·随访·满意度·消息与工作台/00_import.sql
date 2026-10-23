-- 22-互联网医院·随访·满意度·消息与工作台 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_survey_item.sql;
SOURCE ./biz_survey_template.sql;
SOURCE ./sys_ai_prompt_template.sql;
SOURCE ./sys_ai_qa_column.sql;
SOURCE ./sys_ai_qa_relation.sql;
SOURCE ./sys_ai_qa_schema.sql;
SOURCE ./sys_ai_qa_semantic.sql;
SOURCE ./sys_alert_rule.sql;
SOURCE ./sys_faq.sql;
SOURCE ./sys_knowledge_doc.sql;
SOURCE ./sys_workbench_layout.sql;
SOURCE ./sys_workbench_role.sql;
SOURCE ./sys_workbench_widget.sql;
