package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物联网设备档案（biz_iot_device）。
 * <p>
 * 这是设备科台账：设备编号/类型/位置/启停，<b>不承载"现在挂在谁身上"</b>——绑定关系在
 * {@link IotDeviceBind}（biz_iot_device_bind）。档案表的 bind_type/bind_id 是历史遗留列，新代码不读不写。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_iot_device")
public class IotDevice extends BaseEntity {

    /**
     * 设备编号（MQTT 主题里的设备标识，扫码扫到的就是它）
     */
    private String deviceNo;

    /**
     * 设备类型：coldchain/infusion/asset/env/gas/bed/dialysis/cabinet/uv/robot
     */
    private String deviceType;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 归属科室名称（台账登记用，不随患者转科变化）
     */
    private String deptName;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 固定安装床位号（床垫/在床监测类设备专用，其他类型为空）。
     * 床垫跟床走、不跟患者走：患者转床由 ADT 换床，设备绑定关系不动。
     */
    private String bedNo;

    /**
     * 资产标签绑定的设备台账ID（仅 asset 类型）。设备科台账在 platform 模块，
     * 本模块只存外键，展示时由前端调设备台账接口补编码/维保，不跨模块查表。
     */
    private Long equipmentId;

    /**
     * 启用状态（1-启用 0-停用）
     */
    private Integer enabled;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;
}
