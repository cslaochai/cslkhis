package com.his.iot.service;

import com.his.iot.dto.IotBindQueryDTO;
import com.his.iot.dto.IotDeviceBindDTO;
import com.his.iot.dto.IotDeviceUnbindDTO;
import com.his.iot.entity.IotDeviceBind;
import com.his.iot.vo.IotBindCandidateDeviceVO;
import com.his.iot.vo.IotDeviceBindVO;

import java.util.List;

/**
 * 设备—患者绑定（模拟扫码）：入院需要输液设备时绑到在院患者，换泵/治疗结束/出院时解绑。
 */
public interface IotDeviceBindService {

    /**
     * 当前在用绑定清单（护士站按 admissionId 挂到床位卡）。
     */
    List<IotDeviceBindVO> listActiveBinds(IotBindQueryDTO query);

    /**
     * 设备当前在用绑定（含床位/科室快照）。设备事件推送时用它把 device_no 折成科室，
     * 一台设备同时只有一条在用绑定；没有绑定返回 null。
     */
    IotDeviceBind findActiveBind(String deviceNo);

    /**
     * 可绑定设备清单（启用且无在用绑定）。
     */
    List<IotBindCandidateDeviceVO> selectBindableDevices(IotBindQueryDTO query);

    /**
     * 绑定设备到在院患者。
     *
     * @return 新绑定记录ID
     */
    Long bindDevice(IotDeviceBindDTO dto);

    /**
     * 解绑设备（按设备编号找当前在用绑定）。
     */
    void unbindDevice(IotDeviceUnbindDTO dto);
}
