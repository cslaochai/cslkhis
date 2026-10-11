SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('2104803520954777601', '19580003', '呼吸内科', '897001005', '孟瑶', 2, 2, 0, NULL, NULL, NULL, '08:00', '12:00',
        '890000000000000001', 0, NULL, NULL, '0.00', '0.00', 0, '0.00', 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('2106690948695183361', '19580086', '全科医学科', '8900000000000040001', '医生甲', 1, 2, 0, NULL, NULL, NULL,
        '08:00', '17:00', '890000000000000003', 20, NULL, '', '0.00', '0.00', 1, '50.00', 1, 0, 1, 'admin', 'admin', 1,
        NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050001', '19580086', '全科医学科', '8900000000000040001', '医生甲', 1, 1, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 15, '8610000000000000026', '全科医学科一诊室', '30.00', '50.00', 1, '50.00', 1, 6, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050002', '19580086', '全科医学科', '8900000000000040001', '医生甲', 1, 3, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 15, '8610000000000000026', '全科医学科一诊室', '30.00', '50.00', 1, '50.00', 1, 6, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050003', '19580086', '全科医学科', '8900000000000040001', '医生甲', 1, 5, 0, NULL, NULL, 2,
        '14:00', '17:30', NULL, 12, '8610000000000000027', '全科医学科二诊室', '30.00', '50.00', 1, '50.00', 1, 4, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050004', '19580086', '全科医学科', '8900000000000040002', '医生乙', 1, 1, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 30, '8610000000000000027', '全科医学科二诊室', '15.00', '25.00', 0, '0.00', 1, 10, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050005', '19580086', '全科医学科', '8900000000000040002', '医生乙', 1, 3, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 30, '8610000000000000027', '全科医学科二诊室', '15.00', '25.00', 0, '0.00', 1, 10, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050006', '19580086', '全科医学科', '8900000000000040002', '医生乙', 1, 4, 0, NULL, NULL, 2,
        '14:00', '17:30', NULL, 25, '8610000000000000027', '全科医学科二诊室', '15.00', '25.00', 0, '0.00', 1, 8, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050007', '19580086', '全科医学科', '8900000000000040003', '医生丙', 1, 2, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 30, '8610000000000000028', '全科医学科三诊室', '10.00', '20.00', 0, '0.00', 1, 10, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050008', '19580086', '全科医学科', '8900000000000040003', '医生丙', 1, 4, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 30, '8610000000000000028', '全科医学科三诊室', '10.00', '20.00', 0, '0.00', 1, 10, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050009', '19580086', '全科医学科', '8900000000000040003', '医生丙', 1, 6, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 20, '8610000000000000028', '全科医学科三诊室', '10.00', '20.00', 0, '0.00', 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050010', '19580086', '全科医学科', '8900000000000040004', '医生丁', 1, 2, 0, NULL, NULL, 2,
        '14:00', '17:30', NULL, 25, '8610000000000000026', '全科医学科一诊室', '10.00', '20.00', 0, '0.00', 1, 8, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050011', '19580086', '全科医学科', '8900000000000040004', '医生丁', 1, 5, 0, NULL, NULL, 2,
        '14:00', '17:30', NULL, 25, '8610000000000000026', '全科医学科一诊室', '10.00', '20.00', 0, '0.00', 1, 8, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050012', '19580086', '全科医学科', '8900000000000040004', '医生丁', 1, 3, 0, NULL, NULL, 3,
        '08:00', '17:00', NULL, 40, '8610000000000000026', '全科医学科一诊室', '10.00', '20.00', 0, '0.00', 1, 15, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050013', '19580086', '全科医学科', '8900000000000040005', '医生戊', 1, 6, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 20, '8610000000000000028', '全科医学科三诊室', '10.00', '20.00', 0, '0.00', 1, 5, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050014', '19580086', '全科医学科', '8900000000000040005', '医生戊', 1, 7, 0, NULL, NULL, 1,
        '08:00', '12:00', NULL, 20, '8610000000000000028', '全科医学科三诊室', '10.00', '20.00', 0, '0.00', 1, 0, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO biz_clinic_source_template (id, dept_id, dept_name, doctor_id, doctor_name, staff_type, week_day, week_parity,
                                   valid_from, valid_until, schedule_type, start_time, end_time, shift_id, total_source,
                                   room_id, room_name, regist_fee, diagnosis_fee, is_expert, expert_fee, is_appointment,
                                   appointment_source, status, create_by, update_by, del_flag, remark, create_by_id,
                                   update_by_id)
VALUES ('8900000000000050015', '19580086', '全科医学科', '8900000000000040005', '医生戊', 1, 4, 0, NULL, NULL, 2,
        '18:00', '21:00', NULL, 18, '8610000000000000027', '全科医学科二诊室', '10.00', '20.00', 0, '0.00', 1, 6, 1,
        'admin', '', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
