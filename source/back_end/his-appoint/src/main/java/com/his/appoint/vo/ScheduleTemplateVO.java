package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 排班模板 VO
 */
@Data
public class ScheduleTemplateVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 排班人员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 排班人员姓名
     */
    private String doctorName;

    /**
     * 排班对象岗位类别（2-护理 3-医技 4-药学 5-收费 6-行政其他）
     */
    private Integer staffType;

    /**
     * 岗位类别名称（派生只读，按 staffType 从 {@code StaffTypeEnum} 带出）
     */
    private String staffTypeName;

    /**
     * 星期几（1-周一 ... 7-周日）
     */
    private Integer weekDay;

    /**
     * 单双周（0-每周 1-单周 2-双周）
     */
    private Integer weekParity;

    /**
     * 生效起始日期（yyyy-MM-dd，空=不限）
     */
    private String validFrom;

    /**
     * 生效截止日期（yyyy-MM-dd，空=不限）
     */
    private String validUntil;

    /**
     * 班别（1-上午 2-下午 3-全天 4-凌晨 5-夜班）：派生只读，按 shiftId 从班次字典带出，表里没有这一列
     */
    private Integer scheduleType;

    /**
     * 班次名称：派生只读，同上；班次被删过则为空
     */
    private String shiftName;

    /**
     * 开始时间（HH:mm）
     */
    private String startTime;

    /**
     * 结束时间（HH:mm）
     */
    private String endTime;

    /**
     * 班次ID（班次字典的ID）：模板唯一的时间段/班别来源
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shiftId;

    /**
     * 号源总数
     */
    private Integer totalSource;

    /**
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 诊疗费
     */
    private BigDecimal diagnosisFee;

    /**
     * 是否专家（0-否 1-是）
     */
    private Integer isExpert;

    /**
     * 专家费
     */
    private BigDecimal expertFee;

    /**
     * 是否开放预约（0-否 1-是）
     */
    private Integer isAppointment;

    /**
     * 预约号源数
     */
    private Integer appointmentSource;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 段级号源配置（医生岗可配；null/空=该模板未按段细化，生成排班时按半小时均分）
     */
    private List<ScheduleTemplateSlotVO> slots;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
