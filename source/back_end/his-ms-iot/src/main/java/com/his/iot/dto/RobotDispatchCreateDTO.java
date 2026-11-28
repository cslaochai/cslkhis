package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 新建配送任务单入参。
 */
@Data
public class RobotDispatchCreateDTO implements Serializable {

    /** 发起来源：PIVAS/PHARMACY/LAB/WAREHOUSE/CSSD/LAUNDRY/MANUAL */
    @NotBlank(message = "发起来源不能为空")
    @AllowedValues(values = {"PIVAS", "PHARMACY", "LAB", "WAREHOUSE", "CSSD", "LAUNDRY", "MANUAL"},
            message = "发起来源取值不合法")
    private String sourceType;

    /** 发起科室/站点名称 */
    private String sourceDeptName;

    /** 品类：药品/标本/耗材/器械包/被服 */
    @NotBlank(message = "物品品类不能为空")
    private String goodsType;

    /** 物品明细 */
    @NotBlank(message = "物品明细不能为空")
    private String goodsName;

    /** 取货点 */
    @NotBlank(message = "取货点不能为空")
    private String fromStation;

    /** 目标楼层 */
    @NotNull(message = "目标楼层不能为空")
    private Integer toFloor;

    /** 收货站点（护士站） */
    @NotBlank(message = "收货站点不能为空")
    private String toStation;

    /** 优先级：1-常规 2-批量补货 3-急送 */
    @NotNull(message = "优先级不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "优先级取值不合法")
    private Integer priority;

    /** 箱数 */
    @Min(value = 1, message = "箱数至少为 1")
    private Integer boxCount;

    /** 本单总重(kg) */
    private Double loadKg;

    /** 是否创建后立即派车；false 仅落待调度单 */
    private Boolean autoAssign;

    /** 手工指定机器人编号；为空由调度自动选空闲车 */
    private String robotNo;

    private String remark;
}
