-- 372 智能床垫试点病区调整（床位落到真实在院患者，覆盖 3 个病区）
-- 背景：370 把 8 台床垫全放在消化内科 13~20 床，但该段床位是联调批量建的同一批测试入院（8 人同为 52 岁男性），
--       护士站一屏 8 张卡全是同龄同性，既不像真实病区，也不利于验证"按患者看体征"。
-- 调整：按护理部试点铺开——消化内科 4 床、呼吸内科 2 床、心血管内科 2 床，床位均选当前在院且信息完整的患者。
--       床垫跟床走，设备档案的 dept_id/dept_name/bed_no/location 一起改。

UPDATE biz_iot_device
SET dept_id = 19580004, dept_name = '消化内科', bed_no = '09', location = '消化内科病区 09床',
    device_name = '09床床垫监测'
WHERE device_no = 'BED-01' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580004, dept_name = '消化内科', bed_no = '11', location = '消化内科病区 11床',
    device_name = '11床床垫监测'
WHERE device_no = 'BED-02' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580004, dept_name = '消化内科', bed_no = '12', location = '消化内科病区 12床',
    device_name = '12床床垫监测'
WHERE device_no = 'BED-03' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580004, dept_name = '消化内科', bed_no = '13', location = '消化内科病区 13床',
    device_name = '13床床垫监测'
WHERE device_no = 'BED-04' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580003, dept_name = '呼吸内科', bed_no = '11', location = '呼吸内科病区 11床',
    device_name = '11床床垫监测'
WHERE device_no = 'BED-05' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580003, dept_name = '呼吸内科', bed_no = '12', location = '呼吸内科病区 12床',
    device_name = '12床床垫监测'
WHERE device_no = 'BED-06' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580005, dept_name = '心血管内科', bed_no = '10', location = '心血管内科病区 10床',
    device_name = '10床床垫监测'
WHERE device_no = 'BED-07' AND device_type = 'bed';

UPDATE biz_iot_device
SET dept_id = 19580005, dept_name = '心血管内科', bed_no = '11', location = '心血管内科病区 11床',
    device_name = '11床床垫监测'
WHERE device_no = 'BED-08' AND device_type = 'bed';
