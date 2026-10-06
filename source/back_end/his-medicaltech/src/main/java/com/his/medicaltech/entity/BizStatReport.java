package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病案统计上报台账实体（报文落库留痕，打印预留不对外发送）。
 */
@Data
@TableName("biz_stat_report")
public class BizStatReport {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 上报单号
     */
    private String reportNo;

    /**
     * 报表类型（1-卫统年报 2-出院患者统计月报 3-手术工作量专项报表）
     */
    private Integer reportType;

    /**
     * 期间类型（1-月报 2-年报）
     */
    private Integer periodType;

    /**
     * yyyy-MM 或 yyyy
     */
    private String periodValue;

    /**
     * 空=全院口径
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 报表标题
     */
    private String title;

    /**
     * 摘要-出院患者例数
     */
    private Integer dischargeCount;

    /**
     * 摘要-死亡例数
     */
    private Integer deathCount;

    /**
     * 摘要-手术台次
     */
    private Integer operationCount;

    /**
     * 摘要-三级及以上手术台次
     */
    private Integer level3upCount;

    /**
     * 摘要-平均住院日
     */
    private BigDecimal avgLosDays;

    /**
     * 摘要-结算总金额
     */
    private BigDecimal totalAmount;

    /**
     * 上报报文 JSON（生成时点快照，报出后不可变即留痕）
     */
    private String payload;

    /**
     * 0-草稿 1-已报出(打印预留) 2-已作废
     */
    private Integer status;

    /**
     * 报文生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 报出时间
     */
    private LocalDateTime submitTime;

    /**
     * 作废时间
     */
    private LocalDateTime voidTime;

    /**
     * 作废原因
     */
    private String voidReason;

    /**
     * 生成人
     */
    private String operatorName;

    /**
     * 报出人
     */
    private String submitByName;

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
