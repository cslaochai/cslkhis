package com.his.app.service;

import com.his.app.dto.MiniExamBookDTO;
import com.his.app.dto.MiniExamCancelDTO;
import com.his.common.base.PageResult;
import com.his.spi.vo.PatientExamApptVO;
import com.his.spi.vo.PatientExamApplyVO;
import com.his.spi.vo.PatientExamSlotVO;

import java.util.List;

/**
 * 患者端检查预约（小程序「检查预约」）。
 *
 * <p>全部方法先按就诊人绑定关系收口：小程序可以代家人操作，
 * 但 patientId 必须是当前账号绑定过的就诊人，不能由前端随便传。</p>
 */
public interface MiniExamService {

    /**
     * 我的待预约检查申请
     */
    PageResult<PatientExamApplyVO> pendingApplies(Long patientId, int pageNum, int pageSize);

    /**
     * 可选时段
     */
    List<PatientExamSlotVO> recommendSlots(Long patientId, Long applyId, String examDate, String afterTime);

    /**
     * 占号预约
     */
    PatientExamApptVO book(MiniExamBookDTO dto);

    /**
     * 自助取消
     */
    void cancel(MiniExamCancelDTO dto);

    /**
     * 我的检查预约记录
     */
    PageResult<PatientExamApptVO> myAppointments(Long patientId, int pageNum, int pageSize);
}
