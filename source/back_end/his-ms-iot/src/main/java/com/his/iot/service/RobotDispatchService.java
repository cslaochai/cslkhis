package com.his.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.his.iot.dto.RobotDispatchAssignDTO;
import com.his.iot.dto.RobotDispatchCancelDTO;
import com.his.iot.dto.RobotDispatchCreateDTO;
import com.his.iot.dto.RobotDispatchPageQueryDTO;
import com.his.iot.vo.RobotDispatchOrderVO;
import com.his.iot.vo.RobotFleetVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流机器人调度：配送任务单创建、派车分配、状态回写、车队状态总览。
 */
public interface RobotDispatchService {

    /** 新建配送任务单；autoAssign=true 时落单后立即派车。 */
    Long create(RobotDispatchCreateDTO dto);

    /** 任务单分页。 */
    IPage<RobotDispatchOrderVO> listPage(RobotDispatchPageQueryDTO query);

    /** 任务单详情。 */
    RobotDispatchOrderVO getDetail(Long id);

    /** 派车：指定机器人或自动选空闲车；下行 ASSIGN_TASK 指令。 */
    void assign(RobotDispatchAssignDTO dto);

    /** 取消任务单（待调度/已派单可取消）。 */
    void cancel(RobotDispatchCancelDTO dto);

    /** 车队状态：台账 + 最新遥测 + 当前执行任务。 */
    List<RobotFleetVO> fleet();

    /**
     * 设备事件回执回写：按事件码把任务单状态推到配送中/已签收，回填签收人与行程回执。
     * 由事件落库链路调用，订单与事件按 orderNo 关联。
     */
    void syncFromEvent(String eventCode, String orderNo, JsonNode data, String deviceNo, LocalDateTime ts);
}
