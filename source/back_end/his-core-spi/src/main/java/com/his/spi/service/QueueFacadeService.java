package com.his.spi.service;

import com.his.spi.dto.AppointCheckInUpdateDTO;
import com.his.spi.vo.PatientQueueVO;

import java.util.List;

/**
 * 候诊队列端口：患者端排队查询、按挂号签到与医生接诊状态收尾的编排入口。
 */
public interface QueueFacadeService {

    /**
     * 患者端「我的排队」：近 3 日挂号 + 队列状态 + 前方等待人数。
     * 用于小程序排队页。
     */
    List<PatientQueueVO> myQueue(Long patientId);

    /**
     * 按挂号记录签到（签到时创建排队记录）。
     * 用于小程序/自助机「到院签到」。
     */
    boolean checkInByRegistId(AppointCheckInUpdateDTO dto);

    /**
     * 结诊后清医生接诊状态缓存。
     * 用于工作站结诊动作之后，避免下一位患者被算进同一医生的接诊中人数。
     */
    void finishConsulting(Long doctorId);
}
