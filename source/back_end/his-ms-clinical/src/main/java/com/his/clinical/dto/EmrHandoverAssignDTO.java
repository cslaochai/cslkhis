package com.his.clinical.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 病历草稿移交代补入参：批量把指定草稿移交承接人代补。
 */
@Data
public class EmrHandoverAssignDTO {

    /**
     * 待移交病历ID集合（仅草稿态可移交）
     */
    @NotEmpty(message = "请选择要移交的病历")
    private List<Long> recordIds;

    /**
     * 承接人员工ID（上级医师/质控岗）
     */
    @NotNull(message = "请选择承接人")
    private Long receiverId;

    /**
     * 移交原因（1-离职 2-长期休假 3-调离临床岗位 4-其他）
     */
    @NotNull(message = "请选择移交原因")
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "移交原因取值不合法")
    private Integer reason;

    /**
     * 移交说明（可选）
     */
    private String remark;
}
