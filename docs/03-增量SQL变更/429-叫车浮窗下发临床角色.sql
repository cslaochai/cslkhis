-- 429-叫车浮窗下发临床角色（iot:robotDispatch:apply）
--
-- 背景：421 落地机器人调度时，apply（新建配送任务单）只授给了 1 管理员 / 3 护士 /
--      10 护士长 / 30 科护士长 / 39 设备科。试点上真浏览器验证发现：
--        · 医生(2)、急诊医生(8)、门诊分诊护士(9)、手术室护士(32)、急诊分诊护士(44) 完全没有
--          机器人权限码，右下角「叫机器人」浮窗对这几类人整条不渲染 —— 而送药/送标本/送器械的
--          发起人恰恰是这些岗位（住院医生站开立后、门诊抽血、急诊留观、手术间补物）。
--        · 车队空闲数（fleet 接口）需要 iot:robotDispatch:list，属设备科监控视角，
--          **本次不授**：浮窗已按 list 码判断，没有就不拉车队（避免每 30 秒一条 403）。
--
-- 做法：只补 apply 一条按钮码，不授页面(421…101)、不授车队查看、不授派车/取消。
--      前面的 428 把临床从 902 设备管理页收回，叫车入口只剩浮窗，两件事配套。
--
-- 执行后清 Redis 权限缓存：his:perm:role:*（TTL 10 分钟，不清就等下一轮刷新）

-- ---------- 1) 补授 apply：医生 / 急诊医生 / 门诊分诊护士 / 手术室护士 / 急诊分诊护士 ----------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920614290000000001, 2, 4210000000000000102, 'sql429', NOW(), 'sql429', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920614290000000001 OR (rm.role_id = 2 AND rm.menu_id = 4210000000000000102));

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920614290000000002, 8, 4210000000000000102, 'sql429', NOW(), 'sql429', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920614290000000002 OR (rm.role_id = 8 AND rm.menu_id = 4210000000000000102));

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920614290000000003, 9, 4210000000000000102, 'sql429', NOW(), 'sql429', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920614290000000003 OR (rm.role_id = 9 AND rm.menu_id = 4210000000000000102));

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920614290000000004, 32, 4210000000000000102, 'sql429', NOW(), 'sql429', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920614290000000004 OR (rm.role_id = 32 AND rm.menu_id = 4210000000000000102));

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920614290000000005, 44, 4210000000000000102, 'sql429', NOW(), 'sql429', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920614290000000005 OR (rm.role_id = 44 AND rm.menu_id = 4210000000000000102));

-- ---------- 2) 回查：apply 码现在覆盖哪些角色 ----------
-- SELECT rm.role_id, r.role_name
-- FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
-- WHERE rm.menu_id = 4210000000000000102 ORDER BY rm.role_id;
-- 预期：1 管理员 / 2 医生 / 3 护士 / 8 急诊医生 / 9 门诊分诊护士 / 10 护士长 /
--      30 科护士长 / 32 手术室护士 / 39 设备科 / 44 急诊分诊护士

-- ---------- 3) 回查：车队查看(list)仍是设备科的活，没被带出来 ----------
-- SELECT rm.role_id FROM sys_role_menu rm WHERE rm.menu_id = 4210000000000000101 ORDER BY rm.role_id;
-- 预期仍只有 1 / 3 / 10 / 29 / 30 / 39（页面 is_visible=0，仅作 app 内路由与码载体）
