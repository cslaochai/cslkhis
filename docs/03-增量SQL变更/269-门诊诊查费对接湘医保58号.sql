-- 269-门诊诊查费对接湘医保58号（目录 + 报销规则）
-- 背景：湘医保发〔2025〕58 号（湖南省医保局，2025-11-20 发文，2026-01-01 执行）废止「挂号费」等
--       71 个收费项目，合并设立「门诊诊查费」等 36 个项目。门诊诊查费支付分类 = 丙类
--       （医保不报、100% 自付），不进职工门诊统筹。
--       本院口径：现行记账仍拆 GHF（挂号费，自费）+ ZCF（诊查费）两项；ZCF 此前按甲类记账，
--       甲类行在 computePoolFromRule 里只能压 pool_ratio、没有「先自付 100%」档，导致诊查费
--       被错误纳入统筹。本文件 + 配套代码改动（ChargeFacadeServiceImpl ZCF 目录类别 甲类→丙类）
--       把「丙类 100% 自付」落到规则表，算钱链路与政策对齐。
-- 表：biz_yb_catalog（国家医保目录本地库，管理/报盘用，不参与算钱）插 4 条门诊诊查费族；
--     biz_insurance_catalog_rule（唯一参与统筹计算的表）插 ZCF 门诊丙类规则 1 条。
-- 依据：湘医保发〔2025〕58 号。编码 011102020010000~011102020010003（国家医保医疗服务项目编码）。
--       价格（在长部省属/基准价）：普通门诊 10/9 元；副主任医师加收 15/13；主任医师加收 25/22；
--       知名专家加收 68/61；儿童加收 30%。价格属于院内收费项目（sys_treatment_item），
--       本文件只落医保目录属性，不写价格列。
-- 幂等：yb_catalog 走 uk_yb_code（非空列）ON DUPLICATE KEY UPDATE；
--       catalog_rule 的 uk_item_catalog_encounter 含 insurance_type NULL 列，
--       MySQL 唯一键不拦 NULL（ON DUPLICATE 会失效），故用 NOT EXISTS 写法。
-- 顺序：须先于 270（医保域表名前缀统一）执行——本文件引用旧表名，数据随 270 的
--       RENAME TABLE 自动跟改，无需在本文件写新表名。
-- 范围：只铺目录 + 规则。yb_mapping 暂不插 ZCF→目录对照（等院内 ZCF 诊疗项目编码落定后补）；
--       GHF 维持自费目录类别不变（挂号费项目已废止，只是本院历史口径未清）。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- 门诊诊查费族：4 条国家医保目录项（catalog_type=3 医疗服务项目，insurance_level=3 丙类，pay_ratio=0）
INSERT INTO biz_yb_catalog
(id, catalog_type, yb_code, yb_name, spec, unit, dosage_form, insurance_level, pay_ratio,
 effective_date, expire_date, status, create_by, create_by_id, create_time,
 update_by, update_by_id, update_time, del_flag, remark)
VALUES
(891000000000010061, 3, '011102020010000', '门诊诊查费(普通门诊)', '次', '次', NULL, 3, 0.00,
 '2026-01-01', NULL, 1, 'admin', 1, NOW(), 'admin', 1, NOW(), 0,
 '湘医保发〔2025〕58号：废止挂号费合并设立门诊诊查费，支付分类丙类（100%自付）；在长部省属10元/基准价9元'),
(891000000000010062, 3, '011102020010001', '门诊诊查费(副主任医师加收)', '次', '次', NULL, 3, 0.00,
 '2026-01-01', NULL, 1, 'admin', 1, NOW(), 'admin', 1, NOW(), 0,
 '湘医保发〔2025〕58号：副主任医师加收，支付分类丙类（100%自付）；在长部省属15元/基准价13元'),
(891000000000010063, 3, '011102020010002', '门诊诊查费(主任医师加收)', '次', '次', NULL, 3, 0.00,
 '2026-01-01', NULL, 1, 'admin', 1, NOW(), 'admin', 1, NOW(), 0,
 '湘医保发〔2025〕58号：主任医师加收，支付分类丙类（100%自付）；在长部省属25元/基准价22元'),
(891000000000010064, 3, '011102020010003', '门诊诊查费(知名专家加收)', '次', '次', NULL, 3, 0.00,
 '2026-01-01', NULL, 1, 'admin', 1, NOW(), 'admin', 1, NOW(), 0,
 '湘医保发〔2025〕58号：知名专家加收，支付分类丙类（100%自付）；在长部省属68元/基准价61元')
ON DUPLICATE KEY UPDATE
  yb_name        = VALUES(yb_name),
  spec           = VALUES(spec),
  unit           = VALUES(unit),
  insurance_level= VALUES(insurance_level),
  pay_ratio      = VALUES(pay_ratio),
  effective_date = VALUES(effective_date),
  expire_date    = VALUES(expire_date),
  status         = VALUES(status),
  update_by      = 'admin',
  update_by_id   = 1,
  update_time    = NOW(),
  del_flag       = 0,
  remark         = VALUES(remark);

-- ZCF 诊查费报销规则：门诊、通用（insurance_type NULL）、丙类先自付 100%、统筹 0
-- 与存量 ICR2026092700005（ZL001 针灸治疗，丙类 100% 自付）同构，字段口径对齐
INSERT INTO biz_insurance_catalog_rule
(id, rule_no, item_code, item_name, catalog_type, encounter_type, insurance_type,
 self_pay_ratio, deductible, ceiling, pool_ratio, limit_flags,
 effective_date, expire_date, priority, status,
 create_by, create_by_id, create_time, update_by, update_by_id, update_time, del_flag, remark)
SELECT 891000000000009307, 'ICR202610180001', 'ZCF', '诊查费', 3, 1, NULL,
       100.00, 0.00, 999999.99, 0.00, 0,
       '2026-01-01', NULL, 0, 1,
       'admin', 1, NOW(), 'admin', 1, NOW(), 0,
       '湘医保发〔2025〕58号：门诊诊查费支付分类丙类（100%自付），2026-01-01起执行；配套 ZCF 记账目录类别 甲类→丙类'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM biz_insurance_catalog_rule
    WHERE item_code = 'ZCF' AND catalog_type = 3 AND encounter_type = 1
      AND insurance_type IS NULL AND effective_date = '2026-01-01'
);

COMMIT;
SET FOREIGN_KEY_CHECKS=1;
