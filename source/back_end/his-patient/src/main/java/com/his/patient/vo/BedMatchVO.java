package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 床位匹配候选
 */
@Data
public class BedMatchVO {

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
     * 匹配档位：1-本科室同床型 2-本科室可降级 3-跨科同床型 4-跨科可降级
     */
    private Integer matchLevel;

    private String matchLevelText;

    /**
     * 综合得分（越高越适合，仅用于排序）
     */
    private Integer matchScore;

    /**
     * 一句话说明为什么推荐这张床
     */
    private String matchReason;

    /**
     * 与期望病区一致
     */
    private Boolean expectWardMatched;

    /**
     * 患者性别限制在此床上需要人工确认
     */
    private Boolean genderHint;

    /**
     * 隔离需求在此床上需要人工确认
     */
    private Boolean isolationHint;
}
