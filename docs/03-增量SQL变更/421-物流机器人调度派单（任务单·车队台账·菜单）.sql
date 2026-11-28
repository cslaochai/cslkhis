-- =====================================================================
-- 421 物流机器人调度派单（配送任务单 · 车队台账 · 菜单权限）
--
-- 背景：此前送药机器人的任务单、下单人、签收人全部由仿真器内部随机生成，
--       HIS 侧没有任何派单/调度端点。本脚本补齐真实物流调度的业务承载：
--         1) 配送任务单 biz_robot_dispatch_order：发起 → 待调度 → 派单 → 配送中 → 已签收；
--         2) 车队台账：机器人全院共享调度（不按科室固定配车），扩到 10 台；
--         3) 菜单与权限：设备科/护士站可建单，设备科调度派车。
--
-- 口径对标主流厂商调度平台（赛特/擎朗/云迹等）：
--   - 车队全院共享，科室/楼层只是收发站点；
--   - 按空闲状态、电量、优先级选车；急送插队，常规 FIFO；
--   - 任务全程留痕：取货点、收货站点、箱数、重量、签收人、里程、电梯等待。
--
-- 执行后清 Redis 权限缓存：his:perm:role:*
-- 回滚：见 421-回滚.sql
-- =====================================================================

-- ----------------------------
-- 1) 配送任务单
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_robot_dispatch_order` (
    `id`              BIGINT       NOT NULL COMMENT '主键',
    `order_no`        VARCHAR(40)  NOT NULL COMMENT '配送单号（RB+yyyyMMdd+5位序列）',
    `source_type`     VARCHAR(16)  NOT NULL DEFAULT 'MANUAL' COMMENT '发起来源：PIVAS静配中心/PHARMACY住院药房/LAB检验科/WAREHOUSE中心库房/CSSD消毒供应/LAUNDRY洗涤中心/MANUAL手工派单',
    `source_dept_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '发起科室/站点名称快照',
    `requester_id`    BIGINT       NULL COMMENT '发起人员工ID（手工派单取当前登录人，仿真来源可空）',
    `requester_name`  VARCHAR(64)  NULL COMMENT '发起人姓名快照',
    `goods_type`      VARCHAR(16)  NOT NULL COMMENT '品类：药品/标本/耗材/器械包/被服',
    `goods_name`      VARCHAR(200) NOT NULL COMMENT '物品明细',
    `from_station`    VARCHAR(100) NOT NULL COMMENT '取货点（如 B1 中心药房）',
    `to_floor`        INT          NOT NULL COMMENT '目标楼层',
    `to_station`      VARCHAR(100) NOT NULL COMMENT '收货站点（如 12F 护士站）',
    `priority`        TINYINT      NOT NULL DEFAULT 1 COMMENT '优先级：1-常规 2-批量补货 3-急送',
    `box_count`       INT          NOT NULL DEFAULT 1 COMMENT '箱数',
    `load_kg`         DOUBLE       NOT NULL DEFAULT 0 COMMENT '本单总重(kg)',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-待调度 2-已派单 3-配送中 4-已签收 5-已取消 6-异常',
    `robot_no`        VARCHAR(32)  NULL COMMENT '分配机器人编号',
    `signer`          VARCHAR(64)  NULL COMMENT '签收人',
    `dispatch_by`     VARCHAR(64)  NULL COMMENT '派单人姓名',
    `dispatch_by_id`  BIGINT       NULL COMMENT '派单人员工ID',
    `dispatch_time`   DATETIME     NULL COMMENT '派单时间',
    `complete_time`   DATETIME     NULL COMMENT '签收完成时间',
    `total_sec`       INT          NULL COMMENT '全程用时(秒)',
    `elevator_sec`    INT          NULL COMMENT '乘梯用时(秒，含排队)',
    `queue_sec`       INT          NULL COMMENT '梯口排队(秒)',
    `ride_sec`        INT          NULL COMMENT '轿厢运行(秒)',
    `odometer_m`      DOUBLE       NULL COMMENT '本单里程(米)',
    `cancel_reason`   VARCHAR(500) NULL COMMENT '取消原因',
    `remark`          VARCHAR(500) NULL COMMENT '备注',
    `create_by`       VARCHAR(64)  NOT NULL COMMENT '创建人',
    `create_time`     DATETIME     NOT NULL COMMENT '创建时间',
    `update_by`       VARCHAR(64)  NOT NULL COMMENT '更新人',
    `update_time`     DATETIME     NOT NULL COMMENT '更新时间',
    `create_by_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人员工ID',
    `update_by_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '更新人员工ID',
    `del_flag`        TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_robot_dispatch_no` (`order_no`),
    KEY `idx_robot_dispatch_status` (`status`),
    KEY `idx_robot_dispatch_robot` (`robot_no`),
    KEY `idx_robot_dispatch_source` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流机器人配送任务单';

-- ----------------------------
-- 2) 车队台账：10 台送药机器人（全院共享，归属设备科/中心药房，充电桩在药房）
--    编号 RBT-01..10；INSERT IGNORE 可重复执行，已有台账不覆盖。
--    规模对标中型三甲全院共享车队（5~10 台档位）；数量按高峰频次×单趟时长测算。
-- ----------------------------
INSERT IGNORE INTO biz_iot_device
    (id, device_no, device_type, power_type, device_name, dept_id, dept_name, location,
     bind_type, fw_version, online, last_state, alarm_flag, enabled,
     create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
    (4210000000000000101, 'RBT-01', 'robot', 'battery_dock', '送药机器人 01', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000102, 'RBT-02', 'robot', 'battery_dock', '送药机器人 02', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000103, 'RBT-03', 'robot', 'battery_dock', '送药机器人 03', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000104, 'RBT-04', 'robot', 'battery_dock', '送药机器人 04', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000105, 'RBT-05', 'robot', 'battery_dock', '送药机器人 05', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000106, 'RBT-06', 'robot', 'battery_dock', '送药机器人 06', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000107, 'RBT-07', 'robot', 'battery_dock', '送药机器人 07', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000108, 'RBT-08', 'robot', 'battery_dock', '送药机器人 08', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000109, 'RBT-09', 'robot', 'battery_dock', '送药机器人 09', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0),
    (4210000000000000110, 'RBT-10', 'robot', 'battery_dock', '送药机器人 10', NULL, '设备科', 'B1 中心药房充电桩', 'none', '1.0.4', 0, 'IDLE', 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '全院共享调度车队', 0, 0);

-- 2.1) 存量旧台账对齐：机器人全部归设备科共享车队，脱离原病区/床位/患者绑定快照。
--      INSERT IGNORE 不会覆盖已存在的 RBT-01/02，这里显式归一归属科室、停靠位置和遗留绑定列。
UPDATE biz_iot_device
SET dept_id      = NULL,
    dept_name    = '设备科',
    location     = 'B1 中心药房充电桩',
    bed_no       = NULL,
    bind_type    = 'none',
    bind_id      = NULL,
    power_type   = 'battery_dock',
    enabled      = 1,
    fw_version   = '1.0.4',
    remark       = '全院共享调度车队',
    update_by    = 'admin',
    update_time  = NOW()
WHERE device_type = 'robot'
  AND del_flag = 0
  AND device_no LIKE 'RBT-%';

-- ----------------------------
-- 3) 菜单：机器人调度（物资设备 900 下，排在设备共享调配之后）
-- ----------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 4210000000000000101, '机器人调度', 900, 13, 2, '/iot-robot-dispatch',
       'iot/RobotDispatchView', 'iot.robotDispatch', 'Van',
       'iot:robotDispatch:list', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '物流机器人工厂车队与配送任务派单调度',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'iot.robotDispatch');

-- 按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT seed.id, seed.name, 4210000000000000101, seed.sort, 3, NULL, NULL, seed.`key`, NULL,
       seed.perm, 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, seed.remark, 0, 0
FROM (
    SELECT 4210000000000000102 AS id, '调度-新建派单' AS name, 1 AS sort, 'iot.robotDispatch:apply' AS `key`,
           'iot:robotDispatch:apply' AS perm, '科室/站点发起配送任务' AS remark UNION ALL
    SELECT 4210000000000000103, '调度-派车', 2, 'iot.robotDispatch:assign',
           'iot:robotDispatch:assign', '设备科选择空闲机器人派单' UNION ALL
    SELECT 4210000000000000104, '调度-取消', 3, 'iot.robotDispatch:cancel',
           'iot:robotDispatch:cancel', '取消待调度/已派单任务'
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.permission = seed.perm);

-- 角色授权：1 管理员 / 3 护士 / 10 护士长 / 29 护理部主任 / 30 科护士长 / 39 设备与供应室管理员
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    -- 页面可见
    SELECT 4210000000000000201 AS id, 1  AS role_id, 4210000000000000101 AS menu_id UNION ALL
    SELECT 4210000000000000202, 3,  4210000000000000101 UNION ALL
    SELECT 4210000000000000203, 10, 4210000000000000101 UNION ALL
    SELECT 4210000000000000204, 29, 4210000000000000101 UNION ALL
    SELECT 4210000000000000205, 30, 4210000000000000101 UNION ALL
    SELECT 4210000000000000206, 39, 4210000000000000101 UNION ALL
    -- 新建派单：护士/护士长/科护士长/管理员/admin
    SELECT 4210000000000000207, 1,  4210000000000000102 UNION ALL
    SELECT 4210000000000000208, 3,  4210000000000000102 UNION ALL
    SELECT 4210000000000000209, 10, 4210000000000000102 UNION ALL
    SELECT 4210000000000000210, 30, 4210000000000000102 UNION ALL
    SELECT 4210000000000000211, 39, 4210000000000000102 UNION ALL
    -- 派车：admin / 设备与供应室管理员
    SELECT 4210000000000000212, 1,  4210000000000000103 UNION ALL
    SELECT 4210000000000000213, 39, 4210000000000000103 UNION ALL
    -- 取消：admin / 护士 / 护士长 / 科护士长 / 设备科
    SELECT 4210000000000000214, 1,  4210000000000000104 UNION ALL
    SELECT 4210000000000000215, 3,  4210000000000000104 UNION ALL
    SELECT 4210000000000000216, 10, 4210000000000000104 UNION ALL
    SELECT 4210000000000000217, 30, 4210000000000000104 UNION ALL
    SELECT 4210000000000000218, 39, 4210000000000000104
) seed;

-- 回查：
-- SELECT device_no, device_name, location, power_type FROM biz_iot_device WHERE device_type='robot' AND del_flag=0 ORDER BY device_no;
-- SELECT id, menu_name, permission FROM sys_menu WHERE menu_key LIKE 'iot.robotDispatch%';
