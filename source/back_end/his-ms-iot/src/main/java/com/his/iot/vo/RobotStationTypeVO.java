package com.his.iot.vo;

import lombok.Data;

/**
 * 站点类型候选项（与 RobotStationTypeEnum 一一对应，供维护页下拉）。
 */
@Data
public class RobotStationTypeVO {

    private Integer value;
    private String label;
}
