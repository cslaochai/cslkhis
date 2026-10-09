SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102769744674410497', 'QCG17GLU-2', NULL, 'G17GLU', 'g17-verify 葡萄糖', NULL, 'G17-仪-01', 2, '质控血清',
        'LOT2026G17', NULL, '5.0000', '0.2500', NULL, NULL, 1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102769744741519362', 'QCG17GLU-2-49382', NULL, 'G17GLU', 'g17-verify 葡萄糖', NULL, 'G17-仪-01', 2, NULL,
        NULL, NULL, '5.0000', '0.2500', NULL, NULL, 1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2102770056302809090', 'QCG17DBG-2', NULL, 'G17DBG', 'g17-DBG 葡萄糖', NULL, 'DBG-仪', 2, NULL, NULL, NULL,
        '5.0000', '0.2500', NULL, NULL, 1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014001', 'QCLALT-2', '4', 'LB004', '肝功能全套', 'LIS-02', '贝克曼AU5800生化仪', 2,
        '中值质控血清', 'LOT2026ALT', '伯乐生物', '42.0000', '1.9000', '8.00', '2026-12-31', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014002', 'QCLCRE-2', '5', 'LB005', '肾功能三项', 'LIS-02', '贝克曼AU5800生化仪', 2,
        '中值质控血清', 'LOT2026CRE', '伯乐生物', '88.0000', '4.0000', '8.00', '2026-12-31', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014003', 'QCLGLU-3', '6', 'LB006', '空腹血糖', 'LIS-02', '贝克曼AU5800生化仪', 3, '高值质控血清',
        'LOT2026GLU', '伯乐生物', '14.5000', '0.6500', '8.00', '2026-12-31', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014004', 'QCLGLU-2', '6', 'LB006', '空腹血糖', 'LIS-02', '贝克曼AU5800生化仪', 2, '中值质控血清',
        'LOT2026GLU', '伯乐生物', '5.6000', '0.2500', '8.00', '2026-12-31', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014005', 'QCLGLU-1', '6', 'LB006', '空腹血糖', 'LIS-02', '贝克曼AU5800生化仪', 1, '低值质控血清',
        'LOT2026GLU', '伯乐生物', '3.1000', '0.1400', '8.00', '2026-12-31', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014006', 'QCLHbA1c-2', '7', 'LB007', '糖化血红蛋白', 'LIS-04', '伯乐D-10糖化仪', 2,
        '中值质控血清', 'LOT2026HbA1c', '伯乐生物', '6.5000', '0.2000', '8.00', '2026-12-31', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014007', 'QCLPT-2', '13', 'LB013', '凝血功能四项', 'LIS-03', '希森美康CS-5100凝血仪', 2,
        '中值质控血清', 'LOT2026PT', '伯乐生物', '12.5000', '0.5500', '8.00', '2026-12-31', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014008', 'QCLTC-2', '8', 'LB008', '血脂四项', 'LIS-02', '贝克曼AU5800生化仪', 2, '中值质控血清',
        'LOT2026TC', '伯乐生物', '4.8000', '0.2200', '8.00', '2026-12-31', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014009', 'QCLWBC-2', '1', 'LB001', '血常规', 'LIS-01', '迈瑞BC-5390血球仪', 2, '中值质控血清',
        'LOT2026WBC', '伯乐生物', '9.2000', '0.4600', '8.00', '2026-12-31', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_lis_qc_plan (id, plan_no, item_id, item_code, item_name, instrument_no, instrument_name, qc_level,
                             control_name, control_lot_no, manufacturer, mean_value, sd_value, cv_limit, expire_date,
                             status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000014010', 'QCLWBC-1', '1', 'LB001', '血常规', 'LIS-01', '迈瑞BC-5390血球仪', 1, '低值质控血清',
        'LOT2026WBC', '伯乐生物', '4.5000', '0.2200', '8.00', '2026-12-31', 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
