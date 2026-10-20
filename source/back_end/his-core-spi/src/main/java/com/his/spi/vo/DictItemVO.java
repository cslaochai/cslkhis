package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典项：跨模块做码值↔文案互译的出参（只带翻译要用的两列）。
 */
@Data
@NoArgsConstructor
public class DictItemVO {

    /**
     * 字典值（存储的码值文本）
     */
    private String dictValue;

    /**
     * 字典标签（展示文案）
     */
    private String dictLabel;
}
