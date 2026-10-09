SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8100000000000000002', 'P0WARD', '【P0】门诊转住院验证病区', '19580003', 2, 2, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8100000000000000003', 'P0WARD3', '【P0-2】门诊转住院验证病区', '19580003', 2, 2, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8100000000000000004', 'P0FEWARD', '【P0-FE】前端验证病区', '19580003', 2, 2, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8200000000000000001', 'P1WARD', '【P1】住院医嘱验证病区', '19580003', 2, 2, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8300000000000000001', 'P4WARD', '【P4】转科验证病区', '19580004', 2, 1, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8450000000000000001', 'BCQT', '【BC】床位中心验证病区', '19580003', 2, 1, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8450000000000000002', 'BCQT2', '【BC】床位中心验证病区二', '19580004', 2, 1, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8450000000000000011', 'BCUI', '【BCUI】床位中心UI验证病区', '19580003', 2, 0, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8450000000000000021', 'BCMAP', '【BCMAP】床位图验证病区', '19580003', 2, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8450000000000000031', 'BCMUI', '【BCMUI】床位图UI验证病区', '19580003', 2, 0, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000001', 'W001', '呼吸内科病区', '19580003', 30, 4, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000002', 'W002', '消化内科病区', '19580004', 30, 9, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000003', 'W003', '心血管内科病区', '19580005', 30, 5, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000004', 'W004', '神经内科病区', '19580006', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000005', 'W005', '内分泌科病区', '19580007', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000006', 'W006', '肾内科病区', '19580008', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000007', 'W007', '血液内科病区', '19580009', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000008', 'W008', '风湿免疫科病区', '19580010', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000009', 'W009', '老年病科病区', '19580011', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000010', 'W010', '全科医学科病区', '19580086', 30, 1, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000011', 'W011', '普通外科病区', '19580013', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000012', 'W012', '骨科病区', '19580014', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000013', 'W013', '神经外科病区', '19580015', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000014', 'W014', '心胸外科病区', '19580016', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000015', 'W015', '泌尿外科病区', '19580017', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000016', 'W016', '烧伤整形外科病区', '19580018', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000017', 'W017', '血管外科病区', '19580019', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000018', 'W018', '肝胆外科病区', '19580020', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000019', 'W019', '甲乳外科病区', '19580087', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000020', 'W020', '肛肠外科病区', '19580088', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000021', 'W021', '妇科病区', '19580022', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000022', 'W022', '产科病区', '19580023', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000023', 'W023', '小儿内科病区', '19580026', 24, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000024', 'W024', '小儿外科病区', '19580027', 24, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000025', 'W025', '新生儿科病区', '19580028', 24, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000026', 'W026', '急诊内科病区', '19580048', 30, 7, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000027', 'W027', '急诊外科病区', '19580049', 30, 1, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000028', 'W028', '重症医学科(ICU)病区', '19580050', 12, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000029', 'W029', '急诊儿科病区', '19580051', 30, 1, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000030', 'W030', '中医内科病区', '19580053', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000031', 'W031', '中医骨伤科病区', '19580054', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000032', 'W032', '针灸推拿科病区', '19580055', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000033', 'W033', '中医妇科病区', '19580056', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000034', 'W034', '中医儿科病区', '19580057', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000035', 'W035', '肿瘤内科病区', '19580059', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000036', 'W036', '肿瘤外科病区', '19580060', 30, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000037', 'W037', '安宁疗护病房病区', '19580062', 16, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000038', 'W038', '临床心理科病区', '19580065', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_ward (ward_id, ward_code, ward_name, dept_id, total_beds, occupied_beds, status, remark, create_by,
                      update_by, create_by_id, update_by_id)
VALUES ('8600000000000000039', 'W039', '肝病科病区', '19580070', 20, 0, 1, NULL, 'admin', 'admin', '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
