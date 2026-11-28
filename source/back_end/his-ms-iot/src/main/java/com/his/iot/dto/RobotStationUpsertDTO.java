package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 机器人站点新增/修改入参。id 为空走新增。
 */
@Data
public class RobotStationUpsertDTO implements Serializable {

    /** 修改时必传 */
    private Long id;

    /** 站点编码，全表唯一 */
    @NotBlank(message = "站点编码不能为空")
    private String stationCode;

    @NotBlank(message = "站点名称不能为空")
    private String stationName;

    /** 1-护士站 2-药房 3-静配 4-医技 5-消毒供应 6-库房 7-被服 8-充电桩 9-中转驿站 */
    @NotNull(message = "站点类型不能为空")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7", "8", "9"}, message = "站点类型取值不合法")
    private Integer stationType;

    private String building;

    @NotNull(message = "楼层不能为空")
    private Integer floorNo;

    /** 归属科室ID，空表示公共站点 */
    private Long deptId;

    /** 归属科室名称快照，随 deptId 由前端带出 */
    private String deptName;

    @AllowedValues(values = {"0", "1"}, message = "取货能力取值不合法")
    private Integer pickupEnabled;

    @AllowedValues(values = {"0", "1"}, message = "收货能力取值不合法")
    private Integer receiveEnabled;

    /** 厂商停靠点位码，空表示尚未与机器人地图对码 */
    private String dockCode;

    /** 服务时间窗（08:00-17:00），空为全天 */
    private String serviceWindow;

    private Double mapX;

    private Double mapY;

    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer enabled;

    private Integer sortOrder;

    private String remark;
}
