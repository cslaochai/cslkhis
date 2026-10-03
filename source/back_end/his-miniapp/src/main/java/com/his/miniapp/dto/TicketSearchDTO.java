package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 院内工单列表检索条件。
 *
 * <p><b>客服首屏要的是「待受理」</b>：默认不筛状态时按待受理优先排序
 * （见 Impl 的 order by），不要让客服在一堆已办结里翻今天的新单。
 */
@Data
@Schema(description = "院内工单检索条件")
public class TicketSearchDTO {

    @Schema(description = "页码，从 1 开始")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 20;

    @Schema(description = "工单状态：0-待受理 1-处理中 2-已办结 3-已关闭，为空查全部")
    private Integer status;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "关键词：工单号 / 内容 / 就诊人姓名 / 联系电话")
    private String keyword;

    @Schema(description = "只看我受理的（当前登录人）")
    private Boolean onlyMine;
}
