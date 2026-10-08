package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 我的工单列表项（患者视角：能看到医院处理到哪一步）。
 */
@Data
@Schema(name = "ServiceMessageListVO", description = "患者工单列表项")
public class MiniServiceMessageListVO {

    /**
     * 工单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 工单号
     */
    private String messageNo;

    /**
     * 留言分类
     */
    private String categoryCode;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 工单状态（0-待受理 1-处理中 2-已办结 3-已关闭）
     */
    private Integer status;

    /**
     * 状态文案
     */
    private String statusText;

    /**
     * 受理人姓名（未受理为空 —— 患者就能看出"还没人接"）
     */
    private String acceptByName;

    /**
     * 客服回复次数
     */
    private Integer replyCount;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 患者可执行动作：append 补充 / cancel 撤单 / confirm 确认解决 / reopen 重开
     */
    private java.util.List<String> actions;

    /**
     * 提交时间
     */
    private String createTime;
}
