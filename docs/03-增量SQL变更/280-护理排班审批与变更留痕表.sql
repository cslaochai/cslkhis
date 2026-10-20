-- =====================================================================
-- 280 护理排班审批与变更留痕表
-- ---------------------------------------------------------------------
-- 背景：护理周排班（biz_schedule，staff_type IN (2,7)）此前只有同行 publishStatus
--       状态流转（草稿/待审核/已发布/调班中/已变更），提交/审核/撤回/调班/改格均不留痕，
--       审批谁做的、改了什么、为什么无从回看。参照门诊 biz_clinic_source_log 的轻量做法：
--       不拆物理草案表（标准 HIS 护理排班均为单表+状态位+留痕），只补一张只写不删的审计日志。
-- 范围：
--   - 只增不改，不触碰 biz_schedule 与在岗/工时口径；
--   - 日志只写不删（del_flag 恒为 0）；
--   - 冗余快照列：排班随后被删/改，审计仍能读出「谁、哪天、哪个单元、哪位护士、改前改后」；
--   - 动作字典 his_nurse_schedule_change_type 与 Java 枚举 NurseScheduleChangeTypeEnum 一一对应。
-- 幂等：表与字典均 IF NOT EXISTS / NOT EXISTS，可重复执行。
-- =====================================================================

-- 1. 留痕表
CREATE TABLE IF NOT EXISTS biz_nurse_schedule_log (
    id             BIGINT       NOT NULL COMMENT '主键',
    schedule_id    BIGINT       NULL     COMMENT '排班行ID（整周提交/发布/复制等无单行的动作可为空）',
    action_type    TINYINT      NOT NULL COMMENT '动作（1-新增排班 2-修改/调班 3-删除排班 4-提交审核 5-审核发布 6-撤回 7-进入调班 8-整周提交 9-整周发布 10-复制上周）',
    amount         INT          NULL     COMMENT '变更数量（批量铺/复制/整周发布条数）',
    reason         VARCHAR(200) NULL     COMMENT '变更/审批原因或意见',
    occur_time     DATETIME     NOT NULL COMMENT '发生时间',
    -- 冗余快照：排班被删/改后审计仍可读
    schedule_date  DATE         NULL     COMMENT '排班日期快照',
    org_name       VARCHAR(64)  NULL     COMMENT '排班单元名称快照（病区/门诊科室）',
    dept_name      VARCHAR(64)  NULL     COMMENT '科室名称快照',
    nurse_name     VARCHAR(64)  NULL     COMMENT '护士姓名快照',
    from_snapshot VARCHAR(200) NULL     COMMENT '变更前快照（班次/状态文本，调班/改格用）',
    to_snapshot    VARCHAR(200) NULL     COMMENT '变更后快照（班次/状态文本）',
    -- BaseEntity 审计列（MetaObjectHandler 自动填充）
    create_by      VARCHAR(64)  NULL     COMMENT '创建人',
    create_time    DATETIME     NULL     COMMENT '创建时间',
    update_by      VARCHAR(64)  NULL     COMMENT '更新人',
    update_time    DATETIME     NULL     COMMENT '更新时间',
    create_by_id   BIGINT       NULL     COMMENT '创建人ID',
    update_by_id   BIGINT       NULL     COMMENT '更新人ID',
    del_flag       TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除；审计只写不删）',
    remark         VARCHAR(500) NULL     COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_schedule_id (schedule_id),
    KEY idx_occur_time (occur_time),
    KEY idx_dept_date (dept_name, schedule_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '护理排班审批与变更留痕';

-- 2. 动作类型字典（与 NurseScheduleChangeTypeEnum 码值一一对应）
INSERT INTO sys_dict_type (id, dict_type, dict_name, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000002110, 'his_nurse_schedule_change_type', '护理排班变更类型', 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'his_nurse_schedule_change_type');

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021101, 'his_nurse_schedule_change_type', '新增排班', '1', 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021102, 'his_nurse_schedule_change_type', '修改/调班', '2', 2, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '2');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021103, 'his_nurse_schedule_change_type', '删除排班', '3', 3, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '3');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021104, 'his_nurse_schedule_change_type', '提交审核', '4', 4, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '4');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021105, 'his_nurse_schedule_change_type', '审核发布', '5', 5, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '5');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021106, 'his_nurse_schedule_change_type', '撤回', '6', 6, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '6');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021107, 'his_nurse_schedule_change_type', '进入调班', '7', 7, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '7');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021108, 'his_nurse_schedule_change_type', '整周提交', '8', 8, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '8');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021109, 'his_nurse_schedule_change_type', '整周发布', '9', 9, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '9');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, is_default, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id, dict_source)
SELECT 896710000000021110, 'his_nurse_schedule_change_type', '复制上周', '10', 10, 0, 1, 'admin', NOW(), 'admin', NOW(), 1, 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'his_nurse_schedule_change_type' AND dict_value = '10');

-- 3. 回查确认
-- SHOW COLUMNS FROM biz_nurse_schedule_log;
-- SELECT dict_value, dict_label FROM sys_dict_data WHERE dict_type='his_nurse_schedule_change_type' ORDER BY dict_sort;
