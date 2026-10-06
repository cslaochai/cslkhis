package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 影像来源（检查影像帧的来源）
 *
 * <p>必须落库而不是前端猜：学习阶段不接真设备，来源 2 的帧是后端 java.awt 现画的灰阶测试图，
 * 界面上要能一眼看出「这不是病人真实的片子」。真接 PACS 后新数据一律走 1。
 */
@Getter
public enum ExamImageSourceEnum {

    UPLOAD(1, "工作站上传"),
    MOCK_DICOM(2, "模拟DICOM导入");

    private final Integer code;
    private final String label;

    ExamImageSourceEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ExamImageSourceEnum fromCode(Integer code) {
        for (ExamImageSourceEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        ExamImageSourceEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ExamImageSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
