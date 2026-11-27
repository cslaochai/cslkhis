-- =============================================================================
-- 410 体检套餐定价治理与体检菜单权限重构
-- 背景：
--   1) 套餐只有 price 一列，没有“原价 / 折扣率 / 价格底线”，谁填多少就是多少，
--      无法回答“这个套餐打了多少折”“最低能打到几折”。
--   2) checkup:manage:add/edit/delete 三个码同时覆盖“套餐维护 + 体检登记 + 结果录入”，
--      前台角色因此拿到了套餐建价权限；套餐菜单（2930）下没有任何按钮节点，
--      套餐页借用了登记菜单（905）的按钮码，权限树里看不出“套餐的新增”这个开关。
-- 本次：
--   A. 套餐表加 list_price（原价快照）/ discount_rate（折扣率）/ floor_rate（最低折扣率）
--   B. 菜单改名与排序（套餐在前、登记在后），权限码按“套餐/登记/结果”三类拆开
--   C. 角色授权重排：套餐建价归物价口，前台只做登记与录结果，系统管理员仍只读
-- 幂等：可重复执行；ALTER 动态判重，菜单/授权 INSERT 均带 NOT EXISTS 守卫。
-- 回滚见 docs/03-增量SQL变更/410-回滚.sql
-- =============================================================================

-- ----------------------------------------------------------------------------
-- A. 套餐定价三列（动态判重，重复执行不报错）
-- ----------------------------------------------------------------------------
SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_checkup_package ADD COLUMN `list_price` decimal(10,2) NOT NULL DEFAULT ''0.00'' COMMENT ''原价（明细项目价格合计快照）'' AFTER `price`',
    'DO 0')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_checkup_package' AND COLUMN_NAME = 'list_price');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_checkup_package ADD COLUMN `discount_rate` decimal(5,4) NOT NULL DEFAULT ''1.0000'' COMMENT ''套餐折扣率（套餐价/原价，1.0000-不打折）'' AFTER `list_price`',
    'DO 0')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_checkup_package' AND COLUMN_NAME = 'discount_rate');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_checkup_package ADD COLUMN `floor_rate` decimal(5,4) NOT NULL DEFAULT ''1.0000'' COMMENT ''最低折扣率（价格底线，套餐价不得低于原价×本值）'' AFTER `discount_rate`',
    'DO 0')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_checkup_package' AND COLUMN_NAME = 'floor_rate');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 回填：原价 = 明细合计；折扣率 = 套餐价/原价；底线取当前实际折扣（保证存量套餐可继续保存、不因新校验被拦）
UPDATE sys_checkup_package p
JOIN (SELECT package_id, SUM(amount) AS total FROM sys_checkup_package_item WHERE del_flag = 0 GROUP BY package_id) s
  ON s.package_id = p.id
SET p.list_price     = s.total,
    p.discount_rate  = CASE WHEN s.total > 0 THEN ROUND(p.price / s.total, 4) ELSE 1.0000 END,
    p.floor_rate     = CASE WHEN s.total > 0 THEN LEAST(1.0000, ROUND(p.price / s.total, 4)) ELSE 1.0000 END
WHERE p.del_flag = 0;

-- ----------------------------------------------------------------------------
-- B. 菜单重构：体检套餐在前、体检登记在后；按钮码按职责拆开
-- ----------------------------------------------------------------------------
-- 2930 体检套餐：列表码独立，排序提到登记前面
UPDATE sys_menu
SET menu_name = '体检套餐', permission = 'checkup:package:list', sort_order = 1,
    update_by = 'admin', update_time = NOW()
WHERE id = 2930;

-- 905 体检管理 → 体检登记：名字对齐实际内容（它只管登记、结果、总检）
UPDATE sys_menu
SET menu_name = '体检登记', permission = 'checkup:record:list', sort_order = 2,
    update_by = 'admin', update_time = NOW()
WHERE id = 905;

-- 2131/2132/2133 由“体检管理-新增/修改/删除”改为登记侧三个动作（id 保留，授权关系自然迁移）
UPDATE sys_menu
SET menu_name = '体检登记-登记', permission = 'checkup:record:add',
    update_by = 'admin', update_time = NOW()
WHERE id = 2131;

UPDATE sys_menu
SET menu_name = '体检登记-开始体检/总检', permission = 'checkup:record:edit',
    update_by = 'admin', update_time = NOW()
WHERE id = 2132;

UPDATE sys_menu
SET menu_name = '体检登记-删除', permission = 'checkup:record:delete',
    update_by = 'admin', update_time = NOW()
WHERE id = 2133;

-- 新增三个按钮节点（id 取本文件专属段 9210000000000011001~003；三道键判重）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 9210000000000011001, '体检登记-结果录入', 905, 4, 3, NULL, NULL, 'patient.checkup:result', NULL,
       'checkup:result:save', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, NULL, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9210000000000011001
                   OR menu_key = 'patient.checkup:result' OR permission = 'checkup:result:save');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 9210000000000011002, '体检套餐-新增/修改', 2930, 1, 3, NULL, NULL, 'checkup.package:add', NULL,
       'checkup:package:add', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, NULL, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9210000000000011002
                   OR menu_key = 'checkup.package:add' OR permission = 'checkup:package:add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 9210000000000011003, '体检套餐-停用', 2930, 2, 3, NULL, NULL, 'checkup.package:edit', NULL,
       'checkup:package:edit', 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, NULL, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9210000000000011003
                   OR menu_key = 'checkup.package:edit' OR permission = 'checkup:package:edit');

-- ----------------------------------------------------------------------------
-- C. 授权重排（先收口再按方案重发，避免旧码残留）
--    role 1  = 10012 系统管理员     只读：只看，不带任何按钮码
--    role 25 = 10035 门诊办管理员   管理兜底：登记 + 套餐全套
--    role 40 = 10050 物价员         套餐建价：只拿套餐两个按钮（行业口径价格归物价口）
--    role 43 = 10053 体检中心前台   只做登记与录结果：不拿任何 package:* 码
-- ----------------------------------------------------------------------------
DELETE rm FROM sys_role_menu rm
JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id IN (1, 25, 40, 43)
  AND m.id IN (1400, 905, 2930, 2131, 2132, 2133,
               9210000000000011001, 9210000000000011002, 9210000000000011003);

-- 菜单节点：只读角色 1 与 everybody 都要能进两个页面
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT v.role_id, v.menu_id FROM (
  SELECT 1 AS role_id, 1400 AS menu_id UNION ALL SELECT 1, 2930 UNION ALL SELECT 1, 905
  UNION ALL SELECT 25, 1400 UNION ALL SELECT 25, 2930 UNION ALL SELECT 25, 905
  UNION ALL SELECT 43, 1400 UNION ALL SELECT 43, 2930 UNION ALL SELECT 43, 905
  UNION ALL SELECT 40, 1400 UNION ALL SELECT 40, 2930
) v
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = v.role_id AND x.menu_id = v.menu_id);

-- 按钮节点
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT v.role_id, v.menu_id FROM (
  -- 门诊办管理员：全量
  SELECT 25 AS role_id, 2131 AS menu_id UNION ALL SELECT 25, 2132 UNION ALL SELECT 25, 2133
  UNION ALL SELECT 25, 9210000000000011001 UNION ALL SELECT 25, 9210000000000011002 UNION ALL SELECT 25, 9210000000000011003
  -- 门诊办管理员之外的第二套：体检中心前台（登记 / 开始体检与总检 / 结果录入）
  UNION ALL SELECT 43, 2131 UNION ALL SELECT 43, 2132 UNION ALL SELECT 43, 9210000000000011001
  -- 物价员：套餐新增修改 / 套餐停用
  UNION ALL SELECT 40, 9210000000000011002 UNION ALL SELECT 40, 9210000000000011003
) v
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = v.role_id AND x.menu_id = v.menu_id);

-- ----------------------------------------------------------------------------
-- 校验（执行后手工回查，应有对应结果）
-- ----------------------------------------------------------------------------
-- 1) 套餐三列存在且有值：select id, package_name, list_price, price, discount_rate, floor_rate from sys_checkup_package where del_flag=0;
-- 2) 权限码已拆开：select id, menu_name, permission from sys_menu where permission like 'checkup:%' order by id;
--    预期 905=checkup:record:list、2930=checkup:package:list、2131/2132/2133=record:add/edit/delete、
--    11001=result:save、11002/11003=package:add/edit；不再出现 checkup:manage:*
-- 3) 前台不再持有套餐码：select 1 from sys_role_menu rm join sys_menu m on m.id=rm.menu_id
--    where rm.role_id=43 and m.permission like 'checkup:package:%';  -- 必须 0 行
-- 4) 系统管理员仍只读：select count(*) from sys_role_menu rm join sys_menu m on m.id=rm.menu_id
--    where rm.role_id=1 and m.menu_type=3;  -- 必须 0
