package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ICU 入出科登记（ICU 入出科登记）。
 *
 * <p>一次住院同时只允许一条在科记录，一张 ICU 床同时只允许一名在科患者（服务层校验）。
 * 患者、来源科室、病区床位全部快照，出科后不改写。
 *
 * <p>本表不反向改写床位的占用状态：ICU 床位归属以入科记录为准，
 * 避免与「转科/换床」两套账互相覆盖（见 sql/108 设计要点 3）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_icu_stay")
public class BizIcuStay extends BaseEntity implements Serializable {

    /**
     * 入科单号（ICU + yyyyMMdd + 4位流水号）
     */
    private String stayNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 入科来源科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;

    /**
     * 入科来源科室名称
     */
    private String fromDeptName;

    /**
     * ICU 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * ICU 病区名称
     */
    private String wardName;

    /**
     * ICU 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * ICU 床位号
     */
    private String bedNo;

    /**
     * 监护等级（1-特级 2-I级 3-II级）
     */
    private Integer careLevel;

    /**
     * 入科时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inTime;

    /**
     * 入科诊断/原因
     */
    private String inDiag;

    /**
     * 入科 GCS（3~15）
     */
    private Integer inGcs;

    /**
     * 入科登记人
     */
    private String inBy;

    /**
     * 状态（1-在科 2-已出科）
     */
    private Integer status;

    /**
     * 出科时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime;

    /**
     * 转出去向：1-普通病房 2-专科病房 3-手术室 4-转院 5-死亡 6-自动离院
     */
    private Integer outDest;

    /**
     * 出科情况/转归说明
     */
    private String outReason;

    /**
     * 出科 GCS 评分
     */
    private Integer outGcs;

    /**
     * 出科登记人
     */
    private String outBy;

    /**
     * 监护记录条数（冗余派生值）
     */
    private Integer monitorCount;
}
