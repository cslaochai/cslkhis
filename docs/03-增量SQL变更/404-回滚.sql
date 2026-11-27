-- ============================================================
-- 404 回滚：病历更正申请（撤权限 + 删表）
-- 前置：更正留痕已归档或可丢弃；回滚后 /emr/correction/* 接口无表支撑。
-- ============================================================

-- ① 撤角色授权与菜单
DELETE FROM sys_role_menu WHERE menu_id IN (2190, 2191, 2192, 2193);
DELETE FROM sys_menu WHERE id IN (2190, 2191, 2192, 2193);

-- ② 删表（申请与审批留痕一并删除）
DROP TABLE IF EXISTS `biz_emr_correction_request`;

-- 执行后清 Redis：his:perm:role:*、his:user:identity:*、his:menu:*
