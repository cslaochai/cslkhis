SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000001', 'EX-CT01', '16排螺旋CT', 1, '3', '19580036', '放射科', 'CT1号机房', '08:00', '12:00',
        '14:30', '17:30', 30, 1, 7, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000002', 'EX-CT02', '超高端128排256层CT', 1, '1', '19580036', '放射科', 'CT2号机房', '08:00',
        '12:00', '14:30', '17:30', 30, 1, 7, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000003', 'EX-MR01', '3.0T磁共振', 2, '4', '19580036', '放射科', 'MR1号机房', '08:30', '12:00',
        '14:00', '17:30', 30, 1, 7, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000004', 'EX-MR02', '1.5T磁共振', 2, '5', '19580036', '放射科', 'MR2号机房', '08:30', '12:00',
        '14:00', '17:30', 30, 1, 7, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000005', 'EX-DR01', '数字化X线摄影(DR)', 3, '8', '19580036', '放射科', 'DR1号诊室', '08:00',
        '12:00', '14:30', '17:30', 15, 2, 7, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000006', 'EX-US01', '全身彩色多普勒超声', 4, '14', '19580037', '超声医学科', '超声3诊室', '08:00',
        '12:00', '14:30', '17:30', 20, 2, 5, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000007', 'EX-ECG01', '十二导心电图机', 5, '67', '19580034', '医技科室', '心电功能室', '08:00',
        '12:00', '14:30', '17:00', 15, 1, 3, 120, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_exam_device (id, device_code, device_name, device_type, equipment_id, dept_id, dept_name, room_name,
                             am_start, am_end, pm_start, pm_end, slot_minutes, parallel_count, ahead_days,
                             max_slot_minutes, status, create_by, update_by, del_flag, remark, create_by_id,
                             update_by_id)
VALUES ('2026000000000000008', 'EX-ENDO01', '电子胃肠镜主机', 6, NULL, '19580034', '医技科室', '内镜中心2号室', '08:00',
        '11:30', '13:30', '16:30', 30, 1, 7, 240, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
