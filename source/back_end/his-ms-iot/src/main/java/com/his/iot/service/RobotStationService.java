package com.his.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.iot.dto.RobotStationPageQueryDTO;
import com.his.iot.dto.RobotStationUpsertDTO;
import com.his.iot.entity.RobotStation;
import com.his.iot.vo.RobotStationOptionsVO;
import com.his.iot.vo.RobotStationVO;

/**
 * 物流机器人收发站点主数据。
 */
public interface RobotStationService {

    IPage<RobotStationVO> listPage(RobotStationPageQueryDTO query);

    RobotStationVO getDetail(Long id);

    Long upsert(RobotStationUpsertDTO dto);

    void delete(Long id);

    /** 叫车表单候选：启用站点 + 品类（含单箱重量与箱数上限）+ 优先级 */
    RobotStationOptionsVO options();

    /**
     * 取一个可用于本方向的启用站点，校验失败直接抛业务异常（存在性/启用/取收能力/服务时间窗）。
     *
     * @param id    站点ID
     * @param pickup true=作取货点，false=作收货点
     */
    RobotStation requireStation(Long id, boolean pickup);

    /** 只校验存在与启用（发起站点用，不受取货/收货能力限制） */
    RobotStation requireEnabled(Long id);

    /** 按ID取站点，不存在返回 null（派单选车读楼层用，不做抛异常） */
    RobotStation findById(Long id);
}
