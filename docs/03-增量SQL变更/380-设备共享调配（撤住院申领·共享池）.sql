-- 380 设备共享调配（撤住院申领 · 共享池）
-- 背景：378 的「床旁设备申领」把设备租借做成了患者绑定动作，且设备类型写死输液泵。
-- 方案：按设备科共享调配口径重做：
--   1) 撤掉住院管理下「床旁设备申领」菜单，改到「物资设备 · 设备共享调配」；
--   2) 设备台账增加 rentable（是否进入共享池），由设备科维护；
--   3) 新增租赁申请单 + 明细，临床申请、审批、设备科发放、护士归还、设备科验收。
--
-- 执行后清 Redis 权限缓存：his:perm:role:*

-- 1) 设备台账：共享池标志（0-不共享 1-共享）
ALTER TABLE sys_equipment
    ADD COLUMN rentable TINYINT NOT NULL DEFAULT 0 COMMENT '是否进入共享池（0-否 1-是）' AFTER status;

-- 2) 租赁申请单
CREATE TABLE IF NOT EXISTS biz_device_rental_order (
    id BIGINT NOT NULL COMMENT '主键',
    order_no VARCHAR(40) NOT NULL COMMENT '申请单号',
    applicant_dept_id BIGINT NOT NULL COMMENT '申请科室ID',
    applicant_dept_name VARCHAR(100) NOT NULL COMMENT '申请科室名称',
    applicant_id BIGINT NOT NULL COMMENT '申请人员工ID',
    applicant_name VARCHAR(64) NOT NULL COMMENT '申请人姓名',
    patient_id BIGINT NULL COMMENT '患者ID（科室级借用可为空）',
    admission_id BIGINT NULL COMMENT '住院就诊ID（患者级借用）',
    patient_name VARCHAR(100) NULL COMMENT '患者姓名快照',
    bed_no VARCHAR(20) NULL COMMENT '床位号快照',
    ward_id BIGINT NULL COMMENT '病区ID快照',
    ward_name VARCHAR(100) NULL COMMENT '病区名称快照',
    purpose VARCHAR(500) NULL COMMENT '临床用途/借用原因',
    urgency TINYINT NOT NULL DEFAULT 1 COMMENT '紧急程度（1-普通 2-紧急 3-急救）',
    expect_return_time DATETIME NULL COMMENT '预计归还时间',
    delivery_place VARCHAR(200) NULL COMMENT '送达地点',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1-待审批 2-待发放 3-使用中 4-待验收 5-已完成 6-已取消 7-已驳回）',
    approve_by VARCHAR(64) NULL COMMENT '审批人',
    approve_by_id BIGINT NULL COMMENT '审批人员工ID',
    approve_time DATETIME NULL COMMENT '审批时间',
    approve_remark VARCHAR(500) NULL COMMENT '审批意见',
    issue_by VARCHAR(64) NULL COMMENT '发放人',
    issue_by_id BIGINT NULL COMMENT '发放人员工ID',
    issue_time DATETIME NULL COMMENT '发放时间',
    return_by VARCHAR(64) NULL COMMENT '归还申请人',
    return_by_id BIGINT NULL COMMENT '归还申请人员工ID',
    return_time DATETIME NULL COMMENT '申请归还时间',
    return_reason VARCHAR(500) NULL COMMENT '归还原因/设备状态',
    accept_by VARCHAR(64) NULL COMMENT '验收人',
    accept_by_id BIGINT NULL COMMENT '验收人员工ID',
    accept_time DATETIME NULL COMMENT '验收时间',
    accept_remark VARCHAR(500) NULL COMMENT '验收意见',
    remark VARCHAR(500) NULL COMMENT '备注',
    create_by VARCHAR(64) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_by VARCHAR(64) NOT NULL COMMENT '更新人',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by_id BIGINT NOT NULL DEFAULT 0 COMMENT '创建人员工ID',
    update_by_id BIGINT NOT NULL DEFAULT 0 COMMENT '更新人员工ID',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_rental_order_no (order_no),
    KEY idx_device_rental_status (status),
    KEY idx_device_rental_admission (admission_id),
    KEY idx_device_rental_dept (applicant_dept_id)
) COMMENT='设备共享调配申请单';

-- 3) 申请明细（一行 = 一台设备；发放时落到具体设备编号）
CREATE TABLE IF NOT EXISTS biz_device_rental_item (
    id BIGINT NOT NULL COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '申请单ID',
    equipment_id BIGINT NOT NULL COMMENT '设备台账ID',
    equipment_code VARCHAR(32) NULL COMMENT '设备台账编码',
    equipment_name VARCHAR(200) NULL COMMENT '设备名称',
    requested_brand VARCHAR(100) NULL COMMENT '期望品牌（可空）',
    requested_model VARCHAR(100) NULL COMMENT '期望型号（可空）',
    device_no VARCHAR(32) NULL COMMENT '实际发放设备编号（IoT 泵为 PUMP-xx，普通设备为资产编码）',
    device_type VARCHAR(32) NULL COMMENT '设备类型（IoT 设备才有，用于绑定遥测）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1-待发放 2-使用中 3-待验收 4-已归还 5-已取消）',
    issue_time DATETIME NULL COMMENT '发放时间',
    return_time DATETIME NULL COMMENT '归还时间',
    accept_time DATETIME NULL COMMENT '验收时间',
    bind_id BIGINT NULL COMMENT 'IoT 设备—患者绑定ID（普通设备为空）',
    remark VARCHAR(500) NULL COMMENT '备注',
    create_by VARCHAR(64) NOT NULL COMMENT '创建人',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_by VARCHAR(64) NOT NULL COMMENT '更新人',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by_id BIGINT NOT NULL DEFAULT 0 COMMENT '创建人员工ID',
    update_by_id BIGINT NOT NULL DEFAULT 0 COMMENT '更新人员工ID',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-已删除）',
    PRIMARY KEY (id),
    KEY idx_device_rental_item_order (order_id),
    KEY idx_device_rental_item_device (device_no),
    KEY idx_device_rental_item_equipment (equipment_id)
) COMMENT='设备共享调配申请明细';

-- 4) 先撤掉原住院管理入口，避免两个设备借用入口并存
DELETE FROM sys_role_menu WHERE menu_id IN (3780000000000000101, 3780000000000000102, 3780000000000000103);
DELETE FROM sys_menu WHERE id IN (3780000000000000101, 3780000000000000102, 3780000000000000103);

-- 5) 设备共享调配菜单：物资设备（900）下，排在设备管理之后
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
VALUES (3800000000000000101, '设备共享调配', 900, 2, 2, '/device-rental', 'equipment/DeviceRentalView',
        'asset.deviceRental', 'Box', 'asset:deviceRental:list', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '设备科共享池、临床申请、审批发放、归还验收',
        0, 0);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
VALUES (3800000000000000102, '共享设备-申请', 3800000000000000101, 1, 3, NULL, NULL,
        'asset.deviceRental:apply', NULL, 'asset:deviceRental:apply', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '临床科室发起设备借用申请',
        0, 0),
       (3800000000000000103, '共享设备-审批', 3800000000000000101, 2, 3, NULL, NULL,
        'asset.deviceRental:approve', NULL, 'asset:deviceRental:approve', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '护士长/设备科审批申请',
        0, 0),
       (3800000000000000104, '共享设备-发放', 3800000000000000101, 3, 3, NULL, NULL,
        'asset.deviceRental:issue', NULL, 'asset:deviceRental:issue', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '设备科分配设备并发放/绑定',
        0, 0),
       (3800000000000000105, '共享设备-归还', 3800000000000000101, 4, 3, NULL, NULL,
        'asset.deviceRental:return', NULL, 'asset:deviceRental:return', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '临床科室申请归还设备',
        0, 0),
       (3800000000000000106, '共享设备-验收', 3800000000000000101, 5, 3, NULL, NULL,
        'asset.deviceRental:accept', NULL, 'asset:deviceRental:accept', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '设备科验收归还设备、恢复库存',
        0, 0),
       (3800000000000000107, '共享设备-目录维护', 3800000000000000101, 6, 3, NULL, NULL,
        'asset.deviceRental:manage', NULL, 'asset:deviceRental:manage', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '设备科维护设备是否进入共享池',
        0, 0);

-- 6) 授权：角色 1 管理员 / 3 护士 / 10 护士长 / 29 护理部主任 / 30 科护士长 / 39 设备与供应室管理员
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
 (3800000000000000202, 3, 900, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000203, 10, 900, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000204, 29, 900, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000205, 30, 900, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000206, 1, 3800000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000207, 3, 3800000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000208, 10, 3800000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000209, 29, 3800000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000210, 30, 3800000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000211, 39, 3800000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000212, 1, 3800000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000213, 3, 3800000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000214, 10, 3800000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000215, 30, 3800000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000216, 1, 3800000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000217, 10, 3800000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000218, 29, 3800000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000219, 30, 3800000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000220, 39, 3800000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000221, 1, 3800000000000000104, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000222, 39, 3800000000000000104, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000223, 1, 3800000000000000105, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000224, 3, 3800000000000000105, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000225, 10, 3800000000000000105, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000226, 30, 3800000000000000105, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000227, 1, 3800000000000000106, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000228, 39, 3800000000000000106, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000229, 1, 3800000000000000107, 'admin', NOW(), 'admin', NOW(), 0, 0),
 (3800000000000000230, 39, 3800000000000000107, 'admin', NOW(), 'admin', NOW(), 0, 0);

-- 7) 铺共享池底册：可移动、可复用的治疗/监护设备；冷链/气体/床垫/标签等固定设施不纳入
UPDATE sys_equipment
SET rentable = 1
WHERE equipment_code IN (
    'EQ033', 'EQ034', 'EQ035', 'EQ036',
    'EQ054', 'EQ055', 'EQ057', 'EQ059',
    'EQ060', 'EQ061', 'EQ062',
    'EQ065', 'EQ066', 'EQ067', 'EQ068'
);
