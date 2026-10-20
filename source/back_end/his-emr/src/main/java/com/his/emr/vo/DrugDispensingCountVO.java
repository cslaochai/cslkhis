package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/** 发药状态计数：药房发药窗口统计卡上待发药/已发药/已退药的单据数。 */
@Data
public class DrugDispensingCountVO implements Serializable {

    /** 待发药单据数（发药状态 1-待发药） */
    private Long pending;

    /** 已发药单据数（发药状态 2-已发药） */
    private Long dispensed;

    /** 已退药单据数（发药状态 3-已退药） */
    private Long returned;
}
