package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 麻醉用药记录—— 只增不改。
 *
 * <p>为什么不继承 {@code BaseEntity}：表里没有 remark 之外的-update 列，
 * 见 {@link BizAnesthesiaVital} 的同名注释。
 *
 * <p>用药是麻醉记录单里最能回答"这一刀到底怎么麻过来的"的部分：
 * 诱导给了什么、维持用什么泵、苏醒用了什么拮抗。缺了它，麻醉单就只剩下血压数字。
 */
@Data
@TableName("biz_anesthesia_med")
public class BizAnesthesiaMed implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 麻醉记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 给药时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime medTime;

    /**
     * 用药阶段（1-诱导 2-维持 3-苏醒）
     */
    private Integer medPhase;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 剂量
     */
    private BigDecimal dose;

    /**
     * 剂量单位（mg / ug / ml）
     */
    private String unit;

    /**
     * 给药途径：1-静脉推注 2-静脉泵注 3-静脉滴注 4-吸入 5-肌注 6-椎管内 7-局麻浸润 8-其他
     */
    private Integer route;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
