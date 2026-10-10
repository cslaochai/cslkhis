package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.patient.enums.VteEventTypeEnum;
import com.his.patient.enums.VteOnsetEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * VTE 事件登记。
 */
@Data
public class VteEventUpsertDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 入院ID
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 事件类型（1-深静脉血栓DVT 2-肺栓塞PE 3-预防相关出血）
     */
    @NotNull(message = "事件类型不能为空")
    @InEnum(value = VteEventTypeEnum.class, message = "事件类型取值不合法（1-DVT 2-肺栓塞 3-预防相关出血）")
    private Integer eventType;

    /**
     * 发生时机（1-院内发生 2-入院时已存在）
     */
    @NotNull(message = "发生时机不能为空")
    @InEnum(value = VteOnsetEnum.class, message = "发生时机取值不合法（1-院内发生 2-入院时已存在）")
    private Integer onsetType;

    /**
     * 确诊日期
     */
    @NotNull(message = "确诊日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnoseDate;

    /**
     * 诊断依据（1-超声 2-CT肺动脉造影 3-静脉造影 4-临床诊断 5-其他）
     */
    private Integer diagnosisBasis;

    /**
     * 血栓部位
     */
    @Size(max = 100, message = "血栓部位超长")
    private String thrombusSite;

    /**
     * 转归（1-好转 2-未愈 3-死亡 4-未知）
     */
    private Integer outcome;

    /**
     * 事件发生时是否正在药物预防（0-否 1-是）
     */
    private Integer drugPreventFlag;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注超长")
    private String remark;
}
