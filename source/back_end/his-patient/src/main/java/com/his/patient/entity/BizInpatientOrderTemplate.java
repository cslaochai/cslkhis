package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 住院医嘱模板主表（sql/103，sql/142 加共享范围）。
 *
 * <p>一份模板要么属于某个医生（{@code doctorId}，员工ID，与医嘱行医师ID 同一口径），
 * 要么属于一个科室（{@code deptId}），要么属于全院 —— 由 {@code scope} 决定，三者互斥。
 * 「全院组套模板」与「医生个人模板」共用这张表与同一张明细表，
 * 不另起一套模板表：两套明细字段必然漂移，漂移一次就是一次收费对不上。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_order_template")
public class BizInpatientOrderTemplate extends BaseEntity implements Serializable {

    /**
     * 归属医生（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 创建时科室ID
     * <p>scope=2 时它就是「归属科室」（过滤键）；scope=3 时置空，全院可见。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 共享范围：1-个人 2-科室 3-全院（sql/142）
     * <p>1=只有 doctorId 本人可见可改；2=deptId 全科可见可改；3=所有人可见、有 ipd:orderSet:* 的可改。
     */
    private Integer scope;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 默认医嘱类型：1-长期 2-临时（套用时带入表单，医生仍可改）
     */
    private Integer orderType;

    /**
     * 明细条数（服务端算，列表展示用）
     */
    private Integer itemCount;

    /**
     * 明细（库里没有这一列，只在读写明细时承载）
     */
    @TableField(exist = false)
    private List<BizInpatientOrderTemplateItem> items;
}
