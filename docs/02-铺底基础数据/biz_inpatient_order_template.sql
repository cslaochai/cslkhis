SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000201', '897001001', '沈楠', '19580003', 1, '社区获得性肺炎常规医嘱', 1, 6, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000202', '897001015', '施佳明', '19580005', 1, '急性ST段抬高型心肌梗死急救医嘱', 1, 7, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000203', '897001029', '杨远航', '19580007', 1, '2型糖尿病入院常规医嘱', 1, 5, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000204', '897001015', '施佳明', '19580005', 1, '慢性心力衰竭常规医嘱', 1, 7, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000205', '897001022', '倪天', '19580006', 1, '急性脑梗死入院评估医嘱', 1, 5, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000206', '897001008', '温昊天', '19580004', 1, '上消化道出血抢救医嘱', 1, 6, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000207', '897001001', '沈楠', '19580003', 1, '慢阻肺急性加重常规医嘱', 1, 6, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000208', '897001036', '郭怀瑾', '19580008', 1, '肾病综合征常规医嘱', 1, 5, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000209', '897001029', '杨远航', '19580007', 1, '甲状腺功能亢进常规医嘱', 1, 5, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000210', '9302', '林小舟', '19580049', 1, '外科术前常规准备医嘱', 1, 5, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000211', '897001008', '温昊天', '19580004', 1, '肠镜检查肠道准备医嘱', 1, 3, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type,
                                          item_count, create_by, update_by, del_flag, remark, create_by_id,
                                          update_by_id)
VALUES ('2350000000000000212', '897001043', '吕明德', '19580009', 1, '化疗前评估医嘱', 1, 5, 'admin', 'admin', 0, NULL,
        '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
