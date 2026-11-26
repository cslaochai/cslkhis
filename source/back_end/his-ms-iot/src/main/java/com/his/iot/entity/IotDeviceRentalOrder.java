package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备共享调配申请单。
 * <p>
 * 这是业务单据，不等于设备—患者绑定关系：申请/审批/发放/归还/验收都落在这张单，
 * {@code biz_iot_device_bind} 只是发放时 IoT 设备产生的底层绑定记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_device_rental_order")
public class IotDeviceRentalOrder extends BaseEntity {

    private String orderNo;

    private Long applicantDeptId;
    private String applicantDeptName;
    private Long applicantId;
    private String applicantName;

    private Long patientId;
    private Long admissionId;
    private String patientName;
    private String bedNo;
    private Long wardId;
    private String wardName;

    private String purpose;
    private Integer urgency;
    private LocalDateTime expectReturnTime;
    private String deliveryPlace;

    /** 见 DeviceRentalOrderStatusEnum */
    private Integer status;

    private String approveBy;
    private Long approveById;
    private LocalDateTime approveTime;
    private String approveRemark;

    private String issueBy;
    private Long issueById;
    private LocalDateTime issueTime;

    private String returnBy;
    private Long returnById;
    private LocalDateTime returnTime;
    private String returnReason;

    private String acceptBy;
    private Long acceptById;
    private LocalDateTime acceptTime;
    private String acceptRemark;

    private String remark;

    @TableLogic
    private Integer delFlag;
}
