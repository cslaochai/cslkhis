package com.his.miniapp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 工单流转记录（患者端时间轴的一格）。
 *
 * <p>只返回 {@code visibleToPatient=1} 的记录给患者 —— 内部备注在 Service 侧过滤掉，
 * 不是靠前端不渲染（前端漏判一次就把客服的内部评价推给患者了）。
 */
@Data
@Schema(name = "ServiceTicketLogVO", description = "工单流转记录")
public class ServiceTicketLogVO {

    /** 动作：0-提交 1-受理 2-客服回复 3-办结 4-患者补充 5-关闭 6-患者撤单 7-患者重开 */
    private Integer action;

    /** 动作文案 */
    private String actionText;

    /** 内容 */
    private String content;

    /** 操作人类型（1-患者 2-院内） */
    private Integer operatorType;

    /** 操作人展示名（患者显示"我"，院内显示客服姓名） */
    private String operatorName;

    /** 患者是否可见（0-内部备注 1-患者可见） */
    private Integer visibleToPatient;

    /** 时间 */
    private String createTime;
}
