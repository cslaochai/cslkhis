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
    private final String text;

    ExamImageSourceEnum(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public static String textOf(Integer code) {
        for (ExamImageSourceEnum item : values()) {
            if (item.code.equals(code)) {
                return item.text;
            }
        }
        return "未知(" + code + ")";
    }
}
