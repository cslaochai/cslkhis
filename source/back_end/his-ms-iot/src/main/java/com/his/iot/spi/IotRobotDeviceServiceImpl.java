package com.his.iot.spi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.his.common.exception.BusinessException;
import com.his.iot.entity.IotDevice;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.spi.service.IotRobotDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 机器人接入台账适配器（biz_iot_device）。
 * <p>
 * 只依赖本模块 mapper，不注入任何编排服务：设备档案保存时同步接入行属于"写一台设备"的叶子动作，
 * 一旦牵编排服务，platform 侧的设备档案保存就会跟着拖进 MQTT/派单整张服务图。
 */
@Service
@RequiredArgsConstructor
public class IotRobotDeviceServiceImpl implements IotRobotDeviceService {

    /**
     * 接入类型：物流机器人（DeviceTypeSchema 的注册键，派单算法按它筛车）
     */
    private static final String DEVICE_TYPE_ROBOT = "robot";

    /**
     * 机器人的供电方式固定为回桩充电（台账只标怎么供电，实时电量走遥测）
     */
    private static final String POWER_TYPE_DOCK = "battery_dock";

    private final IotDeviceMapper iotDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncRobot(String deviceNo, String deviceName, Long deptId, String deptName,
                          Integer enabled, Long equipmentId) {
        IotDevice device = iotDeviceMapper.selectOne(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getDeviceNo, deviceNo)
                .last("LIMIT 1"));
        if (device == null) {
            IotDevice created = new IotDevice();
            created.setDeviceNo(deviceNo);
            created.setDeviceType(DEVICE_TYPE_ROBOT);
            created.setPowerType(POWER_TYPE_DOCK);
            created.setDeviceName(deviceName == null ? "" : deviceName);
            created.setDeptId(deptId);
            created.setDeptName(deptName == null ? "" : deptName);
            created.setLocation("");
            created.setEnabled(enabled == null ? 0 : enabled);
            created.setEquipmentId(equipmentId);
            iotDeviceMapper.insert(created);
            return;
        }
        // 编号是全院唯一的 MQTT 设备号：撞上别的类型说明录入错了，不能把它改成机器人
        if (!DEVICE_TYPE_ROBOT.equals(device.getDeviceType())) {
            throw new BusinessException(400, "设备编码已被其他类型物联网设备占用：" + deviceNo);
        }
        // 同一编号已挂到另一份资产档案上：说明是改错了资产行，不能静默覆盖别人的绑定
        if (device.getEquipmentId() != null && !device.getEquipmentId().equals(equipmentId)) {
            throw new BusinessException(400, "接入编号已被其他设备档案绑定：" + deviceNo);
        }
        device.setDeviceName(deviceName == null ? "" : deviceName);
        device.setDeptId(deptId);
        device.setDeptName(deptName == null ? "" : deptName);
        device.setEnabled(enabled == null ? 0 : enabled);
        device.setEquipmentId(equipmentId);
        iotDeviceMapper.updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void detachByEquipmentId(Long equipmentId) {
        if (equipmentId == null) {
            return;
        }
        List<IotDevice> bound = iotDeviceMapper.selectList(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getEquipmentId, equipmentId)
                .eq(IotDevice::getDeviceType, DEVICE_TYPE_ROBOT));
        for (IotDevice device : bound) {
            device.setEnabled(0);
            iotDeviceMapper.updateById(device);
        }
    }
}
