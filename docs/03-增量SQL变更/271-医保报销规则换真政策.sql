-- 271-医保报销规则换真政策 + 修规则命中链路（数据 + 口径校正）
-- 背景：dev 库 biz_yb_catalog_rule 18 行是演示样例（统筹比例 80/85/90 拍脑袋整数），
--       且**运行期一条都命不中**。根因有两处：
--       ① mapper 匹配语义反了：insurance_type = #{t} OR (insurance_type IS NULL AND #{t} IS NULL)
--          —— 只要患者有医保号（#{t} 非空），全表通用规则（insurance_type IS NULL）全部失配，
--          统筹只能回落到 FeeCatalogTypeEnum.byItemType 硬推定（药品乙类、挂号自费、其余默认甲类）。
--       ② 取用只按 item_code 存 Map（ruleByItem.putIfAbsent(itemCode, rule)），丢掉了 catalog_type 维度，
--          同一项目配了甲乙两条时会串。
--       另：computePoolFromRule 里 ceiling 写的是 afterSelfPay = ZERO（整行清零），
--       注释却写"超过封顶线的部分"；deductible 配了却完全没参与计算。
-- 配套代码改动：BizInsuranceCatalogRuleMapper（匹配语义 + 排序 + 删零引用 selectEffectiveRule）、
--             SettlementBillServiceImpl（规则按 itemCode+catalogType 成对取用、封顶线改裁剪、起付线接上）。
--
-- 政策依据（省/市级官方文件原文，不是拍脑袋整数）：
--   1. 住院：湘政办发〔2022〕66号《湖南省职工基本医疗保险实施办法》
--      第三十四条 首次住院起付：基层200/一级500/二级800/三级1100/省部属1600；
--                同级别第二次起付按50%，年度累计不超2000
--      第三十六条 在职支付比例：基层93%/一级92%/二级90%/三级85%/省部属80%；退休各+2个百分点
--      第三十七条 年度住院最高实际支付限额 15 万
--      → 本院为二级医疗机构，故住院取二级档：起付800、在职90%、退休92%
--   2. 门诊：湘医保发〔2023〕22号《关于调整优化职工基本医疗保险普通门诊统筹政策的通知》
--      一级及基层：不设起付，按70%支付
--      二级：每次起付50元，多次累计不超200元，按60%支付
--      三级：每次起付100元，多次累计不超300元，按60%支付
--      年度起付累计不超300元；在职年度最高支付限额1500元，退休2000元
--      → 本院二级，故门诊取二级档：起付50、支付60%、在职年度限额1500
--   3. 居民门诊：限协议基层医疗卫生机构，不设起付按70%，结算年度限额420元（长沙560元）
--      → 二级机构就诊按60%留档，实际以医保局核定为准
--   4. 门诊诊查费：湘医保发〔2025〕58号，丙类100%自付（269 已落 ZCF 通用档，本文件不重复）
--
-- ⚠️ 险种口径铁律（本次踩过的坑，别改回去）：
--    insurance_type 存的是**患者主档 medical_insurance_type 原文**（城镇职工医保 / 城乡居民医保 /
--    公费医疗），不是 MedicalInsuranceTypeEnum 归一后的枚举名。结算时 resolveInsurance 传给
--    mapper 的就是主档原文，两边不同源则规则永远匹配不上。
--    写死枚举名（"职工医保"）= 规则永远不命中 = 又退回 byItemType 推定，且不报错。
--
-- 落地取舍（写进 remark，不假装能表达）：
--   · 规则表四维 = 项目×费别×就诊类型×险种，**没有"医院级别"这一维**。
--     住院的级别差异（92/90/85）无法逐项目表达，按本院实际级别取二级档直接落值。
--   · 湖南省限额（门诊1500/住院15万）是**年度累计**口径，规则表的 deductible/ceiling 是行级配置，
--     跨单累计需要年度台账，现有表结构承载不了。本文件把年度限额落成 ceiling 做单行上限兜底，
--     跨单累计那一半**未实现**（要实现需先建年度累计台账）。别把行级 ceiling 当年度累计器看。
--   · 「退休职工」：患者主档无独立取值（在职/退休都填"城镇职工医保"），本轮并入职工通用档。
--     要区分需先在主档加参保身份字段（另开轮次）。
--
-- 幂等：按 rule_no 判存在（uk_rule_no），已存在即 UPDATE；演示数据不停用不删（保留可回溯），
--       但它们 insurance_type 全为 NULL 的通用档**会与本文件规则抢命中**（通用档是兜底档，
--       mapper 排序里精确险种优先，所以本文件规则在前；演示档仅在未配精确险种时兜底）。
-- 顺序：须在 270（医保域表名前缀统一）之后执行 —— 本文件用新表名 biz_yb_catalog_rule / biz_yb_policy。
--       新环境从 01→02→...→271 顺序执行，270 已 RENAME 过，本文件不再引用旧名。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- ============ 一、规则表：按四维 + 主档原文险种落真政策 ============
-- 幂等写法不用 ON DUPLICATE：uk_item_catalog_encounter 含 insurance_type NULL 列，
-- MySQL 唯一键不拦 NULL（ON DUPLICATE 会失效），故按 rule_no 用 NOT EXISTS + UPDATE 双写。

-- 门诊 · 职工医保 · 甲类（二级机构：起付50、支付60%、年度限额1500）
INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000000, 'ICR202610180101', 'CT001', '头颅CT平扫', 1, 1, '城镇职工医保',
       0.00, 50.00, 1500.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '湘医保发〔2023〕22号：职工门诊二级机构每次起付50元、支付60%、在职年度限额1500元'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180101');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000001, 'ICR202610180102', 'LB001', '血常规', 1, 1, '城镇职工医保',
       0.00, 50.00, 1500.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '湘医保发〔2023〕22号：职工门诊二级机构每次起付50元、支付60%'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180102');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000002, 'ICR202610180103', 'JC001', '胸部X光片', 1, 1, '城镇职工医保',
       0.00, 50.00, 1500.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '湘医保发〔2023〕22号：职工门诊二级机构每次起付50元、支付60%'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180103');

-- 门诊 · 职工医保 · 乙类（先自付20%，余额按60%报）
INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000003, 'ICR202610180111', 'MR001', '头颅MRI平扫', 2, 1, '城镇职工医保',
       20.00, 50.00, 1500.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '乙类先行自付20%，余额按职工门诊二级60%支付'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180111');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000004, 'ICR202610180112', 'LB004', '肝功能全套', 2, 1, '城镇职工医保',
       20.00, 50.00, 1500.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '乙类先行自付20%，余额按职工门诊二级60%支付'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180112');

-- 门诊 · 城乡居民医保 · 甲类（居民门诊限协议基层，基层不设起付按70%；二级按60%留档）
INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000005, 'ICR202610180121', 'CT001', '头颅CT平扫', 1, 1, '城乡居民医保',
       0.00, 0.00, 560.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '居民普通门诊统筹限协议基层机构，基层不设起付按70%（年度限额420元，长沙560元）；本院二级档按60%留档'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180121');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000006, 'ICR202610180122', 'LB001', '血常规', 1, 1, '城乡居民医保',
       0.00, 0.00, 560.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '居民普通门诊统筹限协议基层机构，基层不设起付按70%'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180122');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000007, 'ICR202610180123', 'JC001', '胸部X光片', 1, 1, '城乡居民医保',
       0.00, 0.00, 560.00, 60.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '居民普通门诊统筹限协议基层机构，基层不设起付按70%'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180123');

-- 住院 · 城镇职工医保（二级：首次起付800、支付90%、年度最高实际支付限额15万）
INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000008, 'ICR202610180131', 'CT002', '胸部CT平扫', 1, 2, '城镇职工医保',
       0.00, 800.00, 150000.00, 90.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '湘政办发〔2022〕66号第36条二级住院支付90%；第34条首次起付800元；第37条年度最高实际支付限额15万'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180131');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000009, 'ICR202610180132', 'BP0007', '头孢曲松钠粉针', 1, 2, '城镇职工医保',
       0.00, 800.00, 150000.00, 90.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '湘政办发〔2022〕66号第36条：甲类药品二级住院支付90%'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180132');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000010, 'ICR202610180133', 'BP0035', '伏立康唑片', 2, 2, '城镇职工医保',
       20.00, 800.00, 150000.00, 90.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '乙类先行自付20%，余额按二级住院90%支付'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180133');

INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000011, 'ICR202610180134', 'LB020', '降钙素原(PCT)', 2, 2, '城镇职工医保',
       20.00, 800.00, 150000.00, 90.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '乙类先行自付20%，余额按二级住院90%支付'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180134');

-- 住院 · 城乡居民医保（二级档留档；ceiling=0 表示本行不参与封顶，年度限额以医保局核定为准）
INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000013, 'ICR202610180151', 'CT002', '胸部CT平扫', 1, 2, '城乡居民医保',
       0.00, 800.00, 0.00, 85.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '居民住院按二级机构支付口径留档；起付线与年度限额以医保局核定为准，本行不设封顶'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180151');

-- 丙类/自费 · 通用（不限险种）：100% 自付，269 已落 ZCF，本文件补 ZL001
INSERT INTO biz_yb_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 8930000000000000015, 'ICR202610180162', 'ZL001', '针灸治疗', 3, 1, NULL,
       100.00, 0.00, 0.00, 0.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '丙类诊疗项目，100%自付（通用规则，不限险种）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_catalog_rule WHERE rule_no='ICR202610180162');

-- 重复执行时把口径校正回落到已存在的行（不插新行）
UPDATE biz_yb_catalog_rule SET insurance_type='城镇职工医保'
 WHERE rule_no IN ('ICR202610180101','ICR202610180102','ICR202610180103',
                   'ICR202610180111','ICR202610180112','ICR202610180131',
                   'ICR202610180132','ICR202610180133','ICR202610180134');
UPDATE biz_yb_catalog_rule SET insurance_type='城乡居民医保'
 WHERE rule_no IN ('ICR202610180121','ICR202610180122','ICR202610180123','ICR202610180151');

-- ============ 二、政策表：按主档原文收敛，一险种一档 ============
-- 现状 16 行里 城镇职工医保/2 撞了 6 行（60/80/85/88/92 并存），而 InsurancePolicyServiceImpl.findPolicy
-- 用 orderByAsc(id) LIMIT 1 —— 恒读到 id=1 那条 85%，其余 5 行是永远读不到的死数据。
-- 更要命的是原行 insurance_type 写的是"在职职工/退休职工/异地就医…"，而 findPolicy 拿主档原文
-- ("城镇职工医保") 去 eq 匹配，精确匹配失败后走"只按 settlement_type"的兜底分支才碰巧读到 id=1。
-- 处置：一险种一档 + 键用主档原文，兜底分支不再依赖它。
UPDATE biz_yb_policy SET status = 0, update_by='admin', update_time=NOW()
 WHERE settlement_type = 2 AND status = 1 AND insurance_type <> '城镇职工医保';
UPDATE biz_yb_policy SET policy_name='城镇职工医保', insurance_type='城镇职工医保',
       coverage_ratio=90.00, self_pay_ratio=20.00, status=1,
       remark='湘政办发〔2022〕66号第36条二级住院在职90%；本院二级机构。门诊按二级档60%（湘医保发〔2023〕22号）走目录规则表',
       update_by='admin', update_time=NOW()
 WHERE settlement_type = 2 AND insurance_type = '在职职工';

UPDATE biz_yb_policy SET status = 0, update_by='admin', update_time=NOW()
 WHERE settlement_type = 3 AND status = 1 AND insurance_type <> '城乡居民医保';
UPDATE biz_yb_policy SET policy_name='城乡居民医保', insurance_type='城乡居民医保',
       coverage_ratio=85.00, self_pay_ratio=20.00, status=1,
       remark='居民住院二级机构支付口径85%留档；门诊统筹限协议基层机构不设起付按70%（湘医保发〔2023〕22号口径）',
       update_by='admin', update_time=NOW()
 WHERE settlement_type = 3 AND insurance_type IN ('城乡居民','城乡居民医保-成人','城乡居民医保-学生',
       '城乡居民医保-老人','城乡居民医保-成年居民','城乡居民医保-学生儿童','城乡居民医保-大病保险',
       '异地就医-居民直接结算');

UPDATE biz_yb_policy SET status = 0, update_by='admin', update_time=NOW()
 WHERE settlement_type = 4 AND status = 1;
INSERT INTO biz_yb_policy
(id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio, status,
 create_time, update_time, remark, create_by, update_by, create_by_id, update_by_id)
SELECT 8931000000000000002, '公费医疗', '公费医疗', 4, 90.00, 20.00, 1,
       NOW(), NOW(), '公费医疗按本院二级住院口径90%执行，实际以长沙市公费医疗管理办法为准',
       'admin', 'admin', 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_yb_policy WHERE settlement_type = 4 AND insurance_type = '公费医疗');

-- 「职工医保/退休职工」是本轮口径错配的产物（主档无此取值，findPolicy 永远匹配不到），停用留痕
UPDATE biz_yb_policy SET status = 0, remark='险种口径错配：主档无该取值，已按主档原文重建，停用留痕',
       update_by='admin', update_time=NOW()
 WHERE insurance_type IN ('职工医保','退休职工') AND status = 1;

COMMIT;
SET FOREIGN_KEY_CHECKS=1;