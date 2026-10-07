package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 按状态分组的检查预约单数（{@code BizExamAppointmentMapper#countGroupByStatus} 一行）。
 *
 * <p>deviceId / slotDate 传 null 表示该维度不限（用 {@code jdbcType} 显式声明，
 * 否则 MyBatis 会把null 当成字面量拼进 SQL）。
 */
@Data
public class ExamApptStatusCountRowVO implements Serializable {

    /**
     * 预约单状态（1-已预约 2-已到检 3-已完成 4-已取消 5-已爽约）
     */
    private Integer status;

    /**
     * 该状态单数
     */
    private Long cnt;
}