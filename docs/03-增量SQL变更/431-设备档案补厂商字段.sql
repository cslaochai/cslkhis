-- 431-设备档案补厂商（复用全院供应商表 sys_supplier）
--
-- 背景：设备建档只有「品牌 + 型号」，没有供货厂商。品牌是产品厂牌（西门子/飞利浦），
--      厂商是跟谁买的（国药控股/上药/华润），采购对账、走保修、追资质证照都靠后者。
--      全院已有一张通用供应商表 sys_supplier（药品入库、耗材库存、采购单都在引用），
--      同一家公司既供药品又供设备是常态，因此**不新建设备厂商表**，设备档案按现有惯例
--      存「supplier_id + supplier_name 快照」两列，下拉取 /supplier/selectList（登录即可，不挂药品权限码）。
--
-- 列口径：supplier_name 是建档时的名称快照——供应商改名不动历史档案，与 biz_drug_stock 同口径。

ALTER TABLE sys_equipment
    ADD COLUMN supplier_id BIGINT NULL COMMENT '供应商ID' AFTER model,
    ADD COLUMN supplier_name VARCHAR(128) NULL COMMENT '供应商名称（快照）' AFTER supplier_id;

-- 回查
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = 'hn_biz_his' AND TABLE_NAME = 'sys_equipment'
--   AND COLUMN_NAME IN ('supplier_id', 'supplier_name');
