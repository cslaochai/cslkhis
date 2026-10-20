-- =============================================================
-- 276-DIP付费-演示种子数据（非官方样例，仅用于验证 P1–P3 引擎链路）
-- 说明：
--   1) 下列病种目录 / 辅助目录 / 点值均为【演示样例数据】，不是国家或统筹区下发的官方病种库。
--      病种编码、基准分值、系数、点值都是演示值，仅为了让模拟器「跑得通、看得懂」。
--   2) 正式接入：以目标统筹区医保局下发的 DIP 病种库 / 分值 / 辅助目录 / 点值为准，
--      按同样结构重灌即可（唯一键含 version + del_flag，旧演示数据可先物理删或软删）。
--   3) 演示病种围绕院内常见场景构造：肺炎/高血压/2型糖尿病（内科无手术）、
--      阑尾切除/剖宫产/白内障晶体植入/乳腺根治（手术组合）、以及综合病种前缀归组。
--   4) 手术操作码按 sys_icd9cm3 医保版贯标码写法（带点，如 47.0100x001）；主诊断码同 sys_icd10 贯标码。
-- id 段：27600000000000001xx（病种）/ 27600000000000002xx（辅助目录）。
-- 可重复执行：全部按唯一键守卫（NOT EXISTS），已灌官方数据的环境不受影响（不覆盖已有行）。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- -------------------------------------------------------------
-- 1. biz_dip_disease 演示病种目录（version='2.0'，与 dip.default_version 一致）
-- -------------------------------------------------------------
INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000101', 'DIP2026J189001', '未特指病原体肺炎', 1, 'J18.9', '未特指病原体肺炎',
       NULL, NULL, 1, '58.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：内科无手术病种'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026J189001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000102', 'DIP2026I100001', '原发性高血压', 1, 'I10', '原发性高血压',
       NULL, NULL, 1, '32.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：内科无手术病种'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026I100001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000103', 'DIP2026E119001', '2型糖尿病不伴并发症', 1, 'E11.9', '2型糖尿病不伴并发症',
       NULL, NULL, 1, '40.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：内科无手术病种'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026E119001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000104', 'DIP2026K358001', '急性阑尾炎伴阑尾切除术', 1, 'K35.8', '急性阑尾炎（其他）',
       '47.0100x001', '阑尾切除术', 1, '180.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：手术组合病种'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026K358001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000105', 'DIP2026O820001', '子宫下段剖宫产', 1, 'O82.0', '经剖宫产术分娩',
       '74.0100x001', '子宫下段剖宫产术', 1, '320.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：手术组合病种'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026O820001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000106', 'DIP2026H259001', '老年性白内障伴晶体植入术', 1, 'H25.9', '老年性白内障',
       '13.7000x001', '白内障摘除伴人工晶体植入术', 1, '150.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：手术组合病种'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026H259001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000107', 'DIP2026C509001', '乳腺恶性肿瘤根治术', 1, 'C50.9', '乳房恶性肿瘤（未特指部位）',
       '85.4300x001', '乳腺根治术', 1, '260.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：手术组合病种 + 辅助目录（肿瘤分级）绑定'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026C509001' AND version = '2.0' AND del_flag = 0);

-- 综合病种（disease_type=2，diag_match=2：按主诊断前缀归组，最长前缀优先）
INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000108', 'DIP2026ZH001', '呼吸系统感染类疾病（综合）', 2, 'J18', '肺炎（类目）',
       NULL, NULL, 2, '45.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：综合病种前缀归组（J18 亚目未进核心的落此）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026ZH001' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000109', 'DIP2026ZH002', '循环系统慢性病（综合）', 2, 'I10', '高血压（类目）',
       NULL, NULL, 2, '28.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：综合病种前缀归组'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026ZH002' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000110', 'DIP2026ZH003', '糖尿病类疾病（综合）', 2, 'E11', '2型糖尿病（类目）',
       NULL, NULL, 2, '36.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：综合病种前缀归组'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026ZH003' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000111', 'DIP2026ZH004', '阑尾类疾病（综合）', 2, 'K35', '阑尾炎（类目）',
       NULL, NULL, 2, '120.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：综合病种前缀归组'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026ZH004' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000112', 'DIP2026ZH005', '剖宫产相关（综合）', 2, 'O82', '经剖宫产术分娩（类目）',
       NULL, NULL, 2, '300.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：综合病种前缀归组'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026ZH005' AND version = '2.0' AND del_flag = 0);

INSERT INTO biz_dip_disease (id, disease_code, disease_name, disease_type, main_diag_code, main_diag_name,
                             oper_code, oper_name, diag_match, base_score, source, version, status,
                             create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000113', 'DIP2026ZH006', '乳腺肿瘤类（综合）', 2, 'C50', '乳房恶性肿瘤（类目）',
       NULL, NULL, 2, '150.0000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：综合病种前缀归组'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_disease WHERE disease_code = 'DIP2026ZH006' AND version = '2.0' AND del_flag = 0);

-- -------------------------------------------------------------
-- 2. biz_dip_aux 演示辅助目录（病例组合系数；命中后 结算分值 = 基准分值 × 系数）
-- -------------------------------------------------------------
-- 2.1 年龄型（全局）：70 岁及以上 上浮 5%
INSERT INTO biz_dip_aux (id, disease_code, aux_type, aux_code, aux_name, age_min, age_max, coeff, source, version,
                         status, create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000201', NULL, 1, NULL, '70 岁及以上', 70, 150, '1.0500', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：高龄上浮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_aux WHERE aux_type = 1 AND age_min = 70 AND age_max = 150 AND version = '2.0' AND del_flag = 0);

-- 2.2 年龄型（全局）：6 岁以下 上浮 10%
INSERT INTO biz_dip_aux (id, disease_code, aux_type, aux_code, aux_name, age_min, age_max, coeff, source, version,
                         status, create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000202', NULL, 1, NULL, '6 岁以下', 0, 5, '1.1000', '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：低龄上浮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_aux WHERE aux_type = 1 AND age_min = 0 AND age_max = 5 AND version = '2.0' AND del_flag = 0);

-- 2.3 码型（全局，合并症并发症）：其他诊断含 2型糖尿病 E11.9 → 上浮 10%
INSERT INTO biz_dip_aux (id, disease_code, aux_type, aux_code, aux_name, age_min, age_max, coeff, source, version,
                         status, create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000203', NULL, 2, 'E11.9', '2型糖尿病不伴并发症（合并症）', NULL, NULL, '1.1000',
       '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：合并症系数'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_aux WHERE aux_type = 2 AND aux_code = 'E11.9' AND version = '2.0' AND del_flag = 0);

-- 2.4 码型（病种绑定，肿瘤分级）：乳腺恶性肿瘤根治术 病种 + 其他诊断含 C50.9 → 上浮 20%
INSERT INTO biz_dip_aux (id, disease_code, aux_type, aux_code, aux_name, age_min, age_max, coeff, source, version,
                         status, create_by, update_by, create_by_id, update_by_id, create_time, update_time, del_flag, remark)
SELECT '2760000000000000204', 'DIP2026C509001', 3, 'C50.9', '乳房恶性肿瘤（肿瘤分级细分）', NULL, NULL, '1.2000',
       '演示样例（非官方）', '2.0', 1,
       'admin', 'admin', '1', '1', NOW(), NOW(), 0, '演示数据：病种绑定肿瘤分级系数'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_dip_aux WHERE disease_code = 'DIP2026C509001' AND aux_type = 3 AND aux_code = 'C50.9' AND version = '2.0' AND del_flag = 0);

-- -------------------------------------------------------------
-- 3. sys_config：演示点值（元/分值）。仅当当前为空/未配置时写入演示值，已配置的统筹区值不覆盖。
-- -------------------------------------------------------------
UPDATE sys_config
   SET config_value = '0.8000',
       remark = '演示点值（元/分值）：0.8 为演示样例，正式接入以统筹区公布为准',
       update_by = 'admin', update_by_id = '1', update_time = NOW()
 WHERE config_key = 'dip.point_value'
   AND (config_value IS NULL OR config_value = '');

COMMIT;

-- 回查（人工核对用，勿删）：
-- SELECT COUNT(*) FROM biz_dip_disease WHERE version = '2.0' AND del_flag = 0;
-- SELECT COUNT(*) FROM biz_dip_aux WHERE version = '2.0' AND del_flag = 0;
-- SELECT config_key, config_value FROM sys_config WHERE config_key = 'dip.point_value';
-- 验证链路：DIP 分组模拟页（菜单 908）→ 批量模拟，应出现核心病种入组 + 综合病种入组 + 盈亏金额。
