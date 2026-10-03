package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VTE 事件登记（DVT / 肺栓塞 / 预防相关出血）。
 *
 * <p><b>onset_type 是这张表的灵魂</b>：入院时已存在的 DVT（onset_type=2）是"带入"不是"院内获得"，
 * 混进分子会把院内 VTE 发生率虚高 —— 评审问"你们院内 VTE 发生率多少"，
 * 把带入病例算进去的答案经不起复核。
 */
@Data
@TableName("biz_vte_event")
public class BizVteEvent {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 事件编号 VE+yyyyMMdd+4位 */
    private String eventNo;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 科室ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称（快照） */
    private String deptName;

    /** 病区ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 病区名称（快照） */
    private String wardName;

    /** 事件类型（1-深静脉血栓DVT 2-肺栓塞PE 3-预防相关出血） */
    private Integer eventType;

    /** 发生时机（1-院内发生 2-入院时已存在） */
    private Integer onsetType;

    /** 确诊日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnoseDate;

    /** 诊断依据（1-超声 2-CT肺动脉造影 3-静脉造影 4-临床诊断 5-其他） */
    private Integer diagnosisBasis;

    /** 血栓部位 */
    private String thrombusSite;

    /** 转归（1-好转 2-未愈 3-死亡 4-未知） */
    private Integer outcome;

    /** 事件发生时是否正在药物预防（分析"预防下突破"） */
    private Integer drugPreventFlag;

    /** 登记人（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reporterId;

    /** 登记人姓名 */
    private String reporterName;

    /** 登记时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新人 */
    private String updateBy;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 删除标志（0-正常 1-删除） */
    private Integer delFlag;
    /** 备注 */
    private String remark;
}
