-- =============================================================================
-- 203-住院医师值班点位收敛与补铺（修 sql/202 的铺底口径）
--
-- 【sql/202 铺错了什么】实测（不是推测）：
--   · sql/202 用 `dept_type = 4` 挑"住院科室"。实测 dept_type=4 只有 9 个科室，
--     其中 6 个是「内科系统 / 外科系统 / 妇产科系统…」这类**行政虚科室**（一个医生都没有），
--     另外 3 个里「医院信息系统」只挂着 status=0 的超级管理员 —— 也不是临床科室。
--   · 结果只铺出 4 个科室 × 6 位 = 24 个点位，其中 1958001 那 6 个**永远排不进人**
--     （排班服务校验「还没分配岗位」，本科室根本没有在职医生）。
--   · 而真正有住院业务（sys_ward 有病区）且有在职医生的科室有 **39 个**，一个都没铺到。
--
-- 【为什么不能用 dept_type 挑科室】
--   dept_type 是**行政分类口径**（系统 / 科室 / 病区…），值班是**业务口径**：
--   一个科室要不要排住院医师值班，只取决于它有没有病房、有没有医生 ——
--   跟它在行政树上是"系统"还是"科室"无关。用分类去推业务，必然铺歪。
--
-- 【本脚本做两件事】
--   1. 收敛：把"所属科室没有在职医生"的临床点位物理删（唯一键 post_code 不含 del_flag，
--      软删后同一编码再也插不回来；且这些位从未被排过班，删掉不丢数据）。
--   2. 补铺：按「有病区 + 有在职医生」把 39 个科室的 3 层级 × 白/夜 6 个位补齐。
--
-- 【刻意不做】
--   · 不删 sql/202 建的班次与管床关系（那两样是对的）。
--   · 不铺「只有门诊没有病区」的科室：门诊医生的出诊已经由 biz_schedule 管，
--     再给他铺一条住院值班位，等于凭空多一份没人认领的责任。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 收敛：删掉铺在没有在职医生的科室上的临床点位
--    只删从没被排过班的（biz_duty_roster 里没有引用），排过的必须留着 ——
--    那已经是既成事实，删了历史排班就成了孤儿。
-- -----------------------------------------------------------------------------
DELETE p FROM biz_duty_post p
WHERE p.duty_scope = 6
  AND p.del_flag = 0
  AND NOT EXISTS (SELECT 1 FROM sys_employee_post ep
                   JOIN sys_employee e ON e.id = ep.employee_id
                   WHERE ep.dept_id = p.org_id
                     AND e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0)
  AND NOT EXISTS (SELECT 1 FROM biz_duty_roster r WHERE r.post_id = p.id AND r.del_flag = 0);

-- -----------------------------------------------------------------------------
-- 2. 补铺：39 个「有病区 + 有在职医生」的科室 × 3 层级 × 白/夜
--    沿用 sql/202 的点位编码与 id 公式，已存在的自动跳过（NOT EXISTS 按 post_code）。
--    ⚠ id 公式里 LPAD(d.id, 10, '0') 的位数不能省：MySQL 的 LPAD 在源串超长时是**截断**，
--      sql/202 第一版写 LPAD(d.id, 6, '0')，19580059/19580060/19580062 三个科室
--      全被截成 '195800'，拼出同一个主键直接 ER_DUP_ENTRY。
-- -----------------------------------------------------------------------------
INSERT INTO biz_duty_post (id, post_code, post_name, duty_scope, org_type, org_id,
                           role_type, duty_level, attend_mode, shift_id, required_staff_type,
                           sort_no, status, create_time, update_time, del_flag, remark)
SELECT
  CAST(CONCAT('89676', LPAD(d.id, 10, '0'), lv.lvl, sh.seq) AS UNSIGNED),
  CONCAT('DOC_', d.id, '_', sh.tag, '_L', lv.lvl),
  CONCAT(d.dept_name, lv.name, sh.name),
  6, 1, d.id,
  1, lv.lvl, lv.mode, sh.shift_id, 1,
  lv.lvl * 10 + sh.seq, 1, NOW(), NOW(), 0,
  CONCAT('sql/203 住院医师值班点位（', lv.name, lv.mode_name, '）')
FROM sys_department d
JOIN (SELECT 1 lvl, '一线' name, 3 mode, '驻守' mode_name
      UNION ALL SELECT 2, '二线', 2, '听班'
      UNION ALL SELECT 3, '三线', 2, '听班') lv
JOIN (SELECT 1 seq, 'DAY' tag, '白班' name, 896730000000000104 shift_id
      UNION ALL SELECT 2, 'NIGHT', '夜班', 896730000000000105) sh
WHERE d.del_flag = 0
  AND d.status = 1
  -- 有住院业务：这个科室挂着启用中的病区
  AND EXISTS (SELECT 1 FROM sys_ward w WHERE w.dept_id = d.id AND w.status = 1)
  -- 有在职医生：排得上去才有意义（虚拟科室/全员离职的科室不铺）
  AND EXISTS (SELECT 1 FROM sys_employee_post p JOIN sys_employee e ON e.id = p.employee_id
               WHERE p.dept_id = d.id AND e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0)
  AND NOT EXISTS (SELECT 1 FROM biz_duty_post z
                   WHERE z.post_code = CONCAT('DOC_', d.id, '_', sh.tag, '_L', lv.lvl)
                     AND z.del_flag = 0);

-- -----------------------------------------------------------------------------
-- 3. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
-- ⚠ 自检段里不要写 SET @var：verify-sql.mjs 是拿 marker 之后的整段去 query，
-- 多一条 SET 就会多返回一个 OkPacket，rowsOf() 下钻时把它当成数据行，全段判废。
SELECT 'W1_空科室点位已清零' item, COUNT(*) cnt, 0 expect
FROM biz_duty_post p
WHERE p.duty_scope = 6 AND p.del_flag = 0
  AND NOT EXISTS (SELECT 1 FROM sys_employee_post ep
                   JOIN sys_employee e ON e.id = ep.employee_id
                   WHERE ep.dept_id = p.org_id
                     AND e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0)

UNION ALL SELECT 'W2_临床点位总数', COUNT(*),
  (SELECT COUNT(*) * 6 FROM sys_department d
    WHERE d.del_flag = 0 AND d.status = 1
      AND EXISTS (SELECT 1 FROM sys_ward w WHERE w.dept_id = d.id AND w.status = 1)
      AND EXISTS (SELECT 1 FROM sys_employee_post p JOIN sys_employee e ON e.id = p.employee_id
                   WHERE p.dept_id = d.id AND e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0))
FROM biz_duty_post WHERE duty_scope = 6 AND del_flag = 0

UNION ALL SELECT 'W3_缺位的科室数', COUNT(*), 0
FROM sys_department d
WHERE d.del_flag = 0 AND d.status = 1
  AND EXISTS (SELECT 1 FROM sys_ward w WHERE w.dept_id = d.id AND w.status = 1)
  AND EXISTS (SELECT 1 FROM sys_employee_post p JOIN sys_employee e ON e.id = p.employee_id
               WHERE p.dept_id = d.id AND e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0)
  AND (SELECT COUNT(*) FROM biz_duty_post z
        WHERE z.duty_scope = 6 AND z.del_flag = 0 AND z.org_id = d.id) <> 6

UNION ALL SELECT 'W4_点位编码无重复', COUNT(*), 0
FROM (SELECT post_code FROM biz_duty_post WHERE duty_scope = 6 AND del_flag = 0
      GROUP BY post_code HAVING COUNT(*) > 1) x

UNION ALL SELECT 'W5_点位都绑了医师值班班次', COUNT(*), 0
FROM biz_duty_post p
WHERE p.duty_scope = 6 AND p.del_flag = 0
  AND NOT EXISTS (SELECT 1 FROM biz_shift s
                   WHERE s.id = p.shift_id AND s.del_flag = 0 AND s.status = 1)

UNION ALL SELECT 'W6_层级与响应形态对得上', COUNT(*), 0
FROM biz_duty_post p
WHERE p.duty_scope = 6 AND p.del_flag = 0
  AND ((p.duty_level = 1 AND p.attend_mode <> 3) OR (p.duty_level IN (2, 3) AND p.attend_mode <> 2))

UNION ALL SELECT 'W7_层级合法(1-3)', COUNT(*), 0
FROM biz_duty_post p
WHERE p.duty_scope = 6 AND p.del_flag = 0 AND p.duty_level NOT IN (1, 2, 3)

UNION ALL SELECT 'W8_行政点位未受影响', COUNT(*), 4
FROM biz_duty_post WHERE duty_scope = 1 AND del_flag = 0 AND status = 1

UNION ALL SELECT 'W9_临床点位都应到医生岗', COUNT(*), 0
FROM biz_duty_post p
WHERE p.duty_scope = 6 AND p.del_flag = 0 AND p.required_staff_type <> 1

-- 最后一项刻意写成不带子查询的简单查询：verify-sql.mjs 切自检段时会把结尾的 `);` 一并剥掉，
-- 若最后一项以 `)` 收尾（子查询/NOT EXISTS 的闭括号会被当成语句结尾吃掉），剥完就语法残缺。
UNION ALL SELECT 'W10_临床点位启用中', COUNT(*), '>0'
FROM biz_duty_post WHERE duty_scope = 6 AND del_flag = 0 AND status = 1;
