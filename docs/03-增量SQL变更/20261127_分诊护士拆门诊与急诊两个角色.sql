-- =====================================================================
-- 20261127 分诊护士按门诊 / 急诊拆成两个岗位角色
-- ---------------------------------------------------------------------
-- 背景：
--   role 9「分诊护士」一个角色同时持有门诊分诊工作站(202)与急诊护士工作站
--   (205/2010/2607)，靠 DeptScope 的科室归属兜住数据面，权限面是超集：
--   门诊分诊护士进得了急诊工作站（空列表），急诊分诊护士进得了门诊分诊台；
--   复评(opd:emergency:reeval)这种有准入资质的活，门诊分诊护士也点得动。
--
-- 口径（对齐 WS/T 390-2012、急诊预检分诊专家共识、DB4403/T 244-2022）：
--   急诊分诊护士 = 独立岗位，5 年以上急诊经验、24h 在岗、专科技能准入；
--   门诊分诊护士 = 门诊预检台，做体温体征、分诊记录、叫号改派。
--   两者不是同一岗位，权限集合也不互相包含。
--
-- 做法（不新增权限码、不改任何接口）：
--   ① role 9 定名「门诊分诊护士」，收回 205/2010/2011/2607；
--   ② 新增 role 44 / role_code 10054「急诊分诊护士」，授 205/2010/2607；
--   ③ 急诊三科（急诊内科/急诊外科/急诊儿科）现有 role 9 岗位原地改挂 role 44；
--      发热门诊、肠道门诊、心血管内科等门诊单元仍归 role 9。
--
-- 边界：
--   · 2011 接诊/留观/转住院/离院/死亡 仍只归急诊医生(8)，护士角色一律不授；
--     分诊腕带的按钮权限同时从 edit 改挂 add（见 EmergencyView.vue），
--     腕带是建档环节的延伸，不是接诊动作。
--   · 2601 交班清零 不授护士（现状即未授）。
--
-- 幂等：角色与授权都带 NOT EXISTS 守卫；重复执行零变化。
-- ID 段：本文件专用 8920611270000000101~117，勿与他文件共用（INSERT 主键
--   冲突会被静默吞掉）；沿用前先跑文末回查。
-- 生效：清 Redis 权限缓存 his:perm:role:*（TTL 10min）；受影响人员需重新登录
--   （旧 token 里 currentRole 仍是 10020）。
-- =====================================================================

-- ---------- ① 新角色：急诊分诊护士 ----------
INSERT INTO sys_role
  (`id`, `role_code`, `role_name`, `role_type`, `staff_type`, `data_scope`, `sort_order`,
   `status`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`,
   `create_by_id`, `update_by_id`)
SELECT 44, '10054', '急诊分诊护士', 1, 2, 3, 44,
       1, 'admin', NOW(), 'admin', NOW(), 0,
       '急诊预检分诊专职岗（24h 在岗、5 年以上急诊经验准入）：急诊看板、登记建档、分诊复评、腕带打印；接诊与转归归急诊医生(8)，门诊分诊归门诊分诊护士(9)。',
       1, 1
FROM (SELECT 1) t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role r WHERE r.id = 44 OR r.role_code = '10054'
);

-- ---------- ② role 9 定名：门诊分诊护士（role_code 10020 不动，JWT 依赖） ----------
UPDATE sys_role
   SET role_name = '门诊分诊护士',
       remark = '门诊预检分诊岗：分诊工作站（体温体征/分诊记录）、叫号与改派、门诊日志、患者检索建档；急诊预检归急诊分诊护士(44)。',
       update_by = 'admin',
       update_time = NOW()
 WHERE id = 9
   AND role_code = '10020';

-- ---------- ③ role 9 收回急诊工作面（2011 属急诊医生，一并收回） ----------
DELETE FROM sys_role_menu
 WHERE role_id = 9
   AND menu_id IN (205, 2010, 2011, 2607);

-- ---------- ④ role 44 授权：急诊工作面 + 两岗共用的门户/患者中心 ----------
-- 205 急诊护士工作站   / 2010 登记建档 / 2607 分诊复评
-- 700/701/703/704/705/2109/2110 患者检索、360、标签（登记选人与建档要用）
-- 100/101/102/200 门户与门（急）诊目录；2001 统一待办里处理消息；29654 统一待办；29605 急诊值班只读
INSERT INTO sys_role_menu
  (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`,
   `create_by_id`, `update_by_id`)
SELECT v.id, 44, v.menu_id, 'admin', NOW(), 'admin', NOW(), 1, 1
FROM (
            SELECT 8920611270000000101 AS id, 100  AS menu_id
  UNION ALL SELECT 8920611270000000102, 101
  UNION ALL SELECT 8920611270000000103, 102
  UNION ALL SELECT 8920611270000000104, 200
  UNION ALL SELECT 8920611270000000105, 205
  UNION ALL SELECT 8920611270000000106, 700
  UNION ALL SELECT 8920611270000000107, 701
  UNION ALL SELECT 8920611270000000108, 703
  UNION ALL SELECT 8920611270000000109, 704
  UNION ALL SELECT 8920611270000000110, 705
  UNION ALL SELECT 8920611270000000111, 2001
  UNION ALL SELECT 8920611270000000112, 2010
  UNION ALL SELECT 8920611270000000113, 2109
  UNION ALL SELECT 8920611270000000114, 2110
  UNION ALL SELECT 8920611270000000115, 2607
  UNION ALL SELECT 8920611270000000116, 29605
  UNION ALL SELECT 8920611270000000117, 29654
) v
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 44 AND rm.menu_id = v.menu_id
);

-- ---------- ⑤ 急诊三科岗位改挂 role 44 ----------
-- 只动急诊内科(19580048)/急诊外科(19580049)/急诊儿科(19580051)；
-- 发热门诊、肠道门诊、心血管内科等门诊单元保持 role 9。
-- 本表无 del_flag：这是「角色重定义」而非岗位生效/失效，原地改 role_id，
-- 保留 effective_date 与 is_primary（每人仍恰一条主岗位，unique 键不撞）。
-- 执行前先跑文末回查①，确认这三个科室的 role 9 岗位在 role 44 下没有同日同科重复行。
UPDATE sys_employee_post
   SET role_id = 44,
       update_by = 'admin',
       update_time = NOW(),
       update_by_id = 1
 WHERE role_id = 9
   AND dept_id IN (19580048, 19580049, 19580051);

-- =====================================================================
-- 回查（执行后逐条核对）
-- =====================================================================
-- ① 迁移前防撞（role 44 下不得已有同日同科同人岗位）：
-- SELECT employee_id, dept_id, effective_date, COUNT(*) c
--   FROM sys_employee_post WHERE role_id = 44
--  GROUP BY employee_id, dept_id, effective_date HAVING c > 1;
--
-- ② 角色：
-- SELECT id, role_code, role_name, staff_type, data_scope, status FROM sys_role WHERE id IN (9, 44);
--   预期 9=门诊分诊护士/10020/护理/本部门；44=急诊分诊护士/10054/护理/本部门。
--
-- ③ 授权面（role 9 不得出现 205/2010/2011/2601/2607；role 44 必须出现 205/2010/2607）：
-- SELECT rm.role_id, m.id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id IN (9, 44) AND m.id IN (202, 205, 2010, 2011, 2601, 2607)
--  ORDER BY rm.role_id, m.id;
--
-- ④ 岗位：
-- SELECT ep.role_id, d.dept_name, COUNT(*) c
--   FROM sys_employee_post ep JOIN sys_department d ON d.id = ep.dept_id
--  WHERE ep.role_id IN (9, 44) GROUP BY ep.role_id, d.dept_name ORDER BY ep.role_id;
--   预期 44：急诊内科/急诊外科/急诊儿科（2+2+2）；9：心血管内科、发热门诊、肠道门诊。
--
-- ⑤ 主岗位不变量（每人恰一条 is_primary=1）：
-- SELECT employee_id, SUM(is_primary = 1) s FROM sys_employee_post
--  GROUP BY employee_id HAVING s <> 1;
