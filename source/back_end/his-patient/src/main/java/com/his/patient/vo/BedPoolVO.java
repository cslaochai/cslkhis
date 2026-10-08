package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 全院床位池（一行一床）
 *
 * <p><b>{@code reservedPatientName / useDeptName} 是"这张床被谁预定了"</b>：
 * 跨科调配把 A 科的床借给 B 科患者之后，这张床在 A 科眼里必须是"已预留"，
 * 否则两个科室会把同一张空床安排给两个不同的人。
 */
@Data
public class BedPoolVO {

    /**
     * 本次筛选命中的床位数
     */
    private long total;

    /**
     * 一床一行
     */
    private List<BedRow> rows;

    @Data
    public static class BedRow {

        /**
         * 床位ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long bedId;

        /**
         * 床位号
         */
        private String bedNo;

        private String bedType;

        private String bedTypeText;

        private Integer bedStatus;

        private String bedStatusText;

        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;

        /**
         * 病区名称
         */
        private String wardName;

        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 占用者（bed_status=2 时有值）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        /**
         * 患者ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 入院时间
         */
        private String admitTime;

        /**
         * 预留去向（bed_status=3 时有值）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long allocateId;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long waitId;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long useDeptId;

        private String useDeptName;

        private String reservedPatientName;

        private String reservedTime;

        /**
         * 备注
         */
        private String remark;
    }
}
