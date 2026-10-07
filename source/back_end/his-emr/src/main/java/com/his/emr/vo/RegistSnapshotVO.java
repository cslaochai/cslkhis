package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 门诊挂号单快照（治疗开单时取患者/科室/医生，作为收费明细的来源依据）。
 *
 * <p>用原生 SQL 而不是 his-appoint 的 Mapper：治疗站在 his-emr，跨模块只读一张表，
 * 引对方实体反而把依赖方向搞乱。
 *
 * <p>科室ID 是**开单科室**：挂号挂在哪科，这笔治疗费就归哪科。
 */
@Data
public class RegistSnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    private String registNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private String patientNo;

    private String patientName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    private String doctorName;

    /**
     * 挂号状态（1-正常 5-已取消等）
     */
    private Integer registStatus;

    /**
     * 退号时间（非空即已退号）
     */
    private LocalDateTime refundTime;
}