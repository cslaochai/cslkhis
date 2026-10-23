-- 08-门诊病历与处方 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_diag_template.sql;
SOURCE ./biz_rx_template.sql;
SOURCE ./biz_rx_template_detail.sql;
