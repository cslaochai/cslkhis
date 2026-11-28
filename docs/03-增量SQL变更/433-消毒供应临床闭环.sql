-- 433 消毒供应闭环：临床发起回收 + 供应室确认 + 科室签收
-- 背景：器械包此前只能由供应室自己建（1-已回收），临床科室从头到尾不参与，
--       追溯链与手术室/病区的真实动作脱节。本脚本补上两个端点状态与对应字段。
-- 流程：科室(护士站)申请回收 → 0-待回收 → 供应室确认 1-已回收 → 清洗/打包/灭菌/发放 →
--       6-已发放 → 科室签收 7-已签收（追溯链闭合到临床）。

-- 1. 器械包补申请/签收字段
ALTER TABLE biz_cssd_pack
    ADD COLUMN apply_by VARCHAR(64) NULL COMMENT '申请人' AFTER dept_name,
    ADD COLUMN apply_time DATETIME NULL COMMENT '申请时间' AFTER apply_by,
    ADD COLUMN sign_by VARCHAR(64) NULL COMMENT '签收人' AFTER apply_time,
    ADD COLUMN sign_time DATETIME NULL COMMENT '签收时间' AFTER sign_by;

-- 2. 状态列注释补两个端点（Java 权威枚举 CssdNodeStatusEnum 同步加了 PENDING(0)/SIGNED(7)）
ALTER TABLE biz_cssd_pack
    MODIFY COLUMN status TINYINT NOT NULL COMMENT '包状态（0-待回收 1-已回收 2-清洗中 3-已打包 4-灭菌中 5-待发放 6-已发放 7-已签收）';

-- 3. 字典补 0/7（器械包状态、追溯节点类型各一行，key 名与 label 逐字对齐枚举）
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
                           create_by, update_by, create_by_id, update_by_id)
SELECT 8920614330000000001, 'his_cssd_pack_status', '待回收', '0', 0, 0, 1, 'sql433', 'sql433', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_cssd_pack_status' AND dict_value = '0');

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
                           create_by, update_by, create_by_id, update_by_id)
SELECT 8920614330000000002, 'his_cssd_pack_status', '已签收', '7', 7, 0, 1, 'sql433', 'sql433', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_cssd_pack_status' AND dict_value = '7');

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
                           create_by, update_by, create_by_id, update_by_id)
SELECT 8920614330000000003, 'his_cssd_node_type', '申请回收', '0', 0, 0, 1, 'sql433', 'sql433', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_cssd_node_type' AND dict_value = '0');

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status,
                           create_by, update_by, create_by_id, update_by_id)
SELECT 8920614330000000004, 'his_cssd_node_type', '科室签收', '7', 7, 0, 1, 'sql433', 'sql433', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_cssd_node_type' AND dict_value = '7');

-- 4. 临床侧按钮码：发起回收申请 / 签收（与供应室的 edit 分开，供应室不能替科室签收）
-- ⚠ id 用 9076：2131 已被「体检登记-登记」占用，复用会把体检按钮授给护士（已踩过一次）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, permission, is_frame, is_cache,
                      is_visible, status, create_by, update_by, create_by_id, update_by_id)
SELECT 9076, '消毒供应-临床申请签收', 903, 2, 3, 'asset:cssd:apply', 0, 0, 1, 1, 'sql433', 'sql433', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9076
                   OR permission = 'asset:cssd:apply');

-- 5. 授权
-- 供应室(39)：回收确认 / 流转 / 模板维护（cssd:edit），医废交接与处置（waste:edit/delete）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000031, 39, 2127, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 39 AND menu_id = 2127);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000032, 39, 2129, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 39 AND menu_id = 2129);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000033, 39, 2130, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 39 AND menu_id = 2130);

-- 临床：护士(3) / 护士长(10) / 手术室护士(32)
--   9076 cssd:apply（申请回收 + 签收）、2128 waste:add（科室医废登记）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000061, 3, 9076, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 3 AND menu_id = 9076);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000062, 10, 9076, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 10 AND menu_id = 9076);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000063, 32, 9076, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 32 AND menu_id = 9076);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000044, 3, 2128, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 3 AND menu_id = 2128);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000045, 10, 2128, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 10 AND menu_id = 2128);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614330000000046, 32, 2128, 'sql433', 'sql433' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 32 AND menu_id = 2128);

-- 6. 回查
SELECT id, pack_name, status, apply_by, sign_by FROM biz_cssd_pack ORDER BY id DESC LIMIT 3;
SELECT permission, menu_id, GROUP_CONCAT(role_id ORDER BY role_id) roles FROM sys_role_menu rm
  JOIN sys_menu m ON m.id = rm.menu_id
WHERE m.permission IN ('asset:cssd:edit', 'asset:cssd:apply', 'asset:waste:add', 'asset:waste:edit')
GROUP BY permission, menu_id;

