-- =====================================================================
-- 291 急诊分诊复评闭环（一期：MEWS + 候诊超时复评）
-- ---------------------------------------------------------------------
-- 背景：
--   真实 HIS 急诊域四岗位（分诊台/医生站/护士站/转归）里，分诊台的核心不是"登记定级"
--   那一下，而是《急诊预检分诊专家共识》（2018）要求的动态复评：
--     · 每位患者的分诊级别不是固定不变的，分诊人员需密切观察病情变化，
--       并有权限及时调整患者的分诊级别和相应的诊疗流程；
--     · 候诊超过响应时限（I 即刻 / II 10min / III 30min / IV 60min~4h）应再次评估分级。
--   本系统此前只有"登记时定级 + 超时催医生接诊"，缺两件东西：
--     ① MEWS 评分（WS/T 810—2022 五项版：心率/收缩压/呼吸/体温/意识）——定级没有量化依据；
--     ② 复评动作与留痕——级别改了没有流水，"为什么改"查不到。
--
-- 本文件交付：
--   ① biz_emergency 补 MEWS/意识/复评三件套（登记即算，复评即更新）；
--   ② 新表 biz_emergency_reeval：复评流水（前后级别+体征+原因+操作人），
--      对应共识"有权限及时调整级别"的留痕要求；
--   ③ biz_emergency_handover_item 补 MEWS/待复评定格——交班时"谁要去复评"一眼可见；
--   ④ 新增复评按钮菜单 2607（perm opd:emergency:reeval），授权分诊护士(role 9) + 管理员(role 1)。
--      刻意不授急诊医生(role 8)：与 2011"接诊归医生"对称——候诊中的定级权在分诊台，
--      医生侧调级走联系分诊台（升级通知会主动推给医生）。
--
-- 时限口径（不新增系统参数）：
--   复评到期 = 候诊超过应接诊时限 且 距上次复评又超过一个时限。
--   应接诊时限沿用 emergency.wait_deadline_level1~4（默认 0/10/30/120 分钟，
--   与共识 I 即刻/II 10min/III 30min 对齐；IV 级共识为亚急症 60min、非急症 2~4h 两档，
--   本系统 IV 级不细分，取 120 中值，医院可在系统参数里改）。
--
-- 幂等：ALTER 用 information_schema 探测；INSERT IGNORE 菜单/授权；重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登。
-- =====================================================================

-- ---------- ① biz_emergency：MEWS / 意识 / 复评三件套 ----------
SET @s := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency' AND COLUMN_NAME = 'consciousness') = 0,
  'ALTER TABLE `biz_emergency`
     ADD COLUMN `consciousness` tinyint COMMENT ''意识状态（1-清楚 2-对声音有反应 3-对疼痛有反应 4-无反应）'' AFTER `vital_signs`',
  'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency' AND COLUMN_NAME = 'mews_score') = 0,
  'ALTER TABLE `biz_emergency`
     ADD COLUMN `mews_score` int COMMENT ''MEWS 评分（0-14，WS/T 810—2022 五项版现算快照）'' AFTER `consciousness`',
  'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency' AND COLUMN_NAME = 'last_reeval_time') = 0,
  'ALTER TABLE `biz_emergency`
     ADD COLUMN `last_reeval_time` datetime COMMENT ''最近一次分诊复评时间'' AFTER `mews_score`',
  'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency' AND COLUMN_NAME = 'reeval_count') = 0,
  'ALTER TABLE `biz_emergency`
     ADD COLUMN `reeval_count` int NOT NULL DEFAULT 0 COMMENT ''分诊复评次数'' AFTER `last_reeval_time`',
  'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- ② 新表 biz_emergency_reeval：复评流水 ----------
CREATE TABLE IF NOT EXISTS `biz_emergency_reeval` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emergency_id` bigint NOT NULL COMMENT '急诊记录ID',
  `emergency_no` varchar(32) NOT NULL COMMENT '急诊号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `triage_level_before` tinyint COMMENT '复评前分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）',
  `triage_level_after` tinyint NOT NULL COMMENT '复评后分诊级别（只升不降）',
  `mews_score` int COMMENT '复评时 MEWS 评分（0-14）',
  `consciousness` tinyint COMMENT '复评时意识状态（1-清楚 2-对声音有反应 3-对疼痛有反应 4-无反应）',
  `vital_signs` text COMMENT '复评时生命体征（JSON 快照）',
  `reason` varchar(500) NOT NULL COMMENT '复评原因',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_reeval_emergency` (`emergency_id`),
  KEY `idx_reeval_no` (`emergency_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='急诊分诊复评流水';

-- ---------- ③ 交班明细补 MEWS / 待复评定格 ----------
SET @s := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency_handover_item' AND COLUMN_NAME = 'mews_score') = 0,
  'ALTER TABLE `biz_emergency_handover_item`
     ADD COLUMN `mews_score` int COMMENT ''MEWS 评分（交班时定格）'' AFTER `triage_level`',
  'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency_handover_item' AND COLUMN_NAME = 'reeval_due') = 0,
  'ALTER TABLE `biz_emergency_handover_item`
     ADD COLUMN `reeval_due` tinyint COMMENT ''交班时是否待复评（0-否 1-是）'' AFTER `mews_score`',
  'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- ④ 复评按钮菜单 2607 + 授权 ----------
INSERT IGNORE INTO sys_menu
(`id`, `menu_name`, `parent_id`, `sort_order`, `menu_type`, `path`, `component`, `menu_key`, `icon`, `permission`,
 `is_frame`, `is_cache`, `is_visible`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`, `create_by_id`, `update_by_id`)
VALUES
  (2607, '急诊工作站-分诊复评', 205, 4, 3, NULL, NULL, 'opd.emergency:reeval', NULL, 'opd:emergency:reeval',
   0, 0, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '候诊超时再次评估分级（只升不降，留痕）', 1, 1);

-- 分诊护士(role 9)：复评是分诊台的主职，与 20261022 的看板+登记同一授权线
-- ⚠️ 授权 ID 段 8920610230000000301/302 专用，勿与他文件共用——
--    INSERT IGNORE 会把主键冲突静默吞掉（曾因菜单 ID 撞已存在的 2012 把授权插错菜单，
--    回查 SELECT ... WHERE menu_id = 2607 才发现 role 9 没插上），换 ID 段前先跑回查。
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (8920610230000000301, 9, 2607, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610230000000202, 1, 2607, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 回查（应只见 2607；role 9 必须在列，2011/2012 不得出现 role 9） ----------
-- SELECT rm.role_id, m.id, m.menu_name FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 9 AND m.id IN (205, 2010, 2011, 2601, 2607) ORDER BY m.id;
-- SELECT COUNT(*) AS 待复评表 FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_emergency_reeval';
