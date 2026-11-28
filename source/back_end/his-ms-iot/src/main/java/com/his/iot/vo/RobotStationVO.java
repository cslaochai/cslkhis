package com.his.iot.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机器人站点 VO（列表、详情、下拉共用）。
 */
@Data
public class RobotStationVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String stationCode;
    private String stationName;

    private Integer stationType;
    private String stationTypeLabel;

    private String building;
    private Integer floorNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;

    private Integer pickupEnabled;
    private Integer receiveEnabled;

    private String dockCode;
    private String serviceWindow;

    private Double mapX;
    private Double mapY;

    private Integer enabled;
    private String enabledLabel;

    private Integer sortOrder;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
