package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 手术安全核查单出参（一时段一行）。
 *
 * <p>约定与手术/麻醉链其他 VO 一致：ID 字符串化、码值带后端文案；
 * 某一阶段是否可签（canSignPhase）由手术状态与时段顺序共同决定，规则在服务端。
 */
@Data
public class SafetyCheckVO implements Serializable {

    /**
     * 核查记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 核查单号
     */
    private String checkNo;

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 手术申请单号（快照）
     */
    private String applyNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 手术名称（快照，拟施）
     */
    private String operationName;

    /**
     * 手术间（快照）
     */
    private String operationRoom;

    /**
     * 核查时段码
     */
    private Integer phase;

    /**
     * 核查时段文案
     */
    private String phaseText;

    /**
     * 核查项码值（逗号分隔原始值）
     */
    private String items;

    /**
     * 核查项完整文案（分号拼接）
     */
    private String itemsText;

    /**
     * 异常说明
     */
    private String note;

    /**
     * 手术医师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long surgeonId;

    /**
     * 手术医师姓名（快照）
     */
    private String surgeonName;

    /**
     * 麻醉医师员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anesthetistId;

    /**
     * 麻醉医师姓名（快照）
     */
    private String anesthetistName;

    /**
     * 手术室护士（器械/巡回）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 手术室护士姓名（快照）
     */
    private String nurseName;

    /**
     * 录入人姓名（快照）
     */
    private String recorderName;

    /**
     * 核查完成时间（原文本，避免时区/格式歧义）
     */
    private String checkTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 某一阶段是否可签（applyId + phase → 可签/不可签及原因），供前端渲染三张核查卡。
     */
    @Data
    public static class PhaseCard implements Serializable {

        /**
         * 时段码 1~3
         */
        private Integer phase;

        /**
         * 时段文案
         */
        private String phaseText;

        /**
         * 该时段核查项（含 required 标识）
         */
        private List<CheckItem> items;

        /**
         * 已签的核查行；null = 尚未签
         */
        private SafetyCheckVO signed;

        /**
         * 该时段是否可签（服务端按状态机与时段顺序判定）
         */
        private Boolean canSign;

        /**
         * 不可签原因（可签时为 null）
         */
        private String cannotSignReason;
    }

    /**
     * 核查项（与 OperationApplyVO.CheckItem 同形，但带时段上下文）
     */
    @Data
    public static class CheckItem implements Serializable {

        /**
         * 核查项码值
         */
        private Integer code;

        /**
         * 核查项文案
         */
        private String label;

        /**
         * 是否必核项
         */
        private Boolean required;
    }
}
