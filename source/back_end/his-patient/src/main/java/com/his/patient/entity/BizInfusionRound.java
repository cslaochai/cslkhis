package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 输液巡视记录（G14 输液执行闭环）。
 *
 * <p>挂在医嘱执行行（医嘱执行记录的ID）上：
 * 一次执行 = 开始（写回执行行的开始时间/滴速）→ N 次巡视 → 结束（写回执行行的结束时间/不良反应）。
 * 巡视行只增不改 —— 巡视是对当时状态的定格，改历史巡视等于伪造观察记录。
 */
@Data
@TableName("biz_infusion_round")
public class BizInfusionRound {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 执行行ID（医嘱执行记录的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execId;

    /**
     * 医嘱ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /**
     * 入院ID（冗余）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 巡视护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roundNurseId;

    /**
     * 巡视护士姓名
     */
    private String roundNurseName;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
