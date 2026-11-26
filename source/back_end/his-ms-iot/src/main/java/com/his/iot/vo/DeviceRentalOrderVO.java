package com.his.iot.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备共享调配申请单 VO。
 */
@Data
public class DeviceRentalOrderVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String orderNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicantDeptId;
    private String applicantDeptName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicantId;
    private String applicantName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
    private String patientName;
    private String bedNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;
    private String wardName;

    private String purpose;
    private Integer urgency;
    private String urgencyText;
    private LocalDateTime expectReturnTime;
    private String deliveryPlace;

    private Integer status;
    private String statusText;

    private String approveBy;
    private LocalDateTime approveTime;
    private String approveRemark;

    private String issueBy;
    private LocalDateTime issueTime;

    private String returnBy;
    private LocalDateTime returnTime;
    private String returnReason;

    private String acceptBy;
    private LocalDateTime acceptTime;
    private String acceptRemark;

    private String remark;
    private LocalDateTime createTime;

    private List<DeviceRentalItemVO> items;
}
