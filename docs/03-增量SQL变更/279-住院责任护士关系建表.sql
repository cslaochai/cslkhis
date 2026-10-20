-- =============================================================
-- 279-住院责任护士关系（补「在院患者由哪位护士负责」这一护理线归属）
-- 背景：
--   主管医生管床走 biz_attending_relation（sql/202），但那套硬编码「必须是医生」，
--   relation_type 表达医生团队分工（主管/主诊组长/协作），塞护士会破坏语义与校验。
--   在院患者此前没有固定责任护士归属（只有单次输液、分诊记录里的执行护士）。
--   本增量另建一张责任护士关系表，与主管医生并列，互不污染。
-- 口径：
--   · 一次住院一名有效主责护士；重新指派（转交）时旧记录置失效（status=0 + expire_time）留痕，不删行；
--   · 仅护理岗位（sys_employee.emp_type=2）可被指派，后端校验；
--   · 已出院不再改动历史归属（护理记录凭证）；
--   · 复用住院权限码 ipd:inpatient:list（读）/ ipd:inpatient:edit（写），不另开菜单。
-- 唯一键 uk_adm_nurse(admission_id, relation_type, nurse_id) 不含 del_flag
--   → 误建记录物理删（软删行会占住键位，重新指派必撞重复键）；结束一律置失效。
-- 幂等：建表 IF NOT EXISTS，可重复执行。
-- =============================================================

CREATE TABLE IF NOT EXISTS `biz_charge_nurse_relation` (
    `id`            bigint      NOT NULL COMMENT '主键ID（雪花）',
    `admission_id`  bigint      NOT NULL COMMENT '住院登记ID（biz_admission.admission_id）',
    `patient_id`    bigint      NOT NULL COMMENT '患者ID（biz_patient.id，快照冗余便于反查）',
    `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
    `nurse_id`      bigint      NOT NULL COMMENT '责任护士ID（sys_employee.id，emp_type=2 护理）',
    `nurse_name`    varchar(50) NOT NULL COMMENT '责任护士姓名（快照）',
    `dept_id`       bigint      NOT NULL COMMENT '归属科室ID（快照）',
    `dept_name`     varchar(100) NOT NULL COMMENT '归属科室名称（快照）',
    `ward_id`       bigint      NOT NULL DEFAULT 0 COMMENT '病区ID（快照，0=未记）',
    `bed_id`        bigint      NOT NULL DEFAULT 0 COMMENT '床位ID（快照，0=未记，便于按床反查）',
    `relation_type` tinyint     NOT NULL DEFAULT 1 COMMENT '关系类型（1-责任护士，预留协助护士扩展）',
    `status`        tinyint     NOT NULL DEFAULT 1 COMMENT '状态（1-有效 0-已结束）',
    `effective_time` datetime   NULL COMMENT '生效时间（一般＝入院或交接时刻）',
    `expire_time`   datetime    NULL COMMENT '失效时间（出院或转交时填）',
    `create_by`     varchar(64) NOT NULL DEFAULT '' COMMENT '创建人姓名',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64) NOT NULL DEFAULT '' COMMENT '更新人姓名',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by_id`  bigint      NOT NULL DEFAULT 0 COMMENT '创建人ID',
    `update_by_id`  bigint      NOT NULL DEFAULT 0 COMMENT '更新人ID',
    `del_flag`      tinyint     NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    `remark`        varchar(500) NULL COMMENT '备注（交接说明）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_adm_nurse` (`admission_id`, `relation_type`, `nurse_id`),
    KEY `idx_nurse_status` (`nurse_id`, `status`),
    KEY `idx_patient_status` (`patient_id`, `status`),
    KEY `idx_ward_status` (`ward_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院责任护士关系（与主管医生管床并列的护理线归属）';
