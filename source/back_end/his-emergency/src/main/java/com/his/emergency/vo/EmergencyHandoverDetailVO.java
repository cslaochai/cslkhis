package com.his.emergency.vo;

import lombok.Data;

import java.util.List;

/**
 * 一张交班单的完整凭证（抬头 + 逐条明细）
 */
@Data
public class EmergencyHandoverDetailVO {

    /**
     * 交班单抬头（含五个定格计数）
     */
    private EmergencyHandoverVO handover;

    /**
     * 逐条点名明细（按提交顺序）
     */
    private List<EmergencyHandoverItemVO> items;
}
