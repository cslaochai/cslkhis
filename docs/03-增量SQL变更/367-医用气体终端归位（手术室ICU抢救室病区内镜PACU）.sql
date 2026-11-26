-- 367 医用气体终端归位：原 6 台全挂在消化内科病区，改成真实院内气体终端点位。
-- 医用气体报警按「点位」管理：手术室/ICU/抢救室/病区治疗带/内镜中心/PACU 各一个。

UPDATE biz_iot_device SET dept_id = 19580039, dept_name = '麻醉科',
       device_name = '手术室气体终端', location = '手术室 3 号间 吊塔'
 WHERE device_no = 'GAS-01' AND device_type = 'gas' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580050, dept_name = '重症医学科(ICU)',
       device_name = 'ICU 气体终端', location = 'ICU 1 号吊塔'
 WHERE device_no = 'GAS-02' AND device_type = 'gas' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580048, dept_name = '急诊内科',
       device_name = '抢救室气体终端', location = '急诊抢救室 1 区'
 WHERE device_no = 'GAS-03' AND device_type = 'gas' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580003, dept_name = '呼吸内科',
       device_name = '呼吸病区气体终端', location = '呼吸内科病区 治疗带'
 WHERE device_no = 'GAS-04' AND device_type = 'gas' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580004, dept_name = '消化内科',
       device_name = '内镜中心气体终端', location = '内镜中心 2 号检查间'
 WHERE device_no = 'GAS-05' AND device_type = 'gas' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580039, dept_name = '麻醉科',
       device_name = 'PACU 气体终端', location = '麻醉恢复室(PACU)'
 WHERE device_no = 'GAS-06' AND device_type = 'gas' AND del_flag = 0;
