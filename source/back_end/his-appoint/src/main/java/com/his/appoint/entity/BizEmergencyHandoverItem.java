package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 急诊交班明细（sql/153）——「逐条点名」这一事实的载体。
 *
 * <p>一行 = 一次"这个人现在归你"。患者姓名、负责医生、候诊时长、留观小时数、超时档位
 * 全部是<b>交班时刻的快照</b>：事后病历怎么变都不影响"当时交出去的是什么"。
 *
 * <p>{@code fromDoctorId} 为 NULL 是这张表最有价值的一行：它说明这条在交班前
 * 谁都不负责（在待派单池里），是"上一班没人管"的直接证据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emergency_handover_item")
public class BizEmergencyHandoverItem extends BaseEntity {

    /**
     * 交班单ID（急诊交班单的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handoverId;

    /**
     * 急诊记录ID（急诊记录的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long emergencyId;

    /**
     * 急诊号（快照）
     */
    private String emergencyNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 分诊级别（快照 1-4）
     */
    private Integer triageLevel;

    /**
     * 交班时该患者的急诊状态（1-候诊 2-诊治中 3-留观，定格）
     */
    private Integer emergencyStatus;

    /**
     * 交班时的负责医生ID（NULL = 交班前在待派单池里无人负责）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDoctorId;

    /**
     * 交班时的负责医生姓名（快照）
     */
    private String fromDoctorName;

    /**
     * 接续责任人（这条现在归谁）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long takeDoctorId;

    /**
     * 接续责任人姓名（快照）
     */
    private String takeDoctorName;

    /**
     * 去向/处置交代（如"继续留观，明晨复查头颅CT"）
     */
    private String disposition;

    /**
     * 逐条补充交代（过敏史/管路/家属联系方式等，截到 300）
     */
    private String handoverNote;

    /**
     * 候诊已等多久（定格；非候诊态为空）
     */
    private Long waitMinutes;

    /**
     * 已留观小时数（定格；未留观为空）
     */
    private Long obsHours;

    /**
     * 超时档位定格：0-未超时 1-超时 2-严重超时
     */
    private Integer overdueLevel;
}
