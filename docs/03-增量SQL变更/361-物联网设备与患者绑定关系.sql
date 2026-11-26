-- =====================================================================
-- 361 物联网设备与患者的绑定关系（模拟护士扫泵身条码）
-- 库：hn_biz_his
--
-- 口径：
--   1) 绑定对象是「住院就诊 admission_id」，不是床位。泵跟着患者走，转床不重绑，
--      bed_id/bed_no/ward_id/dept_id 只是绑定当时的快照，用于回溯「绑的时候挂在哪个床」。
--   2) 一台设备同时只允许一条 bind_status=1 在用绑定（业务约束在 IotDeviceBindServiceImpl，
--      表上不加 (device_no, bind_status) 唯一键：历史解绑行 bind_status=2 会重复 device_no）。
--   3) 绑定/解绑都落人（bind_by/bind_by_id、unbind_by/unbind_by_id），出院回收、换泵可追溯。
--   4) 历史 9 条 biz_iot_device.bind_type='bed' 演示绑定按当前在院患者补成绑定行（bind_source=1-扫码），
--      并把设备档案上的 bind_type/bind_id 清空——绑定事实只留在这张表，避免两处真相。
-- =====================================================================

-- ----------------------------
-- biz_iot_device_bind  设备—患者绑定关系
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_iot_device_bind` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号（扫码得到）',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型（coldchain-药品冷藏 infusion-输液泵 asset-定位信标 env-环境监测 gas-医用气体 bed-智能床垫 dialysis-透析机 cabinet-智能药柜 baby-婴儿腕带 uv-空气消毒机 robot-送药机器人）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `admission_id` bigint NOT NULL COMMENT '住院就诊ID（绑定对象，转床不变）',
  `bed_id` bigint DEFAULT NULL COMMENT '绑定时的床位ID',
  `bed_no` varchar(32) DEFAULT NULL COMMENT '绑定时的床位号',
  `ward_id` bigint DEFAULT NULL COMMENT '绑定时的病区ID',
  `dept_id` bigint DEFAULT NULL COMMENT '绑定时的科室ID',
  `bind_source` tinyint NOT NULL DEFAULT '1' COMMENT '绑定方式（1-扫码 2-手工）',
  `bind_time` datetime NOT NULL COMMENT '绑定时间',
  `bind_by` varchar(64) NOT NULL COMMENT '绑定人',
  `bind_by_id` bigint NOT NULL COMMENT '绑定人ID',
  `unbind_time` datetime DEFAULT NULL COMMENT '解绑时间',
  `unbind_by` varchar(64) DEFAULT NULL COMMENT '解绑人',
  `unbind_by_id` bigint DEFAULT NULL COMMENT '解绑人ID',
  `unbind_reason` varchar(128) DEFAULT NULL COMMENT '解绑原因（换泵/治疗结束/出院/设备回收）',
  `bind_status` tinyint NOT NULL DEFAULT '1' COMMENT '绑定状态（1-在用 2-已解绑）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_iot_bind_device` (`device_no`,`bind_status`),
  KEY `idx_iot_bind_admission` (`admission_id`,`bind_status`),
  KEY `idx_iot_bind_status_time` (`bind_status`,`bind_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联网设备—患者绑定关系';

-- ----------------------------
-- 历史演示绑定迁移：bind_type='bed' 的设备 → 该床当前在院患者的绑定行
-- ----------------------------
INSERT INTO `biz_iot_device_bind`
(`id`, `device_no`, `device_type`, `patient_id`, `admission_id`, `bed_id`, `bed_no`, `ward_id`, `dept_id`,
 `bind_source`, `bind_time`, `bind_by`, `bind_by_id`, `bind_status`, `remark`,
 `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
SELECT 361000000000000000 + ROW_NUMBER() OVER (ORDER BY d.device_no),
       d.device_no, d.device_type, a.patient_id, a.admission_id, a.bed_id,
       (SELECT b.bed_no FROM sys_bed b WHERE b.bed_id = a.bed_id LIMIT 1),
       a.ward_id, a.dept_id,
       1, COALESCE(a.admit_time, now()), '系统迁移', 0, 1, '历史演示绑定迁移（模拟扫码）',
       '系统迁移', now(), '系统迁移', now(), 0, 0, 0
FROM `biz_iot_device` d
JOIN `biz_admission` a ON a.bed_id = d.bind_id AND a.admit_status = 1 AND a.del_flag = 0
WHERE d.del_flag = 0 AND d.bind_type = 'bed'
  AND NOT EXISTS (SELECT 1 FROM `biz_iot_device_bind` x
                  WHERE x.device_no = d.device_no AND x.bind_status = 1 AND x.del_flag = 0);

-- 绑定事实迁走后，设备档案上的旧绑定列清空（避免两处真相）
UPDATE `biz_iot_device`
SET `bind_type` = 'none', `bind_id` = NULL
WHERE `del_flag` = 0 AND `bind_type` = 'bed';

-- 回查
SELECT COUNT(*) AS active_binds FROM `biz_iot_device_bind` WHERE `bind_status` = 1 AND `del_flag` = 0;
SELECT `device_no`, `bed_no`, `patient_id`, `admission_id`, `bind_source`, `bind_time`
FROM `biz_iot_device_bind` WHERE `bind_status` = 1 ORDER BY `device_no`;
