-- =============================================================================
-- 202 - 住院医师值班 + 管床关系（补齐住院侧医生排班）
--
-- 【要解决的问题】
-- 排班在门诊侧是完整的（医生 × 午别 → 号源），在护理侧也是完整的（护士 × 病区 → 工时），
-- 唯独**住院医生是断的**：全院 245 名医生里 229 人（93.5%）没有任何排班记录。
-- 根因不是漏配数据，是**住院侧压根没有医生的排班载体**：
--   - biz_nurse_schedule 是「一人一天一个班次」的护理口径，塞不进医生；
--   - biz_duty_roster 是「全院行政总值班」（主班/副班），跟「内科今天谁一线」两码事；
--   - 医生岗位 236 个全部挂在科室，一个都不挂病区，所以按病区排也排不出来。
-- 后果：住院患者出了事不知道该找谁，值班表只能靠科室自己记 Excel。
--
-- 【住院医生到底要排什么】（跟护士不是一回事，别硬塞）
--  1) **值班**：按天的事实。一线=住院医师驻守病区，二线=主治听班，三线=主任听班兜底。
--     这是真正的「排班」，建模上跟护理排班同构（人 × 日 × 班），但字段多一个「层级」。
--  2) **管床**：按时期的**归属关系**，不是按天的事实。主管医生 ↔ 患者，入院到出院全程负责。
--     建模上是带时效的关系表，不是事实表 —— 这点最容易搞错。
-- 查房、手术、写病历都不排班（跟着患者走），所以本文件只做上面两样。
--
-- 【本文件做什么】
--  1. 解锁 biz_duty_roster 的旧唯一键 uk_duty_date_shift_role（duty_date+shift_type+role_type，
--     **不含 post_id**）：只要一个点位就是全院唯一，多科室铺开必然撞键。
--     正确的唯一性由 uk_duty_post_date（点位 × 日期）保证 —— 一个位一天一个人。
--  2. biz_duty_post 加 duty_level（一线/二线/三线）+ attend_mode（坐班/听班/留院）。
--     **一线/二线/三线不能塞进 role_type**：role_type 是「班内主副」（找不到主班就叫副班），
--     层级是「责任档位」（一线解决不了升二线），两者语义正交。
--  3. DutyScopeEnum 加 6-临床科室：把「医师值班」与「行政总值班」在责任范围上分开，
--     派单时才能找对人（网络断了打给行政总值班，他没口令；病人需要升二线打给临床值班）。
--  4. 铺两条医师值班班次（白 08:00-18:00 / 夜 18:00-08:00 跨天），use_scope=4 全院通用、
--     apply_staff_type=1 医生。不复用「总值班白/夜班」—— 那是 use_scope=3 的行政册班次。
--  5. 铺临床科室值班点位：科室 × 白/夜 × 一线/二线/三线。
--     **只给有医生的住院科室铺**（dept_type=4 且该科室有医生岗位），空壳科室不铺空位。
--  6. 新建 biz_attending_relation（管床关系），并从 biz_admission.admit_doctor_id 回填存量。
--
-- 【刻意不做】
--  - 不铺具体的排班数据：要先有科室的真实值班规则（几线、几人、谁有资质），属下一轮的事。
--    本文件只把「位」铺好，人进位是排班功能的事。
--  - 不做主诊组/医疗组：库里没有医疗组主数据，relation_type 留了 2-主诊组长 的码位但不铺数据。
--  - 不动 biz_nurse_schedule：护理排班口径本身是对的，缺的是接入核心表（另一项待办）。
--
-- 【删除口径】biz_attending_relation 唯一键不含 del_flag —— 删除一律物理删。
-- =============================================================================

SET NAMES utf8mb4;
SET @db := DATABASE();

-- -----------------------------------------------------------------------------
-- 1. 解锁旧唯一键 uk_duty_date_shift_role
--    这个键是 sql/199 时代的产物：那时全院只有一组总值班，所以「一天 + 一个班次 + 一个角色」
--    就能唯一定位。sql/200 引入点位（biz_duty_post）后，唯一性应该落在 (post_id, duty_date) 上；
--    旧键没跟着删，于是**只要铺第二个科室的点位，第二天进位就会撞 Duplicate**。
--    删它对存量无损：uk_duty_post_date 已经把「一个位一天一人」约束住了，比旧键更贴合语义。
--    服务层若原来依赖这个键做「全院一天一个主班」的校验，需自行补显式查重。
-- -----------------------------------------------------------------------------
SET @has_uk := (SELECT COUNT(*) FROM information_schema.STATISTICS
                 WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster'
                   AND INDEX_NAME = 'uk_duty_date_shift_role');
SET @sql := IF(@has_uk > 0,
  'ALTER TABLE biz_duty_roster DROP INDEX uk_duty_date_shift_role',
  'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- -----------------------------------------------------------------------------
-- 2. biz_duty_post 加两列：层级 + 响应形态
--    duty_level：0-不适用（行政总值班这类没有层级的）1-一线 2-二线 3-三线
--    attend_mode：1-坐班 2-听班 3-留院值班（二线三线是听班，一线是留院值班）
--    都放点位上而不是排班时手填 —— 「位」先于人存在，位的属性就该由位自己带。
-- -----------------------------------------------------------------------------
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_post' AND COLUMN_NAME = 'duty_level');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_duty_post ADD COLUMN duty_level tinyint NOT NULL DEFAULT 0 COMMENT ''值班层级（0-不适用 1-一线 2-二线 3-三线）'' AFTER role_type',
  'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_post' AND COLUMN_NAME = 'attend_mode');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_duty_post ADD COLUMN attend_mode tinyint NOT NULL DEFAULT 3 COMMENT ''响应形态（1-坐班 2-听班 3-留院值班）'' AFTER duty_level',
  'SELECT 1');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- -----------------------------------------------------------------------------
-- 3. 医师值班班次（2 条）
--    时间跟总值班白/夜班一致（08:00-18:00 / 18:00-08:00），因为一线要驻守覆盖全天；
--    但 use_scope 用 4（全院通用）而不是 3（行政册），apply_staff_type=1 限定医生可排。
-- -----------------------------------------------------------------------------
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, duration_minutes,
                       use_scope, apply_staff_type, status, create_time, update_time, del_flag, remark)
-- 每个字面量列都必须起别名：UNION 的列名取自第一个 SELECT，
-- 同值的字面量（这里 apply_staff_type=1 与 status=1 都是 1）会被 MySQL 一律命名成 "1"，
-- 直接 ER_DUP_FIELDNAME。
SELECT * FROM (
  SELECT 896730000000000104 AS id, '医师值班白班' AS shift_name, '08:00' AS start_time, '18:00' AS end_time,
         0 AS cross_day, 600 AS duration_minutes, 4 AS use_scope, 1 AS apply_staff_type,
         1 AS status, NOW() AS create_time, NOW() AS update_time, 0 AS del_flag,
         'sql/202 住院医师值班（一线驻守 / 二线三线听班）' AS remark
  UNION ALL
  SELECT 896730000000000105, '医师值班夜班', '18:00', '08:00', 1, 840, 4, 1, 1, NOW(), NOW(), 0,
         'sql/202 住院医师值班（跨零点，次日 08:00 收）'
) x
WHERE NOT EXISTS (SELECT 1 FROM biz_shift s WHERE s.id = x.id);

-- -----------------------------------------------------------------------------
-- 4. 铺临床科室值班点位：科室 × 白/夜 × 一线/二线/三线
--    层级与响应形态：一线=留院值班(3)，二线/三线=听班(2)；白班夜班都铺。
--
--    ⚠【口径已在 sql/203 修正过，改的就是下面这段 WHERE】
--    原写法 `dept_type = 4 AND EXISTS(医生岗位)` 实测铺歪了：
--      · dept_type=4 只有 9 个科室，其中 6 个是「内科系统/外科系统」这类行政虚科室；
--      · 「医院信息系统」科只挂着 status=0 的超级管理员 → 铺出 6 个永远排不上人的位；
--      · 真正有住院业务（sys_ward 有病区）且有在职医生的 39 个科室一个都没铺到。
--    教训：**dept_type 是行政分类口径，值班是业务口径**。用分类推业务必然铺歪。
--    现口径 = 有启用中的病区 + 有在职医生（emp_type=1 且 status=1）。
--
--    ⚠ 另一条更隐蔽的坑：verify-sql.mjs 跑自检时会**把整个文件重新执行一遍**。
--    口径不在这里改掉，sql/203 删掉的无效点位会在下次跑 sql/202 自检时原地复活。
-- -----------------------------------------------------------------------------
INSERT INTO biz_duty_post (id, post_code, post_name, duty_scope, org_type, org_id,
                           role_type, duty_level, attend_mode, shift_id, required_staff_type,
                           sort_no, status, create_time, update_time, del_flag, remark)
-- id 生成踩过的坑：MySQL 的 LPAD(str, len, pad) 在 str 比 len **长的时候是截断**，不是原样返回。
-- 写 LPAD(d.id, 6, '0') 时，19580059 / 19580060 / 19580062 三个科室全被截成 '195800'，
-- 拼出来同一个主键 → ER_DUP_ENTRY。位数必须给足（科室 id 实测 8 位，留到 10 位）。
SELECT
  CAST(CONCAT('89676', LPAD(d.id, 10, '0'), lv.lvl, sh.seq) AS UNSIGNED),
  CONCAT('DOC_', d.id, '_', sh.tag, '_L', lv.lvl),
  CONCAT(d.dept_name, lv.name, sh.name),
  6, 1, d.id,
  1, lv.lvl, lv.mode, sh.shift_id, 1,
  lv.lvl * 10 + sh.seq, 1, NOW(), NOW(), 0,
  CONCAT('sql/202 住院医师值班点位（', lv.name, lv.mode_name, '）')
FROM sys_department d
JOIN (SELECT 1 lvl, '一线' name, 3 mode, '驻守' mode_name
      UNION ALL SELECT 2, '二线', 2, '听班'
      UNION ALL SELECT 3, '三线', 2, '听班') lv
JOIN (SELECT 1 seq, 'DAY' tag, '白班' name, 896730000000000104 shift_id
      UNION ALL SELECT 2, 'NIGHT', '夜班', 896730000000000105) sh
WHERE d.del_flag = 0
  AND d.status = 1
  -- 有住院业务：这个科室挂着启用中的病区（口径见本段头部注释，sql/203 修正）
  AND EXISTS (SELECT 1 FROM sys_ward w WHERE w.dept_id = d.id AND w.status = 1)
  -- 有在职医生：status=0 的离职/停用员工不能撑起一个值班位
  AND EXISTS (SELECT 1 FROM sys_employee_post p JOIN sys_employee e ON e.id = p.employee_id
               WHERE p.dept_id = d.id AND e.emp_type = 1 AND e.status = 1 AND e.del_flag = 0)
  AND NOT EXISTS (SELECT 1 FROM biz_duty_post z
                   WHERE z.post_code = CONCAT('DOC_', d.id, '_', sh.tag, '_L', lv.lvl)
                     AND z.del_flag = 0);

-- -----------------------------------------------------------------------------
-- 5. 管床关系表
--    这是**带时效的归属关系**，不是按天的排班事实 —— 「张三主管 3-12 床」从入院生效到出院失效，
--    中间不会因为某天张三休息就消失。跟 biz_staff_schedule（一天一条事实）是完全两种东西。
--    唯一键 (admission_id, relation_type, employee_id) 不含 del_flag → 物理删。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_attending_relation (
  id               BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  admission_id     BIGINT       NOT NULL COMMENT '住院登记ID',
  patient_id       BIGINT       NOT NULL COMMENT '患者ID',
  patient_name     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '患者姓名',
  employee_id      BIGINT       NOT NULL COMMENT '主管医生ID',
  employee_name    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '主管医生姓名',
  dept_id          BIGINT       NOT NULL DEFAULT 0 COMMENT '科室ID',
  dept_name        VARCHAR(100) NOT NULL DEFAULT '' COMMENT '科室名称',
  ward_id          BIGINT       NOT NULL DEFAULT 0 COMMENT '病区ID',
  bed_id           BIGINT       NOT NULL DEFAULT 0 COMMENT '床位ID',
  relation_type    TINYINT      NOT NULL DEFAULT 1 COMMENT '关系类型（1-主管 2-主诊组长 3-协作）',
  status           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-有效 0-已结束）',
  effective_time   DATETIME     DEFAULT NULL COMMENT '生效时间（一般＝入院时间）',
  expire_time      DATETIME     DEFAULT NULL COMMENT '失效时间（出院或转交时填）',
  create_by        VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
  create_time      DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_by        VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
  update_time      DATETIME     DEFAULT NULL COMMENT '更新时间',
  del_flag         TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_adm_rel_emp (admission_id, relation_type, employee_id),
  KEY idx_relation_emp (employee_id, status),
  KEY idx_relation_patient (patient_id, status),
  KEY idx_relation_ward (ward_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住院管床关系（主管医生，带时效的归属）';

-- -----------------------------------------------------------------------------
-- 6. 存量回填：从 biz_admission.admit_doctor_id 生成主管关系
--    只回填「在院的（admit_status=1）且填了入院医生」的登记，已出院的不追溯
--    （历史数据里医生可能早已离职，回填出一百条失效关系没有价值）。
--
--    id 不能用 LPAD 拼：biz_admission.id 是 19 位雪花，拼出来必然被截断（LPAD 超长是截断不是原样返回）。
--    改用「当前最大值 + 行号递增」，既保证唯一，重跑时也不会撞已插入的行。
--
--    【脏引用必须过滤】biz_admission.admit_doctor_id 里有 5 条指向**不存在的员工**（实测）：
--      admission 5/6/7/8 → doctor 6/5/7/7，admission 8950000000000075201（微信演示患者）→ 8950000000000010001。
--      sys_employee 里根本没有 id=5/6/7（现存最小是 1，其余全是 19 位雪花），也没有那个演示医生 id。
--      这批是早期 seed/演示数据留下的悬挂引用。管床关系指向一个不存在的人，比「这条不回填」更糟 ——
--      页面会渲染出「主管医生：（空）」，出事了照样找不到人，还把脏数据固化进了新表。
--      所以回填时用 EXISTS 过滤掉，跳过的条数由 V16 留痕（不判失败，但每次跑都会报出来）。
-- -----------------------------------------------------------------------------
-- 6.1 自愈：把本文件此前回填过的、指向失效员工的行清掉（重跑幂等）
--     用多表 DELETE 而不是 NOT EXISTS 子查询 —— MySQL 不允许在子查询里 SELECT 正在被修改的那张表
--     （ER_UPDATE_TABLE_USED），LEFT JOIN + IS NULL 是等价且合法的写法。
DELETE r FROM biz_attending_relation r
LEFT JOIN sys_employee e ON e.id = r.employee_id
WHERE r.remark LIKE 'sql/202%' AND e.id IS NULL;

SET @rn := (SELECT COALESCE(MAX(id), 8967500000000000000) FROM biz_attending_relation);

INSERT INTO biz_attending_relation (id, admission_id, patient_id, patient_name,
                                    employee_id, employee_name, dept_id, dept_name,
                                    ward_id, bed_id, relation_type, status,
                                    effective_time, create_time, update_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  a.admission_id, a.patient_id, COALESCE(pt.patient_name, ''),
  a.admit_doctor_id, COALESCE(e.emp_name, ''),
  COALESCE(a.dept_id, 0), COALESCE(d.dept_name, ''),
  COALESCE(a.ward_id, 0), COALESCE(a.bed_id, 0),
  1, 1,
  a.admit_time, NOW(), NOW(), 0,
  'sql/202 存量回填（源自 biz_admission.admit_doctor_id）'
FROM biz_admission a
LEFT JOIN biz_patient pt ON pt.id = a.patient_id
LEFT JOIN sys_employee e ON e.id = a.admit_doctor_id
LEFT JOIN sys_department d ON d.id = a.dept_id
WHERE a.admit_status = 1
  AND a.admit_doctor_id IS NOT NULL
  AND a.admit_doctor_id <> 0
  AND EXISTS (SELECT 1 FROM sys_employee e2 WHERE e2.id = a.admit_doctor_id)   -- 过滤悬挂引用，见 6.0 说明
  AND NOT EXISTS (SELECT 1 FROM biz_attending_relation r
                   WHERE r.admission_id = a.admission_id AND r.relation_type = 1
                     AND r.employee_id = a.admit_doctor_id);

-- -----------------------------------------------------------------------------
-- 7. 字典同步（Java 枚举是权威，这里把 sys_dict_type / sys_dict_data 跟上）
--    his_duty_scope 加 6-临床科室：不加的话前端下拉里根本没有这一项，
--    医师值班点位在页面上会显示成「未知(6)」。
--    his_duty_level 是新增的一维字典（0-不适用 1-一线 2-二线 3-三线），
--    0-不适用必须一起铺：行政总值班那些老点位 duty_level 就是 0，字典缺 0 同样显示成未知。
-- -----------------------------------------------------------------------------
INSERT INTO sys_dict_type (id, dict_type, dict_name, status, remark, create_by, create_time, del_flag)
SELECT x.id, x.dict_type, x.dict_name, 1, x.remark, 'sql202', NOW(), 0
FROM (
  SELECT 896710000000000107 id, 'his_duty_level' dict_type, '值班层级' dict_name,
         '0-不适用 1-一线 2-二线 3-三线' remark
) x
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type t WHERE t.dict_type = x.dict_type AND t.del_flag = 0);

-- 已存在的字典组：把新码位补进说明里（INSERT 走的是 NOT EXISTS，不会改旧行，只能显式 UPDATE）
UPDATE sys_dict_type
   SET remark = '1-全院行政 2-急诊 3-感染 4-总务 5-信息 6-临床科室',
       update_by = 'sql202', update_time = NOW()
 WHERE dict_type = 'his_duty_scope' AND del_flag = 0;

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, status, create_by,
                           create_time, del_flag, remark, dict_source)
SELECT x.id, x.dict_type, x.dict_label, x.dict_value, x.dict_sort, 1, 'sql202', NOW(), 0, x.remark, 1
FROM (
            SELECT 896720000000000166 id, 'his_duty_scope' dict_type, '临床科室' dict_label, '6' dict_value, 6 dict_sort,
         'biz_duty_post.duty_scope' remark
  UNION ALL SELECT 896720000000000171, 'his_duty_level', '不适用', '0', 0, 'biz_duty_post.duty_level'
  UNION ALL SELECT 896720000000000172, 'his_duty_level', '一线',   '1', 1, 'biz_duty_post.duty_level'
  UNION ALL SELECT 896720000000000173, 'his_duty_level', '二线',   '2', 2, 'biz_duty_post.duty_level'
  UNION ALL SELECT 896720000000000174, 'his_duty_level', '三线',   '3', 3, 'biz_duty_post.duty_level'
) x
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data d
                   WHERE d.dict_type = x.dict_type AND d.dict_value = x.dict_value AND d.del_flag = 0);

-- -----------------------------------------------------------------------------
-- 8. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'V1_医师值班班次2条' item, COUNT(*) cnt, 2 expect FROM biz_shift
 WHERE del_flag = 0 AND id IN (896730000000000104, 896730000000000105)
UNION ALL SELECT 'V2_临床点位已铺开', COUNT(*), '>0' FROM biz_duty_post
 WHERE del_flag = 0 AND duty_scope = 6
UNION ALL SELECT 'V3_点位编码无重复', COUNT(*), 0 FROM (
   SELECT post_code FROM biz_duty_post WHERE del_flag = 0 GROUP BY post_code HAVING COUNT(*) > 1) x
UNION ALL SELECT 'V4_点位都绑了有效班次', COUNT(*), 0 FROM biz_duty_post p
 WHERE p.del_flag = 0 AND NOT EXISTS (SELECT 1 FROM biz_shift s WHERE s.id = p.shift_id AND s.del_flag = 0)
UNION ALL SELECT 'V5_层级合法(0-3)', COUNT(*), 0 FROM biz_duty_post
 WHERE del_flag = 0 AND duty_level NOT BETWEEN 0 AND 3
UNION ALL SELECT 'V6_医师点位层级非零', COUNT(*), 0 FROM biz_duty_post
 WHERE del_flag = 0 AND duty_scope = 6 AND duty_level = 0
UNION ALL SELECT 'V7_医师点位限定医生', COUNT(*), 0 FROM biz_duty_post
 WHERE del_flag = 0 AND duty_scope = 6 AND required_staff_type <> 1
UNION ALL SELECT 'V8_旧唯一键已解锁', COUNT(*), 0 FROM information_schema.STATISTICS
 WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster' AND INDEX_NAME = 'uk_duty_date_shift_role'
-- 这里必须判「索引存在」而不是「索引行数=1」：uk_duty_post_date 是 (post_id, duty_date) 复合索引，
-- 在 information_schema.STATISTICS 里一列一行，占 2 行。直接 COUNT(*) 拿到的永远是 2，断言写 1 就是自己给自己挖坑。
UNION ALL SELECT 'V9_点位唯一键还在', COUNT(DISTINCT INDEX_NAME), 1 FROM information_schema.STATISTICS
 WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster' AND INDEX_NAME = 'uk_duty_post_date'
UNION ALL SELECT 'V10_管床表已建', COUNT(*), 1 FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_attending_relation'
UNION ALL SELECT 'V11_管床无重复主管', COUNT(*), 0 FROM (
   SELECT admission_id FROM biz_attending_relation
    WHERE del_flag = 0 AND relation_type = 1 GROUP BY admission_id HAVING COUNT(*) > 1) y
UNION ALL SELECT 'V12_管床医生ID有效', COUNT(*), 0 FROM biz_attending_relation r
 WHERE r.del_flag = 0 AND NOT EXISTS (SELECT 1 FROM sys_employee e WHERE e.id = r.employee_id)
-- 期望侧必须跟回填侧用同一把尺子（都过滤悬挂引用），否则两边差 5 条，看起来像回填漏了。
UNION ALL SELECT 'V13_存量回填数对得上',
  (SELECT COUNT(*) FROM biz_attending_relation WHERE del_flag = 0 AND remark LIKE 'sql/202%'),
  (SELECT COUNT(*) FROM biz_admission a WHERE a.admit_status = 1 AND a.admit_doctor_id IS NOT NULL
     AND a.admit_doctor_id <> 0 AND EXISTS (SELECT 1 FROM sys_employee e WHERE e.id = a.admit_doctor_id))
UNION ALL SELECT 'V14_空白点位（无医生科室）', COUNT(*), 0 FROM biz_duty_post p
 WHERE p.del_flag = 0 AND p.duty_scope = 6
   AND NOT EXISTS (SELECT 1 FROM sys_employee_post ep JOIN sys_employee e ON e.id = ep.employee_id
                    WHERE ep.dept_id = p.org_id AND e.emp_type = 1)
-- 留痕项：被过滤掉的悬挂引用条数。expect 写 '*' 表示只报数不判定（verify-sql.mjs 已支持），
-- 目的是让「源数据有多脏」每次跑都看得见，而不是藏在 PASS 后面。
UNION ALL SELECT 'V16_脏引用跳过数(留痕)', COUNT(*), '*' FROM biz_admission a
 WHERE a.admit_status = 1 AND a.admit_doctor_id IS NOT NULL AND a.admit_doctor_id <> 0
   AND NOT EXISTS (SELECT 1 FROM sys_employee e WHERE e.id = a.admit_doctor_id)
UNION ALL SELECT 'V17_层级字典4项', COUNT(*), 4 FROM sys_dict_data
 WHERE dict_type = 'his_duty_level' AND del_flag = 0
UNION ALL SELECT 'V18_责任范围含临床科室', COUNT(*), 1 FROM sys_dict_data
 WHERE dict_type = 'his_duty_scope' AND dict_value = '6' AND del_flag = 0
-- 最后一项刻意写成不带子查询的简单查询：verify-sql.mjs 切自检段时会把结尾的 `);` 一并剥掉，
-- 若最后一项以 `)` 收尾（子查询/NOT EXISTS 的闭括号会被当成语句结尾吃掉），剥完就语法残缺。
UNION ALL SELECT 'V15_医师点位可用', COUNT(*), '>0' FROM biz_duty_post
 WHERE del_flag = 0 AND duty_scope = 6 AND status = 1 AND shift_id > 0;
