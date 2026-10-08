package com.his.charge.vo;

import com.his.charge.api.MedicalTechGateway;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 检验结果项跨域摘要（低编/高编稽核的证据来源）。
 */
@Data
@NoArgsConstructor
public class LabResultBriefVO {

    /**
     * 结果项ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检验项目名
     */
    private String laboratoryItemName;

    /**
     * 结果值
     */
    private String resultValue;

    /**
     * 结果单位
     */
    private String resultUnit;

    /**
     * 参考范围（合规稽核要区分"高值有参考区间"和"无区间裸值"）
     */
    private String referenceRange;

    /**
     * 异常描述（偏高/偏低/阳性…）
     */
    private String abnormalDesc;

    /**
     * 判读意见/备注
     */
    private String judgeNote;
}
