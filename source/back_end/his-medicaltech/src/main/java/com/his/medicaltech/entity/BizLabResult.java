package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检验结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_lab_result")
public class BizLabResult extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检验记录号
     */
    private String recordNo;

    /**
     * 检验项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 检验结果值
     */
    private String resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 参考范围
     */
    private String referenceRange;

    /**
     * 异常标志（0-正常 1-偏高 2-偏低 3-异常）
     */
    private Integer abnormalFlag;

    /**
     * 异常描述
     */
    private String abnormalDesc;

    /**
     * 异常判定说明：判定依据（用了哪个区间）或未判定的原因。
     * <p>
     * 存在的意义是让「漏报」可追查 —— 后台不做判定时不会有任何报错，
     * 只有把「为什么没判定」落下来，事后复盘才有依据。
     */
    private String judgeNote;

    /**
     * 结果类型（1-定量 2-定性 3-文字描述）
     */
    private Integer resultType;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
