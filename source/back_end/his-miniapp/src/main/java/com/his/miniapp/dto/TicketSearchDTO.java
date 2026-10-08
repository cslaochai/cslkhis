package com.his.miniapp.dto;

import com.his.common.base.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 院内工单列表检索条件。
 */
@Data
@Schema(description = "院内工单检索条件")
public class TicketSearchDTO extends PageParam {

    @Schema(description = "工单状态：0-待受理 1-处理中 2-已办结 3-已关闭，为空查全部")
    private Integer status;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "关键词：工单号 / 内容 / 就诊人姓名 / 联系电话")
    private String keyword;

    @Schema(description = "只看我受理的（当前登录人）")
    private Boolean onlyMine;
}
