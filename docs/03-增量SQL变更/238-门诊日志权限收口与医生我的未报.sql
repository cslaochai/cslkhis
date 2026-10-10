-- 门诊日志（菜单 207，法规台账）权限收口 B+C 方案
-- 生成时间 2026-10-10；作用：
--   B：台账是行政/感控核查页（全院口径、跨科室回溯），收回医生(2)/急诊医生(8)的 207，
--      补授给传染病上报责任人——公卫医师(22)与病案质控员(15)；
--   C：医生自查"应报未报"改走新页「我的未报」（菜单 2950，权限码 opd:outpatientLog:mine），
--      后端强制 doctorId=登录员工、deptId 清空、可报+未报固定，客户端伪造的筛选一律覆盖。
-- 分诊护士(9)保留 207：预检分诊与发热监测直接相关，属台账的合理使用方。
-- 前置：sys_role_menu 删除即物理删（uk_role_menu 不含 del_flag），无需软删
-- 执行后必须清 Redis：DEL his:perm:role:{1..24}（否则 10 分钟内仍是旧权限集合）

-- ===== B1：收回（医生 2 / 急诊医生 8；207 无按钮子授权，只删页面本身）=====
DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id=207;
DELETE FROM sys_role_menu WHERE role_id=8 AND menu_id=207;

-- ===== B2：补授（公卫医师 22 已有 100/200 只补 207；病案质控员 15 缺 200 连父菜单一起给）=====
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610100000000101, 22, 207, 'menu-shoukou-20261010', 1, NOW(), 'menu-shoukou-20261010', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610100000000102, 15, 200, 'menu-shoukou-20261010', 1, NOW(), 'menu-shoukou-20261010', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610100000000103, 15, 207, 'menu-shoukou-20261010', 1, NOW(), 'menu-shoukou-20261010', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

-- ===== C：新菜单 2950「我的未报」（医生自查入口，紧随门诊日志 sort 22）=====
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2950', '我的未报', '200', 22, 2, '/my-unreported', 'outpatient-log/MyUnreportedView', 'opd.myUnreported',
        'Bell', 'opd:outpatientLog:mine', 0, 0, 1, 1, 'admin', 'admin', 0,
        '医生自查本人应报未报（后端强制本人，非全院台账）', '1', '1');

-- 授权：医生(2)、急诊医生(8)（其登录态为员工，employeeId 与 biz_medical_record.doctor_id 对齐）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610100000000104, 2, 2950, 'menu-shoukou-20261010', 1, NOW(), 'menu-shoukou-20261010', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610100000000105, 8, 2950, 'menu-shoukou-20261010', 1, NOW(), 'menu-shoukou-20261010', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
