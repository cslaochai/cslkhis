-- ============================================================================
-- 382 - 诊疗科目映射归位：病案科室对应，改由病案统计口主责
--
-- 【为什么改】
-- sql300 建了 29626 页面 + 29627 保存 + 29628 删除，但 29627 后来被
-- 「急诊排班审核」复用（同 id），org:medSubject:add 失去宿主 —— 结果是
-- 任何角色（含超管）点保存都 403，页面只能看不能改。
--
-- 【厂商口径】
-- 科室 → 诊疗科目对照属于「科室字典/基础数据」，不是系统参数；病案首页、
-- 卫生统计直报、医保结算清单的科别口径由病案统计口（病案科/统计室）定义，
-- 信息科只负责系统承载与账号权限，不定义上报口径。
--
-- 【本次口径】
-- 页面留在「组织与资源 800」（科室主数据），改名「病案科室对应（诊疗科目）」；
-- 病案编码员(14) 主责（增改删）；统计上报员(41)、医保结算员(16) 只读；
-- 信息科(33) 保留只读，排障用。
--
-- 【回滚】
-- DELETE FROM sys_role_menu WHERE id BETWEEN 9210000000000001001 AND 9210000000000001009;
-- DELETE FROM sys_menu WHERE id = 29660;
-- UPDATE sys_menu SET parent_id = 800, menu_name = '诊疗科目映射' WHERE id = 29626;
-- ============================================================================

-- ---------------- 1. 菜单改名：从“诊疗科目映射”改成病案口径 ----------------
UPDATE sys_menu
SET menu_name   = '病案科室对应（诊疗科目）',
    remark      = '科室与国家卫健委诊疗科目对照，病案首页/卫统/医保上报的标准科别出口；病案统计口维护',
    update_by   = 'sql382',
    update_time = NOW()
WHERE id = 29626;

-- ---------------- 2. 补回“保存”按钮（原 29627 已被急诊排班审核占用） ----------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, update_by, del_flag, remark, create_by_id, update_by_id)
SELECT 29660, '病案科室对应-保存', 29626, 1, 3, NULL, NULL, 'org.medSubject.add', NULL,
       'org:medSubject:add', 0, 0, 1, 1,
       'sql382', 'sql382', 0, '新增/修改科目与科室映射', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'org.medSubject.add' AND del_flag = 0);

-- ---------------- 3. 授权：病案主责、统计/医保只读、信息科只读 ----------------
-- 3.1 超管补保存按钮（原 29627 丢失后一直没补）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001001, 1, 29660, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29660);

-- 3.1b 超管补删除按钮（sql300 建过，后续菜单治理丢了）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001010, 1, 29628, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29628);

-- 3.2 病案编码员(14)：目录 800 + 页面 + 保存 + 删除
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001002, 14, 800, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 14 AND menu_id = 800);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001003, 14, 29626, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 14 AND menu_id = 29626);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001004, 14, 29660, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 14 AND menu_id = 29660);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001005, 14, 29628, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 14 AND menu_id = 29628);

-- 3.3 统计上报员(41)：目录 800 + 页面（只读）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001006, 41, 800, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 41 AND menu_id = 800);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001007, 41, 29626, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 41 AND menu_id = 29626);

-- 3.4 医保结算员(16)：目录 800 + 页面（只读）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001008, 16, 800, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 16 AND menu_id = 800);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id)
SELECT 9210000000000001009, 16, 29626, 'sql382', 'sql382', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 16 AND menu_id = 29626);

-- ---------------- 4. 回查 ----------------
-- 菜单：29626 改名、29660 建立
SELECT m.id, m.menu_name, m.parent_id, m.menu_type, m.permission
FROM sys_menu m
WHERE m.id IN (29626, 29660) AND m.del_flag = 0
ORDER BY m.id;

-- 授权：14 应有 800/29626/29660/29628；41/16 只有 800/29626；33 只有 29626
SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.menu_type
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (800, 29626, 29628, 29660)
  AND rm.role_id IN (1, 14, 16, 33, 41)
ORDER BY rm.role_id, rm.menu_id;

-- 权限码宿主唯一性：org:medSubject:add 只允许 29660 一个
SELECT id, menu_name, permission FROM sys_menu
WHERE permission = 'org:medSubject:add' AND del_flag = 0;
