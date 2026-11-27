package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物联网设备事件码（biz_iot_event.event_code）→ 中文文案。
 * <p>
 * 码值来自设备/模拟器 MQTT 报文 {@code event} 字段，全仓唯一映射处；前端只渲染 {@code eventCodeText}。
 */
@Getter
public enum IotEventCodeEnum {

    CYCLE_ABORTED("CYCLE_ABORTED", "消毒中断"),
    CYCLE_STARTED("CYCLE_STARTED", "开始消毒"),
    CYCLE_COMPLETE("CYCLE_COMPLETE", "消毒完成"),
    LAMP_NEED_REPLACE("LAMP_NEED_REPLACE", "灯管接近寿命"),
    FILTER_LOW("FILTER_LOW", "滤网余量低"),
    OUT_OF_AREA("OUT_OF_AREA", "离开授权区域"),
    RETURNED("RETURNED", "返回授权区域"),
    LOW_BATTERY("LOW_BATTERY", "电量低，请更换电池"),
    TAG_TAMPER("TAG_TAMPER", "腕带被拆"),
    MATCH_LOST("MATCH_LOST", "母婴配对丢失"),
    MATCH_RESTORED("MATCH_RESTORED", "配对恢复"),
    BED_EXIT("BED_EXIT", "患者离床"),
    BED_RETURN("BED_RETURN", "患者回床"),
    VITAL_ABNORMAL("VITAL_ABNORMAL", "生命体征异常"),
    VITAL_RECOVER("VITAL_RECOVER", "生命体征恢复"),
    BED_EXIT_LONG("BED_EXIT_LONG", "离床超时"),
    DOOR_OPEN("DOOR_OPEN", "门开启"),
    DOOR_CLOSED("DOOR_CLOSED", "门关闭"),
    COMPRESSOR_FAULT("COMPRESSOR_FAULT", "压缩机异常"),
    TEMP_HIGH("TEMP_HIGH", "温度超上限"),
    TEMP_RECOVER("TEMP_RECOVER", "温度恢复"),
    TASK_START("TASK_START", "配送任务开始"),
    ELEVATOR_WAIT("ELEVATOR_WAIT", "机器人等电梯"),
    TASK_COMPLETE("TASK_COMPLETE", "配送任务完成"),
    CHARGE_COMPLETE("CHARGE_COMPLETE", "充电完成"),
    BATTERY_LOW("BATTERY_LOW", "电量低"),
    BATTERY_EMPTY("BATTERY_EMPTY", "电量耗尽"),
    BATTERY_REPLACED("BATTERY_REPLACED", "已更换电池，电量恢复"),
    POWER_LOST("POWER_LOST", "市电断开，转后备电池"),
    POWER_BACK("POWER_BACK", "市电恢复"),
    SWAP_OUT("SWAP_OUT", "设备下机（换机）"),
    TREATMENT_DONE("TREATMENT_DONE", "透析疗程完成"),
    TMP_HIGH("TMP_HIGH", "跨膜压超限"),
    PRESSURE_LOW("PRESSURE_LOW", "压力偏低"),
    PRESSURE_RECOVER("PRESSURE_RECOVER", "压力恢复"),
    REGULATOR_FAULT("REGULATOR_FAULT", "减压阀异常"),
    OCCLUSION("OCCLUSION", "管路阻塞"),
    OCCLUSION_CLEARED("OCCLUSION_CLEARED", "阻塞解除"),
    INFUSION_DONE("INFUSION_DONE", "输液结束"),
    BAG_REPLACED("BAG_REPLACED", "已换瓶"),
    NEAR_EMPTY("NEAR_EMPTY", "余量不足"),
    CABINET_OPEN("CABINET_OPEN", "药柜开柜"),
    DRUG_PICKED("DRUG_PICKED", "取用药"),
    STOCK_LOW("STOCK_LOW", "库存不足"),
    STOCK_REFILLED("STOCK_REFILLED", "库存已补充");

    private final String code;
    private final String label;

    IotEventCodeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IotEventCodeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (IotEventCodeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        IotEventCodeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        IotEventCodeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        IotEventCodeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
