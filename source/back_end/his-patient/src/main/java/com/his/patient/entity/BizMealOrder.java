package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订餐配送（sql/168 §3，院内工作站侧，不含金额）。
 */
@Data
@TableName("biz_meal_order")
public class BizMealOrder {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 订餐单号（MO+yyyyMMdd+4位序号）
     */
    private String mealNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;
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
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;
    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 床号
     */
    private String bedNo;

    /**
     * 来源膳食方案ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dietPlanId;
    /**
     * 饮食类型码
     */
    private String dietCode;
    /**
     * 饮食名称
     */
    private String dietName;

    /**
     * 就餐日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mealDate;
    /**
     * 餐次（1-早餐 2-午餐 3-晚餐 4-加餐）
     */
    private Integer mealType;
    /**
     * 份数
     */
    private Integer quantity;
    /**
     * 配餐内容/食谱
     */
    private String dishContent;

    /**
     * 配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消）
     */
    private Integer deliverStatus;

    /**
     * 配餐完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime prepareTime;
    /**
     * 配送出仓时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliverTime;
    /**
     * 配送人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deliverById;
    /**
     * 配送人姓名
     */
    private String deliverByName;
    /**
     * 签收时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signTime;
    /**
     * 签收人（患者/家属/护士姓名）
     */
    private String signBy;
    /**
     * 退订时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    /**
     * 退订原因（deliver_status=4 必填）
     */
    private String cancelReason;

    /**
     * 来源（1-按膳食方案批量生成 2-手工加订）
     */
    private Integer source;

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
    @TableLogic
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
