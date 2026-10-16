package com.his.miniapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工单工作台统计
 */
@Data
@Schema(name = "TicketStatsVO", description = "工单工作台统计")
public class MiniTicketStatsVO {

    @Schema(description = "待受理")
    private Integer waitAccept;

    @Schema(description = "处理中")
    private Integer handling;

    @Schema(description = "已办结（等患者确认）")
    private Integer finished;

    @Schema(description = "已关闭")
    private Integer closed;

    /**
     * 超过 24 小时仍未受理 —— 这是唯一会被追责的数字
     */
    @Schema(description = "超时未受理（超过 24 小时）")
    private Integer overdueWaitAccept;
}
