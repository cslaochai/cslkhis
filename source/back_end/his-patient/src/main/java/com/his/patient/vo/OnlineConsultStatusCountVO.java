package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 互联网线上问诊按状态计数行（对应 BizOnlineConsultMapper.countByStatus）。
 */
@Data
public class OnlineConsultStatusCountVO implements Serializable {

    /**
     * 问诊单状态（1-待接诊 2-已接诊 3-已完成 4-已拒绝）
     */
    private Integer status;

    /**
     * 该状态下的问诊单条数
     */
    private Long cnt;
}
