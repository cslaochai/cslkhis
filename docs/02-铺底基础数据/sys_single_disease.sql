SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000601', 'SD-I21', '急性心肌梗死（STEMI，住院）', 'I21', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000602', 'SD-I50', '心力衰竭（住院）', 'I50', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000603', 'SD-J18', '社区获得性肺炎（成人，住院）', 'J18', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000604', 'SD-J44', '慢性阻塞性肺疾病急性加重（住院）', 'J44', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000605', 'SD-I63', '脑梗死（住院）', 'I63', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000606', 'SD-I61', '脑出血（住院）', 'I61', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000607', 'SD-E11', '2型糖尿病（住院）', 'E11', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000608', 'SD-K85', '急性胰腺炎（住院）', 'K85', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000609', 'SD-N18', '慢性肾脏病3-5期（住院）', 'N18', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000610', 'SD-K80', '胆囊结石伴急性胆囊炎（住院手术）', 'K80', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000611', 'SD-K35', '急性阑尾炎（住院手术）', 'K35', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000612', 'SD-O82', '择期剖宫产（住院）', 'O82', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000613', 'SD-S72', '老年髋部骨折（65岁以上，手术）', 'S72', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000614', 'SD-C73', '甲状腺癌（手术）', 'C73', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('2350000000000000615', 'SD-C50', '乳腺癌（手术）', 'C50', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, update_by, del_flag, remark,
                                create_by_id, update_by_id)
VALUES ('8950000000000066001', 'SDTEST-J18', '社区获得性肺炎(验证)', 'J18', 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
