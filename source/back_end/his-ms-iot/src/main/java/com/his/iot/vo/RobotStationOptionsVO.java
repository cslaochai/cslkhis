package com.his.iot.vo;

import lombok.Data;

import java.util.List;

/**
 * 叫车表单一次性候选：站点 / 品类（含单箱重量与箱数上限）/ 优先级。
 * <p>
 * 前端不硬编码站点与重量口径，全部从这里取，避免与后端枚举漂移。
 */
@Data
public class RobotStationOptionsVO {

    /** 可用站点（启用状态），含取/收能力标记 */
    private List<RobotStationVO> stations;

    private List<RobotStationTypeVO> stationTypes;

    private List<RobotGoodsTypeVO> goodsTypes;

    private List<RobotPriorityVO> priorities;
}
