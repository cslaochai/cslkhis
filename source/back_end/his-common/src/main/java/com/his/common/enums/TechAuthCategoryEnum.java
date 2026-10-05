package com.his.common.enums;

import lombok.Getter;

/**
 * 医疗技术授权类别枚举（sql/155）。
 *
 * <p>三类别同表不同码，是因为标准把它们放在同一个准入制度里管
 * （《医疗技术临床应用管理办法》的分级授权既管手术也管麻醉与内镜/介入）。
 * 分表会让「这个人有没有权限做这台三级手术的麻醉」变成两次查询加一次口径漂移。
 * <br>字典权威在本枚举，码值改动必须同步 {@code sql/155} 的 {@code his_tech_auth_category} 段。
 */
@Getter
public enum TechAuthCategoryEnum {

    /**
     * 手术：排台主刀、手术医嘱、日间手术的准入按此类别
     */
    SURGERY(1, "手术"),
    /**
     * 麻醉：按所伴手术同级授权，排台麻醉医师按此类别
     */
    ANESTHESIA(2, "麻醉"),
    /**
     * 内镜与介入：内镜/介入操作者按此类别（ERCP 等按四级）
     */
    ENDOSCOPY(3, "内镜与介入");

    private final int code;
    private final String label;

    TechAuthCategoryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechAuthCategoryEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TechAuthCategoryEnum category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        TechAuthCategoryEnum category = fromCode(code);
        return category == null ? "未知(" + code + ")" : category.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        TechAuthCategoryEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
