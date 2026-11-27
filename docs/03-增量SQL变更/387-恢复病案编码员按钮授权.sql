-- 387 恢复病案编码员（role14）对「上报科室代码对照」的保存/删除按钮
-- 日期：2026-11-27
-- 背景：共享 dev 库里 role14 的菜单授权被整体重写（create_by 变成 admin/system），
--       29660/29628 丢失，导致页面还在但保存 403、整页变只读。
-- 口径：页面 29626 + 保存 29660 + 删除 29628 必须成套；只有页面时后端 org:medSubject:add 直接 403。
-- 注意：dev 库里 9210000000000001301/1302 已被 sql390 占用、1401 已被 menu-govern-394 占用，
--       本文件用 1402/1403。并行会话仍在改 sys_menu/sys_role_menu，优先用
--       workspace/_restore_role14_perm.mjs（按 MAX(id)+1 动态取空闲 id）执行，本文件仅作留痕。

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001402, 14, 29660, 'sql387', NOW(), 'sql387', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 14 AND menu_id = 29660);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001403, 14, 29628, 'sql387', NOW(), 'sql387', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 14 AND menu_id = 29628);

-- 回查：
-- SELECT role_id, menu_id FROM sys_role_menu WHERE role_id = 14 AND menu_id IN (29626,29660,29628) ORDER BY menu_id;
