SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('1', 'admin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '超级管理员', '1', 1, NULL, NULL,
        NULL, '2026-10-09 10:43:47', '0:0:0:0:0:0:0:1', 3190, '2026-06-08 09:30:00', 1, 'admin', 'system', 0, NULL, '1',
        '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('887009321', 'zhouyuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周远', '9301', 1,
        NULL, NULL, NULL, '2026-10-08 11:59:56', '0:0:0:0:0:0:0:1', 31, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('887009322', 'linxiaozhou', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '林小舟', '9302', 1,
        NULL, NULL, NULL, '2026-10-08 11:59:56', '0:0:0:0:0:0:0:1', 4, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('887009323', 'wutong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吴桐', '9303', 1, NULL,
        NULL, NULL, '2026-10-08 11:59:56', '0:0:0:0:0:0:0:1', 4, '2026-06-08 09:30:00', 1, 'admin', 'system', 0, NULL,
        '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003001', 'shennan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '沈楠', '897001001', 1,
        NULL, NULL, NULL, '2026-10-08 12:30:06', '0:0:0:0:0:0:0:1', 74, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003002', 'chenjianan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈建安',
        '897001002', 1, NULL, NULL, NULL, '2026-10-08 12:10:33', '0:0:0:0:0:0:0:1', 22, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003003', 'luochaoyang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '罗朝阳',
        '897001003', 1, NULL, NULL, NULL, '2026-10-08 11:59:56', '0:0:0:0:0:0:0:1', 4, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003004', 'lumingqian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '卢鸣谦',
        '897001004', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003005', 'mengyao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孟瑶', '897001005', 1,
        NULL, NULL, NULL, '2026-10-08 12:30:07', '0:0:0:0:0:0:0:1', 35, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003006', 'yangleyi', '$2a$10$Xkkx2mkJMpw89eY9ZtWGjOB9PYPE7DBx/pVQuPY97h2L48t8K3tqS', '杨乐怡', '897001006',
        1, NULL, NULL, NULL, '2026-10-08 11:05:29', '0:0:0:0:0:0:0:1', 26, '2026-10-04 09:50:45', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003007', 'shangmingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尚明珠',
        '897001007', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003008', 'wenhaotian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温昊天',
        '897001008', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003009', 'xumingde', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐明德', '897001009',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003010', 'lijianan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李建安', '897001010',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003011', 'manan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马楠', '897001011', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003012', 'kongqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孔青禾',
        '897001012', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003013', 'jiangjia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江佳', '897001013', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003014', 'shenwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '沈伟', '897001014', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003015', 'shijiaming', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '施佳明',
        '897001015', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003016', 'moyuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫远航',
        '897001016', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003017', 'zengjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曾洁', '897001017', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003018', 'wangsheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王升', '897001018',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003019', 'qiuhaotian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邱昊天',
        '897001019', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003020', 'kangyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康一诺', '897001020',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003021', 'guhanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '顾寒梅', '897001021',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003022', 'nitian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '倪天', '897001022', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003023', 'kangyihang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康亦航',
        '897001023', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003024', 'chenzeyu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈泽宇', '897001024',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003025', 'fanlicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '范立诚',
        '897001025', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003026', 'xuheyu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐和玉', '897001026', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003027', 'jinbailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '金白露', '897001027',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003028', 'dengxinyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邓心怡', '897001028',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003029', 'yangyuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨远航',
        '897001029', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003030', 'xiaolei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '肖蕾', '897001030', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003031', 'tianna', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '田娜', '897001031', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003032', 'yanshusheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严树声',
        '897001032', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003033', 'xiangyixiu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向宜修',
        '897001033', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003034', 'luomingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '罗明珠',
        '897001034', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003035', 'fengming', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '冯明', '897001035', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003036', 'guohuaijin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郭怀瑾',
        '897001036', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003037', 'xuting', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许婷', '897001037', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003038', 'ronggang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '容纲', '897001038', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003039', 'maocaiwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '毛采薇', '897001039',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003040', 'hongwanqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '洪晚晴',
        '897001040', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003041', 'shenshu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '沈舒', '897001041', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003042', 'shijing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '施静', '897001042', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003043', 'lvmingde', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吕明德', '897001043',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003044', 'cuizihan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '崔紫涵', '897001044',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003045', 'yinyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹一诺', '897001045',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003046', 'suleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏乐怡', '897001046', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003047', 'huangrong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '黄蓉', '897001047',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003048', 'dongyixiu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董宜修', '897001048',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003049', 'suxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏秀英', '897001049',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003050', 'huzheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡正', '897001050', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003051', 'rongmingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '容明珠',
        '897001051', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003052', 'linyingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '林映竹',
        '897001052', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003053', 'lusheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '卢升', '897001053', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003054', 'zhuting', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱婷', '897001054', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003055', 'moqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫琪', '897001055', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003056', 'zengxinyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曾心怡', '897001056',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003057', 'qiansong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱松', '897001057', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003058', 'zhenghaiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郑海艳',
        '897001058', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003059', 'huangyanping', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '黄延平',
        '897001059', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003060', 'haoruchu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郝如初', '897001060',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003061', 'hemin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '何敏', '897001061', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003062', 'yutong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '于彤', '897001062', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003063', 'cainianan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蔡念安', '897001063',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003064', 'sunshusheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孙树声',
        '897001064', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003065', 'fengruotong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '冯若彤',
        '897001065', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003066', 'linyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '林一诺', '897001066',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003067', 'mengpengfei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孟鹏飞',
        '897001067', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003068', 'qiujia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '裘佳', '897001068', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003069', 'heqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '贺青禾', '897001069',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003070', 'houhui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '侯慧', '897001070', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003071', 'xiaozhiyuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '肖致远',
        '897001071', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003072', 'luozhiyuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '罗知远',
        '897001072', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003073', 'xielicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谢立诚',
        '897001073', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003074', 'xuqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐庆', '897001074', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003075', 'shenshuhua', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '沈淑华',
        '897001075', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003076', 'qinshu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '秦舒', '897001076', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003077', 'kangzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康子轩',
        '897001077', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003078', 'duzhiyuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杜致远', '897001078',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003079', 'xucheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许成', '897001079', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003080', 'yetao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '叶涛', '897001080', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003081', 'liushan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '刘山', '897001081', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003082', 'kangxue', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康雪', '897001082', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003083', 'dongxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董欣', '897001083', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003084', 'yaoli', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '姚丽', '897001084', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003085', 'xiechenxi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谢晨曦', '897001085',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003086', 'tanling', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谭玲', '897001086', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003087', 'songwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '宋维', '897001087', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003088', 'lucheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陆成', '897001088', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003089', 'wanxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '万欣', '897001089', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003090', 'shenqian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '沈倩', '897001090', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003091', 'jiangyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蒋一诺',
        '897001091', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003092', 'liaomingde', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '廖明德',
        '897001092', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003093', 'caijing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蔡婧', '897001093', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003094', 'mengzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孟子轩',
        '897001094', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003095', 'jiangmengjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江梦洁',
        '897001095', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003096', 'qianyuyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱语嫣', '897001096',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003097', 'xuzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许子轩', '897001097',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003098', 'qiuguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邱国强',
        '897001098', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003099', 'xiaoleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '肖乐怡', '897001099',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003100', 'weiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '魏妍', '897001100', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003101', 'mayuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马远航',
        '897001101', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003102', 'wujia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吴佳', '897001102', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003103', 'fengqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '冯青禾',
        '897001103', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003104', 'yangyuyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨语嫣', '897001104',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003105', 'zengmengjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曾梦洁',
        '897001105', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003106', 'luorenxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '罗仁心', '897001106',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003107', 'lishusheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李树声',
        '897001107', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003108', 'guoguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郭桂芳',
        '897001108', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003109', 'songhai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '宋海', '897001109', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003110', 'mengxinyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孟心怡', '897001110',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003111', 'pengbai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭柏', '897001111', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003112', 'wuwan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吴婉', '897001112', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003113', 'yanlicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严立诚',
        '897001113', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003114', 'xiegang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谢纲', '897001114', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003115', 'dinglan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '丁岚', '897001115', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003116', 'zhaoyixiu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '赵宜修', '897001116',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003117', 'zhujie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱洁', '897001117', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003118', 'huyuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡远航',
        '897001118', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003119', 'nixinyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '倪心怡', '897001119',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003120', 'xuda', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐达', '897001120', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003121', 'qianqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱庆', '897001121', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003122', 'xiangguanlan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向观澜',
        '897001122', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003123', 'linjing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '林婧', '897001123', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003124', 'pengruotong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭若彤',
        '897001124', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003125', 'hewei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '贺薇', '897001125', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003126', 'lidong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李栋', '897001126', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003127', 'cuilei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '崔磊', '897001127', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003128', 'maonan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '毛楠', '897001128', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003129', 'qianjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱捷', '897001129', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003130', 'zhuyuelong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱跃龙',
        '897001130', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003131', 'xiejia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谢佳', '897001131', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003132', 'dengchunyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邓春燕',
        '897001132', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003133', 'dongmingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董明珠',
        '897001133', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003134', 'yutao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '于涛', '897001134', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003135', 'cenrui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '岑瑞', '897001135', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003136', 'qiuyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '裘月白', '897001136',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003137', 'caojing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曹婧', '897001137', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003138', 'xueda', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '薛达', '897001138', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003139', 'duxuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杜萱', '897001139', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003140', 'wenzhiwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温知微', '897001140',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003141', 'xuyulan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许玉兰', '897001141',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003142', 'qinhao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '秦浩', '897001142', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003143', 'shanghanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尚寒梅',
        '897001143', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003144', 'wangmingqian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王鸣谦',
        '897001144', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003145', 'liling', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李玲', '897001145', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003146', 'wangmin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王敏', '897001146', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003147', 'xianghanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向寒梅',
        '897001147', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003148', 'hutian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡天', '897001148', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003149', 'lujing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '卢静', '897001149', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003150', 'yuguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '于国强',
        '897001150', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003151', 'huangxuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '黄萱', '897001151',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003152', 'rencaiwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '任采薇', '897001152',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003153', 'zengsiqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曾思琪', '897001153',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003154', 'zhouheyu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周和玉', '897001154',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003155', 'longsong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '龙松', '897001155', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003156', 'suxiuqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏修齐', '897001156',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003157', 'gaojie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '高捷', '897001157', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003158', 'qianguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱国强',
        '897001158', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003159', 'penganya', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭安雅', '897001159',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003160', 'huangxue', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '黄雪', '897001160', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003161', 'luxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陆秀英', '897001161',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003162', 'kangguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康国强',
        '897001162', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003163', 'fanjingwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '范静文',
        '897001163', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003164', 'xiaoxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '肖秀英',
        '897001164', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003165', 'tangzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '唐子轩',
        '897001165', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003166', 'xuleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐乐怡', '897001166', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003167', 'yintian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹天', '897001167', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003168', 'tangna', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '唐娜', '897001168', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003169', 'houguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '侯桂芳',
        '897001169', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003170', 'xianghoupu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向厚朴',
        '897001170', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003171', 'qilu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '齐璐', '897001171', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003172', 'cenrenxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '岑仁心', '897001172',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003173', 'yanyao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严瑶', '897001173', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003174', 'wenjingwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温静文',
        '897001174', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003175', 'zhaofang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '赵芳', '897001175', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003176', 'qianguanlan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱观澜',
        '897001176', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003177', 'songyao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '宋瑶', '897001177', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003178', 'hanpengfei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '韩鹏飞',
        '897001178', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003179', 'jianghanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江寒梅',
        '897001179', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003180', 'duguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杜桂芳', '897001180',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003181', 'qiuqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邱青禾', '897001181',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003182', 'wangleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王乐怡', '897001182',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003183', 'fengda', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '冯达', '897001183', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003184', 'qinchunyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '秦春燕',
        '897001184', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003185', 'dingjun', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '丁军', '897001185', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003186', 'mabingjun', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马秉钧', '897001186',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003187', 'caixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蔡萱', '897001187', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003188', 'wanguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '万桂芳',
        '897001188', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003189', 'maohong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '毛宏', '897001189', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003190', 'huangmingde', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '黄明德',
        '897001190', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003191', 'sushan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏山', '897001191', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003192', 'longguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '龙桂芳',
        '897001192', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003193', 'chengtian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '程天', '897001193',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003194', 'liulan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '刘岚', '897001194', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003195', 'cuijie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '崔洁', '897001195', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003196', 'houmingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '侯明珠',
        '897001196', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003197', 'guobai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郭柏', '897001197', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003198', 'liangqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '梁琪', '897001198', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003199', 'yuannianan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁念安',
        '897001199', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003200', 'yuannaiwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁乃文',
        '897001200', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003201', 'luoyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '罗妍', '897001201', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003202', 'moxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫欣', '897001202', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003203', 'mowan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫婉', '897001203', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003204', 'chenqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈庆', '897001204', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003205', 'songshuhua', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '宋淑华',
        '897001205', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003206', 'shina', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '施娜', '897001206', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003207', 'maozixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '毛子轩', '897001207',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003208', 'maolan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '毛岚', '897001208', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003209', 'yangtong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨彤', '897001209', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003210', 'yangzhiwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨知微',
        '897001210', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003211', 'qiuhong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邱宏', '897001211', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003212', 'xuchunyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐春燕', '897001212',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003213', 'lianghoupu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '梁厚朴',
        '897001213', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003214', 'xiangwan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向婉', '897001214', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003215', 'huxue', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡雪', '897001215', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003216', 'panyuyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '潘语嫣', '897001216',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003217', 'liangguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '梁桂芳',
        '897001217', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003218', 'renrui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '任瑞', '897001218', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 2, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003219', 'kangjingwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康静文',
        '897001219', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003220', 'caorenxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曹仁心', '897001220',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003221', 'guguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '顾桂芳', '897001221',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003222', 'yaotong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '姚彤', '897001222', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003223', 'shiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '石妍', '897001223', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003224', 'hejunjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '贺俊杰', '897001224',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003225', 'lvrui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吕瑞', '897001225', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003226', 'dubailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杜白露', '897001226',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003227', 'penghaotian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭昊天',
        '897001227', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003228', 'tianda', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '田达', '897001228', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003229', 'tangtong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '唐彤', '897001229', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003230', 'guoqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郭青禾', '897001230',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 2, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003231', 'liangtong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '梁彤', '897001231',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003232', 'zhengyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郑一诺',
        '897001232', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003233', 'xienan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谢楠', '897001233', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003234', 'huxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡秀英', '897001234',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003235', 'pengjia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭佳', '897001235', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003236', 'weiyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '魏月白', '897001236',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003237', 'maqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马青禾', '897001237',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003238', 'yangyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨怡', '897001238', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003239', 'zhengshian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郑世安',
        '897001239', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003240', 'qihaotian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '齐昊天', '897001240',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003241', 'zhouhaiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周海艳',
        '897001241', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003242', 'qiucaiwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '裘采薇', '897001242',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003243', 'qixue', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '齐雪', '897001243', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003244', 'lushuhua', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '卢淑华', '897001244',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003245', 'shijing2', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '施婧', '897001245', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003246', 'yeyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '叶一诺', '897001246',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003247', 'momengjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫梦洁', '897001247',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003248', 'chenrui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈瑞', '897001248', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003249', 'chenjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈捷', '897001249', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003250', 'yinxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹欣', '897001250', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003251', 'suguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏桂芳', '897001251',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003252', 'mengnianan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孟念安',
        '897001252', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003253', 'kangchengyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康承翼',
        '897001253', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003254', 'pengzihan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭紫涵', '897001254',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003255', 'wenlicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温立诚',
        '897001255', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003256', 'yushu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '余舒', '897001256', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003257', 'leiyihang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '雷亦航', '897001257',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003258', 'rongjia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '容佳', '897001258', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003259', 'dengshan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邓山', '897001259', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003260', 'qianyuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱远航',
        '897001260', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003261', 'dengtong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邓彤', '897001261', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003262', 'yanxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严秀英',
        '897001262', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003263', 'morui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫瑞', '897001263', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003264', 'heqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '贺琪', '897001264', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003265', 'wangxinyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王心怡', '897001265',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003266', 'jianganya', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蒋安雅', '897001266',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003267', 'shijiajie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '石嘉杰', '897001267',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003268', 'jinxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '金欣', '897001268', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003269', 'qinan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '齐楠', '897001269', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003270', 'limin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李敏', '897001270', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003271', 'wenmingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温明珠',
        '897001271', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003272', 'sumin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏敏', '897001272', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003273', 'zhuchunyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱春燕',
        '897001273', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003274', 'yanguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严国强',
        '897001274', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003275', 'kangjianguo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康建国',
        '897001275', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003276', 'jinyao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '金瑶', '897001276', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003277', 'yinwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹维', '897001277', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003278', 'fanhanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '范寒梅', '897001278',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003279', 'chenghanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '程寒梅',
        '897001279', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003280', 'mamengjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马梦洁', '897001280',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003281', 'qinhoupu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '秦厚朴', '897001281',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003282', 'duxiaofeng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杜晓峰',
        '897001282', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003283', 'yejie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '叶洁', '897001283', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003284', 'dinglu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '丁璐', '897001284', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003285', 'cenqinghe', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '岑青禾', '897001285',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003286', 'dingyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '丁一诺', '897001286',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003287', 'zhouying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周颖', '897001287', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003288', 'shiheyu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '石和玉', '897001288',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003289', 'pengting', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭婷', '897001289', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003290', 'lusong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '卢松', '897001290', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003291', 'shangsiqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尚思琪', '897001291',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003292', 'houxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '侯秀英',
        '897001292', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003293', 'longli', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '龙丽', '897001293', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003294', 'lvwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吕薇', '897001294', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003295', 'hehong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '何宏', '897001295', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003296', 'zhoujiayi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周嘉怡', '897001296',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003297', 'xuhanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐寒梅', '897001297',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003298', 'chengjing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '程静', '897001298',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003299', 'lumin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陆敏', '897001299', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003300', 'xiahao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '夏浩', '897001300', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003301', 'mengling', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孟玲', '897001301', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003302', 'tanwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谭薇', '897001302', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003303', 'sunzhiyuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孙致远',
        '897001303', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003304', 'yaoyuxuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '姚宇轩', '897001304',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003305', 'xuyanping', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐延平', '897001305',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003306', 'liuleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '刘乐怡', '897001306',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003307', 'dongyao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董瑶', '897001307', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003308', 'tangxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '唐欣', '897001308', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003309', 'yumingde', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '余明德', '897001309',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003310', 'zhuhaiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱海艳', '897001310',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003311', 'hedong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '何栋', '897001311', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003312', 'liangyingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '梁映竹',
        '897001312', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003313', 'guojie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郭洁', '897001313', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003314', 'majingwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马静文', '897001314',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003315', 'liuyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '刘月白', '897001315',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003316', 'xueyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '薛月白', '897001316',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003317', 'weilei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '魏磊', '897001317', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003318', 'suhaiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏海艳', '897001318',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003319', 'kangrong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康蓉', '897001319', 1,
        NULL, NULL, NULL, '2026-10-08 09:46:06', '0:0:0:0:0:0:0:1', 9, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003320', 'xuebailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '薛白露', '897001320',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003321', 'huchaoyang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡朝阳',
        '897001321', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003322', 'xuyixiu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许宜修', '897001322',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003323', 'zhengjingwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郑静文',
        '897001323', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003324', 'huyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '胡怡', '897001324', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003325', 'maosheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '毛升', '897001325', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 2, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003326', 'yinhoupu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹厚朴', '897001326',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003327', 'xiangbailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向白露',
        '897001327', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003328', 'shilan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '石岚', '897001328', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003329', 'lirong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李蓉', '897001329', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003330', 'cenjing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '岑静', '897001330', 1,
        NULL, NULL, NULL, '2026-10-08 12:30:06', '0:0:0:0:0:0:0:1', 29, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003331', 'xuesiqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '薛思琪', '897001331',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003332', 'dongchunyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董春燕',
        '897001332', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003333', 'yaozihan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '姚紫涵', '897001333',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003334', 'lichenxi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李晨曦', '897001334',
        1, NULL, NULL, NULL, '2026-10-08 11:34:47', '0:0:0:0:0:0:0:1', 11, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003335', 'cenguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '岑国强',
        '897001335', 1, NULL, NULL, NULL, '2026-10-08 11:34:47', '0:0:0:0:0:0:0:1', 11, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003336', 'zhuhoupu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱厚朴', '897001336',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003337', 'wanganya', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王安雅', '897001337',
        1, NULL, NULL, NULL, '2026-10-08 11:34:47', '0:0:0:0:0:0:0:1', 14, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003338', 'qiwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '齐伟', '897001338', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003339', 'gaojia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '高佳', '897001339', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003340', 'jinyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '金妍', '897001340', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003341', 'xueyuyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '薛语嫣', '897001341',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003342', 'guyixiu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '顾宜修', '897001342',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003343', 'panleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '潘乐怡', '897001343',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003344', 'songchunyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '宋春燕',
        '897001344', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003345', 'mahaiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '马海艳', '897001345',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 12, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003346', 'yuanguoqiang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁国强',
        '897001346', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 4, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003347', 'yuanheyu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁和玉', '897001347',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003348', 'liusiqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '刘思琪', '897001348',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003349', 'jiangna', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江娜', '897001349', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003350', 'chenyihang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈亦航',
        '897001350', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003351', 'wenjunjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温俊杰', '897001351',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003352', 'xiangling', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向玲', '897001352',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003353', 'helu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '何璐', '897001353', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003354', 'yuanqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁庆', '897001354', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003355', 'kanghui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康慧', '897001355', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003356', 'xuyuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '徐远航',
        '897001356', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003357', 'qianyixiu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱宜修', '897001357',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003358', 'yuanlan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁岚', '897001358', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003359', 'yangtian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨天', '897001359', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003360', 'songqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '宋琪', '897001360', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003361', 'shiqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '施庆', '897001361', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003362', 'yinhanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹寒梅', '897001362',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003363', 'qiutian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '裘天', '897001363', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003364', 'chenmingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陈明珠',
        '897001364', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003365', 'leibailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '雷白露', '897001365',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003366', 'xiangsiqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向思琪', '897001366',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003367', 'zengguang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曾光', '897001367',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003368', 'xialei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '夏蕾', '897001368', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003369', 'yechuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '叶川', '897001369', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003370', 'caihanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蔡寒梅', '897001370',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003371', 'zhouyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周妍', '897001371', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003372', 'liaogang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '廖纲', '897001372', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003373', 'xiaoyanping', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '肖延平',
        '897001373', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003374', 'jiangyuelong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江跃龙',
        '897001374', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003375', 'liaohanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '廖寒梅',
        '897001375', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003376', 'tianyuanhang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '田远航',
        '897001376', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003377', 'fengjiayi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '冯嘉怡', '897001377',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003378', 'yangruchu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨如初', '897001378',
        1, NULL, NULL, NULL, '2026-10-08 15:43:00', '127.0.0.1', 23, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003379', 'xiangxiuying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向秀英',
        '897001379', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003380', 'jiangzhiyuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蒋致远',
        '897001380', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003381', 'xuexuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '薛萱', '897001381', 1,
        NULL, NULL, NULL, '2026-10-08 10:24:12', '0:0:0:0:0:0:0:1', 16, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003382', 'xiaowei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '肖薇', '897001382', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003383', 'dongxinyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董心怡', '897001383',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003384', 'zhoulicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周立诚',
        '897001384', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003385', 'yanyingzhu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严映竹',
        '897001385', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003386', 'denghoupu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邓厚朴', '897001386',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003387', 'jinlicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '金立诚',
        '897001387', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003388', 'dengqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '邓庆', '897001388', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003389', 'tiancheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '田成', '897001389',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003390', 'zhuyuyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '朱语嫣', '897001390',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003391', 'jiangqian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江倩', '897001391',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003392', 'moli', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫丽', '897001392', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003393', 'chengzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '程子轩',
        '897001393', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003394', 'shiruotong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '石若彤',
        '897001394', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003395', 'donghanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董寒梅',
        '897001395', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003396', 'jiangyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蒋月白',
        '897001396', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003397', 'zhengwan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郑婉', '897001397', 1,
        NULL, NULL, NULL, '2026-10-03 21:36:57', '0:0:0:0:0:0:0:1', 7, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003398', 'zhangyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '张月白',
        '897001398', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003399', 'gutao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '顾涛', '897001399', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003400', 'xiangwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '向维', '897001400', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003401', 'chengruotong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '程若彤',
        '897001401', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003402', 'yangxue', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '杨雪', '897001402', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003403', 'dongqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '董琪', '897001403', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003404', 'helicheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '何立诚', '897001404',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003405', 'shimin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '施敏', '897001405', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003406', 'luhanmei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '卢寒梅', '897001406',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003407', 'shanghui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尚慧', '897001407', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003408', 'shina2', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '石娜', '897001408', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003409', 'wanyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '万月白', '897001409',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003410', 'houshan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '侯山', '897001410', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003411', 'liruchu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李如初', '897001411',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003412', 'zhaoxiaofeng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '赵晓峰',
        '897001412', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003413', 'hongwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '洪薇', '897001413', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003414', 'suqian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏倩', '897001414', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003415', 'mojingwen', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '莫静文', '897001415',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003416', 'litong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李彤', '897001416', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003417', 'yanbailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '严白露', '897001417',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003418', 'jiangbailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江白露',
        '897001418', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003419', 'yurenxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '于仁心', '897001419',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003420', 'suwan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏婉', '897001420', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003421', 'wusheng', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吴升', '897001421', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003422', 'chenghong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '程宏', '897001422',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 5, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003423', 'wenbingjun', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '温秉钧',
        '897001423', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003424', 'yulu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '余璐', '897001424', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 2, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003425', 'cailu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蔡璐', '897001425', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003426', 'xuyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许妍', '897001426', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003427', 'jiangpengfei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '江鹏飞',
        '897001427', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003428', 'kangmin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康敏', '897001428', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 5, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003429', 'xiamengjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '夏梦洁',
        '897001429', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003430', 'qinguanlan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '秦观澜',
        '897001430', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003431', 'luohui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '罗慧', '897001431', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003432', 'longyuebai', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '龙月白',
        '897001432', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003433', 'wangyinuo', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '王一诺', '897001433',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003434', 'suhui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '苏慧', '897001434', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 5, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003435', 'lvwei2', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吕伟', '897001435', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003436', 'hanlei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '韩蕾', '897001436', 1,
        NULL, NULL, NULL, '2026-10-08 11:34:47', '0:0:0:0:0:0:0:1', 50, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003437', 'yuanchenxi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '袁晨曦',
        '897001437', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003438', 'fengruchu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '冯如初', '897001438',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003439', 'guleyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '顾乐怡', '897001439', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 4, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003440', 'houyihang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '侯亦航', '897001440',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003441', 'xiaguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '夏桂芳',
        '897001441', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003442', 'pengying', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭颖', '897001442', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003443', 'lirui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李瑞', '897001443', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 5, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003444', 'penghui', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭慧', '897001444', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003445', 'rongxin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '容欣', '897001445', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003446', 'jiangjunjie', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '蒋俊杰',
        '897001446', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003447', 'yinzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '尹子轩', '897001447',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003448', 'xuyuelong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '许跃龙', '897001448',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003449', 'lvjianan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '吕建安', '897001449',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 0, '2026-06-08 09:30:00', 0, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003450', 'longjiaming', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '龙佳明',
        '897001450', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003451', 'longzixuan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '龙子轩',
        '897001451', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003452', 'zhengtian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '郑天', '897001452',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003453', 'qianhaiyan', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱海艳',
        '897001453', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003454', 'heruotong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '贺若彤', '897001454',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003455', 'gaoheyu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '高和玉', '897001455',
        1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system',
        0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003456', 'kangyao', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '康瑶', '897001456', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003457', 'luxue', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '陆雪', '897001457', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003458', 'shenmingqian', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '沈鸣谦',
        '897001458', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('897003459', 'linrong', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '林蓉', '897001459', 1,
        NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00', 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8999999001', '13899000001', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '验证患者A', NULL,
        3, '8', 'mock_wx_4071f718ccab404c78ce63f0', NULL, '2026-10-08 15:43:53', '127.0.0.1', 59, '2026-06-08 09:30:00',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8999999002', '13899000002', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '验证患者B', NULL,
        3, '8900000000000900099', NULL, NULL, '2026-10-08 15:44:26', '127.0.0.1', 11, '2026-06-08 09:30:00', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000001801', 'liguifang', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '李桂芳',
        '896600000000000101', 1, NULL, NULL, NULL, '2026-10-07 19:27:20', '127.0.0.1', 13, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000001802', 'zhouli', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '周丽',
        '896600000000000102', 1, NULL, NULL, NULL, '2026-10-03 21:36:57', '0:0:0:0:0:0:0:1', 20, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000001803', 'sunlihua', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '孙丽华',
        '896600000000000111', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000001804', 'qianyaqin', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '钱雅琴',
        '896600000000000112', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000001805', 'caoyumei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '曹玉梅',
        '896600000000000121', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896600000000001806', 'pengsiqi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '彭思琪',
        '896600000000000122', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 1, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('896809280000000301', 'hanyuwei', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '韩雨薇',
        '896809280000000101', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 12, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('2098255864191287299', 'renyongxing', '$2a$10$9pLPpOeGvHQpgpsm.zbxz.tINYZiOkjgzBL4ovGR55ZuESbpMJ2SC', '任永星',
        '2098255864065458177', 1, NULL, NULL, NULL, '2026-10-08 15:43:28', '127.0.0.1', 1809, '2026-06-08 09:30:00', 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('2106561412607344642', '13978353278', '$2a$10$ZDkagthMY8pHS.JJaZaSFuoGzD6qHbLqfWHbsOl0SmEMogq3V0wya',
        'SM2验证患者', NULL, 3, '2106561412347297793', NULL, NULL, '2026-10-04 09:45:54', '0:0:0:0:0:0:0:1', 1, NULL, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('2106562637776027649', '13978645393', '$2a$10$Z.GNOP412C3Qp7wnyOMaZOys5TRj.6pfqcQkWGU71k4FyipNvW4di',
        'SM2验证患者', NULL, 3, '2106562637515980801', NULL, NULL, '2026-10-04 09:50:46', '0:0:0:0:0:0:0:1', 1, NULL, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000041001', 'yishengjia', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '医生甲',
        '8900000000000040001', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 30, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000041002', 'yishengyi', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '医生乙',
        '8900000000000040002', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 3, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000041003', 'yishengbing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '医生丙',
        '8900000000000040003', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 8, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000041004', 'yishengding', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '医生丁',
        '8900000000000040004', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 2, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000041005', 'yishengwu', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '医生戊',
        '8900000000000040005', 1, NULL, NULL, NULL, '2026-10-03 21:21:30', '0:0:0:0:0:0:0:1', 2, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000070001', 'kefuzhangjing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca',
        '客服-张静', '8900000000000070001', 1, NULL, NULL, NULL, '2026-10-03 21:36:57', '0:0:0:0:0:0:0:1', 18,
        '2026-06-08 09:30:00', 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                      last_login_time, last_login_ip, login_count, password_update_time, status, create_by, update_by,
                      del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000070002', 'kefulina', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '客服-李娜',
        '8900000000000070002', 1, NULL, NULL, NULL, '2026-10-03 21:36:57', '0:0:0:0:0:0:0:1', 9, '2026-06-08 09:30:00',
        1, 'admin', 'system', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
