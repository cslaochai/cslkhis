package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 病历质控写入结果：质控列表页定位这条质控单所需的编号与主键。
 */
@Data
@NoArgsConstructor
public class AiQualityControlWriteVO {

    /**
     * 质控记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 质控单号
     */
    private String qcNo;
}
