package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 互联网线上问诊按状态计数行（对应 {@code BizOnlineConsultMapper.countByStatus}）。
 *
 * <p>与 {@code TeleConsultStatusCountVO} 形状相同但**不能共用一个类**：
 * 两条线的状态枚举含义不同（待接诊 vs 待接诊/已接诊/已完成/已拒绝），
 * 共用一类等于把两套码值混进同一个类型里，编译器拦不住"传错表的那一行"。
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
