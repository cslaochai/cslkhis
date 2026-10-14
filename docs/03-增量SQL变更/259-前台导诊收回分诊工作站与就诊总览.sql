-- 前台导诊（sys_role.id=7 / role_code 10018）收回「分诊工作站」与「就诊总览」及其按钮码
-- 收回的 4 条授权：202（opd:triage:list 页面）、2005（opd:triage:add）、2006（opd:triage:edit）、204（opd:todayVisits:list 页面）
-- 为什么：这两个页面不是前台的岗位动作——
--   分诊工作站是分诊护士(9)的岗位，前台拿着 add/edit 等于能替药师改叫号队列与分诊级别（/queue/triage/save 只认 opd:triage:add|edit）；
--   就诊总览是跨科室回溯分析页，给到前台是全院门诊流水，超出其挂号/收费的操作范围。
-- 为什么会有：20261010_角色菜单权限收口_全量.sql 对 role 7 只收回了 org:% 一个域，
--   「保留 opd/patient/portal」是按域整片留的，没逐页面判过，这两页是被 opd:% 整域带下来的。
-- 影响面：前端只有 views/triage/TriageView.vue 调 /queue/triage/*，挂号预约页不调，收回后不影响前台代收挂号费。
-- 未收口的口子（本次只改授权，不动代码）：BizQueueController 类级
--   @PreAuthorize("hasAnyAuthority('opd:appointments:list', …, 'opd:todayVisits:list', 'opd:triage:list')")
--   含 opd:appointments:list，所以门诊日志读数（/queue/opdLogListPage|Stats）与分诊记录读取
--   （/queue/triage/getByQueueId）对前台仍然放行 —— 收回菜单只让它从侧边栏与路由里消失，
--   接口层要真收口得把类级集合里的 todayVisits/triage 两条摘掉（另有角色依赖，单独一步做）。
-- 前置：sys_role_menu 唯一键 uk_role_menu(role_id, menu_id) 不含 del_flag，删除即物理删（本表无软删列）。
-- 执行后必须清 Redis：DEL his:perm:role:10018（key 后缀是 role_code 不是 role_id，见 RolePermissionCacheImpl.KEY_PREFIX + roleCode；
--   不清则 10 分钟内仍是旧权限集合）
-- 回滚：见文件末尾注释掉的 INSERT（id 逐字取自收回前实测）

DELETE
FROM sys_role_menu
WHERE role_id = 7
  AND menu_id IN (202, 204, 2005, 2006);

-- 回查 1：应为 0 行
SELECT rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 7
  AND rm.menu_id IN (202, 204, 2005, 2006);

-- 回查 2：分诊护士(9) 与系统管理员(1) 不受影响，应仍各自持有 202
SELECT rm.role_id, ro.role_name, rm.menu_id, m.permission
FROM sys_role_menu rm
         JOIN sys_menu m ON m.id = rm.menu_id
         JOIN sys_role ro ON ro.id = rm.role_id
WHERE rm.menu_id IN (202, 204)
ORDER BY rm.menu_id, rm.role_id;

-- 回滚用（收回前的 4 行原始 id，需人工按实际取值补回后再执行）
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id) VALUES
--   (2103308774428188677, 7, 202, 'admin', 'admin', 1, 1),
--   (2103308774495297538, 7, 204, 'admin', 'admin', 1, 1),
--   (2103308774495297543, 7, 2005, 'admin', 'admin', 1, 1),
--   (2103308774558212098, 7, 2006, 'admin', 'admin', 1, 1);
