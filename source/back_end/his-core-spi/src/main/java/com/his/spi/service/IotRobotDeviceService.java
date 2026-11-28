package com.his.spi.service;

/**
 * 机器人接入台账端口（宿主模块 his-ms-iot，biz_iot_device 是接入台账的宿主）。
 * <p>
 * 设备档案（sys_equipment，platform 模块）只管固定资产属性；能不能被派单，取决于接入台账里有没有
 * 同编号的 robot 行且处于启用态。两边靠「设备编码 = MQTT 设备号」对上，本端口就是这条绑定关系的唯一写入口，
 * platform 侧不跨模块读 biz_iot_device。
 */
public interface IotRobotDeviceService {

    /**
     * 同步一台物流机器人的接入台账：编号不存在则建行，已存在则更新名称/归属/位置与启停。
     *
     * @param deviceNo    接入编号（= 设备台账的设备编码）
     * @param deviceName  设备名称
     * @param deptId      归属科室ID
     * @param deptName    归属科室名称
     * @param enabled     启用状态（1-启用 0-停用）：设备台账非「在用」即停用，停用的车不参与派单
     * @param equipmentId 设备台账ID（回填外键，供两页互查）
     */
    void syncRobot(String deviceNo, String deviceName, Long deptId, String deptName,
                   Integer enabled, Long equipmentId);

    /**
     * 设备不再属于物流设备类别（类别改走）时，把已绑定的接入行停用，避免派单算法仍把它当可用车。
     */
    void detachByEquipmentId(Long equipmentId);
}
