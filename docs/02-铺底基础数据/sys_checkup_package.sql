SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102954468424318977', 'g23-verify-套餐-2026-09-24', NULL, 0, '95.00', 'g23-verify 夹具套餐', 1, 'admin',
        'admin', 1, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102955122416967681', 'g23-verify-套餐-1790218546839', NULL, 0, '95.00', 'g23-verify 夹具套餐', 1, 'admin',
        'admin', 1, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102955541415448578', 'g23-verify-套餐-1790218646637', NULL, 0, '95.00', 'g23-verify 夹具套餐', 1, 'admin',
        'admin', 1, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102958757549035522', 'g23-verify-套餐-1790219413536', NULL, 0, '95.00', 'g23-verify 夹具套餐', 1, 'admin',
        'admin', 1, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000501', '健康体检基础套餐（男）', 'PK001', 1, '371.00',
        '覆盖常规检验、胸片与心电图，适合20-45岁男性年度体检', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000502', '健康体检基础套餐（女）', 'PK002', 2, '521.00',
        '基础套餐基础上增加子宫附件彩超，适合20-45岁女性年度体检', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000503', '入职体检套餐', 'PK003', 0, '263.00',
        '满足常规入职健康证明要求，含血常规、肝功能、胸片与心电图', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000504', '中老年关怀套餐（男）', 'PK004', 1, '971.00',
        '基础套餐上增加肿瘤标志物、颈动脉彩超与骨密度，适合45岁以上男性', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000505', '中老年关怀套餐（女）', 'PK005', 2, '1141.00',
        '女性基础套餐上增加肿瘤标志物、颈动脉彩超与骨密度，适合45岁以上女性', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000506', 'VIP尊享全面体检套餐', 'PK006', 0, '2021.00',
        '全面深度筛查：肿瘤标志物、甲功、头颅及胸部CT、心脏彩超、幽门螺杆菌等', 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
