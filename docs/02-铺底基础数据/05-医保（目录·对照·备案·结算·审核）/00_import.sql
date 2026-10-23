-- 05-医保（目录·对照·备案·结算·审核） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_yb_catalog.sql;
SOURCE ./biz_yb_catalog_rule.sql;
SOURCE ./biz_yb_chronic_catalog.sql;
SOURCE ./biz_yb_mapping.sql;
SOURCE ./biz_yb_policy.sql;
