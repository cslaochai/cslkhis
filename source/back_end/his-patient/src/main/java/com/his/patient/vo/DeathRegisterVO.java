package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 死亡登记出参集合（登记簿是「人死了之后院内怎么处理的」唯一事实来源）。
 */
public class DeathRegisterVO {

    /**
     * 台账行：不带办理人电话（列表不渲染，明文只在编辑回显 Detail）
     */
    @Data
    public static class Row {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /**
         * 死亡登记号
         */
        private String registerNo;

        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

        /**
         * 死亡证明ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long certId;

        private String certNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 死者姓名（快照）
         */
        private String patientName;

        /**
         * 死亡时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deathTime;

        /**
         * 死亡科室名称（快照）
         */
        private String deathDeptName;

        /**
         * 死亡床位号（快照）
         */
        private String deathBedNo;

        /**
         * 死亡类型（1-疾病死亡 2-非疾病死亡）
         */
        private Integer deathType;

        /**
         * 是否已报公安/司法（0-否 1-是）
         */
        private Integer policeFlag;

        /**
         * 受理公安机关
         */
        private String policeOrg;

        /**
         * 公安受理/案件编号
         */
        private String policeCaseNo;

        /**
         * 报案时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime policeReportTime;

        /**
         * 是否由法医出具/检验（0-否 1-是）
         */
        private Integer forensicFlag;

        /**
         * 尸体处理方式（1-殡仪馆接运 2-家属自行处理 3-病理解剖 4-其他）
         */
        private Integer bodyDisposal;

        /**
         * 遗体接运/接收单位
         */
        private String bodyUnit;

        /**
         * 遗体移出时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime bodyTransportTime;

        /**
         * 办理人/近亲属姓名
         */
        private String relativeName;

        /**
         * 与死者关系
         */
        private String relativeRelation;

        /**
         * 家属已领取联次（1-记录联 2-户籍联 3-殡葬联 4-家属联）
         */
        private String receivedCopies;

        /**
         * 领取时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime receiveTime;

        /**
         * 是否存在医疗纠纷/患方异议（0-否 1-是）
         */
        private Integer disputeFlag;

        /**
         * 纠纷/异议情况
         */
        private String disputeDesc;

        /**
         * 状态（1-草稿 2-已登记 3-已作废）
         */
        private Integer registerStatus;

        /**
         * 登记人姓名
         */
        private String registrarName;

        /**
         * 登记（确认）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime registerTime;

        /**
         * 作废原因
         */
        private String voidReason;

        /**
         * 作废时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime voidTime;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人
         */
        private String createBy;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /**
     * 详情 = 编辑回显（电话明文，整对象回写 upsert）
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Detail extends Row {
        /**
         * 联系电话
         */
        private String relativePhone;

        /**
         * 登记人ID（值班医师/病区护士/防保科）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long registrarId;

        /**
         * 死亡科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deathDeptId;

        /**
         * 关联证明是否已开具（登记可以先于签发）
         */
        private Integer certStatus;

        /**
         * 该次住院的死亡出院时间（登记死因链与出院对齐的参照）
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dischargeTime;
    }

    /**
     * 登记底稿（服务端按住院重查，不信前端传来的死者信息）：
     * 姓名/死亡时间/科室床位来自「死亡出院 + 病案首页留档」，有有效证明时一并带出证明摘要。
     */
    @Data
    public static class Base {
        /**
         * 住院记录ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        private String admissionNo;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 死者姓名（快照）
         */
        private String patientName;

        /**
         * 死亡时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deathTime;

        /**
         * 死亡科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deathDeptId;

        /**
         * 死亡科室名称（快照）
         */
        private String deathDeptName;

        /**
         * 死亡床位号（快照）
         */
        private String deathBedNo;

        private Boolean deathDischarged;

        /**
         * 死亡证明ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long certId;

        private String certNo;

        private Integer certStatus;

        private String clinicalDiagnosis;

        private String underlyingIcdCode;

        private String underlyingIcdName;
    }
}
