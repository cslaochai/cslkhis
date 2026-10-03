package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 处方分页查询入参（审方工作台 / 处方查询通用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PrescriptionQueryPageDTO extends PageParam {

    /**
     * 关键字（模糊匹配：处方号 / 患者姓名 / 患者号）
     */
    private String keyword;

    /**
     * 处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）
     */
    private Integer prescriptionStatus;

    /**
     * 只看「未审方」（审方签名ID为空）；空则不限。
     * <p>审方工作台的默认筛选 —— 用签名锚点判断，而不是用状态码猜。
     */
    private Boolean unauditedOnly;

    /**
     * 开方医生ID
     */
    private Long doctorId;
}
