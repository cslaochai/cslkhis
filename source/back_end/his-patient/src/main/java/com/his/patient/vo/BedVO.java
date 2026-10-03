package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/** 床位 VO（床位图 / 选床用） */
@Data
public class BedVO {

    /** 床位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /** 床位号 */
    private String bedNo;

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 病区名称 */
    private String wardName;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** normal-普通 ICU-重症 VIP-特需 */
    private String bedType;

    /** 床位状态（0-维修 1-空闲 2-占用 3-锁定） */
    private Integer bedStatus;

    /** 当前占用患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 占用时的患者姓名 */
    private String patientName;

    /** 备注 */
    private String remark;

    /** 状态文案（空闲/占用/维修/锁定），由后端统一给 */
    private String bedStatusText;
}
