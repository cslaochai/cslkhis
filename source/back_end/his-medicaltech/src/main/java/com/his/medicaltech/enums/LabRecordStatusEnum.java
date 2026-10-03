package com.his.medicaltech.enums;

/**
 * 记录状态枚举类
 */
public enum LabRecordStatusEnum {

    REGISTERED(1, "已登记"),
    SAMPLED(2, "已采样"),
    RECEIVED(3, "已接收"),
    TESTING(4, "检测中"),
    RESULTED(5, "已出结果"),
    REVIEWED(6, "已审核"),
    RELEASED(7, "已发布"),
    CANCELLED(8, "已取消");

    private final int code;
    private final String description;

    LabRecordStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取对应的枚举实例
     * @param code 状态码
     * @return 对应的枚举实例，若未找到则返回 null
     */
    public static LabRecordStatusEnum getByCode(int code) {
        for (LabRecordStatusEnum status : LabRecordStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}