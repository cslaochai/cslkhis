-- ============================================================================
-- 297-临床值班听班呼叫链路（呼叫 → 到岗应答 → 超时升级）
-- ============================================================================
-- 背景：急诊值班表能排能打印（F7），但「听班呼叫到岗/超时升级」只有一个
--   attend_mode 标记（1坐班/2听班/3留院），没有呼叫链路 —— 听班的人被叫了
--   谁知道？叫了多久没人来？没有记录，说不清责任。
--
-- 实测底数（2026-10-26 dev 库）：
--   ① biz_duty_post 234 个临床点位（duty_scope=6），39 科室 × {一/二/三线} × {白/夜}，
--      duty_level 与 attend_mode 齐备（一线=3留院，二/三线=2听班）。
--   ② biz_duty_roster **临床（duty_scope=6）排班 0 行**（30 行全是 duty_scope=1 行政总值班）
--      → 听班的人根本没排进班表，呼叫无处落地。
--   ③ biz_schedule **attend_mode=2（听班）0 行**（96 行全是 1坐班，2 行是 3留院）
--      → 「听班」这个形态在排班底座上从未真正出现过。
--   ④ 全库无「呼叫」语义表：唯一沾边的是 biz_duty_log（交班本，行政口径，
--      记的是值班事件不是呼叫）。
--
-- 本次落三件事：
--   A. biz_duty_call —— 听班呼叫单（呼叫→到岗→升级 的状态机 + 全程留痕）。
--   B. biz_duty_call_log —— 呼叫流水（每次状态流转一条，只增不改）。
--   C. biz_duty_roster 铺临床二/三线听班排班（近 14 天，39 科室），
--      让 A 的目标人解析有真实数据可命中。
--
-- 设计要点：
--   · 呼叫单是运行时事件，不塞进 biz_schedule（那是排班事实底座，混进去
--     会把「谁在班」和「谁被叫过」搅在一起）。
--   · 升级是「呼叫单」自己的状态流转，不另建升级表：一条呼叫单走完
--     呼叫→到岗 或 呼叫→超时→升级→到岗(或再升级)，全程可追。
--   · 「是否升级过」必须落库（escalate_status/escalate_time）：超时本身实时算，
--     防重复轰炸的标记必须持久 —— 否则定时任务每轮都会重复升级同一条。
--   · 目标层级来自 biz_duty_post.duty_level（位先于人），呼叫时选「叫第几线」，
--     不是选具体某个人 —— 人由排班决定，位由业务决定。
--
-- ⚠ 运维注意：
--   1) 执行后必须清权限 Redis 缓存，否则菜单/授权变更 10 分钟内不生效：
--      redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--      redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
--   2) 本文件幂等：表 IF NOT EXISTS；菜单按 ID 物理删再插
--      （sys_menu.menu_key 是 UNIQUE 且不含 del_flag，软删会占键位）；
--      sort_order 写死目标值（不依赖现值）；角色授权 INSERT IGNORE。
--   3) sys_role_menu.id 无库默认值，INSERT 必须显式给。本文件用三个互不重叠的号段：
--      sys_role_menu 8920610263* / sys_config 8920610264* / biz_duty_roster 8920610265*
--      （实测均未被占用）。
-- ============================================================================

-- ---------- 1. 听班呼叫单 ----------
CREATE TABLE IF NOT EXISTS `biz_duty_call` (
  `id` bigint NOT NULL COMMENT '主键',
  `call_no` varchar(32) NOT NULL COMMENT '呼叫单号',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL DEFAULT '1' COMMENT '班次（1-白班 2-夜班 00-次日08）',
  `dept_id` bigint NOT NULL COMMENT '呼叫发起科室ID',
  `dept_name` varchar(100) COMMENT '呼叫发起科室名称（快照）',
  `target_level` tinyint NOT NULL COMMENT '被叫层级（2-二线 3-三线）',
  `target_post_id` bigint NOT NULL COMMENT '被叫值班点位ID',
  `target_post_name` varchar(100) COMMENT '被叫值班点位名称（快照）',
  `target_emp_id` bigint DEFAULT NULL COMMENT '被叫值班人',
  `target_emp_name` varchar(50) COMMENT '被叫值班人姓名（快照）',
  `target_phone` varchar(32) COMMENT '被叫值班联系电话',
  `call_reason` varchar(500) NOT NULL COMMENT '呼叫事由',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `patient_location` varchar(100) COMMENT '患者所在位置（病区/床号）',
  `call_time` datetime NOT NULL COMMENT '呼叫时间',
  `arrive_time` datetime COMMENT '到岗时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-待到岗 1-已到岗 2-已超时升级 3-已取消）',
  `escalate_status` tinyint NOT NULL DEFAULT '0' COMMENT '升级状态（0-未升级 1-已升级）',
  `escalate_time` datetime COMMENT '升级时间',
  `escalate_from_level` tinyint COMMENT '升级前的层级（2-二线 3-三线）',
  `cancel_reason` varchar(200) COMMENT '取消事由',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_call_no` (`call_no`),
  KEY `idx_duty_call_date` (`duty_date`),
  KEY `idx_duty_call_target` (`target_emp_id`),
  KEY `idx_duty_call_dept` (`dept_id`),
  KEY `idx_duty_call_status` (`status`,`escalate_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临床值班听班呼叫单';

-- ---------- 2. 呼叫流水（状态流转留痕，只增不改） ----------
CREATE TABLE IF NOT EXISTS `biz_duty_call_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `call_id` bigint NOT NULL COMMENT '呼叫单ID',
  `call_no` varchar(32) NOT NULL COMMENT '呼叫单号',
  `action_type` tinyint NOT NULL COMMENT '动作（1-呼叫 2-到岗 3-超时升级 4-取消 5-通知失败）',
  `action_time` datetime NOT NULL COMMENT '动作时间',
  `from_status` tinyint COMMENT '动作前状态',
  `to_status` tinyint COMMENT '动作后状态',
  `operator_id` bigint COMMENT '操作人ID（系统触发为空）',
  `operator_name` varchar(50) COMMENT '操作人姓名（系统触发为空）',
  `action_note` varchar(500) COMMENT '动作说明',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_call_log_call` (`call_id`),
  KEY `idx_call_log_time` (`action_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='听班呼叫流水';

-- ---------- 3. 呼叫超时阈值参数（分钟） ----------
-- ⚠ sys_config.config_id 无库默认值，INSERT 必须显式给（雪花号段 8920610264*）。
INSERT INTO sys_config (config_id, config_key, config_value, config_name, config_type, is_system, remark, create_by, update_by, create_by_id, update_by_id)
SELECT 8920610264000000001, 'duty.call.timeout_minutes', '10', '听班呼叫到岗时限（分钟）', '1', '0',
       '听班呼叫发出后多少分钟未到岗即升级到上一层级', 'system', 'system', '0', '0'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'duty.call.timeout_minutes');

-- ---------- 4. 菜单：听班呼叫（挂「全院总值班」806 下） ----------
DELETE FROM sys_menu WHERE id IN (29616, 29617, 29618, 29619);
INSERT INTO sys_menu (id, menu_key, menu_name, parent_id, permission, sort_order, path, component, icon,
                       menu_type, is_frame, is_cache, is_visible, status,
                       create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(29616, 'org.dutyCall', '听班呼叫', 806, 'org:dutyCall:list', 4, 'duty-call', 'duty/DutyCallView', 'phone',
 2, 0, 1, 1, 1, 'system', now(), 'system', now(), 0, '临床二/三线听班呼叫：发起呼叫、到岗应答、超时升级', '0', '0'),
(29617, 'org.dutyCall.call', '听班呼叫-发起呼叫', 29616, 'org:dutyCall:call', 1, NULL, NULL, NULL,
 3, 0, 1, 0, 1, 'system', now(), 'system', now(), 0, '发起听班呼叫', '0', '0'),
(29618, 'org.dutyCall.arrive', '听班呼叫-到岗应答', 29616, 'org:dutyCall:arrive', 2, NULL, NULL, NULL,
 3, 0, 1, 0, 1, 'system', now(), 'system', now(), 0, '被叫人应答到岗', '0', '0'),
(29619, 'org.dutyCall.cancel', '听班呼叫-取消', 29616, 'org:dutyCall:cancel', 3, NULL, NULL, NULL,
 3, 0, 1, 0, 1, 'system', now(), 'system', now(), 0, '取消呼叫', '0', '0');

-- ---------- 5. 角色授权 ----------
-- ⚠ admin(1) 是**显式授权制**（sys_role_menu 里已有 519 行，不是自动全量），
--   所以 admin 必须显式加这 4 条，否则管理员调接口直接 403。
-- 医生(2)/急诊医生(8)：发起与应答（临床一线自己叫二线、二线应答到岗）
-- 医务科(26)/科主任(27)：管理视角（查全部 + 发起）
-- 护士长(10)/科护士长(30)：护理侧只读（护士也会呼叫二线医生，但应答是医生的事）
--
-- 幂等写法：先按 (menu_id, role_id) 组合精确删，再显式给 id 插入。
--   uk_role_menu 是 (role_id, menu_id) 复合唯一键 —— 只 INSERT IGNORE 会被已存在的
--   组合静默跳过（授权看着没生效）；而「先删 menu_id 全部再插」在并发执行下
--   会与另一个执行者互撞主键，所以按组合删最稳。
-- 号段 8920610263*：实测该段未被占用（与存量 8920610262*/8926102620* 均不撞）。
DELETE FROM sys_role_menu WHERE (menu_id = 29616 AND role_id IN (1,2,8,10,26,27,30))
                          OR (menu_id = 29617 AND role_id IN (1,2,8,26,27))
                          OR (menu_id = 29618 AND role_id IN (1,2,8))
                          OR (menu_id = 29619 AND role_id IN (1,2,8,26,27));

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 8920610263000000000 + ROW_NUMBER() OVER (ORDER BY r.id) - 1, r.id, 29616
FROM sys_role r WHERE r.del_flag = 0 AND r.status = 1 AND r.id IN (1,2,8,10,26,27,30);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 8920610263000000100 + ROW_NUMBER() OVER (ORDER BY r.id) - 1, r.id, 29617
FROM sys_role r WHERE r.del_flag = 0 AND r.status = 1 AND r.id IN (1,2,8,26,27);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 8920610263000000200 + ROW_NUMBER() OVER (ORDER BY r.id) - 1, r.id, 29618
FROM sys_role r WHERE r.del_flag = 0 AND r.status = 1 AND r.id IN (1,2,8);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 8920610263000000300 + ROW_NUMBER() OVER (ORDER BY r.id) - 1, r.id, 29619
FROM sys_role r WHERE r.del_flag = 0 AND r.status = 1 AND r.id IN (1,2,8,26,27);

-- ---------- 6. 铺临床二/三线听班排班（近 14 天） ----------
-- 为什么必须铺：biz_duty_roster 临床（duty_scope=6）排班 0 行，
--   听班呼叫的目标人解析没有数据可命中 —— 链路建好也是空的。
-- 口径：每个启用临床点位排一个当班医生；按「科室内医生轮转」分配，
--   一线留院、二/三线听班（点位的 attend_mode 已定好，直接继承）。
-- 幂等：uk_duty_post_date(post_id, duty_date) 不含 del_flag → 用 INSERT IGNORE
--   走唯一键，重复执行不会重复排；改了人再执行会被唯一键挡住（需人工 DELETE 重排）。
--
-- ⚠ 这里的排班人是「按科室医生循环取」造出来的演示数据，不是真实排班。
--   真实排班请走「值班排班」页（临床医师值班种类）登记。
--   造数据的目的是让呼叫链路有可命中的目标人，可被真实排班覆盖。
--
-- 取人方式：先把「科室 → 该科室第 N 个在职医生」摊平成临时表再 JOIN，
--   并带上该科室医生总数 cnt，让轮转在医生少的科室也落得到人
--   （rn = ((duty_level-1) % cnt)+1；cnt=1 的科室所有层级只能排同一人，是数据现实）。
--   不用关联子查询里的 LIMIT/OFFSET —— MySQL 不支持该写法（8.0 仍报语法错）。
DROP TEMPORARY TABLE IF EXISTS tmp_duty_call_emp;
CREATE TEMPORARY TABLE tmp_duty_call_emp AS
SELECT e.dept_id, e.id AS emp_id, e.emp_name, e.dept_name, e.phone,
       ROW_NUMBER() OVER (PARTITION BY e.dept_id ORDER BY e.id) AS rn,
       COUNT(*) OVER (PARTITION BY e.dept_id) AS cnt
FROM sys_employee e
WHERE e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0 AND e.dept_id IS NOT NULL;

SET @today = CURDATE();

INSERT IGNORE INTO biz_duty_roster
(id, duty_date, shift_type, role_type, employee_id, employee_name, dept_id, dept_name, phone,
 start_time, end_time, status, publish_status, shift_id, post_id, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT
  -- ID 段：基址 + 点位号低位 6 位 + 日期偏移 × 10 + 班内角色。
  -- ⚠ 不能用 p.id % 1000 —— 234 个临床点位取模后只有 60 个不同值（实测），
  --   会导致不同点位算出同一个主键。低位 6 位实测 234 个点位全不重复。
  CAST(8920610265000000000 + (p.id % 1000000) * 100 + DATEDIFF(d.dt, @today) * 2 + p.role_type AS SIGNED),
  d.dt,
  CASE WHEN s.cross_day = 1 THEN 2 ELSE 1 END,
  p.role_type,
  t.emp_id, t.emp_name, t.dept_id, t.dept_name, t.phone,
  s.start_time, s.end_time, 1, 2,
  p.shift_id, p.id,
  'system', now(), 'system', now(), 0,
  '297-临床听班呼叫链路铺底排班', '0', '0'
FROM biz_duty_post p
JOIN biz_shift s ON s.id = p.shift_id
JOIN tmp_duty_call_emp t
  ON t.dept_id = p.org_id AND t.rn = ((p.duty_level - 1) % t.cnt) + 1
JOIN (SELECT DATE_ADD(@today, INTERVAL seq DAY) dt FROM
      (SELECT 0 seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
       UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12
       UNION SELECT 13) t
     ) d
WHERE p.duty_scope = 6 AND p.status = 1 AND p.del_flag = 0
  AND p.org_type = 1;

DROP TEMPORARY TABLE IF EXISTS tmp_duty_call_emp;

-- ---------- 7. 自检 ----------
SELECT 'A. 呼叫单表存在' AS check_item, COUNT(*) AS cnt FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_name = 'biz_duty_call'
UNION ALL
SELECT 'B. 呼叫流水表存在', COUNT(*) FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_name = 'biz_duty_call_log'
UNION ALL
SELECT 'C. 超时参数已配', COUNT(*) FROM sys_config WHERE config_key = 'duty.call.timeout_minutes'
UNION ALL
SELECT 'D. 菜单 4 条', COUNT(*) FROM sys_menu WHERE id IN (29616, 29617, 29618, 29619) AND del_flag = 0
UNION ALL
SELECT 'E. 角色授权行数', COUNT(*) FROM sys_role_menu WHERE menu_id IN (29616, 29617, 29618, 29619)
UNION ALL
SELECT 'F. 临床排班行数（应为 234 点位 × 14 天）', COUNT(*) FROM biz_duty_roster r
 JOIN biz_duty_post p ON p.id = r.post_id WHERE r.del_flag = 0 AND p.duty_scope = 6
UNION ALL
SELECT 'G. 其中听班（二/三线）行数', COUNT(*) FROM biz_duty_roster r
 JOIN biz_duty_post p ON p.id = r.post_id WHERE r.del_flag = 0 AND p.duty_scope = 6 AND p.attend_mode = 2
UNION ALL
SELECT 'H. 孤儿授权（应为 0）', COUNT(*) FROM sys_role_menu rm
 LEFT JOIN sys_menu m ON m.id = rm.menu_id WHERE rm.menu_id IN (29616, 29617, 29618, 29619) AND m.id IS NULL;
