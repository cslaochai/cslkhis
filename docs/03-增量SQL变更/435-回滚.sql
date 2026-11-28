-- 435-回滚.sql
-- 回滚 rate 列精度（注意：回滚前须确认库内 rate 均 <= 9999.999999，否则 ALTER 会被 MySQL 拒绝）

ALTER TABLE biz_medpay_settle MODIFY COLUMN rate decimal(10,6) COMMENT '费率（元/权重）';
