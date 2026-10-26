package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 可呼叫的听班目标下拉项（sql/297）。
 *
 * <p>呼叫方在发起前看到的是「这一班这一科室有几线听班、各是谁」，
 * 避免叫了之后才发现这一班没排二线。
 */
@Data
public class DutyCallTargetVO {

    /** 值班点位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long postId;

    /** 值班点位名称 */
    private String postName;

    /** 被叫层级：2-二线 3-三线 */
    private Integer targetLevel;

    /** 被叫层级文案 */
    private String targetLevelText;

    /** 值班人 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /** 值班人姓名 */
    private String employeeName;

    /** 值班联系电话 */
    private String phone;

    /** 是否可呼叫（已停用/未排班不可叫） */
    private Boolean callable;
}
