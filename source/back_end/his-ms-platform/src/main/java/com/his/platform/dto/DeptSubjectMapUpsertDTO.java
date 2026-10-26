package com.his.platform.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 科室诊疗科目映射保存入参（以科室为中心，整单提交）。
 *
 * <p>为什么以科室为中心而不是以科目为中心：主科目（is_primary）是「科室侧」的指定，
 * 一个科室只能有一主科目。按科室整单替换才天然保证这个不变量，按科目挂科室则要跨科室
 * 回写别人那一行。
 */
@Data
public class DeptSubjectMapUpsertDTO {

    /**
     * 院内科室ID
     */
    @NotNull(message = "科室不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 该科室对应的科目清单。为空表示清空该科室的全部映射。
     */
    @Valid
    private List<DeptSubjectItemDTO> items;
}
