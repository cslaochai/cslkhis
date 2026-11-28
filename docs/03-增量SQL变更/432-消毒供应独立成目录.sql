-- 432 消毒供应从「物资设备」拆出独立子目录
-- 背景：903/904/2932 三个页面是供应室业务（器械包追溯、器械包模板、医废交接），
--       与设备科的资产台账混在同一层，设备科点进物资设备会看到一堆和自己无关的菜单。
-- 做法：新增二級目录「消毒供应」（menu_type=1），把三页挂到它下面，页面 path/component/permission 一律不动。
-- 授权：仅 设备与供应室管理员(39) 与 系统管理员(1，全站可见只读)。
-- ⚠ 父目录必须一并进 sys_role_menu：userMenus 只返回角色直接授权的行，
--    SysMenuServiceImpl.buildMenuTree 按 parentId 挂树，父节点不在结果集里三个子页面会变成孤儿从侧栏消失。

-- 1. 目录节点（id 段 892061432 为本文件专属）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, update_by, create_by_id, update_by_id)
SELECT 8920614320000000001, '消毒供应', 900, 5, 1, '/cssd-center', NULL, 'asset.cssdCenter', 'Box',
       NULL, 0, 0, 1, 1, 'sql432', 'sql432', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 8920614320000000001
                   OR menu_key = 'asset.cssdCenter'
                   OR (parent_id = 900 AND menu_name = '消毒供应' AND del_flag = 0));

-- 2. 三页改挂新目录并按业务顺序排：追溯 → 模板 → 医废
UPDATE sys_menu SET parent_id = 8920614320000000001, sort_order = 1, menu_name = '器械包追溯',
                    update_by = 'sql432', update_time = NOW()
WHERE id = 903 AND del_flag = 0;

UPDATE sys_menu SET parent_id = 8920614320000000001, sort_order = 2,
                    update_by = 'sql432', update_time = NOW()
WHERE id = 2932 AND del_flag = 0;

UPDATE sys_menu SET parent_id = 8920614320000000001, sort_order = 3, menu_name = '医废交接',
                    update_by = 'sql432', update_time = NOW()
WHERE id = 904 AND del_flag = 0;

-- 3. 目录授权（与三个子页面现有授权对齐：1 管理员 + 39 设备与供应室管理员）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614320000000001, 1, 8920614320000000001, 'sql432', 'sql432' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 8920614320000000001);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by)
SELECT 8920614320000000002, 39, 8920614320000000001, 'sql432', 'sql432' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 39 AND menu_id = 8920614320000000001);

-- 4. 回查
SELECT id, parent_id, menu_name, menu_type, path, sort_order, permission FROM sys_menu
WHERE id IN (8920614320000000001, 903, 904, 2932) ORDER BY sort_order;
SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = 8920614320000000001 ORDER BY role_id;
