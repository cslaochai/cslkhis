-- ============================================================================
-- 437 供应商经营范围（药品 / 器械设备 / 耗材 / 后勤物资）+ 送药机器人厂商落地
-- 背景：sys_supplier 是全院唯一的往来单位主档（药企与器械厂商共用一张表），
--       但表里没有"这家供什么"的字段，采购与设备两个场景的下拉只能混着列。
--       本脚本补经营范围列，并把 10 台送药机器人（擎朗 T5）指向厂商档案。
-- 注意：ADD COLUMN 无 IF NOT EXISTS，本脚本只能执行一次（重复执行会报 Duplicate column）。
-- ============================================================================

ALTER TABLE sys_supplier
    ADD COLUMN supplier_type TINYINT NOT NULL DEFAULT 4
        COMMENT '经营范围（1-药品 2-器械设备 3-耗材 4-后勤物资）' AFTER supplier_name,
    ADD KEY idx_supplier_type (supplier_type);

-- 主档回填：药品供应商 10 家（国药/上药/华润/广药/九州通/华北制药/扬子江/恒瑞/齐鲁/华东医药）
UPDATE sys_supplier
   SET supplier_type = 1, update_by = 'sql437', update_time = NOW()
 WHERE del_flag = 0
   AND supplier_code IN ('SUP001', 'SUP002', 'SUP003', 'SUP004', 'SUP005',
                         'SUP006', 'SUP007', 'SUP008', 'SUP009', 'SUP010');

-- 器械设备厂商 5 家（迈瑞/威高/乐普/科华/鱼跃）
UPDATE sys_supplier
   SET supplier_type = 2, update_by = 'sql437', update_time = NOW()
 WHERE del_flag = 0
   AND supplier_code IN ('SUP011', 'SUP012', 'SUP013', 'SUP014', 'SUP015');

-- 送药机器人厂商：上海擎朗智能科技有限公司（品牌「擎朗智能」T5，服务 10 台 RBT）
-- 证照号由设备科按实际器械注册证补录，这里不编造
INSERT INTO sys_supplier
    (supplier_id, supplier_code, supplier_name, supplier_type, contact_person, phone, address,
     license_no, license_expiry, rating, status, remark,
     create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 4370000000000000001, 'SUP016', '上海擎朗智能科技有限公司', 2, NULL, NULL, NULL,
       NULL, NULL, 3, 1, '送药机器人 RBT-01~10（擎朗 T5）厂商；器械注册证待设备科补录',
       'sql437', NOW(), 'sql437', NOW(), 0, 0, 0
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM sys_supplier WHERE supplier_code = 'SUP016');

-- 10 台送药机器人补厂商指针（supplier_name 是建档快照，与 supplier_id 一起写）
UPDATE sys_equipment
   SET supplier_id   = (SELECT supplier_id FROM sys_supplier WHERE supplier_code = 'SUP016' AND del_flag = 0 LIMIT 1),
       supplier_name = '上海擎朗智能科技有限公司',
       update_by = 'sql437', update_time = NOW()
 WHERE del_flag = 0
   AND category = 8
   AND equipment_code LIKE 'RBT-%';

-- 回查
-- SELECT supplier_type, COUNT(*) FROM sys_supplier WHERE del_flag = 0 GROUP BY supplier_type;
-- SELECT equipment_code, brand, model, supplier_id, supplier_name FROM sys_equipment WHERE category = 8 AND del_flag = 0;
