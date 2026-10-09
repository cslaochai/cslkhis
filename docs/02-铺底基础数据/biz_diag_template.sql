SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_diag_template (id, doctor_id, icd_code, icd_name, sort_order, create_by, update_by, del_flag, remark,
                               create_by_id, update_by_id)
VALUES ('2099349085923319810', '2098255864065458177', 'I10', '原发性高血压', 0, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_diag_template (id, doctor_id, icd_code, icd_name, sort_order, create_by, update_by, del_flag, remark,
                               create_by_id, update_by_id)
VALUES ('2099349085923319811', '2098255864065458177', 'I21.9', '急性心肌梗死', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_diag_template (id, doctor_id, icd_code, icd_name, sort_order, create_by, update_by, del_flag, remark,
                               create_by_id, update_by_id)
VALUES ('2099349085923319812', '2098255864065458177', 'C34.9', '肺癌', 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_diag_template (id, doctor_id, icd_code, icd_name, sort_order, create_by, update_by, del_flag, remark,
                               create_by_id, update_by_id)
VALUES ('2099349085923319813', '2098255864065458177', 'I50.9', '心力衰竭', 3, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
