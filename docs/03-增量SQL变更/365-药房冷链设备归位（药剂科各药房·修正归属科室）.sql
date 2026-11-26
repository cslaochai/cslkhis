-- 365 药房冷链设备归位：冷链柜属药剂科，不是病区设备。
-- 背景：原铺底数据把 FRIDGE-01~06 全挂在消化内科病区，药房页面显示「消化内科 病区」，
--       与 GSP「药品冷藏设备由药品经营部门管理」不符。按真实医院药房布局重新归属。

-- 门诊西药房（19580043）：2 台，冷藏药品日常调配量最大
UPDATE biz_iot_device SET dept_id = 19580043, dept_name = '门诊西药房',
       device_name = '门诊西药房冷藏柜1', location = '门诊西药房 冷藏区 A 柜'
 WHERE device_no = 'FRIDGE-01' AND device_type = 'coldchain' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580043, dept_name = '门诊西药房',
       device_name = '门诊西药房冷藏柜2', location = '门诊西药房 冷藏区 B 柜'
 WHERE device_no = 'FRIDGE-02' AND device_type = 'coldchain' AND del_flag = 0;

-- 住院药房（19580045）：2 台，病区冷藏药品（胰岛素、部分生物制品）集中存放
UPDATE biz_iot_device SET dept_id = 19580045, dept_name = '住院药房',
       device_name = '住院药房冷藏柜1', location = '住院药房 冷藏区 A 柜'
 WHERE device_no = 'FRIDGE-03' AND device_type = 'coldchain' AND del_flag = 0;

UPDATE biz_iot_device SET dept_id = 19580045, dept_name = '住院药房',
       device_name = '住院药房冷藏柜2', location = '住院药房 冷藏区 B 柜'
 WHERE device_no = 'FRIDGE-04' AND device_type = 'coldchain' AND del_flag = 0;

-- 静脉配液中心（19580046）：1 台，配置后待输注的冷藏药品暂存
UPDATE biz_iot_device SET dept_id = 19580046, dept_name = '静脉配液中心',
       device_name = '静配中心冷藏柜', location = '静配中心 成品暂存区'
 WHERE device_no = 'FRIDGE-05' AND device_type = 'coldchain' AND del_flag = 0;

-- 药剂科药库（19580042）：1 台，整箱冷藏药品待验/待发
UPDATE biz_iot_device SET dept_id = 19580042, dept_name = '药剂科',
       device_name = '药库冷藏柜', location = '药剂科药库 冷藏区'
 WHERE device_no = 'FRIDGE-06' AND device_type = 'coldchain' AND del_flag = 0;
