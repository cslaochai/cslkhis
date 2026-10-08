package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 急诊交班单（sql/153）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emergency_handover")
public class BizEmergencyHandover extends BaseEntity {

    /**
     * 交班单号（EJ + yyyyMMdd + 4 位序号，服务端生成，唯一键）
     */
    private String handoverNo;

    /**
     * 交班科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 交班科室名称
     */
    private String deptName;

    /**
     * 交出人员工ID（登录态写入，不信前端）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmpId;

    /**
     * 交出人姓名
     */
    private String fromEmpName;

    /**
     * 接班人员工ID（整单默认接续责任人；明细可逐条改指他人）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long takeEmpId;

    /**
     * 接班人姓名（服务端按 ID 补齐的快照）
     */
    private String takeEmpName;

    /**
     * 班次名（交出人当日该科室在岗排班的班次名；查不到为空，不编造）
     */
    private String shiftName;

    /**
     * 本班区间起（不含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodBegin;

    /**
     * 本班区间止（含，= 本次提交时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    /**
     * 本次移交未闭环人数（定格）
     */
    private Integer pendingCount;

    /**
     * 其中交班前无人指派的条数（定格，= 本次清零掉的池子行数）
     */
    private Integer poolCount;

    /**
     * 其中候诊已超时的条数（定格）
     */
    private Integer overdueCount;

    /**
     * 其中留观中的条数（定格）
     */
    private Integer observationCount;

    /**
     * 其中留观已超时限的条数（定格）
     */
    private Integer obsOverLimitCount;

    /**
     * 整单交代备注
     */
    private String remark;
}
