package com.his.inpatient.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 病历文书提交入参（支持批量）。
 */
@Data
public class InpatientRecordSubmitDTO implements Serializable {

    /**
     * 文书ID列表（批量提交）
     */
    @NotEmpty(message = "请选择要提交的文书")
    private List<Long> ids;

    /**
     * 备注
     */
    private String remark;

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    private String pin;
}
