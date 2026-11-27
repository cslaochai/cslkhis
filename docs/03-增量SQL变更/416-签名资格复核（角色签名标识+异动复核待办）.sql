-- ============================================================
-- 416 签名资格复核：角色签名资格标识 + 复核待办表 + 医务科复核按钮
-- 日期：2026-11-27
-- 背景：转岗不能一刀切吊销证书。判断轴是「新岗位还需不需要电子签名」：
--       医生换科室、医生转护士（仍要签文书）→ 证书继续有效；
--       签名岗转行政/后勤/病案等不再签文书的岗位 → 由医务科复核后吊销。
-- 内容：
--   1) sys_role 增列 sign_required（1-需要电子签名资格 0-不需要），给临床签名角色铺 1；
--   2) 新建 biz_sign_qual_review（人事异动把签名岗换成非签名岗时生成的复核单）；
--   3) 新增按钮 29631「签名资格复核」（parent 29621，sign:cert:review），授 role26（医务科）+ role1（超管）。
-- 幂等：列/表/菜单/授权均按存在性判重；重放不产生重复行。
-- 执行后清 Redis：DEL his:perm:role:10036 his:perm:role:10012
-- 回滚：见同目录 416-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- 0. 前置核对 ----------
-- SELECT COUNT(*) AS col_should_be_0 FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_role' AND COLUMN_NAME = 'sign_required';
-- SELECT COUNT(*) AS table_should_be_0 FROM information_schema.TABLES
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_sign_qual_review';
-- SELECT COUNT(*) AS menu_29631_should_be_0 FROM sys_menu WHERE id = 29631;

-- ---------- 1. 角色签名资格标识 ----------
ALTER TABLE `sys_role`
  ADD COLUMN `sign_required` tinyint NOT NULL DEFAULT 0 COMMENT '是否需要电子签名资格（1-需要 0-不需要）' AFTER `status`;

-- 临床签名角色铺 1：医生/护士/急诊/麻醉/放射诊断/公卫/科主任/护理管理/药剂
UPDATE `sys_role` SET `sign_required` = 1, `update_by` = 'sql416', `update_time` = NOW(),
    `update_by_id` = 0
 WHERE `id` IN (2, 3, 5, 8, 9, 10, 13, 20, 22, 27, 29, 30, 31, 32, 44);

-- ---------- 2. 签名资格复核单 ----------
CREATE TABLE IF NOT EXISTS `biz_sign_qual_review` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `emp_name` varchar(64) NOT NULL DEFAULT '' COMMENT '员工姓名（快照）',
  `from_posts` varchar(500) NOT NULL DEFAULT '' COMMENT '异动前岗位摘要（角色/科室）',
  `to_posts` varchar(500) NOT NULL DEFAULT '' COMMENT '异动后岗位摘要（角色/科室）',
  `new_sign_required` tinyint NOT NULL DEFAULT 0 COMMENT '新岗位是否需要电子签名资格（1-需要 0-不需要）',
  `cert_id` bigint COMMENT '异动时有效证书ID',
  `cert_no` varchar(32) COMMENT '证书编号（快照）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '复核状态（0-待复核 1-保留证书 2-已吊销）',
  `handle_remark` varchar(200) COMMENT '处理意见（吊销时即吊销原因）',
  `handle_by_name` varchar(64) COMMENT '处理人姓名',
  `handle_time` datetime COMMENT '处理时间',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL DEFAULT 0 COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_qual_review_emp` (`employee_id`),
  KEY `idx_qual_review_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='签名资格复核单（人事异动触发，医务科决定保留或吊销）';

-- ---------- 3. 医务科复核按钮 ----------
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `menu_type`, `path`, `component`, `menu_key`, `icon`,
                        `permission`, `is_frame`, `is_cache`, `is_visible`, `status`,
                        `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`,
                        `create_by_id`, `update_by_id`)
SELECT 29631, '签名资格复核', 29621, 30, 3, NULL, NULL, 'sign.cert:review', NULL,
       'sign:cert:review', 0, 1, 0, 1,
       'sql416', NOW(), 'sql416', NOW(), 0, '岗位转出签名岗后决定证书保留/吊销（医务科）',
       0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29631)
   AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'sign:cert:review');

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 9210000000000004161, 26, 29631, 'sql416', NOW(), 'sql416', NOW(), 0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 26 AND `menu_id` = 29631)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29631 AND `permission` = 'sign:cert:review');

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 9210000000000004162, 1, 29631, 'sql416', NOW(), 'sql416', NOW(), 0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 29631)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29631 AND `permission` = 'sign:cert:review');

COMMIT;

-- ---------- 执行后回查 ----------
-- 1) 角色标识（应为 15 行 sign_required=1）
-- SELECT id, role_name, sign_required FROM sys_role WHERE sign_required = 1 ORDER BY id;
-- 2) 复核表结构
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_sign_qual_review' ORDER BY ORDINAL_POSITION;
-- 3) 按钮与授权（应为 1 行菜单 + 2 行授权）
-- SELECT id, menu_name, parent_id, permission FROM sys_menu WHERE id = 29631;
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = 29631 ORDER BY role_id;
