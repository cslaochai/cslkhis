package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VTE 防控月度指标快照。
 *
 * <p>为什么落表而不是每次实时算：指标是<b>对外报数与评审取证的口径</b>，报出去的数必须能复现。
 * 实时查询会随基础数据补录而漂移（今天补录一份上月评估单，上个月的落实率就变了）。
 * 生成时把分子分母一起存下来（xxx_count 系列），随时可核对。
 */
@Data
@TableName("biz_vte_stats")
public class BizVteStats {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 统计月份
     */
    private String statMonth;

    /**
     * 统计范围（1-全院 2-科室）
     */
    private Integer scopeType;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 同期出院患者数（所有比率的分母）
     */
    private Integer dischargeCount;

    /**
     * 其中做过 Caprini 评估的患者数
     */
    private Integer assessedCount;

    /**
     * VTE 风险评估率（%）
     */
    private BigDecimal assessRate;

    /**
     * 最新评估为中高危（risk_level>=2）的患者数
     */
    private Integer highRiskCount;

    /**
     * 中高危占比（%）
     */
    private BigDecimal highRiskRate;

    /**
     * 中高危中至少落实一条措施的患者数
     */
    private Integer preventDoneCount;

    /**
     * 预防措施落实率（%）
     */
    private BigDecimal preventRate;

    /**
     * 院内新发 VTE 患者数（按人算）
     */
    private Integer vteEventCount;

    /**
     * 院内 VTE 发生率（%）
     */
    private BigDecimal vteIncidenceRate;

    /**
     * 预防相关出血患者数（提示性指标）
     */
    private Integer bleedCount;

    /**
     * 生成人
     */
    private String generateBy;
    /**
     * 生成时间
     */
    private LocalDateTime generateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 备注
     */
    private String remark;
}
