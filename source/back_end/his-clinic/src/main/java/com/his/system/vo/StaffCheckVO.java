package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 员工排班区间规则校验结果（全区岗位通用）。
 * 只列告警明细不阻断保存：排班员自查「这个班有没有排得太狠 / 人力够不够」。
 */
@Data
public class StaffCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 开始日期 */
    private String startDate;
    /** 结束日期 */
    private String endDate;
    /** 排班单元类型（空=全部） */
    private Integer orgType;
    /** 排班单元ID（空=全部） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;
    /** 排班单元名称（orgId 为空时为空） */
    private String orgName;
    /** 岗位类别（空=全部） */
    private Integer staffType;
    /** 告警条数（level=1） */
    private Integer alertCount;
    /** 提示条数（level=2） */
    private Integer hintCount;
    /** 校验明细 */
    private List<Warning> warnings;

    /**
     * 单条规则告警
     */
    @Data
    public static class Warning implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 1-告警（违反劳动安全/人力标准，必须处置）2-提示（如漏排空缺）
         */
        private Integer level;
        /**
         * STAFF_GAP / STAFF_OVER / WEEK_HOURS / NIGHT_STREAK / WORK_STREAK / SHIFT_ON_NON_WORK / NOT_SCHEDULED
         */
        private String type;
        /** 规则类型文案 */
        private String typeText;
        /** 排班日期 */
        private String scheduleDate;
        /** 班次ID（0-无班次） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long shiftId;
        /** 班次名称 */
        private String shiftName;
        /** 员工ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /** 员工姓名 */
        private String employeeName;
        /** 岗位类别 */
        private Integer staffType;
        /** 岗位类别文案 */
        private String staffTypeName;
        /** 实际人数/实际工时（规则相关） */
        private Integer actual;
        /** 标准人数/上限（规则相关） */
        private Integer required;
        /** 消息内容 */
        private String message;
    }
}
