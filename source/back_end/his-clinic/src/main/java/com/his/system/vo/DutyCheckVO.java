package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 全院总值班区间规则校验结果（连续值班 / 值班频次 / 主班漏排）。
 * 只列明细不阻断保存。
 */
@Data
public class DutyCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 开始日期 */
    private String startDate;
    /** 结束日期 */
    private String endDate;
    /** 值班点位ID（空=全部） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long postId;
    /** 告警条数（level=1） */
    private Integer alertCount;
    /** 提示条数（level=2） */
    private Integer hintCount;
    /** 校验明细 */
    private List<Warning> warnings;

    /**
     * 单条值班规则告警
     */
    @Data
    public static class Warning implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 1-告警（连续值班/频次超限，必须处置）2-提示（如主班漏排）
         */
        private Integer level;
        /**
         * DUTY_STREAK / DUTY_FREQ / MAIN_GAP
         */
        private String type;
        /** 规则类型文案 */
        private String typeText;
        /** 值班日期 */
        private String dutyDate;
        /** 班次（1-白班 2-夜班） */
        private Integer shiftType;
        /** 班次文案 */
        private String shiftTypeText;
        /** 值班点位ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long postId;
        /** 值班点位名称 */
        private String postName;
        /** 值班人ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /** 值班人姓名 */
        private String employeeName;
        /** 实际值（值班次数/天数等） */
        private Integer actual;
        /** 上限值 */
        private Integer required;
        /** 消息内容 */
        private String message;
    }
}
