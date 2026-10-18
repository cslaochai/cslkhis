-- ============================================================================
-- 274 · 门诊诊查费职级加收分档（副主任 22 / 主任 31 / 知名专家 70）
-- ============================================================================
-- 政策依据：湘医保发〔2025〕58号《关于规范整合综合诊查类医疗服务价格项目的通知》
--          （长沙市医保局同文，2026-01-01 执行）附件1「门诊诊查费(普通门诊)」
--
-- 四档价格（该文件规定的加收后全额）：
--   档位 1 主治及以下      不加收              9.00   011102020010000
--   档位 2 副主任医师      加收 13            22.00   011102020010001
--   档位 3 主任医师        加收 22            31.00   011102020010002
--   档位 4 知名专家        加收 61            70.00   011102020010003
--
-- ── 为什么本轮才补 ──────────────────────────────────────────────────────────
-- 272 把挂号费并入门诊诊查费时，号源只有一个 isExpert 二值开关，前端
-- scheduleTitle.js 用「文案含副主任 / 以主任开头」判断专家号，结果
--   副主任医师（301）和主任医师（401）落进同一档，都按知名专家的 70 元挂。
-- dev 库实测：301 的 12 个号源、401 的 12 个号源，diagnosis_fee 全是 70.00，
-- 而 201（主治）的 12 个是 9.00。按政策，301 应 22.00、401 应 31.00。
--
-- ── 本次改什么、不改什么 ────────────────────────────────────────────────────
-- 改：① 两张表加 diagnosis_tier 列（记录本号源适用哪一档加收，供医保对账）
--     ② 按 sys_employee.title 回填档位与价格
-- 不改：
--   · 已挂号的号源（used_source > 0）—— 改了等于让已挂号患者改约时被加价/减价，
--     价格是<b>号源快照</b>，不是实时按职称推导的。医生升职称不回溯改老号源。
--   · 已过期的号源（schedule_date < 今天）—— 历史事实，不追溯。
--   · registr_fee / expert_fee 列—— 272/273 已归零并标注废止，本轮不动。
--   · 目录表 biz_yb_catalog / 规则表 biz_yb_catalog_rule —— 273 已铺好四档，
--     本轮只改号源侧「哪个号适用哪档」，不动医保目录与报销规则。
--
-- ⚠️ 幂等：全部 NOT EXISTS / 条件 UPDATE，可重复执行。
-- ⚠️ @op 由调用方 SET，本脚本内兜底给默认值。
-- ============================================================================

SET @op = IFNULL(@op, 'system');
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. 加 diagnosis_tier 列
--    1普通 2副主任医师 3主任医师 4知名专家（对应 ClinicDiagnosisTierEnum）
-- ---------------------------------------------------------------------------
-- ⚠️ ALTER TABLE ADD COLUMN 不幂等（重跑报 Duplicate column），用 information_schema 先判存在性。
SET @ddl1 = (SELECT IF(COUNT(*) > 0, 'SELECT 1', 'ALTER TABLE biz_clinic_source
    ADD COLUMN diagnosis_tier TINYINT NULL COMMENT ''门诊诊查费职级加收档位：1普通 2副主任医师 3主任医师 4知名专家'' AFTER diagnosis_fee')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_clinic_source' AND COLUMN_NAME = 'diagnosis_tier');
SET @ddl2 = (SELECT IF(COUNT(*) > 0, 'SELECT 1', 'ALTER TABLE biz_clinic_source_template
    ADD COLUMN diagnosis_tier TINYINT NULL COMMENT ''门诊诊查费职级加收档位：1普通 2副主任医师 3主任医师 4知名专家'' AFTER diagnosis_fee')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_clinic_source_template' AND COLUMN_NAME = 'diagnosis_tier');
PREPARE p1 FROM @ddl1; EXECUTE p1; DEALLOCATE PREPARE p1;
PREPARE p2 FROM @ddl2; EXECUTE p2; DEALLOCATE PREPARE p2;

-- ---------------------------------------------------------------------------
-- 2. 号源表回填档位与价格
--    🔑 只动「未来 + 未挂号」的号源：schedule_date >= 今天 AND used_source = 0。
--       已挂号/已过期的号保持原价，价格是号源快照（见文件头说明）。
--    🔑 档位与价格都按 sys_employee.title 取，不用 is_expert 反推：
--       is_expert=1 混合了副主任和主任，正是本轮要拆开的那一坨。
-- ---------------------------------------------------------------------------

-- 2.1 副主任医师（title=301）→ 档位 2 / 22.00
UPDATE biz_clinic_source s
  JOIN sys_employee e ON e.id = s.doctor_id AND e.del_flag = 0
   SET s.diagnosis_tier = 2,
       s.diagnosis_fee  = 22.00,
       s.is_expert      = 1,
       s.expert_fee     = 0,
       s.update_by      = @op,
       s.update_time    = NOW()
 WHERE s.del_flag = 0
   AND e.title = '301'
   AND s.diagnosis_fee <> 22.00
   AND IFNULL(s.used_source, 0) = 0
   AND DATE(s.schedule_date) >= CURDATE();

-- 2.2 主任医师（title=401）→ 档位 3 / 31.00
--    注意：知名专家（档位 4 / 70）同样挂在 401 上，但那是<b>名单制认定</b>，
--    系统无从判断谁是知名专家，所以这里一律先给主任档 31.00，
--    需要 70 的号由排班人员在号源上手工改成档位 4。
UPDATE biz_clinic_source s
  JOIN sys_employee e ON e.id = s.doctor_id AND e.del_flag = 0
   SET s.diagnosis_tier = 3,
       s.diagnosis_fee  = 31.00,
       s.is_expert      = 1,
       s.expert_fee     = 0,
       s.update_by      = @op,
       s.update_time    = NOW()
 WHERE s.del_flag = 0
   AND e.title = '401'
   AND s.diagnosis_fee <> 31.00
   AND IFNULL(s.used_source, 0) = 0
   AND DATE(s.schedule_date) >= CURDATE();

-- 2.3 主治及以下（title=201 及其他医生序列）→ 档位 1 / 9.00
UPDATE biz_clinic_source s
  JOIN sys_employee e ON e.id = s.doctor_id AND e.del_flag = 0
   SET s.diagnosis_tier = 1,
       s.diagnosis_fee  = 9.00,
       s.is_expert      = 0,
       s.expert_fee     = 0,
       s.update_by      = @op,
       s.update_time    = NOW()
 WHERE s.del_flag = 0
   AND (e.title IS NULL OR e.title NOT IN ('301', '401'))
   AND IFNULL(s.diagnosis_fee, 0) > 0
   AND s.diagnosis_fee <> 9.00
   AND IFNULL(s.used_source, 0) = 0
   AND DATE(s.schedule_date) >= CURDATE();

-- 2.4 兜底：给仍是 NULL 的号补档位。
--   🔑 这里必须「按已有价格反推档位」，不能一律打 1：前面 2.1~2.3 只改「未来 + 未挂号」的号，
--   已挂号/已过期的历史号保持原价（70.00）。若统一打档位 1，就会造出
--   「档位=普通(9元) 但实际挂 70 元」的自相矛盾数据——医保对账时这行既说不清档位、
--   也对不上价，追责时是死结。反推规则：70→知名专家(4) / 31→主任(3) / 22→副主任(2) / 其余→普通(1)。
--   🔑 条件是「档位缺失 <b>或</b> 档位与价格对不上」，不只是 IS NULL：早先版本一律打过 tier=1，
--      留下 21 行「档位1 + 价格70」的脏数据，只判 NULL 修不掉（重跑仍命中 0 行）。
UPDATE biz_clinic_source
   SET diagnosis_tier = CASE
                          WHEN IFNULL(diagnosis_fee, 0) = 70.00 THEN 4
                          WHEN IFNULL(diagnosis_fee, 0) = 31.00 THEN 3
                          WHEN IFNULL(diagnosis_fee, 0) = 22.00 THEN 2
                          ELSE 1
                        END,
       update_by   = @op,
       update_time = NOW()
 WHERE del_flag = 0
   -- ⚠️ 费用为 0 的号/模板（义诊、免费号、不出诊查费的岗）不在此列纠偏范围内：
   --    0 元是<b>合法业务值</b>不是脏数据，强改成 9 元等于给免费号强行定价。
   --    这类保持 tier=1（普通档），由出账时的「费为 0 则不出账」逻辑处理。
   AND IFNULL(diagnosis_fee, 0) > 0
   AND (diagnosis_tier IS NULL
        OR diagnosis_fee <> CASE diagnosis_tier
                             WHEN 1 THEN 9.00 WHEN 2 THEN 22.00
                             WHEN 3 THEN 31.00 WHEN 4 THEN 70.00
                             ELSE -1 END);

-- ---------------------------------------------------------------------------
-- 3. 号源模板表回填
--    模板没有 used_source（还没生成号源），但同样只改「仍在生效期内」的：
--    valid_until 为空 或 valid_until >= 今天。
-- ---------------------------------------------------------------------------
UPDATE biz_clinic_source_template t
  JOIN sys_employee e ON e.id = t.doctor_id AND e.del_flag = 0
   SET t.diagnosis_tier = 2,
       t.diagnosis_fee  = 22.00,
       t.is_expert      = 1,
       t.expert_fee     = 0,
       t.update_by      = @op,
       t.update_time    = NOW()
 WHERE t.del_flag = 0
   AND e.title = '301'
   AND t.diagnosis_fee <> 22.00
   AND (t.valid_until IS NULL OR DATE(t.valid_until) >= CURDATE());

UPDATE biz_clinic_source_template t
  JOIN sys_employee e ON e.id = t.doctor_id AND e.del_flag = 0
   SET t.diagnosis_tier = 3,
       t.diagnosis_fee  = 31.00,
       t.is_expert      = 1,
       t.expert_fee     = 0,
       t.update_by      = @op,
       t.update_time    = NOW()
 WHERE t.del_flag = 0
   AND e.title = '401'
   AND t.diagnosis_fee <> 31.00
   AND (t.valid_until IS NULL OR DATE(t.valid_until) >= CURDATE());

UPDATE biz_clinic_source_template t
  JOIN sys_employee e ON e.id = t.doctor_id AND e.del_flag = 0
   SET t.diagnosis_tier = 1,
       t.diagnosis_fee  = 9.00,
       t.is_expert      = 0,
       t.expert_fee     = 0,
       t.update_by      = @op,
       t.update_time    = NOW()
 WHERE t.del_flag = 0
   AND (e.title IS NULL OR e.title NOT IN ('301', '401'))
   AND IFNULL(t.diagnosis_fee, 0) > 0
   AND t.diagnosis_fee <> 9.00
   AND (t.valid_until IS NULL OR DATE(t.valid_until) >= CURDATE());

-- 同样按价格反推（模板的知名专家档也可能手工指定过 70）
UPDATE biz_clinic_source_template
   SET diagnosis_tier = CASE
                          WHEN IFNULL(diagnosis_fee, 0) = 70.00 THEN 4
                          WHEN IFNULL(diagnosis_fee, 0) = 31.00 THEN 3
                          WHEN IFNULL(diagnosis_fee, 0) = 22.00 THEN 2
                          ELSE 1
                        END,
       update_by   = @op,
       update_time = NOW()
 WHERE del_flag = 0
   -- ⚠️ 费用为 0 的号/模板（义诊、免费号、不出诊查费的岗）不在此列纠偏范围内：
   --    0 元是<b>合法业务值</b>不是脏数据，强改成 9 元等于给免费号强行定价。
   --    这类保持 tier=1（普通档），由出账时的「费为 0 则不出账」逻辑处理。
   AND IFNULL(diagnosis_fee, 0) > 0
   AND (diagnosis_tier IS NULL
        OR diagnosis_fee <> CASE diagnosis_tier
                             WHEN 1 THEN 9.00 WHEN 2 THEN 22.00
                             WHEN 3 THEN 31.00 WHEN 4 THEN 70.00
                             ELSE -1 END);

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- 回验（跑完应看到：档位 2/3/4 各档都有数据，且无 NULL）
-- SELECT diagnosis_tier, COUNT(*) c, MIN(diagnosis_fee) mn, MAX(diagnosis_fee) mx
--   FROM biz_clinic_source WHERE del_flag=0 GROUP BY diagnosis_tier;
-- ---------------------------------------------------------------------------
