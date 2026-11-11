package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 患者端-体检单项结果。
 */
@Data
public class PatientCheckupItemVO {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 参考范围
     */
    private String refStandard;

    /**
     * 结果值
     */
    private String resultValue;

    /**
     * 异常标志码值（0-正常 1-异常 2-待查）
     */
    private Integer abnormalFlag;

    /**
     * 异常标志文案
     */
    private String abnormalText;

    /**
     * 单项小结/建议
     */
    private String summaryText;
}
