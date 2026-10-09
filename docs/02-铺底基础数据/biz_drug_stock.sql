SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('1', '54', '20250301', '2025-03-01', '2027-03-01', '160.00', '142.00', '18.00', '7.50', '1200.00', '中药库A01',
        2, '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2', '55', '20250302', '2025-03-02', '2027-03-02', '6.60', '5.00', '1.60', '6.50', '42.90', '中药库A01', 2,
        '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('3', '56', '20250303', '2025-03-03', '2027-03-03', '200.00', '19.00', '181.00', '6.00', '1200.00', '中药库A02',
        2, '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('4', '57', '20250304', '2025-03-04', '2027-03-04', '300.00', '17.00', '283.00', '4.50', '1350.00', '中药库A02',
        2, '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('5', '58', '20250305', '2025-03-05', '2027-03-05', '80.00', '8.00', '72.00', '9.50', '760.00', '中药库A03', 2,
        '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('6', '59', '20250306', '2025-03-06', '2027-03-06', '90.00', '1.00', '89.00', '8.50', '765.00', '中药库A03', 2,
        '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('7', '60', '20250307', '2025-03-07', '2027-03-07', '180.00', '12.00', '168.00', '5.50', '990.00', '中药库A04',
        2, '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8', '61', '20250401', '2025-04-01', '2027-04-01', '506.00', '50.00', '456.00', '7.00', '3542.00', '西药库B01',
        2, '华北制药', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9', '62', '20250402', '2025-04-02', '2027-04-02', '300.00', '30.00', '270.00', '10.50', '3150.00', '西药库B01',
        2, '第一三共制药', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('10', '63', '20250403', '2025-04-03', '2027-04-03', '400.00', '25.00', '375.00', '3.50', '1400.00', '西药库B02',
        2, '西南药业', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('11', '64', '20250404', '2025-04-04', '2027-04-04', '100.00', '10.00', '90.00', '16.50', '1650.00', '西药库B02',
        2, '辉瑞制药', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('12', '65', '20250405', '2025-04-05', '2027-04-05', '200.00', '20.00', '180.00', '32.00', '6400.00',
        '西药库B03', 2, '罗氏制药', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('13', '66', '20250406', '2025-04-06', '2027-04-06', '350.00', '15.00', '335.00', '5.50', '1925.00', '西药库B03',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('14', '67', '20250407', '2025-04-07', '2027-04-07', '250.00', '30.00', '220.00', '12.00', '3000.00',
        '西药库B04', 2, '博福-益普生', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('15', '68', '20250408', '2025-04-08', '2027-04-08', '180.00', '18.00', '162.00', '8.50', '1530.00', '西药库B04',
        2, '西安杨森', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('16', '69', '20250409', '2025-04-09', '2027-04-09', '220.00', '16.00', '204.00', '5.50', '1210.00', '西药库B05',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('17', '70', '20250410', '2025-04-10', '2027-04-10', '150.00', '20.00', '130.00', '10.50', '1575.00',
        '西药库B05', 2, '拜耳医药', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('18', '71', '20250411', '2025-04-11', '2027-04-11', '130.00', '10.00', '120.00', '8.50', '1105.00', '西药库B06',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('19', '72', '20250412', '2025-04-12', '2027-04-12', '80.00', '5.00', '75.00', '7.00', '560.00', '西药库B06', 2,
        '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('20', '73', '20250413', '2025-04-13', '2027-04-13', '90.00', '8.00', '82.00', '5.50', '495.00', '西药库B07', 2,
        '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21', '74', '20250414', '2025-04-14', '2027-04-14', '464.00', '30.00', '434.00', '2.00', '928.00', '西药库B07',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('22', '75', '20250415', '2025-04-15', '2027-04-15', '450.00', '20.00', '430.00', '1.50', '675.00', '西药库B08',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('23', '76', '20250416', '2025-04-16', '2027-04-16', '300.00', '15.00', '285.00', '4.50', '1350.00', '西药库B08',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('24', '77', '20250417', '2025-04-17', '2027-04-17', '250.00', '10.00', '240.00', '3.50', '875.00', '西药库B09',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('25', '78', '20250418', '2025-04-18', '2027-04-18', '600.00', '50.00', '550.00', '2.00', '1200.00', '输液库C01',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('26', '79', '20250419', '2025-04-19', '2027-04-19', '550.00', '40.00', '510.00', '2.50', '1375.00', '输液库C01',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('27', '80', '20250420', '2025-04-20', '2027-04-20', '100.00', '10.00', '90.00', '16.50', '1650.00', '输液库C02',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('28', '81', '20250501', '2025-05-01', '2027-05-01', '200.00', '0.00', '200.00', '3.50', '700.00', '耗材库D01',
        2, '某消毒用品厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('29', '82', '20250502', '2025-05-02', '2027-05-02', '150.00', '10.00', '140.00', '5.50', '825.00', '耗材库D01',
        2, '某消毒用品厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('30', '83', '20250503', '2025-05-03', '2027-05-03', '400.00', '20.00', '380.00', '2.00', '800.00', '耗材库D02',
        2, '某医疗器械厂', NULL, 1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('31', '54', '20250601', '2025-06-01', '2027-06-01', '50.00', '8.00', '42.00', '7.50', '375.00', '中药库A01', 2,
        '某中药厂', NULL, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('32', '55', '20250602', '2025-06-02', '2027-06-02', '30.00', '0.00', '30.00', '6.50', '195.00', '中药库A01', 2,
        '某中药厂', NULL, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('33', '58', '20250603', '2025-06-03', '2027-06-03', '20.00', '5.00', '15.00', '9.50', '190.00', '中药库A03', 2,
        '某中药厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('34', '61', '20250604', '2025-06-04', '2027-06-04', '100.00', '10.00', '90.00', '7.00', '700.00', '西药库B01',
        2, '华北制药', NULL, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('35', '65', '20250605', '2025-06-05', '2027-06-05', '40.00', '10.00', '30.00', '32.00', '1280.00', '西药库B03',
        2, '罗氏制药', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('36', '70', '20250606', '2025-06-06', '2027-06-06', '25.00', '5.00', '20.00', '10.50', '262.50', '西药库B05', 2,
        '拜耳医药', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('37', '72', '20250607', '2025-06-07', '2027-06-07', '15.00', '0.00', '15.00', '7.00', '105.00', '西药库B06', 2,
        '某制药厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('38', '74', '20250608', '2025-06-08', '2027-06-08', '80.00', '5.00', '75.00', '2.00', '160.00', '西药库B07', 2,
        '某制药厂', NULL, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('39', '76', '20250609', '2025-06-09', '2027-06-09', '45.00', '3.00', '42.00', '4.50', '202.50', '西药库B08', 2,
        '某制药厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('40', '78', '20250610', '2025-06-10', '2027-06-10', '120.00', '20.00', '100.00', '2.00', '240.00', '输液库C01',
        2, '某制药厂', NULL, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('41', '80', '20250611', '2025-06-11', '2027-06-11', '10.00', '5.00', '5.00', '16.50', '165.00', '输液库C02', 2,
        '某制药厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('42', '81', '20250612', '2025-06-12', '2027-06-12', '30.00', '0.00', '30.00', '3.50', '105.00', '耗材库D01', 2,
        '某消毒用品厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('43', '82', '20250613', '2025-06-13', '2027-06-13', '20.00', '5.00', '15.00', '5.50', '110.00', '耗材库D01', 2,
        '某消毒用品厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('44', '83', '20250614', '2025-06-14', '2027-06-14', '50.00', '10.00', '40.00', '2.00', '100.00', '耗材库D02', 2,
        '某医疗器械厂', NULL, 2, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('45', '54', '20240101', '2024-01-01', '2026-01-01', '0.00', '1.00', '-1.00', '7.50', '0.00', '中药库A01', 2,
        '某中药厂', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('46', '61', '20240201', '2024-02-01', '2026-02-01', '14.00', '4.00', '10.00', '7.00', '98.00', '西药库B01', 2,
        '华北制药', NULL, 4, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('47', '56', '20250701', '2025-07-01', '2027-07-01', '160.00', '15.00', '145.00', '6.00', '960.00', '中药库A02',
        2, '某中药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('48', '63', '20250702', '2025-07-02', '2027-07-02', '280.00', '20.00', '260.00', '3.50', '980.00', '西药库B02',
        2, '西南药业', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('49', '67', '20250703', '2025-07-03', '2027-07-03', '190.00', '25.00', '165.00', '12.00', '2280.00',
        '西药库B04', 2, '博福-益普生', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('50', '79', '20250704', '2025-07-04', '2027-07-04', '400.00', '30.00', '370.00', '2.50', '1000.00', '输液库C01',
        2, '某制药厂', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2101318286925934594', '20', 'PH41FIX28294847', '2026-01-10', '2028-01-10', '66.00', '0.00', '66.00', '4.25',
        '280.50', 'VER-D01', 2, '验证供应商PH41', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2101319539659681793', '20', 'FE4128592116', NULL, '2026-09-30', '12.00', '0.00', '12.00', '0.00', '0.00',
        'FE-D01', 2, '前端验证供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102630676540018689', '500000', 'G9UI-B1', NULL, '2027-12-31', '10.00', '0.00', '10.00', '10.00', '100.00',
        NULL, 2, '国药控股股份有限公司', NULL, 1, '超级管理员', '超级管理员', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102630812389330945', '1', 'G9-BX-01', '2026-01-01', '2027-01-01', '0.00', '0.00', '0.00', '12.50', '0.00',
        NULL, 2, '验证供应商甲', NULL, 3, '超级管理员', '超级管理员', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102630812586463235', '2', 'G9-BY-01', NULL, '2027-02-01', '0.00', '0.00', '0.00', '30.00', '0.00', NULL, 2,
        '验证供应商甲', NULL, 3, '超级管理员', '超级管理员', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103828608698146817', '1', 'STKA6801862', NULL, '2027-12-31', '106.00', '6.00', '100.00', '7.00', '742.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103828609218240514', '1', 'STKB6801862', NULL, '2027-12-31', '45.00', '20.00', '25.00', '3.00', '135.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103828609797054465', '1', 'STKC6801862', NULL, '2027-12-31', '3.00', '0.00', '3.00', '2.00', '6.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103828610304565250', '1', 'STKD6801862', NULL, '2027-12-31', '30.00', '0.00', '30.00', '5.00', '150.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103828610883379201', '1', 'STKP68018621', NULL, '2027-12-31', '12.00', '0.00', '12.00', '4.00', '48.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103828611344752642', '1', 'STKP68018622', NULL, '2027-12-31', '11.00', '0.00', '11.00', '4.00', '44.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832603701407745', '1', 'STKA7754369', NULL, '2027-12-31', '106.00', '0.00', '106.00', '7.00', '742.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832604225695745', '1', 'STKB7754369', NULL, '2027-12-31', '45.00', '20.00', '25.00', '3.00', '135.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832604741595138', '1', 'STKC7754369', NULL, '2027-12-31', '3.00', '0.00', '3.00', '2.00', '6.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832605316214785', '1', 'STKD7754369', NULL, '2027-12-31', '30.00', '0.00', '30.00', '5.00', '150.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832605907611649', '1', 'STKP77543691', NULL, '2027-12-31', '12.00', '0.00', '12.00', '4.00', '48.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832606486425602', '1', 'STKP77543692', NULL, '2027-12-31', '11.00', '0.00', '11.00', '4.00', '44.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103832674639671298', '1', 'STKE7754369', NULL, '2027-12-31', '33.00', '0.00', '33.00', '6.00', '198.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833823535009794', '1', 'STKA8045213', NULL, '2027-12-31', '106.00', '0.00', '106.00', '7.00', '742.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833824046714882', '1', 'STKB8045213', NULL, '2027-12-31', '45.00', '20.00', '25.00', '3.00', '135.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833824562614274', '1', 'STKC8045213', NULL, '2027-12-31', '3.00', '0.00', '3.00', '2.00', '6.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833825019793410', '1', 'STKD8045213', NULL, '2027-12-31', '30.00', '0.00', '30.00', '5.00', '150.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833825598607361', '1', 'STKP80452131', NULL, '2027-12-31', '12.00', '0.00', '12.00', '4.00', '48.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833826114506753', '1', 'STKP80452132', NULL, '2027-12-31', '11.00', '0.00', '11.00', '4.00', '44.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103833883475808257', '1', 'STKE8045213', NULL, '2027-12-31', '33.00', '0.00', '33.00', '6.00', '198.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843793794097153', '1', 'STKA0422479', NULL, '2027-12-31', '106.00', '0.00', '106.00', '7.00', '742.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843793924120578', '1', 'STKB0422479', NULL, '2027-12-31', '45.00', '20.00', '25.00', '3.00', '135.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843794054144002', '1', 'STKC0422479', NULL, '2027-12-31', '3.00', '0.00', '3.00', '2.00', '6.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843794117058562', '1', 'STKD0422479', NULL, '2027-12-31', '30.00', '0.00', '30.00', '5.00', '150.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843794179973121', '1', 'STKP04224791', NULL, '2027-12-31', '12.00', '0.00', '12.00', '4.00', '48.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843794309996546', '1', 'STKP04224792', NULL, '2027-12-31', '11.00', '0.00', '11.00', '4.00', '44.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2103843826455142402', '1', 'STKE0422479', NULL, '2027-12-31', '33.00', '0.00', '33.00', '6.00', '198.00',
        '验证货架T4', 2, '验证供应商T4', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2108021818492203009', '500000', 'VF20261008022900', '2026-10-08', '2028-10-07', '98.00', '0.00', '98.00',
        '2.50', '245.00', NULL, 2, '九州通医药集团股份有限公司', NULL, 1, '杨如初', '杨如初', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2108021819524001794', '500369', 'BP0370-W1', '2026-05-30', '2028-03-20', '3.00', '0.00', '3.00', '77.00',
        '231.00', NULL, 2, '九州通医药集团股份有限公司', '5', 1, '杨如初', '杨如初', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2108021962822397953', '500000', 'VF20261008022935', '2026-10-08', '2028-10-07', '98.00', '0.00', '98.00',
        '2.50', '245.00', NULL, 2, '国药控股股份有限公司', NULL, 1, '杨如初', '杨如初', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2108021963556401153', '500370', 'BP0371-W1', '2026-05-30', '2028-03-20', '3.00', '0.00', '3.00', '91.00',
        '273.00', NULL, 2, '国药控股股份有限公司', '1', 1, '杨如初', '杨如初', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2108021997479931905', '500000', 'VF20261008022943', '2026-10-08', '2028-10-07', '98.00', '0.00', '98.00',
        '2.50', '245.00', NULL, 2, '上海医药集团股份有限公司', NULL, 1, '杨如初', '杨如初', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2108021998197157890', '500371', 'BP0372-W1', '2026-05-30', '2028-03-20', '3.00', '0.00', '3.00', '73.50',
        '220.50', NULL, 2, '上海医药集团股份有限公司', '2', 1, '杨如初', '杨如初', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052001', '8900000000000051001', 'MORPH-INJ-A', '2026-01-23', '2027-01-23', '57.00', '0.00',
        '57.00', '2.80', '159.60', '麻精专柜A01', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052002', '8900000000000051001', 'MORPH-INJ-B', '2026-07-23', '2028-01-23', '40.00', '0.00',
        '40.00', '2.80', '140.00', '麻精专柜A01', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052003', '8900000000000051002', 'MORPH-SR-A', '2026-03-23', '2027-03-23', '186.00', '2.00',
        '184.00', '6.40', '1190.40', '麻精专柜A02', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052004', '8900000000000051002', 'MORPH-SR-B', '2026-08-23', '2028-05-23', '100.00', '0.00',
        '100.00', '6.40', '800.00', '麻精专柜A02', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052005', '8900000000000051003', 'CODEINE-A', '2026-04-23', '2027-04-23', '500.00', '0.00',
        '500.00', '0.40', '275.00', '麻精专柜A03', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052006', '8900000000000051003', 'CODEINE-B', '2026-08-23', '2028-03-23', '300.00', '0.00',
        '300.00', '0.40', '165.00', '麻精专柜A03', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052007', '8900000000000051004', 'ARSENIC-A', '2026-05-23', '2027-05-23', '30.00', '0.00', '30.00',
        '160.00', '5940.00', '毒药专柜B01', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000052008', '8900000000000051004', 'ARSENIC-B', '2026-08-23', '2027-11-23', '20.00', '0.00', '20.00',
        '160.00', '3960.00', '毒药专柜B01', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000057001', '500205', 'DIAZ-FIX-A', '2026-03-23', '2026-12-23', '0.00', '0.00', '0.00', '6.50',
        '0.00', 'G10夹具库', 2, '国药控股', NULL, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000057002', '500205', 'DIAZ-FIX-B', '2026-03-23', '2027-12-23', '84.00', '0.00', '84.00', '6.50',
        '546.00', 'G10夹具库', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000057003', '6', 'ASP-FIX-A', '2026-03-23', '2027-09-23', '500.00', '0.00', '500.00', '19.80',
        '9900.00', 'G10夹具库', 2, '国药控股', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000156111', '8900000000000156101', 'TRA-01', '2026-09-27', '2028-02-09', '100.00', '0.00', '100.00',
        '18.00', '1800.00', '追溯验证货位', 1, '追溯验证供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8900000000000156112', '8900000000000156102', 'TRB-01', '2026-09-27', '2028-02-09', '100.00', '0.00', '100.00',
        '18.00', '1800.00', '追溯验证货位', 1, '追溯验证供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100101', '1', 'G13FIX-BATCH', '2026-01-01', '2027-12-31', '97.00', '0.00', '97.00', '5.00',
        '485.00', NULL, 2, NULL, NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100102', '2', 'G13FIX-BATCH', '2026-01-01', '2027-12-31', '100.00', '0.00', '100.00', '5.00',
        '500.00', NULL, 2, NULL, NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100103', '500369', 'BP0370-TCMA', '2026-07-26', '2027-03-26', '18.38', '0.00', '18.38', '77.00',
        '1415.26', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100104', '500370', 'BP0371-TCMA', '2026-07-26', '2027-03-26', '16.58', '0.00', '16.58', '91.00',
        '1508.78', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100105', '500371', 'BP0372-TCMA', '2026-07-26', '2027-03-26', '18.02', '0.00', '18.02', '73.50',
        '1324.47', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100106', '500372', 'BP0373-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '66.50',
        '1330.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100107', '500373', 'BP0374-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '42.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100108', '500374', 'BP0375-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '126.00',
        '2520.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100109', '500375', 'BP0376-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '80.50',
        '1610.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100110', '500376', 'BP0377-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '59.50',
        '1190.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100111', '500377', 'BP0378-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '98.00',
        '1960.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100112', '500378', 'BP0379-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '77.00',
        '1540.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100113', '500379', 'BP0380-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '126.00',
        '2520.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100114', '500380', 'BP0381-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '105.00',
        '2100.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100115', '500381', 'BP0382-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '182.00',
        '3640.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100116', '500382', 'BP0383-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '49.00',
        '980.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100117', '500383', 'BP0384-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '154.00',
        '3080.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100118', '500384', 'BP0385-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '45.50',
        '910.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100119', '500385', 'BP0386-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '66.50',
        '1330.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100120', '500386', 'BP0387-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '266.00',
        '5320.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100121', '500387', 'BP0388-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '77.00',
        '1540.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100122', '500388', 'BP0389-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '66.50',
        '1330.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100123', '500389', 'BP0390-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '182.00',
        '3640.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100124', '500390', 'BP0391-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '105.00',
        '2100.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100125', '500391', 'BP0392-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100126', '500392', 'BP0393-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '84.00',
        '1680.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100127', '500393', 'BP0394-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '59.50',
        '1190.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100128', '500394', 'BP0395-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '42.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100129', '500395', 'BP0396-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100130', '500396', 'BP0397-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '126.00',
        '2520.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100131', '500397', 'BP0398-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '224.00',
        '4480.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100132', '500398', 'BP0399-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '91.00',
        '1820.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100133', '500399', 'BP0400-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '63.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100134', '500400', 'BP0401-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '105.00',
        '2100.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100135', '500401', 'BP0402-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '66.50',
        '1330.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100136', '500402', 'BP0403-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '52.50',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100137', '500403', 'BP0404-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '196.00',
        '3920.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100138', '500404', 'BP0405-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '77.00',
        '1540.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100139', '500405', 'BP0406-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '476.00',
        '9520.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100140', '500406', 'BP0407-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '38.50',
        '770.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100141', '500407', 'BP0408-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '115.50',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100142', '500408', 'BP0409-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100143', '500409', 'BP0410-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '42.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100144', '500410', 'BP0411-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100145', '500411', 'BP0412-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '59.50',
        '1190.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100146', '500412', 'BP0413-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100147', '500413', 'BP0414-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '52.50',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100148', '500414', 'BP0415-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '42.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100149', '500415', 'BP0416-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '59.50',
        '1190.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100150', '500416', 'BP0417-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '266.00',
        '5320.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100151', '500417', 'BP0418-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '63.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100152', '500418', 'BP0419-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '38.50',
        '770.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100153', '500419', 'BP0420-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '91.00',
        '1820.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100154', '500420', 'BP0421-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '56.00',
        '1120.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100155', '500421', 'BP0422-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100156', '500422', 'BP0423-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '24.50',
        '490.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100157', '500423', 'BP0424-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100158', '500424', 'BP0425-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100159', '500425', 'BP0426-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '38.50',
        '770.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100160', '500426', 'BP0427-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100161', '500427', 'BP0428-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100162', '500428', 'BP0429-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100163', '500429', 'BP0430-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '42.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100164', '500430', 'BP0431-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100165', '500431', 'BP0432-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '21.00',
        '420.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100166', '500432', 'BP0433-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100167', '500433', 'BP0434-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100168', '500434', 'BP0435-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100169', '500435', 'BP0436-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '77.00',
        '1540.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100170', '500436', 'BP0437-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100171', '500437', 'BP0438-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '224.00',
        '4480.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100172', '500438', 'BP0439-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100173', '500439', 'BP0440-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '31.50',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100174', '500440', 'BP0441-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '28.00',
        '560.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100175', '500441', 'BP0442-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '266.00',
        '5320.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100176', '500442', 'BP0443-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '112.00',
        '2240.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100177', '500443', 'BP0444-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '133.00',
        '2660.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100178', '500444', 'BP0445-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100179', '500445', 'BP0446-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '38.50',
        '770.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100180', '500446', 'BP0447-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '42.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100181', '500447', 'BP0448-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '38.50',
        '770.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100182', '500448', 'BP0449-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100183', '500449', 'BP0450-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '77.00',
        '1540.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100184', '500450', 'BP0451-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '406.00',
        '8120.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100185', '500451', 'BP0452-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '616.00',
        '12320.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100186', '500452', 'BP0453-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '91.00',
        '1820.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100187', '500453', 'BP0454-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '105.00',
        '2100.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100188', '500454', 'BP0455-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '49.00',
        '980.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100189', '500455', 'BP0456-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '77.00',
        '1540.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100190', '500456', 'BP0457-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '112.00',
        '2240.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100191', '500457', 'BP0458-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '38.50',
        '770.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100192', '500458', 'BP0459-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '91.00',
        '1820.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100193', '500459', 'BP0460-TCMA', '2026-07-26', '2027-03-26', '20.00', '0.00', '20.00', '35.00',
        '700.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100194', '500369', 'BP0370-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100195', '500370', 'BP0371-TCMB', '2026-08-26', '2028-03-26', '30.36', '0.00', '30.36', '91.00',
        '2762.76', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100196', '500371', 'BP0372-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '73.50',
        '2205.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100197', '500372', 'BP0373-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '66.50',
        '1995.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100198', '500373', 'BP0374-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '42.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100199', '500374', 'BP0375-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '126.00',
        '3780.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100200', '500375', 'BP0376-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '80.50',
        '2415.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100201', '500376', 'BP0377-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '59.50',
        '1785.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100202', '500377', 'BP0378-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '98.00',
        '2940.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100203', '500378', 'BP0379-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100204', '500379', 'BP0380-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '126.00',
        '3780.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100205', '500380', 'BP0381-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '105.00',
        '3150.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100206', '500381', 'BP0382-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '182.00',
        '5460.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100207', '500382', 'BP0383-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '49.00',
        '1470.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100208', '500383', 'BP0384-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '154.00',
        '4620.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100209', '500384', 'BP0385-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '45.50',
        '1365.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100210', '500385', 'BP0386-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '66.50',
        '1995.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100211', '500386', 'BP0387-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '266.00',
        '7980.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100212', '500387', 'BP0388-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100213', '500388', 'BP0389-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '66.50',
        '1995.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100214', '500389', 'BP0390-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '182.00',
        '5460.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100215', '500390', 'BP0391-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '105.00',
        '3150.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100216', '500391', 'BP0392-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100217', '500392', 'BP0393-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '84.00',
        '2520.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100218', '500393', 'BP0394-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '59.50',
        '1785.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100219', '500394', 'BP0395-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '42.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100220', '500395', 'BP0396-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100221', '500396', 'BP0397-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '126.00',
        '3780.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100222', '500397', 'BP0398-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '224.00',
        '6720.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100223', '500398', 'BP0399-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '91.00',
        '2730.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100224', '500399', 'BP0400-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '63.00',
        '1890.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100225', '500400', 'BP0401-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '105.00',
        '3150.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100226', '500401', 'BP0402-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '66.50',
        '1995.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100227', '500402', 'BP0403-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '52.50',
        '1575.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100228', '500403', 'BP0404-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '196.00',
        '5880.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100229', '500404', 'BP0405-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100230', '500405', 'BP0406-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '476.00',
        '14280.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100231', '500406', 'BP0407-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '38.50',
        '1155.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100232', '500407', 'BP0408-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '115.50',
        '3465.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100233', '500408', 'BP0409-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100234', '500409', 'BP0410-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '42.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100235', '500410', 'BP0411-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100236', '500411', 'BP0412-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '59.50',
        '1785.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100237', '500412', 'BP0413-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100238', '500413', 'BP0414-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '52.50',
        '1575.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100239', '500414', 'BP0415-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '42.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100240', '500415', 'BP0416-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '59.50',
        '1785.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100241', '500416', 'BP0417-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '266.00',
        '7980.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100242', '500417', 'BP0418-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '63.00',
        '1890.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100243', '500418', 'BP0419-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '38.50',
        '1155.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100244', '500419', 'BP0420-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '91.00',
        '2730.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100245', '500420', 'BP0421-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '56.00',
        '1680.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100246', '500421', 'BP0422-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100247', '500422', 'BP0423-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '24.50',
        '735.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100248', '500423', 'BP0424-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100249', '500424', 'BP0425-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100250', '500425', 'BP0426-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '38.50',
        '1155.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100251', '500426', 'BP0427-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100252', '500427', 'BP0428-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100253', '500428', 'BP0429-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100254', '500429', 'BP0430-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '42.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100255', '500430', 'BP0431-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100256', '500431', 'BP0432-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '21.00',
        '630.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100257', '500432', 'BP0433-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100258', '500433', 'BP0434-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100259', '500434', 'BP0435-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100260', '500435', 'BP0436-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100261', '500436', 'BP0437-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100262', '500437', 'BP0438-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '224.00',
        '6720.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100263', '500438', 'BP0439-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100264', '500439', 'BP0440-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '31.50',
        '945.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100265', '500440', 'BP0441-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '28.00',
        '840.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100266', '500441', 'BP0442-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '266.00',
        '7980.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100267', '500442', 'BP0443-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '112.00',
        '3360.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100268', '500443', 'BP0444-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '133.00',
        '3990.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100269', '500444', 'BP0445-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100270', '500445', 'BP0446-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '38.50',
        '1155.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100271', '500446', 'BP0447-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '42.00',
        '1260.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100272', '500447', 'BP0448-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '38.50',
        '1155.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100273', '500448', 'BP0449-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100274', '500449', 'BP0450-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100275', '500450', 'BP0451-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '406.00',
        '12180.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100276', '500451', 'BP0452-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '616.00',
        '18480.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100277', '500452', 'BP0453-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '91.00',
        '2730.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100278', '500453', 'BP0454-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '105.00',
        '3150.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100279', '500454', 'BP0455-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '49.00',
        '1470.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100280', '500455', 'BP0456-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '77.00',
        '2310.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100281', '500456', 'BP0457-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '112.00',
        '3360.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100282', '500457', 'BP0458-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '38.50',
        '1155.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100283', '500458', 'BP0459-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '91.00',
        '2730.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8920000000000100284', '500459', 'BP0460-TCMB', '2026-08-26', '2028-03-26', '30.00', '0.00', '30.00', '35.00',
        '1050.00', '中药饮片柜A', 2, '本地饮片供应商', NULL, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000001', '500000', 'BP0001-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '10.00',
        '3000.00', '药库整件区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000002', '500205', 'BP0206-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '6.50',
        '1950.00', '药库整件区-06', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000003', '500369', 'BP0370-W1', '2026-05-30', '2028-03-20', '1995.00', '0.00', '1995.00', '77.00',
        '153615.00', '药库饮片区-10', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000004', '500370', 'BP0371-W1', '2026-05-30', '2028-03-20', '1995.00', '0.00', '1995.00', '91.00',
        '181545.00', '药库饮片区-11', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000005', '500371', 'BP0372-W1', '2026-05-30', '2028-03-20', '1995.00', '0.00', '1995.00', '73.50',
        '146632.50', '药库饮片区-12', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000006', '500372', 'BP0373-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '66.50',
        '133000.00', '药库饮片区-13', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000007', '500373', 'BP0374-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '42.00',
        '84000.00', '药库饮片区-14', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000008', '500374', 'BP0375-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '126.00', '252000.00', '药库饮片区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000009', '500375', 'BP0376-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '80.50',
        '161000.00', '药库饮片区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000010', '500376', 'BP0377-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '59.50',
        '119000.00', '药库饮片区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000011', '500377', 'BP0378-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '98.00',
        '196000.00', '药库饮片区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000012', '500378', 'BP0379-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '77.00',
        '154000.00', '药库饮片区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000013', '500379', 'BP0380-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '126.00', '252000.00', '药库饮片区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000014', '500380', 'BP0381-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '105.00', '210000.00', '药库饮片区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000015', '500381', 'BP0382-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '182.00', '364000.00', '药库饮片区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000016', '500382', 'BP0383-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '49.00',
        '98000.00', '药库饮片区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000017', '500383', 'BP0384-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '154.00', '308000.00', '药库饮片区-04', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000018', '500384', 'BP0385-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '45.50',
        '91000.00', '药库饮片区-05', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000019', '500385', 'BP0386-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '66.50',
        '133000.00', '药库饮片区-06', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000020', '500386', 'BP0387-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '266.00', '532000.00', '药库饮片区-07', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000021', '500387', 'BP0388-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '77.00',
        '154000.00', '药库饮片区-08', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000022', '500388', 'BP0389-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '66.50',
        '133000.00', '药库饮片区-09', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000023', '500389', 'BP0390-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '182.00', '364000.00', '药库饮片区-10', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000024', '500390', 'BP0391-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '105.00', '210000.00', '药库饮片区-11', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000025', '500391', 'BP0392-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-12', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000026', '500392', 'BP0393-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '84.00',
        '168000.00', '药库饮片区-13', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000027', '500393', 'BP0394-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '59.50',
        '119000.00', '药库饮片区-14', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000028', '500394', 'BP0395-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '42.00',
        '84000.00', '药库饮片区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000029', '500395', 'BP0396-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000030', '500396', 'BP0397-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '126.00', '252000.00', '药库饮片区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000031', '500397', 'BP0398-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '224.00', '448000.00', '药库饮片区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000032', '500398', 'BP0399-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '91.00',
        '182000.00', '药库饮片区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000033', '500399', 'BP0400-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '63.00',
        '126000.00', '药库饮片区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000034', '500400', 'BP0401-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '105.00', '210000.00', '药库饮片区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000035', '500401', 'BP0402-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '66.50',
        '133000.00', '药库饮片区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000036', '500402', 'BP0403-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '52.50',
        '105000.00', '药库饮片区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000037', '500403', 'BP0404-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '196.00', '392000.00', '药库饮片区-04', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000038', '500404', 'BP0405-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '77.00',
        '154000.00', '药库饮片区-05', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000039', '500405', 'BP0406-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '476.00', '952000.00', '药库饮片区-06', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000040', '500406', 'BP0407-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '38.50',
        '77000.00', '药库饮片区-07', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000041', '500407', 'BP0408-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '115.50', '231000.00', '药库饮片区-08', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000042', '500408', 'BP0409-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-09', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000043', '500409', 'BP0410-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '42.00',
        '84000.00', '药库饮片区-10', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000044', '500410', 'BP0411-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-11', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000045', '500411', 'BP0412-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '59.50',
        '119000.00', '药库饮片区-12', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000046', '500412', 'BP0413-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-13', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000047', '500413', 'BP0414-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '52.50',
        '105000.00', '药库饮片区-14', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000048', '500414', 'BP0415-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '42.00',
        '84000.00', '药库饮片区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000049', '500415', 'BP0416-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '59.50',
        '119000.00', '药库饮片区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000050', '500416', 'BP0417-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '266.00', '532000.00', '药库饮片区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000051', '500417', 'BP0418-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '63.00',
        '126000.00', '药库饮片区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000052', '500418', 'BP0419-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '38.50',
        '77000.00', '药库饮片区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000053', '500419', 'BP0420-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '91.00',
        '182000.00', '药库饮片区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000054', '500420', 'BP0421-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '56.00',
        '112000.00', '药库饮片区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000055', '500421', 'BP0422-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000056', '500422', 'BP0423-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '24.50',
        '49000.00', '药库饮片区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000057', '500423', 'BP0424-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-04', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000058', '500424', 'BP0425-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-05', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000059', '500425', 'BP0426-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '38.50',
        '77000.00', '药库饮片区-06', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000060', '500426', 'BP0427-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-07', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000061', '500427', 'BP0428-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-08', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000062', '500428', 'BP0429-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-09', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000063', '500429', 'BP0430-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '42.00',
        '84000.00', '药库饮片区-10', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000064', '500430', 'BP0431-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-11', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000065', '500431', 'BP0432-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '21.00',
        '42000.00', '药库饮片区-12', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000066', '500432', 'BP0433-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-13', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000067', '500433', 'BP0434-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-14', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000068', '500434', 'BP0435-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000069', '500435', 'BP0436-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '77.00',
        '154000.00', '药库饮片区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000070', '500436', 'BP0437-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000071', '500437', 'BP0438-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '224.00', '448000.00', '药库饮片区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000072', '500438', 'BP0439-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000073', '500439', 'BP0440-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '31.50',
        '63000.00', '药库饮片区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000074', '500440', 'BP0441-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '28.00',
        '56000.00', '药库饮片区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000075', '500441', 'BP0442-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '266.00', '532000.00', '药库饮片区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000076', '500442', 'BP0443-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '112.00', '224000.00', '药库饮片区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000077', '500443', 'BP0444-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '133.00', '266000.00', '药库饮片区-04', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000078', '500444', 'BP0445-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-05', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000079', '500445', 'BP0446-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '38.50',
        '77000.00', '药库饮片区-06', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000080', '500446', 'BP0447-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '42.00',
        '84000.00', '药库饮片区-07', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000081', '500447', 'BP0448-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '38.50',
        '77000.00', '药库饮片区-08', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000082', '500448', 'BP0449-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-09', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000083', '500449', 'BP0450-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '77.00',
        '154000.00', '药库饮片区-10', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000084', '500450', 'BP0451-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '406.00', '812000.00', '药库饮片区-11', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000085', '500451', 'BP0452-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '616.00', '1232000.00', '药库饮片区-12', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000086', '500452', 'BP0453-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '91.00',
        '182000.00', '药库饮片区-13', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000087', '500453', 'BP0454-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '105.00', '210000.00', '药库饮片区-14', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000088', '500454', 'BP0455-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '49.00',
        '98000.00', '药库饮片区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000089', '500455', 'BP0456-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '77.00',
        '154000.00', '药库饮片区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000090', '500456', 'BP0457-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00',
        '112.00', '224000.00', '药库饮片区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927',
        0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000091', '500457', 'BP0458-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '38.50',
        '77000.00', '药库饮片区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000092', '500458', 'BP0459-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '91.00',
        '182000.00', '药库饮片区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000093', '500459', 'BP0460-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '35.00',
        '70000.00', '药库饮片区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000094', '1', 'DRUG0001-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '12.50',
        '3750.00', '药库整件区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000095', '2', 'DRUG0002-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '30.00',
        '9000.00', '药库整件区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000096', '6', 'DRUG0006-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '19.80',
        '5940.00', '药库整件区-07', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000097', '20', 'DRUG0020-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '4.25',
        '1275.00', '药库整件区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000098', '54', 'DRUG0054-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '7.50',
        '15000.00', '药库饮片区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000099', '55', 'DRUG0055-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '6.50',
        '13000.00', '药库饮片区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000100', '56', 'DRUG0056-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '6.00',
        '12000.00', '药库饮片区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000101', '57', 'DRUG0057-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '4.50',
        '9000.00', '药库饮片区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000102', '58', 'DRUG0058-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '9.50',
        '19000.00', '药库饮片区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000103', '59', 'DRUG0059-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '8.50',
        '17000.00', '药库饮片区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000104', '60', 'DRUG0060-W1', '2026-05-30', '2028-03-20', '2000.00', '0.00', '2000.00', '5.50',
        '11000.00', '药库饮片区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000105', '61', 'DRUG0061-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '7.00',
        '2100.00', '药库整件区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000106', '62', 'DRUG0062-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '10.50',
        '3150.00', '药库整件区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000107', '63', 'DRUG0063-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '3.50',
        '1050.00', '药库整件区-04', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000108', '64', 'DRUG0064-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '16.50',
        '4950.00', '药库整件区-05', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000109', '65', 'DRUG0065-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '32.00',
        '9600.00', '药库整件区-06', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000110', '66', 'DRUG0066-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '5.50',
        '1650.00', '药库整件区-07', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000111', '67', 'DRUG0067-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '12.00',
        '3600.00', '药库整件区-08', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000112', '68', 'DRUG0068-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '8.50',
        '2550.00', '药库整件区-09', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000113', '69', 'DRUG0069-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '5.50',
        '1650.00', '药库整件区-10', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000114', '70', 'DRUG0070-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '10.50',
        '3150.00', '药库整件区-11', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000115', '71', 'DRUG0071-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '8.50',
        '2550.00', '药库整件区-12', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000116', '72', 'DRUG0072-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '7.00',
        '2100.00', '药库整件区-13', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000117', '73', 'DRUG0073-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '5.50',
        '1650.00', '药库整件区-14', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000118', '74', 'DRUG0074-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '2.00',
        '600.00', '药库整件区-15', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000119', '75', 'DRUG0075-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '1.50',
        '450.00', '药库整件区-16', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000120', '76', 'DRUG0076-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '4.50',
        '1350.00', '药库整件区-17', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000121', '77', 'DRUG0077-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '3.50',
        '1050.00', '药库整件区-18', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000122', '78', 'DRUG0078-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '2.00',
        '600.00', '药库整件区-19', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000123', '79', 'DRUG0079-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '2.50',
        '750.00', '药库整件区-20', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000124', '80', 'DRUG0080-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '16.50',
        '4950.00', '药库整件区-01', 1, '国药控股股份有限公司', '1', 1, 'seed-20260927', 'seed-20260927', 0, NULL, '1',
        '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000125', '81', 'DRUG0081-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '3.50',
        '1050.00', '药库整件区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000126', '82', 'DRUG0082-W1', '2026-05-30', '2028-03-20', '300.00', '0.00', '300.00', '5.50',
        '1650.00', '药库整件区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927', 'seed-20260927', 0, NULL,
        '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000127', '8900000000000051001', 'NARC001-W1', '2026-05-30', '2028-03-20', '300.00', '0.00',
        '300.00', '2.80', '840.00', '药库整件区-02', 1, '上海医药集团股份有限公司', '2', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000128', '8900000000000051002', 'NARC002-W1', '2026-05-30', '2028-03-20', '300.00', '0.00',
        '300.00', '6.40', '1920.00', '药库整件区-03', 1, '华润医药商业集团有限公司', '3', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000129', '8900000000000051003', 'NARC003-W1', '2026-05-30', '2028-03-20', '300.00', '0.00',
        '300.00', '0.40', '120.00', '药库整件区-04', 1, '广州医药有限公司', '4', 1, 'seed-20260927', 'seed-20260927', 0,
        NULL, '1', '1');
INSERT INTO biz_drug_stock (id, drug_id, batch_no, production_date, expiry_date, quantity, locked_quantity,
                            available_quantity, cost_price, total_amount, location, stock_room, supplier, supplier_id,
                            stock_status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8947000000000000130', '8900000000000051004', 'TOX001-W1', '2026-05-30', '2028-03-20', '300.00', '0.00',
        '300.00', '160.00', '48000.00', '药库整件区-05', 1, '九州通医药集团股份有限公司', '5', 1, 'seed-20260927',
        'seed-20260927', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
