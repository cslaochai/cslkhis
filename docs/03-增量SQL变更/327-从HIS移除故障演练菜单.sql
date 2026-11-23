-- 从 HIS 移除故障演练菜单与授权，项目已独立到 csa-fault
-- 执行前确认 sys_menu 中 29910/29911 只属于本演练入口。
DELETE FROM sys_role_menu WHERE menu_id IN (29910, 29911);
DELETE FROM sys_menu WHERE id IN (29910, 29911);

-- 清角色权限缓存（也可等 TTL 10 分钟或重新登录）：
-- redis-cli -h 192.168.88.132 -a 123456 --scan --pattern 'his:perm:role:*'
-- redis-cli -h 192.168.88.132 -a 123456 --scan --pattern 'his:user:identity:*'
