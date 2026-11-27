-- ============================================================
-- 405 医务科/门诊办角色命名归位 + role26 越界授权收口
-- 日期：2026-11-27
-- 依据：106 基线 P15b（医务管理岗 4–8 页）；107 §5.3 最小必要、按岗位分权。
-- 背景：role 是岗位职能身份，不应直接用科室名；顶栏展示「科室·角色」，
--       原 role_name 与科室同名导致「医务科·医务科」重复。role_code 为前后端契约，保持不动。
-- 备份：backup/sys_role_backup_20261127_114452.csv、sys_role_menu_backup_20261127_114452.csv
-- 回滚：见 405-回滚.sql；执行后清缓存 his:perm:role:*、his:user:identity:*
-- ============================================================

-- ① 角色展示名归位（role_code 不变；科室 sys_department 名不变）
UPDATE sys_role SET role_name = '门诊办管理员', update_time = NOW() WHERE id = 25 AND role_code = '10035';
UPDATE sys_role SET role_name = '医务科管理员', update_time = NOW() WHERE id = 26 AND role_code = '10036';

-- ② 摘 role26（医务科管理员）错挂到临床/字典/公卫/急诊岗的叶子
--    307 会诊管理：院内会诊申请/应答工作站，归医生 2 / 科主任 27
--    2387 医嘱组套（套餐）维护：临床常用医嘱模板，归医生 2
--    2383 医嘱基础字典：给药途径/频次/剂量单位，字典维护归信息科 33（见 ③ 回补）
--    613 公卫上报 / 618 公卫上报-其他上报：归公卫医师 22
--    29605 急诊值班：急诊岗视图，归急诊医生 8 / 分诊 9
DELETE FROM sys_role_menu WHERE role_id = 26 AND menu_id IN
  (307, 2387, 2383, 613, 618, 29605);

-- ③ 摘除后变空的祖先目录一并摘除（避免断链空目录挂在侧栏）
--    29812 在院协作：子树仅余 307；1100 系统管理：子树仅余 2383；29701 院感与公卫：子树仅余 613/618
DELETE FROM sys_role_menu WHERE role_id = 26 AND menu_id IN (29812, 1100, 29701);

-- ④ 回补 2383 医嘱基础字典 → role33 信息科管理员（防孤儿；增/改/删按钮 2384/2385/2386 本轮不回补）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001831, 33, 2383, 'menu-govern-405', NOW(), 'menu-govern-405', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 灰区保留（待业务拍板，本轮不动）：
--   312 双向转诊（归口门诊办 25 / 公卫 22）、315 远程会诊（临床申请/接诊，医务科仅备案审批）、
--   608 危急值管理（只读监督，无写按钮）。

-- 回查：摘除残留 0；2383 持有=[33]；role26 授权 47 条；全库可见孤儿页 0；role26 名下无空目录。
