-- 374 12 台被定位设备的维保基线（保养记录 + 台账最近维保日期）
-- 背景：sys_equipment 这 12 台在用设备 last_maintain_date 全为空，资产定位页的"维保到期"只能显示"未建档"。
--       设备科台账 49 号铺底时未补维保记录，这里按设备实际启用年份补一条最近一次保养记录（类型 1-保养），
--       日期分布刻意覆盖"正常 / 30 天内到期 / 已逾期"三种状态，用于验证维保提醒口径。
-- 说明：handler_name 记岗位不记个人，避免造人名。

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001001, 60, 'EQ060', '输液泵', 1, '2026-10-15', '2027-10-15', 0, null,
       '外观、流量校准、报警测试正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 60 AND m.maintain_date = '2026-10-15');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001002, 61, 'EQ061', '注射泵', 1, '2026-08-20', '2027-08-20', 0, null,
       '外观、注射精度、报警测试正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 61 AND m.maintain_date = '2026-08-20');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001003, 62, 'EQ062', '营养泵', 1, '2025-11-10', '2026-11-10', 0, null,
       '外观、流量校准、管路接口正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 62 AND m.maintain_date = '2025-11-10');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001004, 55, 'EQ055', '多参数监护仪', 1, '2026-06-18', '2027-06-18', 0, null,
       '导联线、血氧探头、报警测试正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 55 AND m.maintain_date = '2026-06-18');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001005, 66, 'EQ066', '振动排痰机', 1, '2025-09-30', '2026-09-30', 0, null,
       '振动频率校准、背心接口检查正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 66 AND m.maintain_date = '2025-09-30');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001006, 67, 'EQ067', '心电图机(十二导)', 1, '2026-03-12', '2027-03-12', 0, null,
       '导联线、走纸、定标电压正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 67 AND m.maintain_date = '2026-03-12');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001007, 57, 'EQ057', '除颤监护仪', 1, '2026-09-05', '2027-09-05', 0, null,
       '电池容量、除颤能量、同步功能正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 57 AND m.maintain_date = '2026-09-05');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001008, 16, 'EQ016', '便携式彩色多普勒超声', 1, '2025-12-20', '2026-12-20', 0, null,
       '探头外观、图像均匀性、电池续航正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 16 AND m.maintain_date = '2025-12-20');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001009, 36, 'EQ036', '转运呼吸机', 1, '2026-05-08', '2027-05-08', 0, null,
       '气密性、氧浓度校准、报警测试正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 36 AND m.maintain_date = '2026-05-08');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001010, 64, 'EQ064', '纤维支气管镜(床旁)', 1, '2026-02-14', '2027-02-14', 0, null,
       '镜身弯曲角度、图像清晰度、消毒记录核查正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 64 AND m.maintain_date = '2026-02-14');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001011, 17, 'EQ017', '床旁超声(掌上超声)', 1, '2025-10-25', '2026-10-25', 0, null,
       '探头外观、图像质量、电池及充电座正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 17 AND m.maintain_date = '2025-10-25');

INSERT INTO biz_equipment_maintain (id, equipment_id, equipment_code, equipment_name, maintain_type, maintain_date,
                                    next_maintain_date, cost, fault_desc, handle_result, maintain_result, handler_name,
                                    create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
SELECT 3740000000000001012, 65, 'EQ065', '亚低温治疗仪', 1, '2026-07-01', '2027-07-01', 0, null,
       '水温控制精度、毯面密封、报警测试正常', 1, '设备科值班工程师', 'admin', NOW(), 'admin', NOW(), 0, 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM biz_equipment_maintain m WHERE m.equipment_id = 65 AND m.maintain_date = '2026-07-01');

UPDATE sys_equipment SET last_maintain_date = '2026-10-15' WHERE id = 60;
UPDATE sys_equipment SET last_maintain_date = '2026-08-20' WHERE id = 61;
UPDATE sys_equipment SET last_maintain_date = '2025-11-10' WHERE id = 62;
UPDATE sys_equipment SET last_maintain_date = '2026-06-18' WHERE id = 55;
UPDATE sys_equipment SET last_maintain_date = '2025-09-30' WHERE id = 66;
UPDATE sys_equipment SET last_maintain_date = '2026-03-12' WHERE id = 67;
UPDATE sys_equipment SET last_maintain_date = '2026-09-05' WHERE id = 57;
UPDATE sys_equipment SET last_maintain_date = '2025-12-20' WHERE id = 16;
UPDATE sys_equipment SET last_maintain_date = '2026-05-08' WHERE id = 36;
UPDATE sys_equipment SET last_maintain_date = '2026-02-14' WHERE id = 64;
UPDATE sys_equipment SET last_maintain_date = '2025-10-25' WHERE id = 17;
UPDATE sys_equipment SET last_maintain_date = '2026-07-01' WHERE id = 65;
