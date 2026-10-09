SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000001', '上午门诊', '08:00', '12:00', 0, 0, '0.0', 15, 240, NULL, 1, 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000002', '下午门诊', '14:00', '17:30', 0, 0, '0.0', 15, 210, NULL, 2, 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000003', '全天门诊', '08:00', '17:00', 0, 0, '0.0', 15, 540, NULL, 3, 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000004', '晚间门诊', '18:00', '21:00', 0, 0, '0.0', 15, 180, NULL, 2, 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000005', '专家门诊（上午）', '08:00', '12:00', 0, 0, '0.0', 15, 240, NULL, 1, 1, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000006', '专家门诊（下午）', '14:00', '17:30', 0, 0, '0.0', 15, 210, NULL, 2, 1, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000007', '急诊白班', '08:00', '16:00', 0, 0, '0.0', 15, 480, NULL, 3, 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000008', '急诊前夜班', '16:00', '23:00', 0, 1, '16.0', 15, 420, NULL, 2, 1, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000009', '急诊后夜班', '00:00', '08:00', 0, 1, '16.0', 15, 480, NULL, 4, 1, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000011', '护理白班', '08:00', '16:00', 0, 0, '0.0', 15, 480, NULL, NULL, 2, 2, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000012', '护理主班', '08:00', '17:30', 0, 0, '0.0', 15, 570, NULL, NULL, 2, 2, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000013', '护理帮班', '11:00', '19:00', 0, 0, '0.0', 15, 480, NULL, NULL, 2, 2, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000014', '护理前夜班', '16:00', '23:00', 0, 1, '16.0', 15, 420, NULL, NULL, 2, 2, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000015', '护理后夜班', '00:00', '08:00', 0, 1, '16.0', 15, 480, NULL, NULL, 2, 2, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000021', '总值班白班', '08:00', '18:00', 0, 0, '0.0', 15, 600, NULL, NULL, 3, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('890000000000000022', '总值班夜班', '18:00', '08:00', 1, 1, '16.0', 15, 840, NULL, NULL, 3, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896730000000000101', '全院白班', '08:00', '17:30', 0, 0, '0.0', 15, 570, NULL, NULL, 4, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896730000000000102', '全院中班', '13:00', '21:00', 0, 0, '0.0', 15, 480, NULL, NULL, 4, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896730000000000103', '全院夜班', '21:00', '08:00', 1, 1, '16.0', 15, 660, NULL, NULL, 4, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896730000000000104', '医师值班白班', '08:00', '18:00', 0, 0, '0.0', 15, 600, NULL, NULL, 4, 1, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, is_night, need_rest_hours, late_grace_minutes,
                       duration_minutes, dept_id, schedule_type, use_scope, apply_staff_type, status, create_by,
                       update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('896730000000000105', '医师值班夜班', '18:00', '08:00', 1, 1, '16.0', 15, 840, NULL, NULL, 4, 1, 1, 'admin', '',
        0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
