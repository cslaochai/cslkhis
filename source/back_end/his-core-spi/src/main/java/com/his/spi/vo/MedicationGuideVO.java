package com.his.spi.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者端用药说明：一张处方怎么吃，由药品域按处方与药品属性组装后跨域给出。
 */
@Data
@NoArgsConstructor
public class MedicationGuideVO {

    /**
     * 处方ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /**
     * 处方号
     */
    private String prescriptionNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 开方科室名称
     */
    private String deptName;

    /**
     * 开方医生姓名
     */
    private String doctorName;

    /**
     * 处方类型文案（由枚举 label 渲染）
     */
    private String prescriptionTypeText;

    /**
     * 是否已发药。未发药的处方不进入「怎么吃」口径，前端据此换成一句「这张方子还没取药」。
     */
    private boolean dispensed;

    /**
     * 药品明细
     */
    private List<MedicationGuideItemVO> items;

    /**
     * 固定免责提示，患者端每一份用药说明都必须带
     */
    private String advice;
}
