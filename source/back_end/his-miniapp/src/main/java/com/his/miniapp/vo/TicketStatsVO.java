package com.his.miniapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工单工作台统计（客服首屏那几个数字）。
 *
 * <p><b>{@code waitAccept} 是这个页面存在的理由</b>：
 * 客服进来第一件事是看有没有新单没人接，不是看历史工单总数。
 */
@Data
@Schema(name = "TicketStatsVO", description = "工单工作台统计")
public class TicketStatsVO {

    @Schema(description = "待受理")
    private Integer waitAccept;

    @Schema(description = "处理中")
    private Integer handling;

    @Schema(description = "已办结（等患者确认）")
    private Integer finished;

    @Schema(description = "已关闭")
    private Integer closed;

    /** 超过 24 小时仍未受理 —— 这是唯一会被追责的数字 */
    @Schema(description = "超时未受理（超过 24 小时）")
    private Integer overdueWaitAccept;
}
