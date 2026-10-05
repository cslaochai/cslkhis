package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 内镜检查类型枚举（码值口径 = 字典 his_endoscopy_type 中**代码里需要判定**的三个值）。
 *
 * <p>全量类型的文案权威在字典（页面渲染走 {@code DictCacheService.text}）；
 * 本枚举只收代码里要做业务判定/默认值的三类，desc 与字典对齐，勿在此扩全量清单。
 */
@Getter
public enum EndoscopyTypeEnum {

    GASTRO(1, "胃镜"),
    COLON(2, "肠镜"),
    /**
     * ERCP 是内镜下的介入操作，按四级技术管理（sql/155 起参与授权闸门）
     */
    ERCP(7, "ERCP");

    private final int code;
    private final String label;

    EndoscopyTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
