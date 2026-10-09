SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_operation_room (id, room_code, room_name, location, sort_order, status, remark, create_by, update_by,
                                del_flag, create_by_id, update_by_id)
VALUES ('89000000000001301', 'OR01', '1号手术间', '外科手术室三楼 东侧', 1, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_operation_room (id, room_code, room_name, location, sort_order, status, remark, create_by, update_by,
                                del_flag, create_by_id, update_by_id)
VALUES ('89000000000001302', 'OR02', '2号手术间', '外科手术室三楼 东侧', 2, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_operation_room (id, room_code, room_name, location, sort_order, status, remark, create_by, update_by,
                                del_flag, create_by_id, update_by_id)
VALUES ('89000000000001303', 'OR03', '3号手术间', '外科手术室三楼 西侧', 3, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_operation_room (id, room_code, room_name, location, sort_order, status, remark, create_by, update_by,
                                del_flag, create_by_id, update_by_id)
VALUES ('89000000000001304', 'OR04', '4号手术间', '外科手术室三楼 西侧', 4, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_operation_room (id, room_code, room_name, location, sort_order, status, remark, create_by, update_by,
                                del_flag, create_by_id, update_by_id)
VALUES ('89000000000001305', 'OR05', '5号手术间', '外科手术室四楼 东侧', 5, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_operation_room (id, room_code, room_name, location, sort_order, status, remark, create_by, update_by,
                                del_flag, create_by_id, update_by_id)
VALUES ('89000000000001306', 'OR06', '6号手术间', '外科手术室四楼 西侧', 6, 1, NULL, 'admin', 'admin', 0, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
