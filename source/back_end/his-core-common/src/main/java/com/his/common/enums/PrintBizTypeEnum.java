package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 打印台账业务类型。
 * <p>码值 = {@code sys_print_record.biz_type} 列的取值，与建表注释逐字一致
 * （docs/03-增量SQL变更/418-打印台账表.sql）。改码值必须同步该列注释。</p>
 */
@Getter
public enum PrintBizTypeEnum {
    WRISTBAND(1, "腕带"),
    SPECIMEN_TUBE(2, "检验标本管"),
    BLOOD_BAG(3, "血袋"),
    PIVAS_LABEL(4, "静配输液贴"),
    ORAL_BAG(5, "口服药袋"),
    CSSD_PACK(6, "CSSD 器械包"),
    PATHOLOGY(7, "病理标本"),
    EXAM_FILM(8, "检查影像袋");

    private final int code;
    private final String label;

    PrintBizTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PrintBizTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PrintBizTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用：合法值返回 label，null/脏值返回空串。
     */
    public static String getLabel(Integer code) {
        PrintBizTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    /**
     * 调用方自带缺省文案。
     */
    public static String getLabel(Integer code, String defaultValue) {
        PrintBizTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    /**
     * 异常/审计用：保留原始码值，绝不喂前端展示。
     */
    public static String getLabelOrUnknown(Integer code) {
        PrintBizTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
