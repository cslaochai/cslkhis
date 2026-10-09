SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003001', '1', 'myTodo', 10, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003002', '1', 'quickEntry', 20, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003003', '1', 'hospitalToday', 30, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003004', '1', 'registToday', 40, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003005', '1', 'deptVisitRank', 50, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003006', '1', 'weekVisitTrend', 60, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003007', '1', 'chargeToday', 70, 1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_layout (id, user_id, widget_code, sort_order, visible, remark, create_by, update_by, del_flag,
                                  create_by_id, update_by_id)
VALUES ('2360000000000003008', '1', 'insuranceSettle', 80, 0, NULL, 'admin', 'admin', 0, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
