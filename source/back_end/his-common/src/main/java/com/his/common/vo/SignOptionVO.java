package com.his.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉选项（id 出参由 {@code ToStringSerializer} 字符串化，雪花 ID 不给前端留 Number 精度坑）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignOptionVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String text;
}
