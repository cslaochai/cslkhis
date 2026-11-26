package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备—患者绑定关系（biz_iot_device_bind）。
 * <p>
 * 真实场景：患者入院后需要输液 → 护士推泵到床边 → 扫泵身条码绑到这位在院患者（admission_id），
 * 泵跟着患者走，所以绑定对象是「住院就诊」而不是「床位」；转床不用重绑，床位只是绑定当时的快照。
 * 换泵/治疗结束/出院时解绑，行保留（bind_status=2-已解绑），便于回溯某台泵某段时间挂在谁身上。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_iot_device_bind")
public class IotDeviceBind extends BaseEntity {

    /**
     * 设备编号（扫到的那台泵）
     */
    private String deviceNo;

    /**
     * 设备类型（infusion-输液泵等，取自设备档案，避免绑定表与档案口径漂移）
     */
    private String deviceType;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 住院就诊ID（绑定对象）
     */
    private Long admissionId;

    /**
     * 绑定时的床位ID
     */
    private Long bedId;

    /**
     * 绑定时的床位号快照
     */
    private String bedNo;

    /**
     * 绑定时的病区ID
     */
    private Long wardId;

    /**
     * 绑定时的科室ID
     */
    private Long deptId;

    /**
     * 绑定方式（1-扫码 2-手工），见 IotBindSourceEnum
     */
    private Integer bindSource;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 绑定人
     */
    private String bindBy;

    /**
     * 绑定人ID
     */
    private Long bindById;

    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;

    /**
     * 解绑人
     */
    private String unbindBy;

    /**
     * 解绑人ID
     */
    private Long unbindById;

    /**
     * 解绑原因（换泵/治疗结束/出院/设备回收）
     */
    private String unbindReason;

    /**
     * 绑定状态（1-在用 2-已解绑），见 IotBindStatusEnum
     */
    private Integer bindStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;
}
