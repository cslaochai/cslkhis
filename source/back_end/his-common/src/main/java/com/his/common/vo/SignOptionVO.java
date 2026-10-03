package com.his.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉选项（id 用字符串以容纳雪花 ID，不给前端留 Number 精度坑）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignOptionVO {

    private String id;
    private String text;
}
