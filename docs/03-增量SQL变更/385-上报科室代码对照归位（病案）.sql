-- 385 上报科室代码对照：归位到病案、收口维护权
-- 日期：2026-11-26
-- 背景：
--   1) 该表是病案/上报口径的基础设置，不属于「组织与资源」的科室主数据，移到「病历与病案」；
--   2) 维护主体是病案编码员（病案室），统计上报员、医保结算员只读；信息科是平台运维，不参与业务口径；
--   3) 超级管理员保留查看，不再保留保存/删除按钮——口径变更必须由业务岗操作。
-- 说明：页面路径不变（/system/medSubject），只改归属与授权，不动前后端契约。

-- 1. 移动到「病历与病案」(600)，排在病案管理之后
UPDATE sys_menu SET parent_id = 600, sort_order = 50 WHERE id = 29626;

-- 2. 按钮名与页面口径对齐
UPDATE sys_menu SET menu_name = '上报科室代码对照-保存' WHERE id = 29660;
UPDATE sys_menu SET menu_name = '上报科室代码对照-删除' WHERE id = 29628;

-- 3. 统计上报员(41)、医保结算员(16) 补「病历与病案」目录，才能看到移过去的页面
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001101, 41, 600, 'sql385', NOW(), 'sql385', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 41 AND menu_id = 600);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001102, 16, 600, 'sql385', NOW(), 'sql385', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 16 AND menu_id = 600);

-- 4. 组织与资源在这三个角色下已无其它页面，去掉空目录
DELETE FROM sys_role_menu WHERE role_id IN (14, 16, 41) AND menu_id = 800;

-- 5. 信息科管理员不再看这张业务口径表
DELETE FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29626;

-- 6. 超管只读：撤掉保存/删除按钮
DELETE FROM sys_role_menu WHERE role_id = 1 AND menu_id IN (29660, 29628);

-- 回滚（按序执行）：
-- UPDATE sys_menu SET parent_id=800, sort_order=40 WHERE id=29626;
-- UPDATE sys_menu SET menu_name='病案科室对应-保存' WHERE id=29660;
-- UPDATE sys_menu SET menu_name='诊疗科目映射-删除' WHERE id=29628;
-- DELETE FROM sys_role_menu WHERE create_by='sql385';
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- VALUES (9210000000000001006, 41, 800, 'sql382', NOW(), 'sql382', NOW(), 0, 0),
--        (9210000000000001008, 16, 800, 'sql382', NOW(), 'sql382', NOW(), 0, 0),
--        (9210000000000001010, 1, 29628, 'sql382', NOW(), 'sql382', NOW(), 0, 0),
--        (9210000000000001001, 1, 29660, 'sql382', NOW(), 'sql382', NOW(), 0, 0);
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- VALUES (9210000000000001103, 33, 29626, 'sql385', NOW(), 'sql385', NOW(), 0, 0);

-- 回查：
-- SELECT id,parent_id,menu_name FROM sys_menu WHERE id IN (29626,29628,29660);
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id IN (29626,29628,29660,600,800) AND role_id IN (1,14,16,33,41) ORDER BY role_id, menu_id;
