package com.his.patient.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 停止长期医嘱入参。
 *
 * <p>只允许"停止"，**不允许作废**：已执行的次数是既成事实，回退不掉，停止只影响后续。
 * 若该医嘱属于某个组套，同组医嘱会**整组一起停**（同起同停）。
 */
@Data
public class InpatientOrderStopDTO implements Serializable {

    /**
     * 医嘱ID（必填）
     */
    private Long orderId;

    /**
     * 停止原因（必填：停止是个医疗决定，必须有人负责）
     */
    private String stopReason;
}
