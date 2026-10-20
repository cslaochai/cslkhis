package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 临床规则核查写入结果：医生工作站校验页定位这条记录所需的编号与主键。
 */
@Data
@NoArgsConstructor
public class AiRuleCheckWriteVO {

    /**
     * 核查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 核查单号
     */
    private String checkNo;
}
