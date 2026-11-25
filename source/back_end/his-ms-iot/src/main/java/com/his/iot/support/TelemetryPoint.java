package com.his.iot.support;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * 一条待落库的遥测点。
 * <p>
 * 只在 support 包内部流转，不进 Controller 契约；对外出参是 VO。
 *
 * @param deviceType 设备类型（coldchain/infusion/...），决定写哪张超级表
 * @param deviceNo   设备编号（PUMP-01），决定子表名与 tag
 * @param ts         设备上报时间，坏值在解析处已回落为本机当前时间
 * @param metrics    原始 metrics 对象（camelCase），列名由 {@link DeviceTypeSchema} 翻译
 */
public record TelemetryPoint(String deviceType, String deviceNo, LocalDateTime ts, JsonNode metrics) {
}
