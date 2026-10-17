-- 265-码值口径统一：启用停用/正常异常/通过不通过/错误级别收敛到唯一枚举
-- 背景：同一含义全仓只允许一个枚举（AGENTS 第 13 节）。本次把四组重复枚举收敛后，
--       列注释与字典行必须跟着枚举码值走，否则「Java 权威」与库内注释漂移。
-- 数据：实测 dev 库这四列都不存在需要刷新的旧码值行（停用=2、异常=2、不通过=2 命中 0 行），
--       所以本文件只改元数据与字典，不刷业务数据；若在已有 2 档的库上执行，必须先跑文末的 UPDATE。

ALTER TABLE biz_survey_template
    MODIFY COLUMN status tinyint NOT NULL COMMENT '状态（1-启用 0-停用）';

ALTER TABLE biz_equipment_maintain
    MODIFY COLUMN maintain_result tinyint NOT NULL COMMENT '维保结果（0-异常 1-正常）';

ALTER TABLE biz_day_surgery_apply
    MODIFY COLUMN eval_result tinyint COMMENT '术前评估结论（0-不通过 1-通过）';

ALTER TABLE biz_clinical_rule_check
    MODIFY COLUMN error_level tinyint COMMENT '错误级别（1-提示 2-警告 3-严重）';

-- 字典行跟随枚举码值（同码同义）
UPDATE sys_dict_data SET dict_value = '0' WHERE id = '895000000000000182' AND dict_type = 'his_survey_tpl_status';
UPDATE sys_dict_data SET dict_value = '0' WHERE id = '892000000000000552' AND dict_type = 'his_day_surgery_eval_result';
UPDATE sys_dict_data SET dict_label = '提示' WHERE id = '891000000000000598' AND dict_type = 'his_rule_error_level';
UPDATE sys_dict_data SET dict_label = '警告' WHERE id = '891000000000000599' AND dict_type = 'his_rule_error_level';

-- 在已有旧码值的库上执行本文件前，先按下列语句刷数据（dev 库实测各 0 行）：
-- UPDATE biz_survey_template      SET status = 0 WHERE status = 2;
-- UPDATE biz_equipment_maintain   SET maintain_result = 0 WHERE maintain_result = 2;
-- UPDATE biz_day_surgery_apply    SET eval_result = 0 WHERE eval_result = 2;
