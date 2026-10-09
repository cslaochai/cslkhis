SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000101', 'F-8X10', '8×10英寸激光胶片', '25.00', '张', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000102', 'F-10X12', '10×12英寸激光胶片', '30.00', '张', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000103', 'F-11X14', '11×14英寸激光胶片', '35.00', '张', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000104', 'F-14X17', '14×17英寸激光胶片', '45.00', '张', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000105', 'F-A4', 'A4纸质报告附图', '5.00', '张', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000106', 'F-A3', 'A3纸质报告附图', '8.00', '张', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_film_spec (id, spec_code, spec_name, unit_price, unit, sort_order, status, create_by, update_by,
                           del_flag, remark, create_by_id, update_by_id)
VALUES ('210500000000000107', 'F-CD', '影像光盘（CD/DVD）', '20.00', '张', 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
