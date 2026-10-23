-- 17-药房药库（采购·库存·发药·调拨·盘点·静配） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_drug_package.sql;
SOURCE ./biz_drug_package_detail.sql;
SOURCE ./biz_drug_stock.sql;
