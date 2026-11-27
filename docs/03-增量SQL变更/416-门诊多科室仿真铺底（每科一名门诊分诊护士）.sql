-- 416-门诊多科室仿真铺底（每科一名门诊分诊护士）
-- 背景：签到/分诊接口要求 opd:triage:add/edit，该权限只授给「门诊分诊护士(10020)」；
--       10020 是 data_scope=3（本科室）角色，跨科室分诊会被 DeptScopeService 拒。
--       测试库原来只有发热门诊/肠道门诊/心血管内科三个分诊护士，仿真因此只能开一个科室。
-- 做法：给每个开诊科室的一名在职护士追加一条 10020 岗位（不动原护士岗位，is_primary=0）。
-- 幂等：同员工同角色同科室已存在则跳过；ID 段 8870... 为仿真预留段。
-- 回滚：416-回滚.sql
-- 校验：SELECT COUNT(DISTINCT dept_id) FROM sys_employee_post WHERE role_id=9 AND create_by='sql416';

INSERT INTO sys_employee_post
  (id, employee_id, role_id, dept_id, is_primary, effective_date, create_by, create_time,
   update_by, update_time, create_by_id, update_by_id)
SELECT v.id, v.emp_id, 9, v.dept_id, 0, CURDATE(), 'sql416', NOW(), 'sql416', NOW(), 1, 1
FROM (
  SELECT 8870000000000000001 AS id, 897001006 AS emp_id, 19580003 AS dept_id, '呼吸内科(杨琴云)' AS note
UNION ALL
  SELECT 8870000000000000002 AS id, 897001076 AS emp_id, 19580013 AS dept_id, '普通外科(秦静娜)' AS note
UNION ALL
  SELECT 8870000000000000003 AS id, 897001146 AS emp_id, 19580022 AS dept_id, '妇科(王文晴)' AS note
UNION ALL
  SELECT 8870000000000000004 AS id, 897001174 AS emp_id, 19580026 AS dept_id, '小儿内科(温梦霞)' AS note
UNION ALL
  SELECT 8870000000000000005 AS id, 897001202 AS emp_id, 19580030 AS dept_id, '眼科(莫梅楠)' AS note
UNION ALL
  SELECT 8870000000000000006 AS id, 897001237 AS emp_id, 19580053 AS dept_id, '中医内科(马亚云)' AS note
UNION ALL
  SELECT 8870000000000000007 AS id, 897001293 AS emp_id, 19580064 AS dept_id, '精神科门诊(龙珍)' AS note
UNION ALL
  SELECT 8870000000000000008 AS id, 897001013 AS emp_id, 19580004 AS dept_id, '消化内科(江静晶)' AS note
UNION ALL
  SELECT 8870000000000000009 AS id, 897001083 AS emp_id, 19580014 AS dept_id, '骨科(董静娟)' AS note
UNION ALL
  SELECT 8870000000000000010 AS id, 897001153 AS emp_id, 19580023 AS dept_id, '产科(曾晶婷)' AS note
UNION ALL
  SELECT 8870000000000000011 AS id, 897001181 AS emp_id, 19580027 AS dept_id, '小儿外科(邱晓蓉)' AS note
UNION ALL
  SELECT 8870000000000000012 AS id, 897001210 AS emp_id, 19580031 AS dept_id, '耳鼻喉科(杨芳媛)' AS note
UNION ALL
  SELECT 8870000000000000013 AS id, 897001244 AS emp_id, 19580054 AS dept_id, '中医骨伤科(卢丹薇)' AS note
UNION ALL
  SELECT 8870000000000000014 AS id, 897001300 AS emp_id, 19580065 AS dept_id, '临床心理科(夏晓锋)' AS note
UNION ALL
  SELECT 8870000000000000015 AS id, 897001020 AS emp_id, 19580005 AS dept_id, '心血管内科(康琴丹)' AS note
UNION ALL
  SELECT 8870000000000000016 AS id, 897001090 AS emp_id, 19580015 AS dept_id, '神经外科(沈慧丹)' AS note
UNION ALL
  SELECT 8870000000000000017 AS id, 897001160 AS emp_id, 19580024 AS dept_id, '生殖医学中心(黄洁楠)' AS note
UNION ALL
  SELECT 8870000000000000018 AS id, 897001188 AS emp_id, 19580028 AS dept_id, '新生儿科(万雨蓉)' AS note
UNION ALL
  SELECT 8870000000000000019 AS id, 897001216 AS emp_id, 19580032 AS dept_id, '口腔科(潘雅兰)' AS note
UNION ALL
  SELECT 8870000000000000020 AS id, 897001251 AS emp_id, 19580055 AS dept_id, '针灸推拿科(苏芷)' AS note
UNION ALL
  SELECT 8870000000000000021 AS id, 897001307 AS emp_id, 19580066 AS dept_id, '睡眠医学中心(董媛妮)' AS note
UNION ALL
  SELECT 8870000000000000022 AS id, 897001314 AS emp_id, 19580070 AS dept_id, '肝病科(马倩琴)' AS note
UNION ALL
  SELECT 8870000000000000023 AS id, 897001027 AS emp_id, 19580006 AS dept_id, '神经内科(金颖薇)' AS note
UNION ALL
  SELECT 8870000000000000024 AS id, 897001097 AS emp_id, 19580016 AS dept_id, '心胸外科(许睿阳)' AS note
UNION ALL
  SELECT 8870000000000000025 AS id, 897001223 AS emp_id, 19580033 AS dept_id, '皮肤科(石云)' AS note
UNION ALL
  SELECT 8870000000000000026 AS id, 897001258 AS emp_id, 19580056 AS dept_id, '中医妇科(容丹晴)' AS note
UNION ALL
  SELECT 8870000000000000027 AS id, 897001167 AS emp_id, 19580089 AS dept_id, '产前诊断中心(尹德然)' AS note
UNION ALL
  SELECT 8870000000000000028 AS id, 897001195 AS emp_id, 19580090 AS dept_id, '儿童保健科(崔晓琳)' AS note
UNION ALL
  SELECT 8870000000000000029 AS id, 897001034 AS emp_id, 19580007 AS dept_id, '内分泌科(罗晶玉)' AS note
UNION ALL
  SELECT 8870000000000000030 AS id, 897001104 AS emp_id, 19580017 AS dept_id, '泌尿外科(杨敏妮)' AS note
UNION ALL
  SELECT 8870000000000000031 AS id, 897001265 AS emp_id, 19580057 AS dept_id, '中医儿科(王燕娜)' AS note
UNION ALL
  SELECT 8870000000000000032 AS id, 897001230 AS emp_id, 19580091 AS dept_id, '医疗美容科(郭颖艳)' AS note
UNION ALL
  SELECT 8870000000000000033 AS id, 897001041 AS emp_id, 19580008 AS dept_id, '肾内科(沈晓梅)' AS note
UNION ALL
  SELECT 8870000000000000034 AS id, 897001111 AS emp_id, 19580018 AS dept_id, '烧伤整形外科(彭军)' AS note
UNION ALL
  SELECT 8870000000000000035 AS id, 897001048 AS emp_id, 19580009 AS dept_id, '血液内科(董萍玉)' AS note
UNION ALL
  SELECT 8870000000000000036 AS id, 897001118 AS emp_id, 19580019 AS dept_id, '血管外科(胡铭文)' AS note
UNION ALL
  SELECT 8870000000000000037 AS id, 897001055 AS emp_id, 19580010 AS dept_id, '风湿免疫科(莫蕾妮)' AS note
UNION ALL
  SELECT 8870000000000000038 AS id, 897001125 AS emp_id, 19580020 AS dept_id, '肝胆外科(贺梦怡)' AS note
UNION ALL
  SELECT 8870000000000000039 AS id, 897001062 AS emp_id, 19580011 AS dept_id, '老年病科(于燕婷)' AS note
UNION ALL
  SELECT 8870000000000000040 AS id, 897001132 AS emp_id, 19580087 AS dept_id, '甲乳外科(邓颖琪)' AS note
UNION ALL
  SELECT 8870000000000000041 AS id, 897001069 AS emp_id, 19580086 AS dept_id, '全科医学科(贺红颖)' AS note
UNION ALL
  SELECT 8870000000000000042 AS id, 897001139 AS emp_id, 19580088 AS dept_id, '肛肠外科(杜玉霞)' AS note
) v
WHERE NOT EXISTS (
  SELECT 1 FROM sys_employee_post p
   WHERE p.employee_id = v.emp_id AND p.role_id = 9 AND p.dept_id = v.dept_id
);
