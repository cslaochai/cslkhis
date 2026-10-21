package com.his.platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 维保登记。
 */
@Data
public class MaintainCreateDTO implements Serializable {

    /** 设备ID */
    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 维保类型（1-保养 2-维修 3-巡检）
     */
    @NotNull(message = "维保类型不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "维保类型取值不合法")
    private Integer maintainType;

    /** 维保日期 */
    @NotNull(message = "维保日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maintainDate;

    /** 下次维保日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextMaintainDate;

    /** 费用（元） */
    private BigDecimal cost;

    /**
     * 故障描述（维修类型时建议填）
     */
    private String faultDesc;

    private String handleResult;

    /**
     * 维保结果（1-正常 2-异常）；不填默认按「正常」落库
     */
    @AllowedValues(values = {"1", "2"}, message = "维保结果取值不合法")
    private Integer maintainResult;

    /**
     * 维保人（不填取当前登录人）
     */
    private String handlerName;
}
