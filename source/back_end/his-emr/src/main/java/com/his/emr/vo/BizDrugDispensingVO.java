package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品发药记录出参
 */
@Data
public class BizDrugDispensingVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 发药单号
     */
    private String dispensingNo;

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 处方明细ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionDetailId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 发药数量
     */
    private BigDecimal quantity;

    /**
     * 单价，单位：元
     */
    private BigDecimal price;

    /**
     * 金额，单位：元
     */
    private BigDecimal amount;

    /**
     * 发药状态：1-待发药 2-已发药 3-已退药
     */
    private Integer dispensingStatus;

    /**
     * 发药药师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pharmacistId;

    /**
     * 发药药师姓名
     */
    private String pharmacistName;

    /**
     * 发药时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dispensingTime;

    /**
     * 发药前库存
     */
    private BigDecimal stockBefore;

    /**
     * 发药后库存
     */
    private BigDecimal stockAfter;

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
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 药品特殊管理分类（0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品）。
     *
     * <p><b>不是药品发药记录的列</b>，由发药服务按 {@code drugId} 从药品字典.special_flag
     * 批量补齐（跨模块读，见 {@code NarcoticRegisterMapper.selectDrugSpecial}）。
     * 放在出参里的理由：发药窗口必须在**点发药之前**就知道这行是不是麻精、
     * 要不要选复核药师，否则只能等后端抛错才知道。
     */
    private Integer specialFlag;

}
