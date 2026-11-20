-- 24-工作流（流程定义·实例·待办） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./sys_flow_definition.sql;
SOURCE ./sys_flow_node.sql;
SOURCE ./sys_flow_skip.sql;
