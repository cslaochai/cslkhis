-- ============================================================================
-- 273-门诊诊查费：儿童加收 30% + 三档加收价格数据铺底
--
-- 政策依据（已核官方原文，非拍脑袋）：
--   1. 湘医保发〔2025〕58号《关于规范整合综合诊查类医疗服务价格项目的通知》附件1
--      「门诊诊查费(普通门诊)」011102020010000
--        基准价 9 元（长沙市上浮后 9 元）、支付分类 丙类、自付比例 100%
--        计价说明：**儿童加收30%**
--        加收项：01副主任医师加收 / 02主任医师加收 / 03知名专家加收
--   2. 湖南省医保局《医疗服务价格项目立项指南湖南省落地政策解读(第4期)》第6问：
--      「儿童加收30%怎么算？**在实际诊查费基础上加收**，比如主任医师诊查费是35元，
--        加上儿童加收35*0.3=10.5元，患者应支付45.5元。」
--   3. 湖南省医保局 2026-09-09 答复人大代表原文：
--      湘医保发〔2020〕51号 曾对门诊诊查费设**50%**加收；湘医保发〔2025〕58号
--      「将原『门诊诊查费』儿童加收比率从50%调整为30%」→ **当前值 30%，50% 是废止前的旧值**。
--   4. 加收项基准价（湖南省价格表附件1「加收项」列）：
--        副主任医师 011102020010001 = 13 元（长沙按基准执行）
--        主任医师   011102020010002 = 22 元
--        知名专家   011102020010003 = 61 元
--      故全额：主治及以下 9 / 副主任 9+13=22 / 主任 9+22=31 / 知名专家 9+61=70
--
-- 本增量只动数据，**不改号源模型**（按老王划线，职称维度分档是下一轮）：
--   1. 目录表登记三个加收项（若缺失）+ 补儿童加收说明到 remark
--   2. 规则表为三个加收项各配一条丙类通用档（与主项同口径：丙类100%自付）
--   3. 号源 expert_fee 遗留列清零 —— 272 已把加收价并进 diagnosis_fee，
--      该列非零会让前端/小程序按「原价+加收」再算一遍（dev 库 24 个专家号源仍是 2.00）
--
-- ⚠️ 幂等：全部 NOT EXISTS / 条件 UPDATE，可重复执行。
--    不用 ON DUPLICATE（uk_yb_code 唯一键在历史 NULL 数据上不生效，269 已踩过）。
-- ⚠️ 儿童加收**不落库成独立目录行**：政策是「在相应价格基础上加收」，不是另立收费项目，
--    结算仍按 011102020010000 一行报（详见 PediatricSurchargePolicy 类注释）。
--    这里只把「儿童加收30%」写进主项 remark 留痕，供结算追溯查得到依据。
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

SET @op := 'migrate58';

-- ---------------------------------------------------------------------------
-- 1. 目录表：登记三个加收项（271 已建三条的则只补 remark，不重复插）
--    insurance_level=3 丙类：加收项本身是「在丙类项目上加收的钱」，
--    属性随主项 —— 主项丙类100%自付，加收部分同样不报，不能单独给它一个甲类口径。
-- ---------------------------------------------------------------------------
INSERT INTO biz_yb_catalog
  (id, catalog_type, yb_code, yb_name, insurance_level, pay_ratio,
   effective_date, expire_date, status, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000050001, 3, '011102020010001', '门诊诊查费(普通门诊)-副主任医师(加收)', 3, 0.00,
       '2026-01-01', NULL, 1, @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号附件1加收项：副主任医师加收 基准价13元；全额=普通9+13=22元；丙类100%自付',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog WHERE yb_code = '011102020010001');

INSERT INTO biz_yb_catalog
  (id, catalog_type, yb_code, yb_name, insurance_level, pay_ratio,
   effective_date, expire_date, status, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000050002, 3, '011102020010002', '门诊诊查费(普通门诊)-主任医师(加收)', 3, 0.00,
       '2026-01-01', NULL, 1, @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号附件1加收项：主任医师加收 基准价22元；全额=普通9+22=31元；丙类100%自付',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog WHERE yb_code = '011102020010002');

INSERT INTO biz_yb_catalog
  (id, catalog_type, yb_code, yb_name, insurance_level, pay_ratio,
   effective_date, expire_date, status, create_by, create_time, update_by, update_time,
   del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000050003, 3, '011102020010003', '门诊诊查费(普通门诊)-知名专家(加收)', 3, 0.00,
       '2026-01-01', NULL, 1, @op, NOW(), @op, NOW(), 0,
       '湘医保发〔2025〕58号附件1加收项：知名专家加收 基准价61元；全额=普通9+61=70元；丙类100%自付',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog WHERE yb_code = '011102020010003');

-- ---------------------------------------------------------------------------
-- 2. 主项 remark 补「儿童加收30%」依据
--    加收不单独建目录行（政策是"基础上加收"，不是独立项目），
--    但主项 remark 必须写清楚，否则结算追溯查不到这30%从哪来。
-- ---------------------------------------------------------------------------
UPDATE biz_yb_catalog
   SET remark = CONCAT(IFNULL(remark,''), ' | ', @op,
                       ': 湘医保发〔2025〕58号附件1计价说明「儿童加收30%」',
                       '（六周岁以内，在实际诊查费基础上加收，即 base×1.3；',
                       '原50%为湘医保发〔2020〕51号旧值，2025-12-31废止）'),
       update_by = @op, update_time = NOW()
 WHERE yb_code = '011102020010000' AND del_flag = 0 AND status = 1
   AND remark NOT LIKE '%儿童加收30%';

-- ---------------------------------------------------------------------------
-- 3. 规则表：三个加收项各配一条丙类通用档
--    口径与主项 ICR202610180161 完全一致（丙类：self=100 / pool=0 / 不封顶）——
--    加收的钱跟着主项走，不单独给待遇，否则「9元主项不报、加收13元报了」是套取基金。
--    item_code 用国家编码本身（加收项尚无院内 item_code，见下方说明）。
--    limit_flags 必须落 0：该列 NOT NULL 且无默认值，传 NULL 会报
--    「Column 'limit_flags' cannot be null」（与 MP 的 NOT_NULL 策略撞列同一类坑）。
-- ---------------------------------------------------------------------------
INSERT INTO biz_yb_catalog_rule
  (id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
   self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
   effective_date, expire_date, priority, status,
   create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000060001, 'ICR202610180171', '011102020010001', '门诊诊查费-副主任医师加收', 3, 1, NULL,
       100.00, 0.00, 0.00, 0.00, 0,
       '2026-01-01', NULL, 10, 1, @op, NOW(), @op, NOW(), 0,
       '加收项随主项丙类口径：自付100%、统筹0、不封顶；依据湘医保发〔2025〕58号附件1',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no = 'ICR202610180171');

INSERT INTO biz_yb_catalog_rule
  (id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
   self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
   effective_date, expire_date, priority, status,
   create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000060002, 'ICR202610180172', '011102020010002', '门诊诊查费-主任医师加收', 3, 1, NULL,
       100.00, 0.00, 0.00, 0.00, 0,
       '2026-01-01', NULL, 10, 1, @op, NOW(), @op, NOW(), 0,
       '加收项随主项丙类口径：自付100%、统筹0、不封顶；依据湘医保发〔2025〕58号附件1',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no = 'ICR202610180172');

INSERT INTO biz_yb_catalog_rule
  (id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
   self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
   effective_date, expire_date, priority, status,
   create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 8976000000000060003, 'ICR202610180173', '011102020010003', '门诊诊查费-知名专家加收', 3, 1, NULL,
       100.00, 0.00, 0.00, 0.00, 0,
       '2026-01-01', NULL, 10, 1, @op, NOW(), @op, NOW(), 0,
       '加收项随主项丙类口径：自付100%、统筹0、不封顶；依据湘医保发〔2025〕58号附件1',
       1, 1
 WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no = 'ICR202610180173');

-- ---------------------------------------------------------------------------
-- 4. 号源 expert_fee 遗留列清零（不删列）
--    272 已把专家加收价并进 diagnosis_fee（9+61=70），expert_fee 变成第二份重复的加收数据。
--    dev 库 24 个专家号源该列仍是 2.00，前端/小程序若按「diagnosis+expert」累加就会多显示 2 元。
--    不删列：DTO/VO/实体仍按该字段传值（恒 0），删列要连带改十几个类与页面。
-- ---------------------------------------------------------------------------
UPDATE biz_clinic_source
   SET expert_fee = 0, update_by = @op, update_time = NOW()
 WHERE del_flag = 0 AND IFNULL(expert_fee, 0) <> 0;

UPDATE biz_clinic_source_template
   SET expert_fee = 0, update_by = @op, update_time = NOW()
 WHERE del_flag = 0 AND IFNULL(expert_fee, 0) <> 0;

COMMIT;
SET FOREIGN_KEY_CHECKS=1;