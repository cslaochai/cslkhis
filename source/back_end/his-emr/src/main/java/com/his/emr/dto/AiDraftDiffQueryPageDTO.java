package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 草稿留痕查询入参（AI 管理台）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDraftDiffQueryPageDTO extends PageParam {

    /** 患者姓名（模糊） */
    private String patientName;

    /** 终审医生姓名（模糊） */
    private String doctorName;

    /** 是否修改（1-有修改 0-未修改） */
    private Integer changed;
}
