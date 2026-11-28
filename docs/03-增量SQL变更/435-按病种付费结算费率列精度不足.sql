-- 435-按病种付费结算费率列精度不足（DRG 结算必然报「系统内部错误」）.sql
-- 背景：drg.rate 演示值 12000.000000（元/权重），而 biz_medpay_settle.rate 原为 decimal(10,6)，
--       整数位只放得下 4 位（最大 9999.999999），插入即 MysqlDataTruncation: Out of range value for column 'rate'，
--       接口统一报「系统内部错误」，所有 DRG 正式结算全部失败。
-- 影响：/report/medpay/settle、结算工作台按病种付费结果落库。
-- 处置：整数位扩到 6 位（费率上限 999999.999999），保留 6 位小数；不动历史数据。

ALTER TABLE biz_medpay_settle MODIFY COLUMN rate decimal(12,6) COMMENT '费率（元/权重）';
