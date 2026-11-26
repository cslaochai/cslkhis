-- 373 资产定位标签挂接设备台账 + 设备档案补 equipment_id
-- 背景：12 个通用 "资产定位标签 ASSET-xx" 没有任何被定位对象，护士/设备科看到的是编号不是设备。
-- 方案：设备档案新增 equipment_id（仅 asset 类型），标签挂到 sys_equipment 的真实在用设备；
--       标签归属科室/安装区域改成设备实际科室（信标 homeArea 与设备平台造数已按同一区域对齐）。
-- 设备台账只读展示走 /equipment/equipment/listPage，不在 iot 模块直连设备科表（跨模块走接口）。

ALTER TABLE biz_iot_device
    ADD COLUMN equipment_id BIGINT NULL COMMENT '资产标签绑定的设备台账ID（仅 asset 类型）'
        AFTER bed_no;

UPDATE biz_iot_device
SET equipment_id = 60, device_name = '输液泵定位标签', dept_id = 19580004, dept_name = '消化内科',
    location = '住院楼3F-消化内科病区'
WHERE device_no = 'ASSET-01' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 61, device_name = '注射泵定位标签', dept_id = 19580004, dept_name = '消化内科',
    location = '住院楼3F-消化内科病区'
WHERE device_no = 'ASSET-02' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 62, device_name = '营养泵定位标签', dept_id = 19580004, dept_name = '消化内科',
    location = '住院楼3F-消化内科病区'
WHERE device_no = 'ASSET-03' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 55, device_name = '多参数监护仪定位标签', dept_id = 19580004, dept_name = '消化内科',
    location = '住院楼3F-消化内科病区'
WHERE device_no = 'ASSET-04' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 66, device_name = '振动排痰机定位标签', dept_id = 19580003, dept_name = '呼吸内科',
    location = '住院楼4F-呼吸内科病区'
WHERE device_no = 'ASSET-05' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 67, device_name = '心电图机定位标签', dept_id = 19580005, dept_name = '心血管内科',
    location = '住院楼5F-心内科'
WHERE device_no = 'ASSET-06' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 57, device_name = '除颤监护仪定位标签', dept_id = 19580048, dept_name = '急诊内科',
    location = '急诊楼1F-抢救室'
WHERE device_no = 'ASSET-07' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 16, device_name = '便携式彩超定位标签', dept_id = 19580048, dept_name = '急诊内科',
    location = '急诊楼1F-抢救室'
WHERE device_no = 'ASSET-08' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 36, device_name = '转运呼吸机定位标签', dept_id = 19580048, dept_name = '急诊内科',
    location = '急诊楼1F-抢救室'
WHERE device_no = 'ASSET-09' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 64, device_name = '纤维支气管镜定位标签', dept_id = 19580050, dept_name = '重症医学科(ICU)',
    location = '住院楼6F-ICU'
WHERE device_no = 'ASSET-10' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 17, device_name = '床旁超声定位标签', dept_id = 19580050, dept_name = '重症医学科(ICU)',
    location = '住院楼6F-ICU'
WHERE device_no = 'ASSET-11' AND device_type = 'asset';

UPDATE biz_iot_device
SET equipment_id = 65, device_name = '亚低温治疗仪定位标签', dept_id = 19580050, dept_name = '重症医学科(ICU)',
    location = '住院楼6F-ICU'
WHERE device_no = 'ASSET-12' AND device_type = 'asset';
