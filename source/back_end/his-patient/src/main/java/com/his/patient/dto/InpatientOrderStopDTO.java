package com.his.patient.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 停止长期医嘱入参。
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
