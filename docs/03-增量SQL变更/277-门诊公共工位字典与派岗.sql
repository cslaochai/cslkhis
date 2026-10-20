-- =============================================================
-- 277-门诊公共工位字典 + 工位派岗（补「护士今天去哪个门诊工位」这一小层）
-- 背景：
--   现有门诊排班(biz_clinic_source)只排出诊医生与号源；岗位出勤(biz_schedule)是纯考勤底座，
--   都不回答「A 护士今天被排到总导诊/分诊台/输液室/注射室/采血室哪个位」。本增量补两层：
--     1) biz_opd_station        门诊公共工位字典（位先定义存在，全院公共、不挂具体科室）；
--     2) biz_opd_station_assignment 工位派岗记录（排班员指定某人某天某工位某班，含跨单元借调标记）。
-- 口径（与需求确认一致）：
--   · 独立派岗表，不并入 biz_schedule 出勤底座，不进工时/缺勤闭环；
--   · 仅全院公共工位（总导诊/门诊分诊台/输液室/注射室/采血室），不做专科科室分诊台；
--   · 仅排班员指定派岗，不做员工自助认领待岗池。
-- 为什么不并入现有「值守点位(biz_duty_post/biz_duty_roster)」（行业做法核对）：
--   · 值守点位装的是夜间/应急兜底：全院行政/急诊/感染/总务/信息总值班 + 临床一线/二线/三线白夜班，
--     带发布审核流与替班，本质是责任兜底与升级链；门诊公共位是白天常规服务岗（弹性白班、高峰增配），
--     二者是两条线，混在一张值班表会叫错人。
--   · 真实医院门诊护理（预检分诊台、导诊台、采血室、注射室、治疗室、输液）由门诊护士长在护理部/门诊办
--     双重领导下统筹，按「岗位（导医岗/分诊岗/输液岗/注射岗/采血岗）+ 按天按岗定员派人」登记，
--     与病区护理周矩阵、行政总值班分别管理；跨科顶岗走机动护士调配（本表用 is_loan+source_org 轻量记录来源）。
-- 唯一键均不含 del_flag → 派岗记录物理删（软删行会占住「同日同工位同班同人」唯一键，撤销后重派必撞键）。
-- 幂等：建表用 IF NOT EXISTS；铺底工位按编码判重；菜单/授权按 id/permission/role+menu 判重，可重复执行。
-- =============================================================

-- -------------------------------------------------------------
-- 一、门诊公共工位字典
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_opd_station` (
    `id`                  bigint      NOT NULL COMMENT '主键ID（雪花）',
    `station_code`        varchar(32) NOT NULL COMMENT '工位编码（全局唯一）',
    `station_name`        varchar(64) NOT NULL COMMENT '工位名称（如：门诊总导诊台）',
    `station_kind`        tinyint     NOT NULL COMMENT '工位类别（1-总导诊 2-门诊分诊台 3-输液室 4-注射室 5-采血室 9-其他）',
    `required_staff_type` tinyint     NOT NULL DEFAULT 2 COMMENT '应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工，默认2护理）',
    `capacity`            int         NOT NULL DEFAULT 1 COMMENT '建议同时在岗人数',
    `phone`               varchar(32) NULL COMMENT '工位电话',
    `sort_no`             int         NOT NULL DEFAULT 0 COMMENT '排序号（小的在前）',
    `status`              tinyint     NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
    `create_by`           varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`        bigint      NULL COMMENT '创建人ID',
    `create_time`        datetime    NOT NULL COMMENT '创建时间',
    `update_by`           varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`        bigint      NULL COMMENT '更新人ID',
    `update_time`        datetime    NOT NULL COMMENT '更新时间',
    `del_flag`           tinyint     NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    `remark`              varchar(500) NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_opd_station_code` (`station_code`),
    KEY `idx_opd_station_kind` (`station_kind`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊公共工位字典';

-- -------------------------------------------------------------
-- 二、工位派岗记录（谁 · 哪天 · 哪个工位 · 什么班；is_loan=1 为跨单元借调）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_opd_station_assignment` (
    `id`              bigint      NOT NULL COMMENT '主键ID（雪花）',
    `assign_date`     date        NOT NULL COMMENT '上岗日期',
    `station_id`      bigint      NOT NULL COMMENT '工位ID（biz_opd_station.id）',
    `station_name`    varchar(64) NOT NULL COMMENT '工位名称快照',
    `station_kind`    tinyint     NOT NULL COMMENT '工位类别快照（1总导诊 2分诊台 3输液室 4注射室 5采血室 9其他）',
    `employee_id`     bigint      NOT NULL COMMENT '上岗员工ID',
    `employee_name`   varchar(50) NOT NULL COMMENT '上岗员工姓名快照',
    `emp_code`        varchar(32) NULL COMMENT '员工工号快照',
    `staff_type`      tinyint     NOT NULL DEFAULT 2 COMMENT '上岗人岗位类别快照（默认2护理）',
    `shift_id`        bigint      NULL COMMENT '班次ID',
    `start_time`      varchar(5)  NULL COMMENT '班次开始时间快照 HH:mm',
    `end_time`        varchar(5)  NULL COMMENT '班次结束时间快照 HH:mm',
    `is_loan`         tinyint     NOT NULL DEFAULT 0 COMMENT '是否跨单元借调（0-否 1-是）',
    `source_org_type` tinyint     NULL COMMENT '借出方单元类型（借调填：1科室 2病区 3全院）',
    `source_org_id`   bigint      NULL COMMENT '借出方单元ID（借调填）',
    `source_org_name` varchar(100) NULL COMMENT '借出方单元名称快照（借调填）',
    `assign_mode`     tinyint     NOT NULL DEFAULT 1 COMMENT '派岗方式（1-排班员指定）',
    `status`          tinyint     NOT NULL DEFAULT 1 COMMENT '状态（1-在岗 0-撤销）',
    `create_by`       varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`    bigint      NULL COMMENT '创建人ID',
    `create_time`     datetime    NOT NULL COMMENT '创建时间',
    `update_by`       varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`    bigint      NULL COMMENT '更新人ID',
    `update_time`     datetime    NOT NULL COMMENT '更新时间',
    `del_flag`        tinyint     NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500) NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_opd_assign_day_station_shift_emp` (`assign_date`, `station_id`, `shift_id`, `employee_id`),
    KEY `idx_opd_assign_date_station` (`assign_date`, `station_id`),
    KEY `idx_opd_assign_emp_date` (`employee_id`, `assign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊工位派岗记录';

-- -------------------------------------------------------------
-- 三、铺底五个公共工位（按编码判重，可重复执行）
-- id 段 27700000000000001xx，与雪花 id、既有铺底错开
-- -------------------------------------------------------------
INSERT INTO biz_opd_station
    (id, station_code, station_name, station_kind, required_staff_type, capacity, phone,
     sort_no, status, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT * FROM (
    SELECT 2770000000000000101 AS id, 'OP-GUIDE'  AS station_code, '门诊总导诊台' AS station_name, 1 AS station_kind, 2 AS required_staff_type, 1 AS capacity, NULL AS phone, 10 AS sort_no, 1 AS status, 'admin' AS create_by, NOW() AS create_time, 'admin' AS update_by, NOW() AS update_time, 0 AS del_flag, '门诊大厅总导诊/咨询' AS remark UNION ALL
    SELECT 2770000000000000102, 'OP-TRIAGE', '门诊分诊台', 2, 2, 1, NULL, 20, 1, 'admin', NOW(), 'admin', NOW(), 0, '挂号后分诊到科室候诊' UNION ALL
    SELECT 2770000000000000103, 'OP-INFUSION', '门诊输液室', 3, 2, 2, NULL, 30, 1, 'admin', NOW(), 'admin', NOW(), 0, '静脉输液在岗护理' UNION ALL
    SELECT 2770000000000000104, 'OP-INJECT', '门诊注射室', 4, 2, 1, NULL, 40, 1, 'admin', NOW(), 'admin', NOW(), 0, '肌肉/皮下注射在岗护理' UNION ALL
    SELECT 2770000000000000105, 'OP-BLOOD', '门诊采血室', 5, 2, 1, NULL, 50, 1, 'admin', NOW(), 'admin', NOW(), 0, '静脉采血在岗护理'
) seed
WHERE NOT EXISTS (SELECT 1 FROM biz_opd_station s WHERE s.station_code = seed.station_code);

-- -------------------------------------------------------------
-- 四、菜单：排班中心(2945)下新增页面 2960「门诊工位派岗」+ 两个按钮码
-- （2940/2950 已被 AI 运营问数、我的未报占用，本页取 2960/29601/29602）
-- 页面行 list；新增/派岗与修改同走 upsert → 一个 add 码；撤销/删除一个 delete 码（与岗位出勤同口径）
-- -------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 2960, '门诊工位派岗', 2945, 8, 2, '/opd-station', 'schedule/OpdStationView', 'org.opdStation:list', NULL,
       'opd:station:list', 0, 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 0, '公共工位字典 + 按日派岗/借调登记', 1, 1
FROM DUAL
WHERE NOT EXISTS(SELECT 1 FROM sys_menu m WHERE m.id = 2960 OR m.permission = 'opd:station:list');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 29601, '门诊工位-派岗/新增', 2960, 1, 3, NULL, NULL, 'org.opdStation:add', NULL, 'opd:station:add',
       0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '工位字典新增/修改与派岗登记同走 upsert', 1, 1
FROM DUAL
WHERE NOT EXISTS(SELECT 1 FROM sys_menu m WHERE m.id = 29601 OR m.permission = 'opd:station:add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 29602, '门诊工位-撤销/删除', 2960, 2, 3, NULL, NULL, 'org.opdStation:delete', NULL, 'opd:station:delete',
       0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '撤销派岗 / 删除工位字典行（物理删）', 1, 1
FROM DUAL
WHERE NOT EXISTS(SELECT 1 FROM sys_menu m WHERE m.id = 29602 OR m.permission = 'opd:station:delete');

-- -------------------------------------------------------------
-- 五、角色授权：超管(1)、护士长(10)、医务科(26)（与岗位出勤 2935 同批）
-- -------------------------------------------------------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT s.nid, s.rid, s.mid, 'admin', NOW(), 'admin', NOW(), 1, 1
FROM (
    SELECT 1  AS rid, 2960  AS mid, 2770000000000000201 AS nid UNION ALL
    SELECT 1, 29601, 2770000000000000202 UNION ALL
    SELECT 1, 29602, 2770000000000000203 UNION ALL
    SELECT 10, 2960, 2770000000000000204 UNION ALL
    SELECT 10, 29601, 2770000000000000205 UNION ALL
    SELECT 10, 29602, 2770000000000000206 UNION ALL
    SELECT 26, 2960, 2770000000000000207 UNION ALL
    SELECT 26, 29601, 2770000000000000208 UNION ALL
    SELECT 26, 29602, 2770000000000000209
) s
WHERE NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = s.rid AND rm.menu_id = s.mid);

-- =============================================================
-- 六、回查（执行后逐条确认；key 没落 = 菜单隐藏 / 接口 403）
-- =============================================================
-- SELECT id, menu_name, parent_id, permission, menu_type FROM sys_menu WHERE id IN (2960,29601,29602);
-- SELECT rm.role_id, GROUP_CONCAT(rm.menu_id ORDER BY rm.menu_id) ids
--   FROM sys_role_menu rm WHERE rm.menu_id IN (2960,29601,29602) GROUP BY rm.role_id;
-- SELECT id, station_code, station_name, station_kind, capacity, status FROM biz_opd_station WHERE del_flag=0 ORDER BY sort_no;
-- 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或重启后端，否则旧权限集合仍生效。
