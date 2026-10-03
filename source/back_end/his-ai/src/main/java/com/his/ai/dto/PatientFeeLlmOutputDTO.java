package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 患者端费用解释的模型输出结构。
 * <p>
 * 与报告解读同理：账单拆分是确定性计算，模型只负责把这堆数字说成一句人话。
 * 输出只有 {@code summary} 一个字段，且必须过 {@code PatientTextGuard}。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientFeeLlmOutputDTO {

    /**
     * 面向患者的一句话总结，不超过 80 字。
     * 不得出现具体报销政策（起付线、封顶线、各险种比例），
     * 那些会变且各地不同，只能说本账单实际发生了什么。
     */
    private String summary;
}
