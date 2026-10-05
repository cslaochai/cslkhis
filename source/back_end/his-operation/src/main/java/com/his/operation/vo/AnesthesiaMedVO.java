package com.his.operation.vo;

import com.his.operation.entity.BizAnesthesiaMed;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 麻醉用药出参。
 *
 * <p>{@code doseText} 由服务端拼装（剂量 + 单位），前端不要自己去拼：
 * "0.2mg"和"0.2 mg"看着一样，但跨库检索时是两个值 —— 拼串这件事只有一处能做。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaMedVO extends BizAnesthesiaMed {

    private String medPhaseText;

    private String routeText;

    private String doseText;
}
