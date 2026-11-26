-- 390 内镜 / 超声：原 edit 码按动作环节细分为 exec / write / audit / publish
-- 日期：2026-11-26
-- 背景：
--   内镜(2056)、超声(2059) 原先一个 edit 码同时罩住「签到 / 执行 / 活检送检 / 测量保存 /
--   出具报告 / 审核 / 发布」六个动作，码粒度过粗，无法按环节收口。现对齐放射(2373-2376)、
--   心电(2395-2399) 的码模型，拆成四个独立按钮码：
--     exec     签到 / 执行 / 活检送检 / 测量保存（检查执行环节）
--     write    出具报告（书写 / 提交报告正文）
--     audit    报告审核（审核人不得为报告人本人）
--     publish  报告发布
-- 后端接口注解与前端 v-perm 已同步拆分；本脚本只铺菜单按钮与初始授权。
-- 说明：
--   1) 过渡授权沿用原 edit 的角色集合（角色 6 医技人员），保证在岗不被 403、功能不降级；
--      目标归属建议：exec 归检查技师(12)，write/audit/publish 归诊断医师（超声诊断医师 2、
--      放射/心电诊断医师 20）。角色-菜单关联正在并行梳理，授权迁移待对齐后单独落库。
--   2) 原 endoscopy:edit / ultrasound:edit 菜单行暂保留（兼容历史与回滚），后端已不再引用；
--      待授权迁移完成后再下线。

-- 1. 内镜四个新按钮码（父目录 401，与 2055/2056/2057 同组）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
  (29820, '内镜工作站-执行/签到',   401, 1, 3, NULL, NULL, 'medtech.endoscopy.exec',    NULL,
   'medtech:endoscopy:exec',    0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '签到 / 执行 / 活检送检（检查执行环节）', 0, 0),
  (29821, '内镜报告-书写/提交',     401, 2, 3, NULL, NULL, 'medtech.endoscopy.write',   NULL,
   'medtech:endoscopy:write',   0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '出具内镜报告（镜下所见 / 诊断 / 正文）', 0, 0),
  (29822, '内镜报告-审核',          401, 3, 3, NULL, NULL, 'medtech.endoscopy.audit',   NULL,
   'medtech:endoscopy:audit',   0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '报告审核（审核人不得为报告人本人）', 0, 0),
  (29823, '内镜报告-发布',          401, 4, 3, NULL, NULL, 'medtech.endoscopy.publish', NULL,
   'medtech:endoscopy:publish', 0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '报告发布，链路收口', 0, 0);

-- 2. 超声四个新按钮码
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
  (29824, '超声工作站-执行/签到',   401, 1, 3, NULL, NULL, 'medtech.ultrasound.exec',    NULL,
   'medtech:ultrasound:exec',    0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '签到 / 执行 / 结构化测量值保存（检查执行环节）', 0, 0),
  (29825, '超声报告-书写/提交',     401, 2, 3, NULL, NULL, 'medtech.ultrasound.write',   NULL,
   'medtech:ultrasound:write',   0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '出具超声报告（超声所见 / 提示）', 0, 0),
  (29826, '超声报告-审核',          401, 3, 3, NULL, NULL, 'medtech.ultrasound.audit',   NULL,
   'medtech:ultrasound:audit',   0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '报告审核（审核人不得为报告人本人）', 0, 0),
  (29827, '超声报告-发布',          401, 4, 3, NULL, NULL, 'medtech.ultrasound.publish', NULL,
   'medtech:ultrasound:publish', 0, 0, 1, 1, 'sql390', NOW(), 'sql390', NOW(), 0,
   '报告发布，链路收口', 0, 0);

-- 3. 初始授权：角色 6 医技人员（等价原 edit 现状；目标归属见文件头说明）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001301, 6, 29820, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29820);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001302, 6, 29821, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29821);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001303, 6, 29822, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29822);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001304, 6, 29823, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29823);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001305, 6, 29824, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29824);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001306, 6, 29825, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29825);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001307, 6, 29826, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29826);
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001308, 6, 29827, 'sql390', NOW(), 'sql390', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 6 AND menu_id = 29827);

-- 回滚：
-- DELETE FROM sys_role_menu WHERE create_by = 'sql390';
-- DELETE FROM sys_menu WHERE id IN (29820, 29821, 29822, 29823, 29824, 29825, 29826, 29827);

-- 回查：
-- SELECT id, menu_name, permission FROM sys_menu WHERE id BETWEEN 29820 AND 29827 ORDER BY id;
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id BETWEEN 29820 AND 29827 ORDER BY menu_id;
