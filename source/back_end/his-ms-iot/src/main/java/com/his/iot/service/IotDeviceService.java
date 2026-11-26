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
}
