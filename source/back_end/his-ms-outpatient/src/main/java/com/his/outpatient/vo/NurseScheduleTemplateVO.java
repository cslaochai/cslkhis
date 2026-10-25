package com.his.outpatient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 护理排班模板出参（内层静态类组织，同 NurseScheduleVO）。
 */
public class NurseScheduleTemplateVO {

    /**
     * 模板（列表/详情通用；items 仅详情返回）
     */
    @Data
    public static class Template {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 模板名称 */
        private String name;
        /** 排班单元类型（1-病区 2-门诊科室） */
        private Integer unitType;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /** 排班单元名称（快照） */
        private String wardName;
        /** 单双周（0-每周 1-单周 2-双周） */
        private Integer weekParity;
        private LocalDate validFrom;
        private LocalDate validUntil;
        /** 共享范围（0-个人 1-权限组共享） */
        private Integer scope;
        /** 状态（0-停用 1-启用） */
        private Integer status;
        private String remark;
        /** 创建人（姓名，BaseEntity.createBy） */
        private String creatorName;
        private LocalDateTime createTime;
        /** 明细行数（列表页展示用，避免每行都拉明细） */
        private Integer itemCount;
        /** 是否已被套用过（存在适用记录） */
        private Boolean applied;
        /** 明细行（仅详情接口返回） */
        private List<Item> items;
    }

    /**
     * 模板明细行
     */
    @Data
    public static class Item {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long templateId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        private String employeeName;
        /** 星期几（1-周一 … 7-周日） */
        private Integer weekDay;
        /** 出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班） */
        private Integer scheduleStatus;
        /** 班次ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long shiftId;
        /** 班次名称（派生） */
        private String shiftName;
        private String remark;
    }

    /**
     * 模板规则（班次实用排班模式）
     */
    @Data
    public static class Rule {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String ruleName;
        private String ruleCode;
        private String patternDetail;
        private Integer status;
        private String remark;
    }

    /**
     * 适用记录（套用历史）
     */
    @Data
    public static class Apply {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long templateId;
        private String templateName;
        private Integer unitType;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        private String wardName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate applyWeekStart;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate applyWeekEnd;
        private String appliedByName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime appliedAt;
        /** 实际生成排班格数 */
        private Integer generatedCount;
        /** 跳过格数 */
        private Integer skippedCount;
        private Integer status;
        private String remark;
    }

    /**
     * 首页四张统计卡（我的模板 / 已用模板 / 已删除模板 / 权限组模板）
     */
    @Data
    public static class Stats {
        /** 我的模板（delFlag=0 且创建人=当前用户） */
        private long myCount;
        /** 已用模板（delFlag=0 且存在适用记录） */
        private long usedCount;
        /** 已删除模板（delFlag=1） */
        private long deletedCount;
        /** 权限组模板（delFlag=0 且 scope=1 共享） */
        private long sharedCount;
    }
}
