package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 远程会诊按状态计数行（对应 BizTeleConsultMapper.countByStatus）。
 */
@Data
public class TeleConsultStatusCountVO implements Serializable {

    /**
     * 会诊单状态（1-待接诊 2-已安排 3-已完成 4-已取消）
     */
    private Integer status;

    /**
     * 该状态下的会诊单条数
     */
    private Long cnt;
}
