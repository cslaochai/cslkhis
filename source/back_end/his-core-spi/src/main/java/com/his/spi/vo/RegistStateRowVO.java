package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 挂号状态行：跨域推进就诊流程时要读、要改的那几列。
 */
@Data
public class RegistStateRowVO {

    /**
     * 挂号ID（挂号记录主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 挂号状态（1-已受理 2-已完成 3-已退号 等，见宿主枚举）
     */
    private Integer registStatus;

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
     * 接诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊医生姓名
     */
    private String doctorName;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;
}
