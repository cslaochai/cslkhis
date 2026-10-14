-- =====================================================================
-- 261 门诊排班变更留痕表
-- ---------------------------------------------------------------------
-- 背景（来自排班重构核对，20261014）：
--   门诊出诊排班（biz_clinic_source）的停诊/启用/加号/批量退号/段级编辑
--   此前只有 remark 文本拼接（加号）或完全无痕（停诊/退号/段编辑/删除/复刻），
--   而 biz_clinic_source_change_log 实为岗位排班（换班/代班）的留痕表，
--   字段是 employee/shift 维度，无法承载号源维度的变更审计。
--   本次新建独立的号源变更日志：谁、何时、对哪个班次、做了什么动作、
--   涉及多少号源、为什么 —— 停诊纠纷、加号对账、排班运营分析都靠它。
-- 范围：
--   - 只增不改：不触碰既有排班表与岗位排班留痕表；
--   - 日志只写不删（del_flag 恒为 0，按审计习惯保留列）；
--   - 动作类型字典（his_clinic_source_change_type）与 Java 枚举
--     ClinicSourceChangeTypeEnum 一一对应。
-- =====================================================================

-- 1. 留痕表
CREATE TABLE IF NOT EXISTS biz_clinic_source_log (
    id           BIGINT       NOT NULL COMMENT '主键',
    schedule_id  BIGINT       NOT NULL COMMENT '门诊排班ID',
    action_type  TINYINT      NOT NULL COMMENT '变更动作（1-新增排班 2-修改排班 3-删除排班 4-停诊 5-启用 6-加号 7-批量退号 8-段级编辑 9-复刻）',
    amount       INT          NULL     COMMENT '变更数量（加号数/退号数/批量或复刻条数）',
    reason       VARCHAR(200) NULL     COMMENT '变更原因',
    occur_time   DATETIME     NOT NULL COMMENT '发生时间',
    create_by    VARCHAR(64)  NULL     COMMENT '创建人',
    create_time  DATETIME     NULL     COMMENT '创建时间',
    update_by    VARCHAR(64)  NULL     COMMENT '更新人',
    update_time  DATETIME     NULL     COMMENT '更新时间',
    del_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    remark       VARCHAR(500) NULL     COMMENT '备注',
    create_by_id BIGINT       NULL     COMMENT '创建人ID',
    update_by_id BIGINT       NULL     COMMENT '更新人ID',
    PRIMARY KEY (id),
    KEY idx_schedule_id (schedule_id),
    KEY idx_occur_time (occur_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '门诊排班变更日志';

-- 2. 动作类型字典（与 ClinicSourceChangeTypeEnum 码值一一对应）
INSERT INTO sys_dict_type (id, dict_type, dict_name, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000110, 'his_clinic_source_change_type', '门诊排班变更类型', 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'his_clinic_source_change_type');

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000411, 'his_clinic_source_change_type', '新增排班', '1', 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000412, 'his_clinic_source_change_type', '修改排班', '2', 2, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '2');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000413, 'his_clinic_source_change_type', '删除排班', '3', 3, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '3');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000414, 'his_clinic_source_change_type', '停诊', '4', 4, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '4');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000415, 'his_clinic_source_change_type', '启用', '5', 5, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '5');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000416, 'his_clinic_source_change_type', '加号', '6', 6, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '6');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000417, 'his_clinic_source_change_type', '批量退号', '7', 7, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '7');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000418, 'his_clinic_source_change_type', '段级编辑', '8', 8, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '8');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000000419, 'his_clinic_source_change_type', '复刻', '9', 9, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' AND dict_value = '9');

-- 3. 回查确认（跑完人工核对：类型 1 条、数据 9 条）
-- SELECT id, dict_type, dict_name FROM sys_dict_type WHERE dict_type = 'his_clinic_source_change_type';
-- SELECT dict_value, dict_label FROM sys_dict_data WHERE dict_type = 'his_clinic_source_change_type' ORDER BY dict_sort;
