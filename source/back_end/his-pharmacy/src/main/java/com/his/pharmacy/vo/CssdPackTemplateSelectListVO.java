package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 器械包模板下拉出参：回收登记「选包带出默认灭菌方式」用的三列。
 *
 * <p>组成清单不在这里给 —— 选中后前端拿 id 再取一次详情，避免每个候选包都摊平一份明细行。
 */
@Data
@Schema(name = "CssdPackTemplateSelectListVO", description = "器械包模板下拉出参")
public class CssdPackTemplateSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 包名
     */
    private String packName;

    /**
     * 默认灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）
     */
    private Integer sterilizeMethod;
}
