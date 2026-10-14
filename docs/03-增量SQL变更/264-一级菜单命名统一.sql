-- 一级菜单命名统一（2026-10-14）
-- ① 门诊业务 → 门（急）诊管理（id=200）
-- ② 住院业务 → 住院管理（id=300）
-- ③ 急诊中心 → 急诊工作站（id=205：单页工作台，与分诊/医生工作站词性统一；按钮权限名同步）
-- 对应铺底 docs/02-铺底基础数据/sys_menu.sql；每条 UPDATE 带旧名守卫，重复执行安全
UPDATE sys_menu
SET menu_name = '门（急）诊管理'
WHERE id = '200'
  AND menu_name = '门诊业务';

UPDATE sys_menu
SET menu_name = '住院管理'
WHERE id = '300'
  AND menu_name = '住院业务';

UPDATE sys_menu
SET menu_name = '急诊工作站'
WHERE id = '205'
  AND menu_name = '急诊中心';

UPDATE sys_menu
SET menu_name = '急诊工作站-新增'
WHERE id = '2010'
  AND menu_name = '急诊中心-新增';

UPDATE sys_menu
SET menu_name = '急诊工作站-修改'
WHERE id = '2011'
  AND menu_name = '急诊中心-修改';

UPDATE sys_menu
SET menu_name = '急诊工作站-交班清零'
WHERE id = '2601'
  AND menu_name = '急诊中心-交班清零';
