-- =====================================================================
-- 2026-10-19  G-15 排班发布态（草稿/待审核/已发布/调班中/已变更）
-- 对应蓝图 P4-G15，SQL 编号 215
-- 背景：排班当前「排完即生效」（出诊源保存即出号、员工排班即计在岗、值守即展示），
--       缺少「审核发布才正式生效」的闸门。本脚本给三条排班主表加发布态维度。
-- 口径：
--   · publish_status tinyint NOT NULL（无默认值，铁律：非 del_flag 的 NOT NULL 列一律禁 DEFAULT）。
--     存量数据靠下面的 UPDATE 一次性置为已发布，避免存量号源/排班/值守瞬间从对外消失。
--   · 生效判定 = publish_status ∈ {2 已发布, 4 已变更}；草稿/待审核/调班中均不生效。
--   · 三张表的 status / duty_status 含义不变（号源可用性 / 出勤态 / 有效停用）。
-- 为什么必须有 DEFAULT 的禁令落到这一列上（实测踩坑）：
--   MyBatis-Plus 默认更新策略会「字段为 null 就省列」，列上挂着 DEFAULT 2 时，
--   任何没显式带 publish_status 的 INSERT 都被库填成 2-已发布 —— 新排的班跳过审核直接生效，
--   且零报错、看不出是谁发布的。去掉默认值后漏传直接报 1364，问题当场暴露。
--   代价：写这条列的 INSERT 路径必须自己显式赋值（biz_schedule 已收口在
--   StaffScheduleServiceImpl.resolvePublishStatus，新增=草稿、改单按原态迁移）。
-- 执行：逐语句执行（ALTER / UPDATE / MODIFY 必须分开提交，不能同一语句）。
-- =====================================================================

ALTER TABLE `biz_clinic_source`
  ADD COLUMN `publish_status` tinyint NULL
  COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）'
  AFTER `status`;

ALTER TABLE `biz_schedule`
  ADD COLUMN `publish_status` tinyint NULL
  COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）'
  AFTER `duty_status`;

ALTER TABLE `biz_duty_roster`
  ADD COLUMN `publish_status` tinyint NULL
  COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）'
  AFTER `status`;

-- 存量兜底：新列先留 NULL，历史行统一置为已发布，保证历史排班对外不变。
UPDATE `biz_clinic_source` SET `publish_status` = 2 WHERE `publish_status` IS NULL;
UPDATE `biz_schedule`       SET `publish_status` = 2 WHERE `publish_status` IS NULL;
UPDATE `biz_duty_roster`    SET `publish_status` = 2 WHERE `publish_status` IS NULL;

-- 存量填完再收紧成 NOT NULL（此时表内无 NULL，语句可过），且不回补 DEFAULT。
ALTER TABLE `biz_clinic_source`
  MODIFY COLUMN `publish_status` tinyint NOT NULL
  COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）';
ALTER TABLE `biz_schedule`
  MODIFY COLUMN `publish_status` tinyint NOT NULL
  COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）';
ALTER TABLE `biz_duty_roster`
  MODIFY COLUMN `publish_status` tinyint NOT NULL
  COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）';

-- =====================================================================
-- 二、审核权限注册（⚠️ 三处联动：库 sys_menu.permission + 后端 @PreAuthorize + 前端 v-perm）
-- ---------------------------------------------------------------------
-- 「提交审核」复用各域已有的 add 权限（排班人自己就能提交）；
-- 「审核发布」是新动作，必须独立权限码，否则排班人自己就能把排班发布出去，
-- 审核形同虚设 —— 新增 outpatient:schedule:audit / nursing:schedule:audit。
--
-- 菜单ID：2980 / 2981（已确认 sys_menu 未占用；2980-2989 只出现在 sys_icd9cm3，不同表）
-- menu_type = 3 按钮，挂在各域主菜单下（804 门诊排班 / 331 病区护理排班）
-- =====================================================================

INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2980', '门诊排班-审核发布', '804', 4, 3, NULL, NULL, 'outpatient.schedule:audit', NULL,
        'outpatient:schedule:audit', 0, 1, 0, 1, 'admin', 'admin', 0,
        'G-15 排班发布态：待审核→已发布的审核动作（独立审核人）', '1', '1');

INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2981', '病区护理排班-审核发布', '331', 4, 3, '', '', 'nursing.schedule:audit', '',
        'nursing:schedule:audit', 0, 0, 1, 1, 'admin', 'admin', 0,
        'G-15 排班发布态：待审核→已发布的审核动作（独立审核人）', '1', '1');

INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2982', '总值班排班-审核发布', '806', 4, 3, NULL, NULL, 'org.duty:audit', NULL,
        'org:duty:audit', 0, 1, 0, 1, 'admin', 'admin', 0,
        'G-15 排班发布态：待审核→已发布的审核动作（独立审核人）', '1', '1');

-- 授权：审核权给「门诊办(25)」「科主任(27)」—— 排班业务的院级/科室级主管岗；
-- 护士长(10) 增护理排班审核权；医务科(26) 增总值班审核权。
-- ⚠️ 超管(1) 不是「天然全有」：权限串是从 sys_role_menu 逐行查出来的（实测 admin 共 476 条），
--    没有超级角色绕过分支。漏授权的结果不是「管理员能审」而是「全仓没人能审」——
--    门诊办/科主任当下零人挂岗（sys_employee_post 无行），审核发布只剩超管一个出口，
--    所以三个审核按钮必须显式补授 role 1，与本仓「超管持全部页面按钮」的既有口径对齐。
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id) VALUES
(8920610100000000201, 25, 2980, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8920610100000000202, 27, 2980, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8920610100000000203, 10, 2981, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8920610100000000204, 26, 2982, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8920610100000000205,  1, 2980, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8920610100000000206,  1, 2981, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8920610100000000207,  1, 2982, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ⚠️ 执行后必须清 Redis 权限缓存 his:perm:role:*（TTL 10min）或重启后端，否则新权限短时不生效。
--   redis-cli -a <pwd> --scan --pattern 'his:perm:role:*' | xargs redis-cli -a <pwd> del

-- =====================================================================
-- 三、【第二步，换新 jar 之后才执行】把已执行过的库里残留的 DEFAULT 2 摘掉
-- ---------------------------------------------------------------------
-- 本脚本第一节原本写成 `NOT NULL DEFAULT 2`，违反 §15（非 del_flag 的 NOT NULL 列禁默认值），
-- 且实测后果是「审核闸被静默绕过」：任何没显式带 publish_status 的 INSERT 被库填成 2-已发布。
-- 现在三条写入路径已全部显式赋值：
--   · biz_clinic_source   → BizClinicSourceServiceImpl.initNewScheduleRow（手工/批量/模板/复刻共用）
--   · biz_schedule        → StaffScheduleServiceImpl.resolvePublishStatus（新增=草稿、改单按原态迁移）
--   · biz_duty_roster     → DutyRosterServiceImpl.nextPublishStatus
-- 所以下面三条 DROP DEFAULT 可以执行。
--
-- ⚠️ 顺序不能反：正在跑的旧 jar（本机 8080 调试实例、容器 :8009）没有这些赋值语句，
--    它保存排班时 INSERT 不带 publish_status 列。库里留着 DEFAULT 2 它还能跑（只是绕过审核），
--    一旦摘掉默认值就当场 1364「Field 'publish_status' doesn't have a default value」。
--    必须先「重新打包 + 重启成新代码」，再跑本节。跑完回查 COLUMN_DEFAULT IS NULL。
-- =====================================================================

ALTER TABLE `biz_clinic_source` ALTER COLUMN `publish_status` DROP DEFAULT;
ALTER TABLE `biz_schedule`      ALTER COLUMN `publish_status` DROP DEFAULT;
ALTER TABLE `biz_duty_roster`   ALTER COLUMN `publish_status` DROP DEFAULT;

-- 回查（必须三行 COLUMN_DEFAULT 全为 NULL、IS_NULLABLE 全为 NO）：
--   SELECT TABLE_NAME, COLUMN_DEFAULT, IS_NULLABLE
--     FROM information_schema.COLUMNS
--    WHERE TABLE_SCHEMA = DATABASE() AND COLUMN_NAME = 'publish_status'
--    ORDER BY TABLE_NAME;
