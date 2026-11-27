-- 408 设备换机仿真铺底（输液泵独立共享池 · 补备用泵）
-- 背景：380 上了「设备共享调配」，但 biz_iot_device 里 PUMP-xx 的 equipment_id 全是空，
--       共享池「输液泵 EQ060」底下挂不到任何具体泵，发放时只能写台账编码 EQ060、不产生 IoT 绑定。
-- 口径：
--   1) 病区泵单独一条台账「输液泵（共享池）EQ091」：EQ060 上挂的是资产标签 ASSET-01（373），
--      如果泵也挂到 EQ060，发放会按编号升序先选中 ASSET-01，把标签当泵发出去；
--   2) PUMP-01~09（在患者身上）与 PUMP-11/12（备用）都登记到 EQ091 下；
--   3) PUMP-10 是病区自留泵，不进共享池（不挂 equipment_id）；
--   4) 补 PUMP-11/12 两台备用泵，放心血管内科护士站、不绑患者，换机时才有满电泵可发。
-- 回滚：
--   UPDATE biz_iot_device SET equipment_id = NULL WHERE equipment_id = 91;
--   DELETE FROM biz_iot_device WHERE id IN (355000000000000064, 355000000000000065);
--   DELETE FROM sys_equipment WHERE id = 91;
--   （泵已被申请单引用时先查 biz_device_rental_item.device_no 再决定删不删）

-- 1) 病区输液泵共享池台账（category 5-治疗设备，与 EQ060 同口径）
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           status, rentable, remark,
                           create_by, create_time, update_by, update_time, del_flag, create_by_id, update_by_id)
VALUES (91, 'EQ091', '输液泵（共享池）', 5, 19580004, '心血管内科', '科曼', 'P-600',
        1, 1, '病区输液泵共享池，发放落到具体 PUMP-xx 并绑定患者',
        'admin', NOW(), 'admin', NOW(), 0, 0, 0);

-- 2) 备用泵（满电入库，可被共享调配发放）
INSERT IGNORE INTO biz_iot_device (id, device_no, device_type, device_name, dept_id, dept_name, location,
                            bind_type, bind_id, fw_version, online, last_seen, last_state, alarm_flag,
                            enabled, equipment_id, create_by, create_time, update_by, update_time,
                            del_flag, remark, create_by_id, update_by_id)
VALUES
 (355000000000000064, 'PUMP-11', 'infusion', '输液泵 PUMP-11', 19580004, '心血管内科', '心血管内科护士站',
  'none', NULL, '1.0.4', 0, NULL, '', 0, 1, 91, 'admin', NOW(), 'admin', NOW(), 0,
  '共享池备用泵（换机专用）', 0, 0),
 (355000000000000065, 'PUMP-12', 'infusion', '输液泵 PUMP-12', 19580004, '心血管内科', '心血管内科护士站',
  'none', NULL, '1.0.4', 0, NULL, '', 0, 1, 91, 'admin', NOW(), 'admin', NOW(), 0,
  '共享池备用泵（换机专用）', 0, 0);

-- 3) 病区泵登记到 EQ091（含 408 首版误挂到 EQ060 的 9 台，重复执行也幂等）
UPDATE biz_iot_device
SET equipment_id = 91,
    update_by    = 'admin',
    update_time  = NOW()
WHERE device_type = 'infusion'
  AND device_no <> 'PUMP-10'
  AND (equipment_id IS NULL OR equipment_id <> 91);
