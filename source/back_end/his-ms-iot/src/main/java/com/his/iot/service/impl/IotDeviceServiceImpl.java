package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.iot.dto.IotDeviceQueryDTO;
import com.his.iot.entity.IotDevice;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.iot.service.IotDeviceService;
import com.his.iot.vo.IotDeviceVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备档案只读查询。只返回启用设备：停用设备不参与任何监控页面。
 */
@Service
public class IotDeviceServiceImpl extends ServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceService {

    @Override
    public List<IotDeviceVO> selectList(IotDeviceQueryDTO query) {
        return list(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getEnabled, 1)
                .eq(query.getDeviceType() != null && !query.getDeviceType().isBlank(),
                        IotDevice::getDeviceType, query.getDeviceType())
                .eq(query.getDeviceNo() != null && !query.getDeviceNo().isBlank(),
                        IotDevice::getDeviceNo, query.getDeviceNo())
                .orderByAsc(IotDevice::getDeviceType)
                .orderByAsc(IotDevice::getDeviceNo))
                .stream().map(this::toVO).toList();
    }

    private IotDeviceVO toVO(IotDevice d) {
        IotDeviceVO vo = new IotDeviceVO();
        vo.setDeviceNo(d.getDeviceNo());
        vo.setDeviceType(d.getDeviceType());
        vo.setPowerType(d.getPowerType());
        vo.setDeviceName(d.getDeviceName());
        vo.setDeptName(d.getDeptName());
        vo.setLocation(d.getLocation());
        vo.setBedNo(d.getBedNo());
        vo.setEquipmentId(d.getEquipmentId());
        vo.setEnabled(d.getEnabled());
        return vo;
    }
}
