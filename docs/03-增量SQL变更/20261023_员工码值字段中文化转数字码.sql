-- =====================================================================
-- 20261023 员工码值字段「中文 → 数字码」迁移
-- ---------------------------------------------------------------------
-- 背景：his_education 字典已改为数字码（1博士 2硕士 3本科 4大专 5中专），
--   EmployeeUpsertDTO / SysUserUpsertDTO 已加 @AllowedValues 强制数字码，
--   前端下拉也只传 dictValue。但存量数据与种子 SQL 里 education 仍是中文，
--   另有少量 position / title 中文残留（CS001/CS002 的「客服专员」、
--   YW001 谭一鸣的「医务科管理岗」「副主任科员」），且 CS001/CS002 的
--   gender=0 已非法（现口径 1男2女9未知）。本脚本一次性校准。
--
-- 幂等：字典项用 INSERT IGNORE + 固定 id；数据转换用 CASE / JOIN 只改
--   「含非数字」的行，已合规的行不动；可重复执行零变化。
-- ⚠️ 执行后必须清 Redis 字典缓存（sys:dict:* 或重启后端），否则前端 10 分钟内
--   仍按旧字典翻译、且新码值取不到。
-- =====================================================================

-- 一、补齐缺失的字典项（客服专员 / 医务科管理岗 / 副主任科员）
INSERT IGNORE INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                                 create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('145', 'hospital_position', '客服专员', '46', 85, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT IGNORE INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                                 create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('146', 'hospital_position', '医务科管理岗', '47', 86, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT IGNORE INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                                 create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10137', 'sys_hospital_title', '副主任科员', '517', 46, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');

-- 二、education 中文 → 数字码（his_education：1博士 2硕士 3本科 4大专 5中专）
UPDATE sys_employee
SET education = CASE education
    WHEN '博士研究生' THEN '1'
    WHEN '硕士研究生' THEN '2'
    WHEN '博士' THEN '1'
    WHEN '硕士' THEN '2'
    WHEN '本科' THEN '3'
    WHEN '大专' THEN '4'
    WHEN '中专' THEN '5'
    ELSE education
END
WHERE del_flag = 0 AND education IS NOT NULL AND education REGEXP '[^0-9]';

-- 三、position / title 中文 → 数字码（按字典 label 映射，新字典项由第一节补齐）
UPDATE sys_employee e
JOIN sys_dict_data d ON d.dict_label = e.position AND d.dict_type = 'hospital_position' AND d.del_flag = 0
SET e.position = d.dict_value
WHERE e.del_flag = 0 AND e.position IS NOT NULL AND e.position REGEXP '[^0-9]';

UPDATE sys_employee e
JOIN sys_dict_data d ON d.dict_label = e.title AND d.dict_type = 'sys_hospital_title' AND d.del_flag = 0
SET e.title = d.dict_value
WHERE e.del_flag = 0 AND e.title IS NOT NULL AND e.title REGEXP '[^0-9]';

-- 四、gender=0 已非法（现口径 1男2女9未知），CS001/CS002 等历史遗留统一置 9（未知）
UPDATE sys_employee SET gender = 9 WHERE del_flag = 0 AND gender = 0;

-- 五、回查（执行后核对，应均为 0 行）
-- SELECT education, COUNT(*) c FROM sys_employee WHERE del_flag=0 AND education REGEXP '[^0-9]' GROUP BY education;
-- SELECT position, COUNT(*) c FROM sys_employee WHERE del_flag=0 AND position REGEXP '[^0-9]' GROUP BY position;
-- SELECT title, COUNT(*) c FROM sys_employee WHERE del_flag=0 AND title REGEXP '[^0-9]' GROUP BY title;
-- SELECT gender, COUNT(*) c FROM sys_employee WHERE del_flag=0 AND gender NOT IN (1,2,9) GROUP BY gender;
