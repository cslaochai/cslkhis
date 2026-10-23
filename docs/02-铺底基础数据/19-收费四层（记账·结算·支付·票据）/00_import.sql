-- 19-收费四层（记账·结算·支付·票据） 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_arrears_policy.sql;
SOURCE ./biz_fund_account.sql;
