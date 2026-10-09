SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('1', '10012', '系统管理员', 1, 6, 1, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2', '10013', '医生', 1, 1, 3, 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('3', '10014', '护士', 1, 2, 3, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('4', '10015', '收费员', 1, 5, 3, 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('5', '10016', '药剂师', 1, 4, 3, 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('6', '10017', '医技人员', 1, 3, 3, 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('7', '10018', '前台导诊', 1, 6, 3, 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8', '10019', '急诊医生', 1, 1, 3, 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9', '10020', '分诊护士', 1, 2, 3, 9, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('10', '10021', '护士长', 1, 2, 3, 10, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('11', '10022', '检验技师', 1, 3, 3, 11, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('12', '10023', '检查技师', 1, 3, 3, 12, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('13', '10024', '临床药师', 1, 4, 1, 13, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('14', '10025', '病案编码员', 1, 6, 1, 14, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('15', '10026', '病案质控员', 1, 6, 1, 15, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('16', '10027', '医保结算员', 1, 5, 1, 16, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('17', '10028', '审计统计员', 1, 6, 1, 17, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('18', '10029', '院领导', 1, 6, 1, 18, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('19', 'PATIENT', '患者', 3, 6, 5, 19, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('20', '10030', '放射诊断医师', 1, 1, 3, 20, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21', '10031', '营养师', 1, 3, 1, 21, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('22', '10032', '公卫医师', 1, 1, 3, 22, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('23', '10033', '行政人员', 1, 6, 5, 23, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status, create_by,
                      update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('24', '10034', '客服专员', 1, 6, 1, 24, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
