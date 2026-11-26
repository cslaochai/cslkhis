-- ============================================================
-- 388 新增角色 42「应用维护工程师」+ 系统管理域日常运维分权
-- 日期：2026-11-26
-- 背景（真实医院分工，非本项目自创）：
--   信息科内部从来不是一个"系统管理员"包打天下。三甲通行做法是
--     · 系统管理员（超管）：账号封存/双人保管，只在应急时用，属兜底；
--     · 应用维护工程师：日常实名账号，办账号开通/停用/改岗、参数、字典、
--       工作台配置、流程定义，是全科最忙的那个岗；
--     · 安全管理员 / 审计员：策略与日志复核，与运维分权（等保三级三员分立）。
--   本项目现状：role 1 系统管理员 一人持有系统管理全部写按钮（338 已把业务域收成只读），
--   role 33 信息科管理员 实际只落了签名证书台账（313/319/326）。
--   即"日常运维"这一档没有角色承接，日常操作只能借用 admin，与真实分工不符。
-- 口径：
--   A 新增 role 42 / role_code 10052「应用维护工程师」，定位 P16a 执行档（106 基线）；
--   B 授「系统管理」域日常写权限：用户（增/改）+ 字典（增/改）+ 参数（增）
--     + 工作台配置页 + 流程定义页与配置按钮；角色/菜单/部门/日志 = 只读可查；
--   C 不授：角色与菜单的写按钮、字典删除、用户删除、日志导出、签名审计记录、
--     部门增删、医嘱字典族（2383/2387 族按 338 归住院临床配置域）；
--   D role 1 与 role 33 授权一律不动（超管兜底、33 管证书，互不侵占）。
-- 为什么不给角色/菜单写权限（本项目实证，不是保守）：
--   340 空壳角色、343 隐藏页转按钮、345 孤儿页补位、347 菜单树断链修复——
--   四批脚本全在收拾"角色管理页手工保存 / 菜单树改动"造成的授权断链。
--   改一次授权树 = 全科有人侧栏空掉，这类操作必须留在有审批链的人手里。
-- 边界：
--   · 只动 role 42（新角色），不碰其他 41 个角色的任何授权行；
--   · 只授现有菜单 id，不新增菜单、不改 sys_menu；
--   · 角色本身不落账号/岗位，需在"用户管理→岗位"给人挂 P16a 岗（脚本不代劳）。
-- 避坑：
--   · sys_role_menu 唯一键 uk_role_menu(role_id, menu_id)，补授统一 NOT EXISTS 判重；
--   · id 段 92100000000000008xx 为 388 专属，避开 341/342 已占的 6xx 段；
--   · 角色 id 42 / code 10052：现库 max(id)=41、max(code)=10051，顺延不撞。
-- 执行后清缓存（TTL 10 分钟，SQL 改库不自动失效）：
--   redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--   redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
-- 回滚：见文末「回滚段」。
-- ============================================================

-- ---------------- A 新增角色 ----------------
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
 (42, '10052', '应用维护工程师', 1, 6, 1, 44, 1, 'sql388', NOW(), 'sql388', NOW(), 0,
  '信息科日常运维执行岗（P16a 执行档）：账号开通/停用/改岗、参数、业务字典、工作台配置、流程定义；角色/菜单/部门只读，超管(1)仅兜底',
 1, 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), remark = VALUES(remark), update_time = NOW();

-- ---------------- B 授权：目录 + 页面 ----------------
-- 100 门户目录 / 101 工作台 / 29654 统一待办（登录后基础入口，与全员同）
-- 1100 系统管理目录
-- 1101 用户管理 / 1102 角色管理(只读) / 1103 菜单管理(只读) / 1104 字典管理
-- 1105 参数设置 / 1106 工作台配置 / 1108 日志审计(只读) / 1109 部门管理(只读)
-- 29652 流程定义
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT seed.id, 42, seed.menu_id, 'sql388', NOW(), 'sql388', NOW(), 1, 1
FROM (
    SELECT 9210000000000000801 AS id, 100    AS menu_id UNION ALL
    SELECT 9210000000000000802, 101    UNION ALL
    SELECT 9210000000000000803, 29654  UNION ALL
    SELECT 9210000000000000804, 1100   UNION ALL
    SELECT 9210000000000000805, 1101   UNION ALL
    SELECT 9210000000000000806, 1102   UNION ALL
    SELECT 9210000000000000807, 1103   UNION ALL
    SELECT 9210000000000000808, 1104   UNION ALL
    SELECT 9210000000000000809, 1105   UNION ALL
    SELECT 9210000000000000810, 1106   UNION ALL
    SELECT 9210000000000000811, 1108   UNION ALL
    SELECT 9210000000000000812, 1109   UNION ALL
    SELECT 9210000000000000813, 29652
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = 42 AND x.menu_id = seed.menu_id);

-- ---------------- C 授权：写按钮（白名单 6 个） ----------------
-- 2148 用户新增 / 2149 用户修改（停用、重置密码、挂岗位都在这里）
-- 2155 字典新增 / 2156 字典修改
-- 2158 参数新增（参数页 upsert 只挂这一个码）
-- 29641 流程定义配置
-- 刻意不授：2150 用户删除、2151/2152 角色增删、2153/2154 菜单增删、2157 字典删除、
--           2510 日志导出、29624 签名记录、2933/2934 部门增删、2384-2386/组套族、2943 RAG 配置
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT seed.id, 42, seed.menu_id, 'sql388', NOW(), 'sql388', NOW(), 1, 1
FROM (
    SELECT 9210000000000000814 AS id, 2148 AS menu_id UNION ALL
    SELECT 9210000000000000815, 2149  UNION ALL
    SELECT 9210000000000000816, 2155  UNION ALL
    SELECT 9210000000000000817, 2156  UNION ALL
    SELECT 9210000000000000818, 2158  UNION ALL
    SELECT 9210000000000000819, 29641
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = 42 AND x.menu_id = seed.menu_id);

-- ---------------- D 回查 ----------------
-- D0 角色本体（期望 1 行，role_code=10052）
-- SELECT id, role_code, role_name, role_type, staff_type, data_scope, status FROM sys_role WHERE id = 42;

-- D1 授权类型分布（期望 目录 2 / 页面 11 / 按钮 6，共 19）
-- SELECT m.menu_type, COUNT(*) c FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id WHERE rm.role_id = 42 GROUP BY m.menu_type;

-- D2 越权残留（期望 0 行）：黑名单按钮一个都不许出现
-- SELECT rm.menu_id, m.menu_name, m.permission FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE rm.role_id = 42 AND m.menu_type = 3
--   AND rm.menu_id NOT IN (2148,2149,2155,2156,2158,29641);

-- D3 侧栏可达性（期望无孤儿）：授了叶子但祖先目录没授的行
-- SELECT rm.menu_id, m.menu_name, m.parent_id FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE rm.role_id = 42 AND m.menu_type = 2 AND m.parent_id <> 0
--   AND NOT EXISTS (SELECT 1 FROM sys_role_menu p WHERE p.role_id = 42 AND p.menu_id = m.parent_id);

-- ============================================================
-- 回滚段
-- DELETE FROM sys_role_menu WHERE role_id = 42;
-- DELETE FROM sys_role WHERE id = 42 AND role_code = '10052';
-- 回滚后同样清 his:perm:role:* 与 his:user:identity:* 缓存。
-- ============================================================
