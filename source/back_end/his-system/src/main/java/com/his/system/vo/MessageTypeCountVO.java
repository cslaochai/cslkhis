package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 按业务类型分组的消息计数（抽屉 Tab / 分组徽标用）。
 */
@Data
public class MessageTypeCountVO implements Serializable {

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 该类型消息总数
     */
    private Long total;

    /**
     * 该类型未读数
     */
    private Long unread;
}
