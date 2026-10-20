-- =====================================================================
-- 20261019 班次字典独立菜单与权限域拆分
-- ---------------------------------------------------------------------
-- 背景：
--   ① biz_shift（班次字典）是全院共用数据，ShiftUseScopeEnum 分 4 册：
--      门诊排班 / 病区护理 / 全院值守 / 全院通用，被排班面板、排班周模板、
--      值守点位（总值班经点位引用）、岗位人力标准等多处下拉引用。
--      但维护入口原先藏在「门诊排班(804)」页面的「班次字典」签里，
--      「护理排班 / 值守点位 / 人力标准」的使用方要维护班次得绕进门诊排班页面。
--   ② 权限错位：后端 ShiftController 一直借用岗位出勤(2935)的
--      staff:schedule:* 权限域（listPage→list、shiftUpsert→add、
--      rename/updateStatus→edit、deleteById→delete），
--      前端「班次字典」签按钮却用门诊排班的 outpatient:schedule:add/edit，
--      两套串不一致 → 前端看得见按钮但后端 403、或有权限但界面不可见。
--  本次动作（三处联改，与 20261011「岗位人力标准权限域拆分」同一套路）：
--    A. sys_menu：新增排班中心(2945)下平级菜单「班次字典」(2939) + 三个按钮码
--       （menu_type=3：shift:dict:add / edit / delete），菜单权限 shift:dict:list。
--    B. 后端：ShiftController 的 @PreAuthorize 同步改为 shift:dict:*（已随代码提交）。
--    C. 前端：班次字典签抽成独立页面 ShiftDictView.vue（路由 /shift-dict），
--       ScheduleView.vue 移除该签；ScheduleDictAdd / ScheduleShiftEdit 的
--       v-perm 同步改为 shift:dict:*（已随代码提交）。
--  授权口径：仅授给「现状已能维护班次」的角色，不扩权不回归 ——
--    超管(1) 全量；护士长(10) 原在门诊排班签内维护班次 → 独立后保持；
--    医务科(26)、科主任(27) 原持 staff:schedule:add/edit/delete 按钮码
--    （可直连接口）→ 独立后补页面入口。
--  幂等：sys_menu 有 uk_menu_key、sys_role_menu 有 uk_role_menu(role_id,menu_id)，
--       一律 INSERT IGNORE + 固定 id，重复执行零变化。
-- ⚠️ 执行后必须清 Redis 权限缓存，否则已登录用户仍按旧权限快照走：
--     ① his:perm:role:*   角色→权限码集合（TTL 10min）
--     ② his:user:identity:*  用户身份快照（含权限并集，JwtAuthenticationFilter 每次请求先读它，
--        /auth/info 返回的 permissions 就来自这里 —— 不清它，前端 v-perm 会继续按旧集合藏按钮）
--     redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli del
--     redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli del
--     或重启后端（重启不保证清 Redis，两个 key 都要删）。
-- =====================================================================

-- =====================================================================
-- 一、新增「班次字典」独立菜单（父菜单 2945 排班中心，sort_order=8 排最后）
-- =====================================================================
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status,
                             create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (2939, '班次字典', 2945, 8, 2, '/shift-dict', 'schedule/ShiftDictView', 'org.shiftDict', 'Clock',
        'shift:dict:list', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '全院标准班次（门诊/护理/值守/通用四册）唯一维护入口，20261019 自门诊排班签拆出', 1, 1);

-- =====================================================================
-- 二、补「班次字典」三个按钮码（父菜单 2939，码值逐字取自后端 @PreAuthorize）
-- =====================================================================
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status,
                             create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (29391, '班次字典-新增', 2939, 1, 3, NULL, NULL, 'org.shiftDict:add', NULL, 'shift:dict:add', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '新增班次（完整建条，后端 shiftUpsert）', 1, 1),
       (29392, '班次字典-修改', 2939, 2, 3, NULL, NULL, 'org.shiftDict:edit', NULL, 'shift:dict:edit', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '班次改名 / 状态开关 / 适用域整条覆盖', 1, 1),
       (29393, '班次字典-删除', 2939, 3, 3, NULL, NULL, 'org.shiftDict:delete', NULL, 'shift:dict:delete', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '逻辑删班次（界面当前只给停用，未接该动作）', 1, 1);

-- =====================================================================
-- 三、角色授权（保持现有维护能力不回归）
-- ---------------------------------------------------------------------
-- 超管(1)    → 菜单 + 三个按钮（超管本就应当持有全部菜单行）
-- 护士长(10) → 菜单 + 三个按钮（原门诊排班签内维护班次，独立后保持）
-- 医务科(26) → 菜单 + 三个按钮（原持 staff:schedule:add/edit/delete，直连接口可用）
-- 科主任(27) → 菜单 + 三个按钮（同上）
-- =====================================================================
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (2026101900000000001, 1, 2939, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000002, 1, 29391, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000003, 1, 29392, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000004, 1, 29393, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000005, 10, 2939, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000006, 10, 29391, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000007, 10, 29392, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000008, 10, 29393, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000009, 26, 2939, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000010, 26, 29391, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000011, 26, 29392, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000012, 26, 29393, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000013, 27, 2939, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000014, 27, 29391, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000015, 27, 29392, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101900000000016, 27, 29393, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- =====================================================================
-- 四、回查
-- =====================================================================
-- 4.1 新菜单与按钮码必须存在且 menu_key 唯一
-- SELECT id, menu_name, menu_key, permission FROM sys_menu WHERE id IN (2939, 29391, 29392, 29393);
-- 4.2 前端引用的 shift:dict:* 必须 100% 能在 menu_type=3 里找到
-- SELECT permission FROM sys_menu WHERE menu_type = 3 AND del_flag = 0 AND permission LIKE 'shift:dict%';
-- 4.3 授权角色应同时持有菜单与三个按钮
-- SELECT rm.role_id, COUNT(*) FROM sys_role_menu rm
--   WHERE rm.menu_id IN (2939, 29391, 29392, 29393) GROUP BY rm.role_id ORDER BY rm.role_id;
-- 4.4 后端 ShiftController 权限串与菜单码逐字一致（list→shift:dict:list, add→shift:dict:add,
--     edit→shift:dict:edit, delete→shift:dict:delete）；/shift/selectList 仍只要求登录，供全场景下拉。
