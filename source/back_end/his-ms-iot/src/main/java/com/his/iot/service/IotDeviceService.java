package com.his.iot.service;

import com.his.iot.dto.IotDeviceQueryDTO;
import com.his.iot.vo.IotDeviceVO;

import java.util.List;

/**
 * 设备档案查询（只读）。实时值走遥测接口，绑定关系走绑定服务。
 */
public interface IotDeviceService {

    /**
     * 设备清单：按类型筛选，含名称/归属/位置。
     */
    List<IotDeviceVO> selectList(IotDeviceQueryDTO query);

    /**
     * 全院共享车队清单：按类型取启用设备，<b>不做科室数据收口</b>。
     * <p>
     * 机器人是全院共享资源（收费站点只是收发站点，不配车），护士站/设备科的受限岗位
     * 也必须看到整队，否则调度页看不到车、派单也选不到车。
     */
    List<IotDeviceVO> listSharedFleet(String deviceType);
}
