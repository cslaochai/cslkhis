package com.his.iot.service;

import com.his.iot.dto.IotHistoryQueryDTO;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.vo.IotHistoryPointVO;
import com.his.iot.vo.IotLatestVO;

import java.util.List;

/**
 * 物联网遥测查询。数据在 TDengine（iot_raw 库），本接口只读，不写业务库。
 */
public interface IotTelemetryService {

    /**
     * 设备最新一行遥测。deviceType 为空则 11 类设备全查，用于运维总览卡片。
     */
    List<IotLatestVO> latestList(IotLatestQueryDTO query);

    /**
     * 单台设备的历史遥测点，按时间升序，最多 {@code limit} 行。
     */
    List<IotHistoryPointVO> historyList(IotHistoryQueryDTO query);
}
