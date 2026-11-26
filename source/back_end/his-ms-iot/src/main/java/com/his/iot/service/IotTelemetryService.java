package com.his.iot.service;

import com.his.iot.dto.IotColdChainRecordQueryDTO;
import com.his.iot.dto.IotHistoryQueryDTO;
import com.his.iot.dto.IotLatestQueryDTO;
import com.his.iot.vo.IotColdChainRecordVO;
import com.his.iot.vo.IotColdChainSummaryVO;
import com.his.iot.vo.IotDeviceTypeVO;
import com.his.iot.vo.IotHistoryPointVO;
import com.his.iot.vo.IotLatestVO;

import java.util.List;

/**
 * 物联网遥测查询。数据在 TDengine（iot_raw 库），本接口只读，不写业务库。
 */
public interface IotTelemetryService {

    /**
     * 设备类型下拉。类型注册表在 DeviceTypeSchema，前端不重复维护。
     */
    List<IotDeviceTypeVO> deviceTypes();

    /**
     * 设备最新一行遥测。deviceType 为空则 11 类设备全查，用于运维总览卡片。
     */
    List<IotLatestVO> latestList(IotLatestQueryDTO query);

    /**
     * 单台设备的历史遥测点，按时间升序，最多 {@code limit} 行。
     */
    List<IotHistoryPointVO> historyList(IotHistoryQueryDTO query);

    /**
     * 冷链温湿度记录：固定 5 分钟一窗，含平均/最低/最高与点数，供 GSP 检查导出。
     * 与 historyList 分开是因为原始点太密（5 秒一条，一天 1.7 万点），导出要的是可归档的记录表。
     */
    List<IotColdChainRecordVO> coldChainRecords(IotColdChainRecordQueryDTO query);

    /**
     * 只查 TDengine 的 5 分钟窗口记录，不含 GSP 归档表。
     * <p>
     * 归档任务专用：它自己就是往归档表写，再调 {@link #coldChainRecords} 会把刚写进去的行读回来
     * 去撞唯一键，白跑一遍读 + 写。
     */
    List<IotColdChainRecordVO> coldChainRealtimeRecords(IotColdChainRecordQueryDTO query);

    /**
     * 冷链近 N 小时汇总（最低/最高温度、归档窗口数、原始点数）。卡片一眼看稳定性，不用逐台拉曲线。
     */
    List<IotColdChainSummaryVO> coldChainSummary(Integer hours);
}
