-- 294-员工编号改E随机段加序号.sql
-- 口径（2026-10-25 老王定）：员工编号 = E + 3 位随机段 + 3 位段内序号（如 E482001），
-- 生成端唯一出口 RedisSequenceServiceImpl.generateEmployeeNo()，
-- 段内计数器 key = EMPLOYEE:SEG:{3位段}，永久有效（不过期、不按天重置）。
--
-- 为什么不是「E+日期+流水」也不是「全局 3 位序号」：
--   ① 日期段让编号随入职日跳动，排班/考勤快照跨天看不出是一类人；
--   ② 全局 3 位序号全院上限 999 人（当前存量 519），扩容即撞顶；
--   ③ 随机段切空间 = 1000 段 × 每段 999 = 全院 999000，段内序号 3 位不会溢出。
--
-- 本文件把历史数据切到新格式。编排脚本 workspace/_mig_empcode.mjs 按 0→3 顺序执行，
-- 也可按注释手工执行。所有步骤幂等：重跑零变化。
--
-- 涉及表（emp_code 共 4 处，漏一处 = 快照与主档对不上）：
--   sys_employee                主档（uk_emp_code 唯一键）
--   biz_schedule                排班快照（employee_id join）
--   biz_staff_attendance        考勤快照（employee_id join）
--   biz_opd_station_assignment  门诊公共工位派岗快照（employee_id join）

-- ---------- 0. 备份（先跑；脚本落 workspace/_bak_empcode_<日期>.json，四表全量含 NULL 行） ----------
-- SELECT CAST(id AS CHAR) AS id, emp_code FROM sys_employee ORDER BY id;
-- SELECT CAST(id AS CHAR) AS id, CAST(employee_id AS CHAR) AS employee_id, emp_code FROM biz_schedule;
-- SELECT CAST(id AS CHAR) AS id, CAST(employee_id AS CHAR) AS employee_id, emp_code FROM biz_staff_attendance;
-- SELECT CAST(id AS CHAR) AS id, CAST(employee_id AS CHAR) AS employee_id, emp_code FROM biz_opd_station_assignment;

-- ---------- 1. 重编号 sys_employee（确定性映射） ----------
-- 映射规则：待改行按 id 升序第 n 行（n 从 1 起）→
--   段   = 100 + (n-1) % 900        （100~999，避开 000~099 视觉噪声）
--   序号 = (n-1) / 900 + 1          （3 位，%03d）
-- 519 行 → E100001 ~ E618001，与 id 序一一对应，便于人工核对与回滚。
-- NOT REGEXP 守卫：已是新格式的行（含迁移后新生成的）不重编，重跑零变化。
START TRANSACTION;
UPDATE sys_employee e
JOIN (
    SELECT id,
           CONCAT('E',
                  LPAD(100 + (rn - 1) % 900, 3, '0'),
                  LPAD(FLOOR((rn - 1) / 900) + 1, 3, '0')) AS new_code
    FROM (
        SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
        FROM sys_employee
        WHERE emp_code NOT REGEXP '^E[0-9]{6}$'
    ) t
) m ON m.id = e.id
SET e.emp_code = m.new_code;
COMMIT;

-- ---------- 2. 同步 3 张快照表（按 employee_id join 主档，写入时快照、无改名联动） ----------
UPDATE biz_schedule s JOIN sys_employee e ON e.id = s.employee_id
SET s.emp_code = e.emp_code
WHERE s.emp_code <> e.emp_code;
UPDATE biz_staff_attendance a JOIN sys_employee e ON e.id = a.employee_id
SET a.emp_code = e.emp_code
WHERE a.emp_code <> e.emp_code;
UPDATE biz_opd_station_assignment o JOIN sys_employee e ON e.id = o.employee_id
SET o.emp_code = e.emp_code
WHERE o.emp_code <> e.emp_code;

-- ---------- 3. 播种 Redis 段计数器（SQL 之外，脚本用 RESP SET 执行） ----------
-- 对每个在用段 SET EMPLOYEE:SEG:{seg} = 该段当前最大序号。
-- 不播种的后果：新员工随机命中该段时 INCR 从 1 重来 → 撞 uk_emp_code → 新增员工 500。
-- SELECT SUBSTRING(emp_code, 2, 3) AS seg,
--        MAX(CAST(SUBSTRING(emp_code, 5, 3) AS UNSIGNED)) AS max_seq
--   FROM sys_employee
--  WHERE emp_code REGEXP '^E[0-9]{6}$'
--  GROUP BY seg;

-- ---------- 4. 验证 ----------
-- 4.1 全量新格式 + 无重复（new_fmt 应 = total，uniq 应 = total）：
-- SELECT COUNT(*) AS total,
--        SUM(emp_code REGEXP '^E[0-9]{6}$') AS new_fmt,
--        COUNT(DISTINCT emp_code) AS uniq
--   FROM sys_employee;
-- 4.2 快照与主档一致（三条都应 0 行）：
-- SELECT COUNT(*) AS c FROM biz_schedule s JOIN sys_employee e ON e.id = s.employee_id
--  WHERE s.emp_code <> e.emp_code;
-- SELECT COUNT(*) AS c FROM biz_staff_attendance a JOIN sys_employee e ON e.id = a.employee_id
--  WHERE a.emp_code <> e.emp_code;
-- SELECT COUNT(*) AS c FROM biz_opd_station_assignment o JOIN sys_employee e ON e.id = o.employee_id
--  WHERE o.emp_code <> e.emp_code;
-- 4.3 段序号连续无洞（每段 cnt 应 = max_seq）：
-- SELECT SUBSTRING(emp_code, 2, 3) AS seg, COUNT(*) AS cnt,
--        MAX(CAST(SUBSTRING(emp_code, 5, 3) AS UNSIGNED)) AS max_seq
--   FROM sys_employee WHERE emp_code REGEXP '^E[0-9]{6}$' GROUP BY seg HAVING cnt <> max_seq;

-- ---------- 回滚 ----------
-- 用 workspace/_bak_empcode_<日期>.json 按 id 逐行 UPDATE 还原（uk_emp_code 唯一，
-- 需先清目标行再写旧码；脚本 _mig_empcode.mjs --rollback 支持）。
