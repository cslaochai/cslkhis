package com.his.iot.vo;

import lombok.Data;

/**
 * 设备类型下拉项。来源是 {@link com.his.iot.support.DeviceTypeSchema}（后端唯一的类型注册表），
 * 前端不自己维护一份类型清单——否则新增设备类型时页面下拉会漏。
 */
@Data
public class IotDeviceTypeVO {

    /** 类型编码，与 MQTT 主题里的 {type}、时序库 st_* 一一对应 */
    private String type;

    /** 中文名，用于下拉展示与表格列 */
    private String label;
}
