package com.his.pharmacy.vo;

import lombok.Data;

import java.io.Serializable;

/** 麻精登记计数：麻醉/精神药品登记列表统计卡上的单据数与安瓿回收计数。 */
@Data
public class NarcoticRegisterCountVO implements Serializable {

    /** 登记单据总数（当前科室数据范围内） */
    private Long total;

    /** 安瓿待回收单据数（安瓿回收状态 1-待回收） */
    private Long pendingAmpoule;

    /** 安瓿已回收单据数（安瓿回收状态 2-已回收） */
    private Long returnedAmpoule;
}
