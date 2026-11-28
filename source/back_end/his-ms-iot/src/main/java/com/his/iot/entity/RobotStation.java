package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物流机器人收发站点（biz_robot_station）。
 * <p>
 * 站点是物理收发点，不是组织节点：一个科室可有多个站点，充电桩/中转驿站不属任何科室。
 * 楼层由站点带出，任务单不再手填；机器人地图上的精确点由厂商 RCS 持有，这里只存停靠点位码。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_robot_station")
public class RobotStation extends BaseEntity {

    /** 站点编码（ST-1F-LAB），与厂商停靠点表对码 */
    private String stationCode;

    /** 站点名称 */
    private String stationName;

    /** 站点类型：1-护士站 2-药房 3-静配 4-医技 5-消毒供应 6-库房 7-被服 8-充电桩 9-中转驿站 */
    private Integer stationType;

    /** 楼栋 */
    private String building;

    /** 楼层（B1 为 -1） */
    private Integer floorNo;

    /** 归属科室ID（仅归属参考，不参与调度） */
    private Long deptId;

    /** 归属科室名称快照 */
    private String deptName;

    /** 可作取货点：1-是 0-否 */
    private Integer pickupEnabled;

    /** 可作收货点：1-是 0-否 */
    private Integer receiveEnabled;

    /** 机器人停靠点位编码（厂商地图 point） */
    private String dockCode;

    /** 服务时间窗（如 08:00-17:00）；空为全天 */
    private String serviceWindow;

    /** 仿真地图横坐标 */
    private Double mapX;

    /** 仿真地图纵坐标 */
    private Double mapY;

    /** 状态：1-启用 0-停用 */
    private Integer enabled;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    /** 删除标志 */
    @TableLogic
    private Integer delFlag;
}
