package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检验结果明细出参
 */
@Data
public class BizLabResultVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所属检验记录ID
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
     * 参考区间
     */
    private String referenceRange;

    /**
     * 异常标志：0-正常 1-偏高 2-偏低 3-异常
     * <p>
     * <b>不要直接用它渲染文案。</b> 未判定时这里也是 0（列默认值），
     * 与「正常」同值；展示一律用 {@link #abnormalFlagText}。
     */
    private Integer abnormalFlag;

    /**
     * 异常描述
     */
    private String abnormalDesc;

    /**
     * 判定留痕：判定依据，或未判定的原因（以「未判定：」开头）
     */
    private String judgeNote;

    /**
     * 异常标志展示文案（正常 / 偏高 / 偏低 / 异常 / <b>未判定</b> / 未知）
     * <p>
     * 由后端统一计算，前端直接显示即可 —— 这样「未判定」的语义只有一处定义，
     * 不会在各个页面各写一遍三目判断。
     */
    private String abnormalFlagText;

    /** 结果类型（1-定量 2-定性 3-文字描述） */
    private Integer resultType;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;
}
