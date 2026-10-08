package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交班入参：把本班的遗留事项交给下一班。
 */
@Data
public class DutyLogHandoverDTO {

    @NotNull(message = "日志ID不能为空")
    private Long id;

    /**
     * 接班人（留空 = 下一班总值班）
     */
    private Long handoverEmpId;

    /**
     * 交班说明（留给接班人的话；不是必填，但交接时说清背景能省对方半小时）
     */
    private String handleResult;
}
