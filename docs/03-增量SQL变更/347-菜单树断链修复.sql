-- 347 菜单树断链修复（补祖先目录 + 操作按钮上提）
-- 日期：2026-11-20
-- 背景：343 把隐藏页降为按钮后，其下的操作按钮变成「按钮套按钮」；
--       且多个角色被直接授了叶子页面而其上级目录没授，buildMenuTree 从 parent_id=0 组装，
--       挂不上的页面侧栏永远看不见（授权页 ≠ 可见页）。
-- 做法：操作按钮上提到宿主页；为每个角色补齐已授页面的全部祖先目录。

-- 操作按钮上提
UPDATE sys_menu SET parent_id=402 WHERE id=2046; -- 标本管理-修改 -> 402
UPDATE sys_menu SET parent_id=401 WHERE id=2055; -- 内镜工作站-新增 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2056; -- 内镜工作站-修改 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2057; -- 内镜工作站-删除 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2058; -- 超声工作站-新增 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2059; -- 超声工作站-修改 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2060; -- 超声工作站-删除 -> 401
UPDATE sys_menu SET parent_id=402 WHERE id=2061; -- 室内质控-新增 -> 402
UPDATE sys_menu SET parent_id=402 WHERE id=2062; -- 室内质控-修改 -> 402
UPDATE sys_menu SET parent_id=406 WHERE id=2063; -- 血库管理-新增 -> 406
UPDATE sys_menu SET parent_id=406 WHERE id=2064; -- 血库管理-修改 -> 406
UPDATE sys_menu SET parent_id=406 WHERE id=2065; -- 血库管理-删除 -> 406
UPDATE sys_menu SET parent_id=602 WHERE id=2085; -- 病历审核-修改 -> 602
UPDATE sys_menu SET parent_id=609 WHERE id=2095; -- 病案借阅复印-新增 -> 609
UPDATE sys_menu SET parent_id=609 WHERE id=2096; -- 病案借阅复印-修改 -> 609
UPDATE sys_menu SET parent_id=609 WHERE id=2097; -- 病案借阅复印-删除 -> 609
UPDATE sys_menu SET parent_id=609 WHERE id=2098; -- 编码任务池-新增 -> 609
UPDATE sys_menu SET parent_id=609 WHERE id=2099; -- 编码任务池-修改 -> 609
UPDATE sys_menu SET parent_id=610 WHERE id=2171; -- 医疗纠纷-登记与修改 -> 610
UPDATE sys_menu SET parent_id=610 WHERE id=2172; -- 医疗纠纷-受理/调查/处理/结案/撤销 -> 610
UPDATE sys_menu SET parent_id=607 WHERE id=2184; -- 单病种质控-目录维护 -> 607
UPDATE sys_menu SET parent_id=607 WHERE id=2185; -- 单病种质控-纳入/质控/上报 -> 607
UPDATE sys_menu SET parent_id=401 WHERE id=2373; -- 放射报告-书写/提交 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2374; -- 放射报告-审核/退回 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2375; -- 放射报告-发布 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2376; -- 放射报告-模板维护 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2377; -- 胶片-登记/修改 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2378; -- 胶片-记账 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2379; -- 胶片-打印/发放 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2393; -- 胶片-作废 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2395; -- 心电-签到/波形采集 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2396; -- 心电报告-书写/提交 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2397; -- 心电报告-审核/退回 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2398; -- 心电报告-发布 -> 401
UPDATE sys_menu SET parent_id=401 WHERE id=2399; -- 心电-模板维护 -> 401
UPDATE sys_menu SET parent_id=613 WHERE id=2640; -- 公卫上报-提交 -> 613
UPDATE sys_menu SET parent_id=613 WHERE id=2641; -- 公卫上报-审核 -> 613
UPDATE sys_menu SET parent_id=402 WHERE id=2901; -- 室间质评-新增 -> 402
UPDATE sys_menu SET parent_id=402 WHERE id=2902; -- 室间质评-修改 -> 402
UPDATE sys_menu SET parent_id=402 WHERE id=2903; -- 室间质评-删除 -> 402

-- 祖先目录补授
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
 (9210000000000000720, 2, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000721, 5, 300, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000722, 6, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000723, 8, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000724, 10, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000725, 11, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000726, 12, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000727, 13, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000728, 13, 29805, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000729, 15, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000730, 16, 1200, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000731, 20, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000732, 23, 700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000733, 25, 700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000734, 25, 800, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000735, 25, 1400, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000736, 26, 500, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000737, 26, 700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000738, 26, 800, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000739, 26, 1100, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000740, 26, 29804, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000741, 26, 29810, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000742, 31, 29700, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000743, 36, 800, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0),
 (9210000000000000744, 37, 29801, 'menu-govern-347', NOW(), 'menu-govern-347', NOW(), 0, 0);

-- 回滚
-- UPDATE sys_menu SET parent_id=403 WHERE id=2046;
-- UPDATE sys_menu SET parent_id=408 WHERE id=2055;
-- UPDATE sys_menu SET parent_id=408 WHERE id=2056;
-- UPDATE sys_menu SET parent_id=408 WHERE id=2057;
-- UPDATE sys_menu SET parent_id=409 WHERE id=2058;
-- UPDATE sys_menu SET parent_id=409 WHERE id=2059;
-- UPDATE sys_menu SET parent_id=409 WHERE id=2060;
-- UPDATE sys_menu SET parent_id=410 WHERE id=2061;
-- UPDATE sys_menu SET parent_id=410 WHERE id=2062;
-- UPDATE sys_menu SET parent_id=411 WHERE id=2063;
-- UPDATE sys_menu SET parent_id=411 WHERE id=2064;
-- UPDATE sys_menu SET parent_id=411 WHERE id=2065;
-- UPDATE sys_menu SET parent_id=603 WHERE id=2085;
-- UPDATE sys_menu SET parent_id=611 WHERE id=2095;
-- UPDATE sys_menu SET parent_id=611 WHERE id=2096;
-- UPDATE sys_menu SET parent_id=611 WHERE id=2097;
-- UPDATE sys_menu SET parent_id=612 WHERE id=2098;
-- UPDATE sys_menu SET parent_id=612 WHERE id=2099;
-- UPDATE sys_menu SET parent_id=615 WHERE id=2171;
-- UPDATE sys_menu SET parent_id=615 WHERE id=2172;
-- UPDATE sys_menu SET parent_id=2183 WHERE id=2184;
-- UPDATE sys_menu SET parent_id=2183 WHERE id=2185;
-- UPDATE sys_menu SET parent_id=414 WHERE id=2373;
-- UPDATE sys_menu SET parent_id=414 WHERE id=2374;
-- UPDATE sys_menu SET parent_id=414 WHERE id=2375;
-- UPDATE sys_menu SET parent_id=414 WHERE id=2376;
-- UPDATE sys_menu SET parent_id=415 WHERE id=2377;
-- UPDATE sys_menu SET parent_id=415 WHERE id=2378;
-- UPDATE sys_menu SET parent_id=415 WHERE id=2379;
-- UPDATE sys_menu SET parent_id=415 WHERE id=2393;
-- UPDATE sys_menu SET parent_id=417 WHERE id=2395;
-- UPDATE sys_menu SET parent_id=417 WHERE id=2396;
-- UPDATE sys_menu SET parent_id=417 WHERE id=2397;
-- UPDATE sys_menu SET parent_id=417 WHERE id=2398;
-- UPDATE sys_menu SET parent_id=417 WHERE id=2399;
-- UPDATE sys_menu SET parent_id=618 WHERE id=2640;
-- UPDATE sys_menu SET parent_id=618 WHERE id=2641;
-- UPDATE sys_menu SET parent_id=416 WHERE id=2901;
-- UPDATE sys_menu SET parent_id=416 WHERE id=2902;
-- UPDATE sys_menu SET parent_id=416 WHERE id=2903;
-- DELETE FROM sys_role_menu WHERE create_by='menu-govern-347';
