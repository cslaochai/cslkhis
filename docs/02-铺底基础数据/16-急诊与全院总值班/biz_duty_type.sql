-- biz_duty_type  铺底数据：4 行（按主键序 自 dev 库 2026-11-20 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_duty_type` (`id`, `type_code`, `type_name`, `manage_dept_id`, `manage_dept_name`, `required_staff_type`, `required_positions`, `leader_layer`, `sort_no`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES
(896750000000000001, 'MEDICAL', '医疗总值班', 19580077, '医务科', 1, '1,2,16', 1, 1, 1, '临床、医技科室正/副主任担任，医务科按月排班', 'admin', '2026-11-16 11:07:31', 'admin', '2026-11-16 11:07:31', 0, 1, 1),
(896750000000000002, 'NURSING', '护理总值班', 19580078, '护理部', 2, '7,8,9', 1, 2, 1, '全院护士长轮流承担，护理部按月排班', 'admin', '2026-11-16 11:07:31', 'admin', '2026-11-16 11:07:31', 0, 1, 1),
(896750000000000003, 'ADMIN', '行政总值班', 19580076, '院办', 6, '21,22,23,24', 1, 3, 1, '职能科室中层与行政干事担任，院办按月排班并全院公开', 'admin', '2026-11-16 11:07:31', 'admin', '2026-11-16 11:07:31', 0, 1, 1),
(896750000000000004, 'LEADER', '院领导带班', 19580076, '院办', 6, '21', 0, 4, 1, '院级领导二线带班，院办随同月度值班表一并排', 'admin', '2026-11-16 11:07:31', 'admin', '2026-11-16 11:07:31', 0, 1, 1);
COMMIT;
