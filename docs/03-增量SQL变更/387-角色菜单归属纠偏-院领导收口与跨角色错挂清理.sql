-- ============================================================
-- 387 角色菜单归属纠偏：院领导只读收口 + 跨角色错挂清理
-- 日期：2026-11-26
-- 依据：docs/00-开发文档/107-角色菜单归属梳理与纠偏清单.md（A 级必修项）
-- 备份：执行前导出 sys_role_menu 全量（1481 行）至工作区 CSV。
-- 口径：授权表无 del_flag，删除走物理删；迁移按钮先摘原角色、再回补目标角色。
-- 说明：B 级复核项（患者建档/档案写按钮、医保窗口收费、临床药师患者域等）本轮不动。
-- ============================================================

-- ---------- 1. role18 院领导：摘越界叶子 + 空目录 + 质控按钮 ----------
DELETE FROM sys_role_menu
WHERE role_id = 18 AND menu_id IN (
  330, 332, 333, 334,                       -- 护理管理 / VTE / 护理质控
  29811, 428, 2666, 2667,                   -- 营养膳食 / 营养指标监测 + 按钮
  500, 29804, 518,                          -- 药事管理 / 抗菌药物管理 / 抗菌监测
  700, 703, 705, 2111, 2112, 2113,          -- 患者中心 / 360 / 健康档案 + 按钮
  1400, 2930,                               -- 健康管理(体检) / 体检套餐
  2945, 2938,                               -- 排班值守中心 / 排班总览
  605,                                      -- 全院数据质量监测按钮
  2131, 2132, 2133                          -- 体检新增/修改/删除按钮（迁 25 门诊办）
);

-- role18 病案统计上报只读收口：摘写操作按钮（页面 2159 保留）
DELETE FROM sys_role_menu
WHERE role_id = 18 AND menu_id IN (2160, 2161, 2162);

-- ---------- 2. 护士3 / 护士长10：摘死亡证明全套按钮 ----------
DELETE FROM sys_role_menu
WHERE role_id IN (3, 10) AND menu_id IN (2614, 2615, 2616, 2617, 2618, 2619);

-- ---------- 3. 医生2 / 手术室护士32：摘麻醉工作站按钮（仅留麻醉医师31） ----------
DELETE FROM sys_role_menu WHERE role_id = 2  AND menu_id IN (2033, 2034);
DELETE FROM sys_role_menu WHERE role_id = 32 AND menu_id IN (2033, 2034, 2928);

-- ---------- 4. 导诊7：摘残留收费结算窗口页 ----------
DELETE FROM sys_role_menu WHERE role_id = 7 AND menu_id = 1001;

-- ---------- 5. 护理部29：摘全院总值班写按钮（归 36/26） ----------
DELETE FROM sys_role_menu WHERE role_id = 29 AND menu_id IN (2668, 2669, 2670);

-- ---------- 6. 信息科33：摘证书吊销按钮（309 口径，仅授医务科26） ----------
DELETE FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29625;

-- ---------- 7. 医保16：摘价格修改按钮（归物价员40） ----------
DELETE FROM sys_role_menu WHERE role_id = 16 AND menu_id = 2146;

-- ---------- 8. 审计17：摘上报写按钮（归统计上报员41） ----------
DELETE FROM sys_role_menu WHERE role_id = 17 AND menu_id IN (2160, 2161, 2162);

-- ---------- 9. 药剂师5：摘药库主责按钮（归药库管理员37） ----------
DELETE FROM sys_role_menu
WHERE role_id = 5 AND menu_id IN
  (2075, 2076, 2077, 2078, 2079, 2080, 2081, 2503, 2504, 2505);

-- ---------- 10. 麻精专册2074：5 药剂师 / 13 临床药师 摘 → 37 药库管理员 ----------
DELETE FROM sys_role_menu WHERE role_id IN (5, 13) AND menu_id = 2074;

-- ---------- 11. 回补迁移目标角色（NOT EXISTS 守卫，唯一键 uk_role_menu） ----------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (9210000000000001201, 25, 2131, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001202, 25, 2132, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001203, 25, 2133, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001204, 40, 2146, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001205, 41, 2160, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001206, 41, 2161, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001207, 41, 2162, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001208, 37, 2075, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001209, 37, 2076, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001210, 37, 2077, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001211, 37, 2078, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001212, 37, 2079, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001213, 37, 2080, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001214, 37, 2081, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001215, 37, 2503, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001216, 37, 2504, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001217, 37, 2505, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0),
  (9210000000000001218, 37, 2074, 'menu-govern-387', NOW(), 'menu-govern-387', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ---------- 回查 ----------
-- role18 剩余应 13 条：10 页面 + 2 目录 + 1 按钮（100/101/102/29654/2001/1200/906/907/908/910/1201/2159/2940）
-- SELECT menu_id FROM sys_role_menu WHERE role_id=18 ORDER BY menu_id;
-- 各摘除项应为 0：
-- SELECT role_id, menu_id FROM sys_role_menu WHERE (role_id,menu_id) IN
--   ((3,2614),(10,2614),(2,2033),(32,2033),(32,2928),(7,1001),(29,2668),(33,29625),(16,2146),(17,2160),(5,2075),(13,2074));
-- 迁移目标应持有：
-- SELECT role_id, menu_id FROM sys_role_menu WHERE role_id IN (25,37,40,41) AND menu_id IN
--   (2131,2132,2133,2146,2160,2161,2162,2074,2075,2076,2077,2078,2079,2080,2081,2503,2504,2505);

-- ---------- 回滚 ----------
-- 1) 删回补行：DELETE FROM sys_role_menu WHERE create_by='menu-govern-387';
-- 2) 按备份 CSV 恢复被摘行（role_id, menu_id 逐行 INSERT，id 用备份原值）。
