-- 21-专项质控与专科管理（路径·抗菌药物·VTE·营养·体检·透析·监护） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_pathway.sql;
SOURCE ./biz_pathway_step.sql;
