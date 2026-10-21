package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用下拉选项（码值 + 文案），供本模块各查询卡使用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionItemVO {

    /** 选项码值 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 选项展示文案 */
    private String text;
}
