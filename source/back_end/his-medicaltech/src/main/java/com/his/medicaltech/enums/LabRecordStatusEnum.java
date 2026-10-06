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

    /**
     * 根据状态码获取对应的枚举实例
     *
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

    /**
     * Integer 入口（null 安全，与 {@link #getByCode(int)} 同义）
     */
    public static LabRecordStatusEnum fromCode(Integer code) {
        return code == null ? null : getByCode(code);
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null / 越界码值返回空串 ——
     * 检验的码表与检查**不是同一套**（这里 2=已采样、3=已接收，检查的 2=已签到），
     * 措辞差异由调用侧按语境处理，不改这里的 description。
     */
    public static String getText(Integer code) {
        LabRecordStatusEnum item = fromCode(code);
        return item == null ? "" : item.description;
    }

    /**
     * 异常 / 审计用：null 或越界码值返回「未知(n)」（null 本身渲染成「未知」）。
     */
    public static String labelOrUnknown(Integer code) {
        LabRecordStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}