package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 住院医嘱校对完成通知载荷（对应 {@code InpatientOrderServiceImpl} 的校对提醒）。
 *
 * <p>按「开单医生 + 本次住院」分组发一条，所以 {@code orderNo} 只是一组里的首条医嘱号，
 * {@code count} 才是这组实际条数 —— 前端摘要 chips 读的就是这两个键。
 */
@Data
public class InpatientOrderVerifyNotifyPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 床号（在院患者为空）
     */
    private String bedNo;

    /**
     * 该组首条医嘱号
     */
    private String orderNo;

    /**
     * 本组医嘱条数
     */
    private Integer count;

    /**
     * 校对护士姓名
     */
    private String verifyNurse;
}
