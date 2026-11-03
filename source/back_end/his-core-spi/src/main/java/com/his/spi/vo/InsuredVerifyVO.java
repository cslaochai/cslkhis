package com.his.spi.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 参保核验结果（登记环节用：只查人在不在，不算费用）。
 */
@Data
public class InsuredVerifyVO implements Serializable {

    /**
     * 参保库是否查到此卡号
     */
    private boolean found;

    /**
     * 参保状态是否正常
     */
    private boolean active;

    /**
     * 险种（城镇职工医保/城乡居民医保/公费医疗）
     */
    private String insuranceType;

    /**
     * 参保人姓名
     */
    private String name;

    /**
     * 异常说明（未参保/停保/身份不符/平台不可用）
     */
    private String errMsg;
}
