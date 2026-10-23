-- 09-住院与医嘱（入出转·会诊·床位调度） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_inpatient_order_template.sql;
SOURCE ./biz_inpatient_order_template_item.sql;
