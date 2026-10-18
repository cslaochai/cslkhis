-- ============================================================================
-- 272-门诊挂号费并入门诊诊查费（湘医保发〔2025〕58号 / 长沙市医保局同文）
--
-- 政策依据（已核原文）：
--   一、规范设立「门诊诊查费(普通门诊)」011102020010000
--      在长部省属 10 元 / 基准价 9 元，支付分类=丙类，自付比例 100%
--      加收项：副主任医师 15/13、主任医师 25/22、知名专家 68/61
--   二、废止「挂号费」等 71 个价格项目
--      原挂号费编码 001101000010000 / 110100001
--      原普通门诊诊查费 001102000010000 / 110200001（也一并废止）
--   三、执行日期 2026-01-01，有效期 5 年
--
-- 本增量只动数据，不改表结构（regist_fee / diagnosis_fee 两列保留）：
--   1. 目录表登记两个被废止编码（留痕，让结算追溯查得到"这个编码去哪了"）
--   2. 规则表停用 ZCF 重复行
--   3. 映射表补 ZCF ↔ 011102020010000 对照（此前 43 行全是药品，诊疗项目零映射）
--   4. 号源模板/号源：regist_fee 归零，diagnosis_fee 按基准价落值
--
-- ⚠️ 幂等：全部用 NOT EXISTS / 条件 UPDATE，可重复执行。
--    不用 ON DUPLICATE —— uk_yb_code 虽非 NULL，但 uk_item(item_type,item_id) 在历史数据里
--    有 NULL 参与，ON DUPLICATE 对含 NULL 的唯一键不生效（269 已踩过这个坑）。
-- ============================================================================

SET @op := 'migrate58';
SET @base_consult := 9.00;      -- 长沙市普通门诊诊查费基准价
SET @expert_consult := 70.00;    -- 知名专家：普通 9 + 加收 61

-- ---------------------------------------------------------------------------
-- 1. 目录表登记废止编码
--    status=0 + expire_date=2025-12-31（执行前一天失效）。
--    保留登记而不是删表：结算追溯查「这个编码去哪了」时能查到废止去向，
--    凭空消失会让人误以为是漏录。
-- ---------------------------------------------------------------------------
INSERT INTO biz_yb_catalog
  (id, catalog_type, yb_code, yb_name, insurance_level, pay_ratio,
   effective_date, expire_date, status, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000030000 + (SELECT COUNT(*) FROM biz_yb_catalog), 3,
       '001101000010000', '挂号费', 0, 0.00,
       NULL, '2025-12-31', 0, @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号：已废止，合并为门诊诊查费(普通门诊) 011102020010000；原院内编码 GHF 不再出账',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog WHERE yb_code = '001101000010000');

INSERT INTO biz_yb_catalog
  (id, catalog_type, yb_code, yb_name, insurance_level, pay_ratio,
   effective_date, expire_date, status, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000030000 + (SELECT COUNT(*) FROM biz_yb_catalog), 3,
       '001102000010000', '普通门诊诊查费', 0, 0.00,
       NULL, '2025-12-31', 0, @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号：已废止，规范为门诊诊查费(普通门诊) 011102020010000；原编码 110200001',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog WHERE yb_code = '001102000010000');

-- ---------------------------------------------------------------------------
-- 2. 规则表收敛 ZCF 重复行
--    两条启用规则语义相同（self=100 / pool=0），差别只在 ceiling。
--    停用 269 建的 ICR202610180001，留 271 的 ICR202610180161 作唯一权威行。
--    ceiling=999999.99 这种魔数改成 0：丙类本来就不封顶，写 999999.99 只会
--    让下一个读代码的人以为「有封顶但设得极大」。
-- ---------------------------------------------------------------------------
UPDATE biz_yb_catalog_rule
   SET status = 0, update_by = @op, update_time = NOW(),
       remark = CONCAT(IFNULL(remark,''), ' | ', @op,
                       ': 与 ICR202610180161 语义重复(丙类100%自付)，本行停用保留追溯')
 WHERE rule_no = 'ICR202610180001' AND del_flag = 0 AND status = 1;

UPDATE biz_yb_catalog_rule
   SET ceiling = 0, update_by = @op, update_time = NOW()
 WHERE rule_no = 'ICR202610180161' AND ceiling = 999999.99;

-- ---------------------------------------------------------------------------
-- 3. 映射表补诊疗项目对照
--    此前 biz_yb_mapping 43 行全是药品(item_type=1)，诊疗项目(item_type=2)零映射 ——
--    院内 ZCF 与国家编码 011102020010000 之间没有桥。真接医保局时这张表是对照关系
--    的唯一出处，没有对照报不上去。
--    GHF 也登记一条废止对照：留痕「院内 GHF 曾对照什么、为什么不再报」。
-- ---------------------------------------------------------------------------
INSERT INTO biz_yb_mapping
  (id, item_type, item_id, item_code, item_name, catalog_id, yb_code, yb_name,
   match_type, mapped_by, mapped_time, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000040001, 2, 8976000000000010001, 'ZCF', '门诊诊查费',
       (SELECT id FROM biz_yb_catalog WHERE yb_code = '011102020010000' LIMIT 1),
       '011102020010000', '门诊诊查费(普通门诊)',
       2, @op, NOW(), @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号：挂号费废止后，院内 ZCF 对照国家编码 011102020010000；丙类100%自付',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_mapping WHERE item_code = 'ZCF' AND del_flag = 0);

INSERT INTO biz_yb_mapping
  (id, item_type, item_id, item_code, item_name, catalog_id, yb_code, yb_name,
   match_type, mapped_by, mapped_time, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000040002, 2, 8976000000000010002, 'GHF', '挂号费',
       (SELECT id FROM biz_yb_catalog WHERE yb_code = '001101000010000' LIMIT 1),
       '001101000010000', '挂号费',
       2, @op, NOW(), @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号：挂号费已废止(合并入门诊诊查费)，院内不再出账；此行仅留痕不对照',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_mapping WHERE item_code = 'GHF' AND del_flag = 0);

-- ---------------------------------------------------------------------------
-- 4. 号源费用按长沙市价格表落值
--    regist_fee 归零但**不删列**：后端 DTO/VO/前端表单仍按该字段传值（恒 0），
--    删列要连带改十几个类与页面，收益不抵风险；注释已标"已废止"。
--    diagnosis_fee 承载门诊诊查费：普通号基准价 9 元；
--    专家号按「知名专家加收」61 元 + 普通 9 元 = 70 元（加收项是叠加不是替代）。
-- ---------------------------------------------------------------------------
UPDATE biz_clinic_source_template
   SET diagnosis_fee = IF(is_expert = 1, @expert_consult, @base_consult),
       regist_fee = 0, update_by = @op, update_time = NOW()
 WHERE del_flag = 0 AND (IFNULL(regist_fee,0) <> 0 OR IFNULL(diagnosis_fee,0) <> 0);

UPDATE biz_clinic_source
   SET diagnosis_fee = IF(is_expert = 1, @expert_consult, @base_consult),
       regist_fee = 0, update_by = @op, update_time = NOW()
 WHERE del_flag = 0 AND (IFNULL(regist_fee,0) <> 0 OR IFNULL(diagnosis_fee,0) <> 0);