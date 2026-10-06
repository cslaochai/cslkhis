package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.system.enums.MaintainResultEnum;
import com.his.system.enums.MaintainTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 维保登记。
 *
 * <p>日期入参一律宽进：LocalDate 用 yyyy-MM-dd（AGENTS.md 日期格式铁律）。
 */
@Data
public class MaintainCreateDTO implements Serializable {

    @NotNull(message = "设备ID不能为空")
    private Long equipmentId;

    /**
     * 维保类型（1-保养 2-维修 3-巡检）
     */
    @NotNull(message = "维保类型不能为空")
    @InEnum(value = MaintainTypeEnum.class, message = "维保类型取值不合法（1-保养 2-维修 3-巡检）")
    private Integer maintainType;

    @NotNull(message = "维保日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maintainDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextMaintainDate;

    private BigDecimal cost;

    /**
     * 故障描述（维修类型时建议填）
     */
    private String faultDesc;

    private String handleResult;

    /**
     * 维保结果（1-正常 2-异常）；不填默认按「正常」落库
     */
    @InEnum(value = MaintainResultEnum.class, message = "维保结果取值不合法（1-正常 2-异常）")
    private Integer maintainResult;

    /**
     * 维保人（不填取当前登录人）
     */
    private String handlerName;
}
