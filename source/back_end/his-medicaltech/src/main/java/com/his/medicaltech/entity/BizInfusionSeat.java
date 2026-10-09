package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 门诊输液室座位（M10）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_infusion_seat")
public class BizInfusionSeat extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 座位号
     */
    private String seatNo;

    /**
     * 区域（成人区/儿童区/隔离区等）
     */
    private String area;

    /**
     * 状态（1-空闲 2-占用 3-停用）
     */
    private Integer seatStatus;
}
