-- =====================================================================
-- 362 婴儿防盗（腕带登记 + 设备事件落库 + 住院管理入口）
-- 库：hn_biz_his
--
-- 背景：本院没有产科在院患者，但婴儿防盗是智慧病房的标准能力，按"腕带发放登记"实现，
--       不依赖产科住院数据：护士发放腕带时登记婴儿/母亲/床号，防盗看板按腕带监区/拆带/电量告警。
-- 口径：
--   1) biz_iot_baby_tag 是"谁戴着哪条腕带"的事实，status=1 佩戴中、2 已回收；回收行保留可回溯。
--   2) biz_iot_event 全存设备事件（355 已设计，此前未建表），MQTT 的 /event 主题落这里，
--      幂等键 (device_no, seq, ts)；处理状态 0-未处理 1-已处理 2-已忽略。
--   3) 演示：BABY-01~03 三条腕带（模拟器已在产科-01~03 床上报），BABY-04 留空用于演示发放。
-- =====================================================================

-- ----------------------------
-- biz_iot_event  物联网设备事件
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_iot_event` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型',
  `dept_id` bigint DEFAULT NULL COMMENT '归属科室ID',
  `event_code` varchar(64) NOT NULL COMMENT '事件码',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '事件级别（1-提示 2-警告 3-严重）',
  `detail` varchar(500) NOT NULL DEFAULT '' COMMENT '事件描述',
  `data_json` json DEFAULT NULL COMMENT '事件明细数据',
  `seq` bigint NOT NULL DEFAULT '0' COMMENT '设备侧报文序号',
  `ts` datetime NOT NULL COMMENT '事件发生时间',
  `handled_flag` tinyint NOT NULL DEFAULT '0' COMMENT '处理标志（0-未处理 1-已处理 2-已忽略）',
  `handle_by_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handle_by` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理说明',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  PRIMARY KEY (`id`,`ts`),
  UNIQUE KEY `uk_iot_event_idem` (`device_no`,`seq`,`ts`),
  KEY `idx_iot_event_dev_ts` (`device_no`,`ts`),
  KEY `idx_iot_event_code_ts` (`event_code`,`ts`),
  KEY `idx_iot_event_handle_ts` (`handled_flag`,`ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联网设备事件'
PARTITION BY RANGE COLUMNS(`ts`) (
  PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
  PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
  PARTITION p202701 VALUES LESS THAN ('2027-02-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);

-- ----------------------------
-- biz_iot_baby_tag  婴儿防盗腕带登记
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_iot_baby_tag` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tag_no` varchar(64) NOT NULL COMMENT '腕带设备编号',
  `baby_name` varchar(64) NOT NULL COMMENT '婴儿姓名',
  `mother_name` varchar(64) NOT NULL DEFAULT '' COMMENT '母亲姓名',
  `bed_no` varchar(32) NOT NULL DEFAULT '' COMMENT '床位号',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(64) NOT NULL DEFAULT '' COMMENT '病区名称',
  `patient_id` bigint DEFAULT NULL COMMENT '婴儿患者ID（HIS 有登记时填）',
  `admission_id` bigint DEFAULT NULL COMMENT '婴儿住院就诊ID（HIS 有登记时填）',
  `issue_time` datetime NOT NULL COMMENT '发放时间',
  `issue_by` varchar(64) NOT NULL COMMENT '发放人',
  `issue_by_id` bigint NOT NULL COMMENT '发放人ID',
  `recycle_time` datetime DEFAULT NULL COMMENT '回收时间',
  `recycle_by` varchar(64) DEFAULT NULL COMMENT '回收人',
  `recycle_by_id` bigint DEFAULT NULL COMMENT '回收人ID',
  `recycle_reason` varchar(128) DEFAULT NULL COMMENT '回收原因（出院/换带/设备故障）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '腕带状态（1-佩戴中 2-已回收）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_iot_baby_tag_no` (`tag_no`,`status`),
  KEY `idx_iot_baby_tag_status` (`status`,`issue_time`),
  KEY `idx_iot_baby_tag_ward` (`ward_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='婴儿防盗腕带登记';

-- ----------------------------
-- 演示腕带（模拟器 BABY-01~03 在产科-01~03 床上报）
-- ----------------------------
INSERT INTO `biz_iot_baby_tag`
(`id`, `tag_no`, `baby_name`, `mother_name`, `bed_no`, `ward_id`, `ward_name`,
 `issue_time`, `issue_by`, `issue_by_id`, `status`, `remark`,
 `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
SELECT seed.id, seed.tag_no, seed.baby_name, seed.mother_name, seed.bed_no, 8600000000000000022, '产科病区',
       NOW() - INTERVAL 2 HOUR, '系统迁移', 0, 1, '演示腕带（本院无产科在院患者，用于演示防盗流程）',
       '系统迁移', NOW(), '系统迁移', NOW(), 0, 0, 0
FROM (
    SELECT 3620000000000000301 AS id, 'BABY-01' AS tag_no, '演示婴儿01' AS baby_name,
           '演示产妇01' AS mother_name, '产科-01床' AS bed_no UNION ALL
    SELECT 3620000000000000302, 'BABY-02', '演示婴儿02', '演示产妇02', '产科-02床' UNION ALL
    SELECT 3620000000000000303, 'BABY-03', '演示婴儿03', '演示产妇03', '产科-03床'
) seed
WHERE NOT EXISTS (SELECT 1 FROM `biz_iot_baby_tag` t
                  WHERE t.tag_no = seed.tag_no AND t.status = 1 AND t.del_flag = 0);

-- ----------------------------
-- 菜单：住院管理 → 婴儿防盗
-- ----------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3620000000000000101, '婴儿防盗', 300, 5, 2, '/iot-baby',
       'iot/BabyAntiTheftView', 'ipd.iot.baby', 'Lock',
       'asset:iot:baby', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '护士站婴儿腕带防盗：监区越界/拆带/配对/电量告警与处理留痕',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'ipd.iot.baby');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 3620000000000000201 AS id, 1 AS role_id, 3620000000000000101 AS menu_id UNION ALL
    SELECT 3620000000000000202, 10, 3620000000000000101 UNION ALL
    SELECT 3620000000000000203, 29, 3620000000000000101
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);
