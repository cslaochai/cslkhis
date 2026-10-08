package com.his.operation.vo;

import com.his.operation.entity.BizAnesthesiaMed;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 麻醉用药出参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnesthesiaMedVO extends BizAnesthesiaMed {

    private String medPhaseText;

    private String routeText;

    private String doseText;
}
