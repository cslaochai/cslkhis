-- =====================================================================
-- 432 机器人站点主数据
--
-- 背景：421 落地配送任务单时，取货点/收货站点是自由文本（from_station / to_station），
--       目标楼层靠人手填，派单选车再从「B1 中心药房」这类字符串里正则抠楼层。
--       站点没有主数据，科室换个叫法、护士站改个名，调度就取不到楼层，还一声不响。
--
-- 做法：对标厂商的业务站点层（瑞仕格 TransLogic 的 station、Aethon 的 delivery destination）：
--   1) 新增 biz_robot_station：站点编码、名称、类型、楼栋、楼层、归属科室、取/收能力、
--      机器人停靠点位码（dock_code，联调用）、服务时间窗；
--   2) 任务单增加发起/取货/收货三个站点 id，文本列保留为落单时的快照（存量单据不动）；
--   3) 铺 18 个站点：药房/静配/检验/输血/库房/CSSD/洗涤 + 5~15F 护士站 + 充电桩/中转驿站；
--   4) 菜单：设备管理(900) 下新增「机器人站点」维护页，只授设备科(39)与管理员(1)。
--
-- 口径：站点是物理收发点，不是组织节点 —— 一个科室可有多个站点，充电桩/中转驿站不属任何科室。
--      路由/坐标/朝向归厂商 RCS，HIS 只存 dock_code 这一个指针。
--
-- 执行后清 Redis 权限缓存：his:perm:role:*
-- 回滚：见 432-回滚.sql
-- =====================================================================

-- ----------------------------
-- 1) 站点表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_robot_station` (
    `id`              BIGINT       NOT NULL COMMENT '主键',
    `station_code`    VARCHAR(32)  NOT NULL COMMENT '站点编码',
    `station_name`    VARCHAR(64)  NOT NULL COMMENT '站点名称',
    `station_type`    TINYINT      NOT NULL DEFAULT 9 COMMENT '站点类型（1-护士站 2-药房 3-静配 4-医技 5-消毒供应 6-库房 7-被服 8-充电桩 9-中转驿站）',
    `building`        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '楼栋',
    `floor_no`        INT          NOT NULL DEFAULT 1 COMMENT '楼层（B1 为 -1）',
    `dept_id`         BIGINT       NULL COMMENT '归属科室ID（仅归属参考，不参与调度）',
    `dept_name`       VARCHAR(100) NOT NULL DEFAULT '' COMMENT '归属科室名称快照',
    `pickup_enabled`  TINYINT      NOT NULL DEFAULT 1 COMMENT '可作取货点（1-是 0-否）',
    `receive_enabled` TINYINT      NOT NULL DEFAULT 1 COMMENT '可作收货点（1-是 0-否）',
    `dock_code`       VARCHAR(64)  NULL COMMENT '机器人停靠点位编码（厂商地图 point）',
    `service_window`  VARCHAR(32)  NULL COMMENT '服务时间窗，空为全天',
    `map_x`           DOUBLE       NULL COMMENT '仿真地图横坐标',
    `map_y`           DOUBLE       NULL COMMENT '仿真地图纵坐标',
    `enabled`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
    `sort_order`      INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `remark`          VARCHAR(500) NULL COMMENT '备注',
    `create_by`       VARCHAR(64)  NOT NULL COMMENT '创建人',
    `create_time`     DATETIME     NOT NULL COMMENT '创建时间',
    `update_by`       VARCHAR(64)  NOT NULL COMMENT '更新人',
    `update_time`     DATETIME     NOT NULL COMMENT '更新时间',
    `create_by_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人员工ID',
    `update_by_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人员工ID',
    `del_flag`        TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_robot_station_code` (`station_code`),
    KEY `idx_robot_station_type` (`station_type`),
    KEY `idx_robot_station_floor` (`building`, `floor_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流机器人收发站点';

-- 本表唯一键不含 del_flag：删除走物理删（RobotStationMapper#purgeById），不做软删占键。

-- ----------------------------
-- 2) 站点铺底：18 个（INSERT IGNORE 可重复执行，已有站点不覆盖）
--    dept_id 取 sys_department 现有科室；充电桩/中转驿站/10F 以上病区无对应科室，留空。
-- ----------------------------
INSERT IGNORE INTO `biz_robot_station`
    (id, station_code, station_name, station_type, building, floor_no, dept_id, dept_name,
     pickup_enabled, receive_enabled, dock_code, service_window, map_x, map_y, enabled, sort_order, remark,
     create_by, create_time, update_by, update_time, create_by_id, update_by_id, del_flag)
VALUES
    -- 取货类：药房 / 静配 / 库房 / 医技 / 消供 / 洗涤
    (4320000000000000201, 'ST-1F-PHARM',  '1F 住院药房',          2, '住院部', 1, 19580045, '住院药房',        1, 1, NULL, NULL, NULL, NULL, 1, 10, '摆药与出院带药', 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000202, 'ST-1F-PIVAS',  '1F 静脉配液中心',       3, '住院部', 1, 19580046, '静脉配液中心',    1, 1, NULL, NULL, NULL, NULL, 1, 20, '长期医嘱静配',   'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000203, 'ST-1F-WH',     '1F 中心库房',          6, '住院部', 1, 19580084, '总务科',          1, 1, NULL, NULL, NULL, NULL, 1, 30, '耗材与办公物资', 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000204, 'ST-1F-LAB',    '1F 检验科标本接收窗',   4, '医技楼', 1, 19580035, '医学检验科',      1, 1, NULL, NULL, NULL, NULL, 1, 40, '标本送检',       'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000205, 'ST-1F-BLOOD',  '1F 输血科取血窗',       4, '医技楼', 1, 19580040, '输血科',          1, 1, NULL, NULL, NULL, NULL, 1, 50, '取血与血袋回收', 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000206, 'ST-1F-CSSD',   '1F 消毒供应中心发放窗', 5, '后勤楼', 1, 19580094, '消毒供应中心',    1, 1, NULL, NULL, NULL, NULL, 1, 60, '无菌器械包发放', 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000207, 'ST-1F-CSSDR',  '1F 污染器械回收窗',     5, '后勤楼', 1, 19580094, '消毒供应中心',    1, 1, NULL, NULL, NULL, NULL, 1, 70, '术后器械回收',   'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000208, 'ST-1F-LAUND',  '1F 洗涤中心被服收发',   7, '后勤楼', 1, NULL,     '洗涤中心',        1, 1, NULL, NULL, NULL, NULL, 1, 80, '被服上下送',     'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000209, 'ST-4F-OPDS',   '4F 外科门诊分诊台',     1, '门诊楼', 4, 19580013, '普通外科',        1, 1, NULL, NULL, NULL, NULL, 1, 90, '换药与门诊标本', 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),

    -- 收货类：病区护士站
    (4320000000000000210, 'ST-5F-NUR',    '5F 肾内/血液内科护士站', 1, '住院部', 5,  19580008, '肾内科',          1, 1, NULL, NULL, NULL, NULL, 1, 110, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000211, 'ST-6F-NUR',    '6F 老年病科护士站',     1, '住院部', 6,  19580011, '老年病科',        1, 1, NULL, NULL, NULL, NULL, 1, 120, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000212, 'ST-7F-NUR',    '7F 新生儿科护士站',     1, '住院部', 7,  19580028, '新生儿科',        1, 1, NULL, NULL, NULL, NULL, 1, 130, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000213, 'ST-8F-ICU',    '8F 重症医学科护士站',   1, '住院部', 8,  19580050, '重症医学科(ICU)', 1, 1, NULL, NULL, NULL, NULL, 1, 140, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000214, 'ST-10F-NUR',   '10F 外科病区护士站',    1, '住院部', 10, NULL,     '',                1, 1, NULL, NULL, NULL, NULL, 1, 150, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000215, 'ST-12F-NUR',   '12F 内科病区护士站',    1, '住院部', 12, NULL,     '',                1, 1, NULL, NULL, NULL, NULL, 1, 160, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000216, 'ST-15F-NUR',   '15F 综合病区护士站',    1, '住院部', 15, NULL,     '',                1, 1, NULL, NULL, NULL, NULL, 1, 170, NULL, 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),

    -- 公共类：充电桩只作停靠点（不收不发），中转驿站收发皆可
    (4320000000000000217, 'ST-1F-DOCK',   '1F 住院药房充电桩',     8, '住院部', 1, 19580045, '住院药房',        0, 0, 'DOCK-B1-PHARM', NULL, NULL, NULL, 1, 200, '车队默认停靠与补能位', 'sql432', NOW(), 'sql432', NOW(), 1, 1, 0),
    (4320000000000000218, 'ST-1F-HUB',    '1F 电梯厅中转驿站',     9, '住院部', 1, NULL,     '',                1, 1, 'HUB-1F-ELEV',   NULL, NULL, NULL, 1, 210, '高峰时段临时存放',     'sql432', NOW(), 'sql432', NOW(), 1, 1, 0);

-- ----------------------------
-- 3) 任务单增加三个站点 id（一次性执行；文本列保留为落单快照）
-- ----------------------------
ALTER TABLE `biz_robot_dispatch_order`
    ADD COLUMN `apply_station_id` BIGINT NULL COMMENT '发起站点ID' AFTER `source_dept_name`,
    ADD COLUMN `from_station_id`  BIGINT NULL COMMENT '取货站点ID' AFTER `from_station`,
    ADD COLUMN `to_station_id`    BIGINT NULL COMMENT '收货站点ID' AFTER `to_station`,
    ADD KEY `idx_robot_dispatch_to_station` (`to_station_id`);

-- 3.1) 存量回填：按站点名称精确匹配（匹配不上的留空，历史单据仍按文本展示）
UPDATE `biz_robot_dispatch_order` o
JOIN `biz_robot_station` s ON s.station_name = o.from_station AND s.del_flag = 0
SET o.from_station_id = s.id
WHERE o.from_station_id IS NULL;

UPDATE `biz_robot_dispatch_order` o
JOIN `biz_robot_station` s ON s.station_name = o.to_station AND s.del_flag = 0
SET o.to_station_id = s.id
WHERE o.to_station_id IS NULL;

-- 3.2) 车队台账停靠位置对齐站点名称（原「B1 中心药房充电桩」与住院药房实际楼层不符）
UPDATE `biz_iot_device`
SET location     = '1F 住院药房充电桩',
    update_by    = 'sql432',
    update_time  = NOW()
WHERE device_type = 'robot'
  AND del_flag = 0
  AND location = 'B1 中心药房充电桩';

UPDATE `biz_iot_device`
SET location     = '1F 住院药房',
    update_by    = 'sql432',
    update_time  = NOW()
WHERE device_type = 'robot'
  AND del_flag = 0
  AND location = 'B1 中心药房';

-- ----------------------------
-- 4) 菜单：机器人站点（设备管理 900 下，排在机器人调度之后）
-- ----------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 4320000000000000101, '机器人站点', 900, 14, 2, '/iot-robot-station',
       'equipment/RobotStationView', 'iot.robotStation', 'Location',
       'iot:robotStation:list', 0, 0, 1, 1,
       'sql432', NOW(), 'sql432', NOW(), 0, '物流机器人收发站点主数据（设备科维护）',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'iot.robotStation');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT seed.id, seed.name, 4320000000000000101, seed.sort, 3, NULL, NULL, seed.`key`, NULL,
       seed.perm, 0, 0, 1, 1,
       'sql432', NOW(), 'sql432', NOW(), 0, seed.remark, 0, 0
FROM (
    SELECT 4320000000000000102 AS id, '站点-新增/修改' AS name, 1 AS sort, 'iot.robotStation:add' AS `key`,
           'iot:robotStation:add' AS perm, '维护站点名称、楼层与停靠码' AS remark UNION ALL
    SELECT 4320000000000000103, '站点-删除', 2, 'iot.robotStation:delete',
           'iot:robotStation:delete', '物理删除站点（配置类，无留档价值）'
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.permission = seed.perm);

-- 角色授权：1 管理员 / 39 设备与供应室管理员（页面 + 两个按钮码）
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'sql432', NOW(), 'sql432', NOW(), 1, 1
FROM (
    SELECT 4320000000000000301 AS id, 1  AS role_id, 4320000000000000101 AS menu_id UNION ALL
    SELECT 4320000000000000302, 39, 4320000000000000101 UNION ALL
    SELECT 4320000000000000303, 1,  4320000000000000102 UNION ALL
    SELECT 4320000000000000304, 39, 4320000000000000102 UNION ALL
    SELECT 4320000000000000305, 1,  4320000000000000103 UNION ALL
    SELECT 4320000000000000306, 39, 4320000000000000103
) seed;

-- 回查：
-- SELECT station_code, station_name, station_type, building, floor_no, enabled FROM biz_robot_station ORDER BY sort_order;
-- SELECT id, menu_name, permission, is_visible FROM sys_menu WHERE menu_key = 'iot.robotStation' OR parent_id = 4320000000000000101;
-- SELECT rm.role_id, rm.menu_id FROM sys_role_menu rm WHERE rm.menu_id IN (4320000000000000101, 4320000000000000102, 4320000000000000103) ORDER BY rm.menu_id, rm.role_id;

