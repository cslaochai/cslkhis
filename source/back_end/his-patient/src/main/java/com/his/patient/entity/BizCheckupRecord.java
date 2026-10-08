package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体检登记实体（体检登记）。
 *
 * <p>状态机：1 已登记 → 2 检查中 → 3 已完成 → 4 已出报告（4 为终态）。
 * 单向流转：出报告要求全部明细已录且总检结论必填；出报告后禁改禁删。
 */
@Data
@TableName("biz_checkup_record")
public class BizCheckupRecord {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 体检编号
     */
    private String recordNo;

    /**
     * 体检人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 体检人姓名
     */
    private String patientName;

    /**
     * 性别字典:1男 2女 9未知
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 体检对象（1-个人 2-团体）
     */
    private Integer personType;

    /**
     * 套餐ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 应收金额
     */
    private BigDecimal totalAmount;

    /**
     * 体检日期
     */
    private LocalDate checkupDate;

    /**
     * 状态（1-已登记 2-检查中 3-已完成 4-已出报告）
     */
    private Integer recordStatus;

    /**
     * 总检结论
     */
    private String conclusion;

    /**
     * 总检医师
     */
    private String doctorName;

    /**
     * 报告时间
     */
    private LocalDateTime reportTime;

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
