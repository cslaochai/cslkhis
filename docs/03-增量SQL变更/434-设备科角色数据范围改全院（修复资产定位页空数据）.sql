-- =====================================================================
-- 434 设备与供应室管理员(39 / 10049)数据范围改为全院
--
-- 现象：设备科账号进「资产定位与维保」，定位标签/在线/离位告警全为 0，提示没有匹配设备。
-- 根因：
--   1) 角色 39 data_scope=3（本部门），岗位挂在设备科(19580082)，授权科室集合 = {设备科}；
--   2) biz_iot_device 的 asset 标签归属科室全是临床科室（消化/呼吸/心内/急诊/ICU，见 373），
--      biz_iot_event 的 dept_id 同为临床科室；
--   3) IotDeviceServiceImpl.selectList 按 dept_id IN 授权科室收口，交集为空时强制空结果；
--      IotTelemetryServiceImpl.latestList 先取 selectList 的允许编号再过滤时序库，清单空则遥测全空；
--      IotEventServiceImpl.listPage 同样按授权科室收口，告警一并滤空。
--   设备科是全院保障职能岗（设备分布在各临床科室），按本科室收口必然一台都看不到。
--
-- 修法：data_scope 放开为 1（全部数据）。参照 237 挂号/收费岗同类问题；
--       同院级职能岗 25 门诊办 / 26 医务科 / 33 信息科均为 data_scope=1。
-- 幂等：按 role_code 定位，重复执行结果一致。
-- 执行后清 Redis 权限/数据范围缓存（若有），或重新登录生效。
-- =====================================================================

UPDATE sys_role
SET data_scope = 1,
    remark = '设备管理/CSSD/医废/器械包模板；全院保障职能岗，data_scope=1 全院（移动设备分布各临床科室）',
    update_by = 'sql434', update_by_id = 1, update_time = NOW()
WHERE role_code = '10049' AND del_flag = 0;

-- 回查：data_scope 应为 1
-- SELECT id, role_code, role_name, data_scope FROM sys_role WHERE role_code = '10049' AND del_flag = 0;

-- 回滚：
-- UPDATE sys_role SET data_scope = 3, update_by = 'sql434', update_time = NOW()
--  WHERE role_code = '10049' AND del_flag = 0;
