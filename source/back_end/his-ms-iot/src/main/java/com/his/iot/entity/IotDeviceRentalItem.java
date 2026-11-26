package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备共享调配申请明细：一行代表一台设备。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_device_rental_item")
public class IotDeviceRentalItem extends BaseEntity {

    private Long orderId;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String requestedBrand;
    private String requestedModel;

    /** 发放时落具体设备编号；IoT 泵为 PUMP-xx，普通设备为资产编码 */
    private String deviceNo;
    private String deviceType;

    /** 见 DeviceRentalItemStatusEnum */
    private Integer status;

    private LocalDateTime issueTime;
    private LocalDateTime returnTime;
    private LocalDateTime acceptTime;

    /** IoT 设备—患者绑定ID；普通设备为空 */
    private Long bindId;

    private String remark;

    @TableLogic
    private Integer delFlag;
}
