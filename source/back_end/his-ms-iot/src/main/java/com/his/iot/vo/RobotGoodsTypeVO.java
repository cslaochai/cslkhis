package com.his.iot.vo;

import lombok.Data;

/**
 * 叫车表单候选项：物品品类 + 单箱基准重量 + 该品类最多能装几箱（按额定载重折算）。
 */
@Data
public class RobotGoodsTypeVO {

    private String code;
    private String label;
    private Double unitWeightKg;
    private Integer maxBoxCount;
}
