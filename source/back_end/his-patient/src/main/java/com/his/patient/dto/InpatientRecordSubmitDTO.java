package com.his.patient.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 病历文书提交入参（支持批量）。
 *
 * <p>提交是"医生写完了"的声明，会记录 {@code submitTime} 与书写医生；
 * 提交后**仍可修改**（真实场景里医生提交后经常发现错字，要求能改但要留痕），
 * 只有**归档后**才锁死 —— 与病案首页同一套口径。
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
}
