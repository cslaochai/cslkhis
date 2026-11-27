-- ============================================================
-- 407 转诊闭环按钮补授权：门诊办管理员(25)补确认/完成/取消权限
-- 日期：2026-11-27
-- 背景：406 已把双向转诊页面 312 与新增按钮 2041 归到 role25；闭环动作权限此前全员未授权。
--   · 2042 inpatient:referral:edit —— 确认接收(audit)、完成(finish)、超时催办(escalate)
--   · 2043 inpatient:referral:delete —— 取消转诊(cancel)
--   二者同属转诊协调闭环，归口 role25 门诊办管理员，与 312/2041 一致。
-- 备份：backup/sys_role(_menu)_backup_20261127_121500.csv；回滚见 407-回滚.sql
-- 执行后清缓存 his:perm:role:*、his:user:identity:*
-- ============================================================

-- ① 补 role25 双向转诊-修改（确认/完成/催办）、双向转诊-删除（取消）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001833, 25, 2042, 'menu-govern-407', NOW(), 'menu-govern-407', NOW(), 0, 0),
  (9210000000000001834, 25, 2043, 'menu-govern-407', NOW(), 'menu-govern-407', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 保留不动：2174 ipd:teleconsult:edit 维持仅挂 role2 医生。该码混合两类动作——
--   远程会诊安排/完成/取消（协调管理）与线上问诊接诊/回复/退诊（临床接诊），后端尚未按岗位拆码；
--   role26 在 315 远程会诊为只读审批备案视角，不直接操作会诊单。
--   技术债：后续把远程会诊安排/取消拆为独立权限码，再按岗位分别授权（见 107 文档）。
-- 回查：2041/2042/2043 持有=[25]；25 转诊授权 312,2041,2042,2043；全库孤儿页 0；25/26 名下无空目录。
