package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 门诊输液巡视记录（M10）。只增不改 —— 巡视是对当时状态的定格（与 G14 住院输液同一铁律）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_outp_infusion_round")
public class BizOutpInfusionRound extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 输液单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long infusionId;

    /**
     * 巡视时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime roundTime;

    /**
     * 滴速（滴/分）
     */
    private Integer dripRate;

    /**
     * 余量（ml）
     */
    private Integer remainingVolume;

    /**
     * 巡视护士ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 巡视护士姓名
     */
    private String nurseName;
}
