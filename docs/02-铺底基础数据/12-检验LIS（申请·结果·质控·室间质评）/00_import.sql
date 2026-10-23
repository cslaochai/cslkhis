-- 12-检验LIS（申请·结果·质控·室间质评） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_laboratory_template.sql;
SOURCE ./biz_lis_qc_plan.sql;
SOURCE ./sys_lab_plain_item.sql;
