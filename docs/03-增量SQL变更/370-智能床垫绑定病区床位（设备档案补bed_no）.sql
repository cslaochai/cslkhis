-- 370 智能床垫设备绑定病区床位 + 设备档案补 bed_no
-- 背景：智能床垫是"固定安装"设备，跟着床位走，不跟患者走（患者转床由 ADT 换床，床垫不动）。
--       原 8 台床垫全部挂在"消化内科/消化内科病区"，location 只写病区名，护士站无法把床垫读数落到具体床位。
-- 方案：设备档案新增 bed_no（仅床垫/在床监测类使用），把 8 台床垫落到消化内科病区 13~20 床（当前均为在院患者）。

ALTER TABLE biz_iot_device
    ADD COLUMN bed_no VARCHAR(32) NULL COMMENT '固定安装床位号（床垫/在床监测类设备专用，其他类型为空）'
        AFTER location;

UPDATE biz_iot_device
SET bed_no = '13', device_name = '13床床垫监测', location = '消化内科病区 13床'
WHERE device_no = 'BED-01' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '14', device_name = '14床床垫监测', location = '消化内科病区 14床'
WHERE device_no = 'BED-02' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '15', device_name = '15床床垫监测', location = '消化内科病区 15床'
WHERE device_no = 'BED-03' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '16', device_name = '16床床垫监测', location = '消化内科病区 16床'
WHERE device_no = 'BED-04' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '17', device_name = '17床床垫监测', location = '消化内科病区 17床'
WHERE device_no = 'BED-05' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '18', device_name = '18床床垫监测', location = '消化内科病区 18床'
WHERE device_no = 'BED-06' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '19', device_name = '19床床垫监测', location = '消化内科病区 19床'
WHERE device_no = 'BED-07' AND device_type = 'bed';

UPDATE biz_iot_device
SET bed_no = '20', device_name = '20床床垫监测', location = '消化内科病区 20床'
WHERE device_no = 'BED-08' AND device_type = 'bed';
