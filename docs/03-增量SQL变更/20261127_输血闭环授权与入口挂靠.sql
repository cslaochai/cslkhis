-- ============================================================================
-- 20261127 输血闭环：申请入口挂医生站 + 授权按岗位拆分
-- ============================================================================
-- 背景：输血管理（406）原来整块挂在「医技医辅」下，权限只授 医技人员(10017)/
--   检验技师(10022)。《临床用血技术规范（2025 年版）》第十二条要求申请单由医师
--   填写、上级医师审核，第三十三条要求两名医护人员床旁核对后输注 —— 医师和护士
--   在系统里既没有入口也没有权限，等于把医师的法定动作交给了输血科代劳。
--
-- 改动三件事：
--   1. 权限按动作拆开：审批 audit、配血发血 dispatch、输注执行 execute，
--      不再由 edit 一把梭（edit 只剩「修改申请单」）。
--      对应后端 @PreAuthorize 已同步改：approve=audit、crossmatch/issue=dispatch、
--      startInfusion/finish/reportReaction=execute。**必须重启 HIS 才生效**。
--   2. 入口挂靠：住院医生站(302) 加「输血申请」页、护士工作站(303) 加「输血执行」页，
--      复用输血中心页（页签按权限自动显隐，医生看不到血库页签）。
--   3. 授权：医生 申请+审批(上级医师级)；科主任/医务科 审批；护士 执行；
--      输血科 配血发血+血库全量；医技保留原有（新增 dispatch/execute，避免既有流程断）。
--
-- 幂等：菜单按 id 删再插；授权按 id 删再插。
-- ============================================================================

-- ---------- 1. 菜单 ----------
DELETE FROM sys_menu WHERE id IN (8910000000000001, 8910000000000002,
                                  8910000000000003, 8910000000000004, 8910000000000005);

-- 医生站入口
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component,
                      menu_key, icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
VALUES
(8910000000000001, '输血申请', 302, 40, 2, '/inpatient-transfusion',
 'module-center/TransfusionCenterView', 'inpatient.transfusion', NULL,
 'medtech:transfusion:list', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0,
 '医生站输血入口：开申请、看审批进度', 1, 1);

-- 护士站入口
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component,
                      menu_key, icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
VALUES
(8910000000000002, '输血执行', 303, 40, 2, '/nurse-transfusion',
 'module-center/TransfusionCenterView', 'nurse.transfusion', NULL,
 'medtech:transfusion:list', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0,
 '护士站输血入口：双人核对、开始/结束输注、上报反应', 1, 1);

-- 按钮级权限（挂在输血管理 406 下，跟既有按钮同一层）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component,
                      menu_key, icon, permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
VALUES
(8910000000000003, '输血管理-审批', 406, 20, 3, NULL, NULL, 'medtech.transfusion:audit',
 NULL, 'medtech:transfusion:audit', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0,
 '按备血量分级的审批动作，只给医师/科主任/医务科', 1, 1),
(8910000000000004, '输血管理-配血发血', 406, 21, 3, NULL, NULL, 'medtech.transfusion:dispatch',
 NULL, 'medtech:transfusion:dispatch', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0,
 '交叉配血与发血，只给输血科', 1, 1),
(8910000000000005, '输血管理-输注执行', 406, 22, 3, NULL, NULL, 'medtech.transfusion:execute',
 NULL, 'medtech:transfusion:execute', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0,
 '床旁双人核对、开始/结束输注、反应上报，只给护士', 1, 1);

-- ---------- 2. 授权 ----------
DELETE FROM sys_role_menu WHERE id BETWEEN 8910000000000011 AND 8910000000000059;

-- 医生(2)：看单 + 开单 + 审批（上级医师级）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8910000000000011, 2, 406, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000012, 2, 2049, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000013, 2, 8910000000000001, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000014, 2, 8910000000000003, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 科主任(27)：看单 + 审批
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8910000000000021, 27, 406, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000022, 27, 8910000000000003, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 医务科管理员(26)：看单 + 审批（≥800ml 那档）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8910000000000031, 26, 406, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000032, 26, 8910000000000003, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 护士(3)：看单 + 执行
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8910000000000041, 3, 406, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000042, 3, 8910000000000002, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000043, 3, 8910000000000005, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 输血科(11,10022)：配血发血 + 血库全量（改动前它连血库入库都没有）INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8910000000000051, 11, 8910000000000004, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000052, 11, 411, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000053, 11, 2063, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000054, 11, 2064, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000055, 11, 2065, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 医技人员(6,10017)：既有工作站保留，补 dispatch/execute 两个新码，否则原有流程立刻 403
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
(8910000000000061, 6, 8910000000000004, 'admin', NOW(), 'admin', NOW(), 1, 1),
(8910000000000062, 6, 8910000000000005, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 3. 数据范围 ----------
-- 输血科要处理全院各科室的用血单，当前 10022 是「本部门」范围，
-- 审核/配血/发血一律报「该数据所属科室不在当前岗位的数据范围内」。
-- 这里放到全院。副作用：复用 10022 的医学检验科账号也一起放大，
-- 根治办法是拆出独立的「输血科技师」角色，本次先按最小改动处理。
UPDATE sys_role SET data_scope = 1, update_by = 'admin', update_time = NOW()
 WHERE role_code = '10022';

-- ---------- 回查 ------------ SELECT r.role_code, r.role_name, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE m.permission LIKE 'medtech:transfusion%' ORDER BY m.permission, r.role_code;
-- 预期：add → 医生/检验/医技；audit → 医生/科主任/医务科；dispatch/execute → 输血科/护士/医技
