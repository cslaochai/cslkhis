-- =============================================================================
-- 410 回滚（体检套餐定价治理与体检菜单权限重构）
-- 用途：把菜单/权限码恢复成 410 之前的形态，并去掉套餐定价三列。
-- ⚠ 回滚会丢定价数据（list_price / discount_rate / floor_rate 全列删除），仅演练/放弃该改造时用。
-- ⚠ 执行后必须清权限缓存：his:perm:role:10012 / 10035 / 10050 / 10053 与 his:user:identity:*
-- =============================================================================

-- 1) 撤销新增按钮节点（先删授权再删菜单行）
DELETE rm FROM sys_role_menu rm
WHERE rm.menu_id IN (9210000000000011001, 9210000000000011002, 9210000000000011003);

DELETE FROM sys_menu WHERE id IN (9210000000000011001, 9210000000000011002, 9210000000000011003);

-- 2) 菜单名与权限码回退到 410 之前
UPDATE sys_menu SET menu_name = '体检管理', permission = 'checkup:manage:list', sort_order = 1 WHERE id = 905;
UPDATE sys_menu SET menu_name = '体检套餐', permission = 'checkup:manage:list', sort_order = 40 WHERE id = 2930;
UPDATE sys_menu SET menu_name = '体检管理-新增', permission = 'checkup:manage:add' WHERE id = 2131;
UPDATE sys_menu SET menu_name = '体检管理-修改', permission = 'checkup:manage:edit' WHERE id = 2132;
UPDATE sys_menu SET menu_name = '体检管理-删除', permission = 'checkup:manage:delete' WHERE id = 2133;

-- 3) 撤销物价员(40)的体检授权（410 之前物价员没有体检菜单）
DELETE rm FROM sys_role_menu rm
JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 40 AND m.id IN (1400, 2930, 9210000000000011001, 9210000000000011002, 9210000000000011003);

-- 4) 前台(43)撤销结果录入码（410 之前前台没有 result:save）
DELETE rm FROM sys_role_menu rm WHERE rm.role_id = 43 AND rm.menu_id = 9210000000000011001;

-- 5) 删除定价三列（数据不可恢复）
SET @ddl = (SELECT IF(COUNT(*) > 0,
    'ALTER TABLE sys_checkup_package DROP COLUMN `floor_rate`', 'DO 0')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_checkup_package' AND COLUMN_NAME = 'floor_rate');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) > 0,
    'ALTER TABLE sys_checkup_package DROP COLUMN `discount_rate`', 'DO 0')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_checkup_package' AND COLUMN_NAME = 'discount_rate');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) > 0,
    'ALTER TABLE sys_checkup_package DROP COLUMN `list_price`', 'DO 0')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_checkup_package' AND COLUMN_NAME = 'list_price');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 注意：后端/前端代码里的新权限码（checkup:package:* / record:* / result:save）与三列字段
-- 必须同时回退（git revert 对应提交），只回滚 SQL 会让页面 403、保存报未知列。
