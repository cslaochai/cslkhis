SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_arrears_policy (id, warn_line, stop_line, stop_enabled, stop_classes, remark, update_by, create_by,
                                create_by_id, update_by_id)
VALUES ('1', '1000.00', '3000.00', 0, '2,3,4', NULL, 'admin', 'admin', '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
