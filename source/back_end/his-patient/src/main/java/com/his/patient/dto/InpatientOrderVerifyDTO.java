package com.his.patient.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 护士医嘱校对入参（支持批量）。
 *
 * <p>校对是"医嘱双人核对"的最低要求：**未校对的医嘱不进执行队列**，
 * 所以这个动作是医嘱从"医生意图"变成"护理指令"的分界点，校对人与时间必须留痕。
 */
@Data
public class InpatientOrderVerifyDTO implements Serializable {

    /**
     * 医嘱ID列表（≥1 条）
     */
    private List<Long> orderIds;
}
