-- =====================================================================
-- 20261026 新增「麻醉医师」「手术室护士」两个岗位角色（排台人员候选收口）
-- ---------------------------------------------------------------------
-- 背景：手术排台弹框的麻醉医师 / 巡回护士 / 器械（洗手）护士三个下拉原来是
--       `GET /system/employee/selectList` 全量（全院 535 人），能选到行政、药房、病房护士，
--       排台时等于没候选约束。项目里原先没有「麻醉医师」「手术室护士」的结构化标识：
--         · sys_role 30 个角色里只有笼统的「医生(2)/护士(3)」；
--         · emp_type 字典（1医士 2医师 3药士 4药师 5护士 6护师）与库内数据对不上
--           （246 人填 1、167 人填 2），不能作为口径；
--         · 麻醉科挂在 dept_type=2「医技科室」下，同组还有检验/放射/病理等 11 个，
--           按科室类型分不出麻醉科。
--       故按「加角色」口径落地（老王 2026-10-26 定）：
--         31 麻醉医师   role_code=10041  staff_type=1（医生）
--         32 手术室护士 role_code=10042  staff_type=2（护理）
--
-- 绑人依据（不编数据）：
--   麻醉医师   ← 科室=麻醉科(19580039) 且 emp_type=1（医师）：袁和玉/刘思琪/江娜/陈亦航
--   手术室护士 ← ① 麻醉科护士 温俊杰（手术室内护士）
--                ② 历史排台记录里真被排过巡回/器械护士的人（biz_operation_apply
--                   circulating_nurse_id / scrub_nurse_id 实查：孟瑶(巡回4)、尚明珠(巡回1)、
--                   杨乐怡(器械4)）—— 她们当前档案在呼吸内科，说明现有人事数据里没有
--                   「手术室」科室，只能以真实排台事实为准。日后人事补手术室科室后，
--                   把病房护士摘掉 role 32 即可，不影响其他权限。
--   ⚠️ 消毒供应中心(19580094) 4 人是 emp_type=6 / 角色「医技人员」，不属于手术室护士，未绑。
--
-- 菜单授权口径：门户(100)/工作台(101)/消息待办(102) 登录必需 + 住院管理(300) 祖先目录
--   + 手术排期(306) 及其 add/edit/delete + 麻醉工作站(309) 及其 add/edit。
--
-- 幂等：角色按 role_code 判存在；授权与岗位按 (role_id) / (employee_id, role_id) 先删后插。
-- ⚠️ 执行后必须清 Redis 权限缓存 his:perm:role:{role_code}，或重启后端（TTL 10min 不自愈）。
-- =====================================================================

-- ===================== 回滚段（需要时取消注释执行） =====================
-- DELETE FROM sys_employee_post WHERE role_id IN (31, 32);
-- DELETE FROM sys_role_menu     WHERE role_id IN (31, 32);
-- DELETE FROM sys_role          WHERE id      IN (31, 32);
-- -- 并清 Redis：his:perm:role:10041 / his:perm:role:10042

-- ---------------- 1. 两个角色 ----------------
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 31, '10041', '麻醉医师', 1, 1, 1, 41, 1,
       'sql298', NOW(), 'sql298', NOW(), 0,
       '手术排台「麻醉医师」候选口径：排台时按此角色取人，不再全院 535 人', 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = '10041');

INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 32, '10042', '手术室护士', 1, 2, 1, 42, 1,
       'sql298', NOW(), 'sql298', NOW(), 0,
       '手术排台「巡回/器械护士」候选口径：排台时按此角色取人', 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = '10042');

-- ---------------- 2. 菜单授权（先删后插，保证幂等） ----------------
DELETE FROM sys_role_menu WHERE role_id IN (31, 32);

-- 31 麻醉医师
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (8926102620000000100, 31, 100,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000101, 31, 101,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000102, 31, 102,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000103, 31, 300,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000104, 31, 306,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000105, 31, 2024, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000106, 31, 2025, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000107, 31, 2026, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000108, 31, 309,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000109, 31, 2033, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000110, 31, 2034, 'sql298', NOW(), 'sql298', NOW(), 1, 1);

-- 32 手术室护士
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (8926102620000000120, 32, 100,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000121, 32, 101,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000122, 32, 102,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000123, 32, 300,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000124, 32, 306,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000125, 32, 2024, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000126, 32, 2025, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000127, 32, 2026, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000128, 32, 309,  'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000129, 32, 2033, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000130, 32, 2034, 'sql298', NOW(), 'sql298', NOW(), 1, 1);

-- ---------------- 3. 岗位绑定（is_primary=0，不动原主岗位） ----------------
DELETE FROM sys_employee_post
WHERE role_id IN (31, 32)
  AND employee_id IN (897001347, 897001348, 897001349, 897001350, 897001351,
                      897001005, 897001006, 897001007);

-- 31 麻醉医师：麻醉科 4 名医师
INSERT INTO sys_employee_post (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
                               create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (8926102620000000150, 897001347, 31, 19580039, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000151, 897001348, 31, 19580039, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000152, 897001349, 31, 19580039, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000153, 897001350, 31, 19580039, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1);

-- 32 手术室护士：麻醉科护士 + 历史真排过台的 3 名护士
INSERT INTO sys_employee_post (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
                               create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (8926102620000000160, 897001351, 32, 19580039, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000161, 897001005, 32, 19580003, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000162, 897001006, 32, 19580003, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1),
  (8926102620000000163, 897001007, 32, 19580003, 0, NULL, NULL, 'sql298', NOW(), 'sql298', NOW(), 1, 1);

-- ---------------- 4. 自检 ----------------
-- ⚠️ 不写成 UNION ALL：中文别名在 UNION 里会撞 Illegal mix of collations。
SELECT id, role_code, role_name, staff_type, status FROM sys_role WHERE id IN (31, 32);
SELECT role_id, COUNT(*) AS menu_cnt FROM sys_role_menu WHERE role_id IN (31, 32) GROUP BY role_id;
SELECT role_id, COUNT(*) AS post_cnt FROM sys_employee_post WHERE role_id IN (31, 32) GROUP BY role_id;
