package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.system.vo.DrugRationalHitVO;
import lombok.Data;

import java.util.List;

/**
 * 单张处方的合理用药审查结果（审方列表标注 + 审方前提示共用）
 */
@Data
public class PrescriptionRationalVO {

    /** 处方ID（字符串化避免前端精度丢失） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionId;

    /** 是否存在禁忌配伍（true 时点「通过」会被服务端拒绝） */
    private Boolean blocked;

    /** 拦阻理由（逐字取自知识表，与后端拒绝时抛出的文案同源） */
    private String blockMessage;

    /** 全部命中（禁忌 + 慎用 + 剂量提示），禁忌排在最前 */
    private List<DrugRationalHitVO> hits;
}
