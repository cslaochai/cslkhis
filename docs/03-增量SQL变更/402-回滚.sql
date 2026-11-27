-- ============================================================
-- 402 回滚：病历草稿移交代补（撤权限 + 删表）
-- 前置：代补留痕已归档或可丢弃；回滚后 /emr/handover/* 接口无表支撑。
-- ============================================================

-- ① 撤角色授权与菜单
DELETE FROM sys_role_menu WHERE menu_id = 2189;
DELETE FROM sys_menu WHERE id = 2189;

-- ② 删表（留痕记录一并删除）
DROP TABLE IF EXISTS `biz_emr_draft_handover`;

-- 执行后清 Redis：his:perm:role:*、his:user:identity:*、his:menu:*
