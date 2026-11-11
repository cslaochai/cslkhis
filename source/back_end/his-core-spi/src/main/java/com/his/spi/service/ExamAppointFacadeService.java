package com.his.spi.service;

import com.his.common.base.PageResult;
import com.his.spi.vo.PatientExamApptVO;
import com.his.spi.vo.PatientExamApplyVO;
import com.his.spi.vo.PatientExamSlotVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 检查预约患者自助端口（实现落在 his-medicaltech）。
 *
 * <p>与员工侧 {@code ExamAppointmentService} 并列而不是复用：员工侧每个写操作都按
 * 「当前登录员工」取操作人、按岗位授权科室过滤设备；患者账号没有 employeeId 与岗位科室，
 * 复用会在 {@code UserUtils} 与 {@code DeptScopeGuard} 两处被拒。本端口的所有方法
 * 由调用方显式传就诊人ID，归属校验在实现内完成。</p>
 */
public interface ExamAppointFacadeService {

    /**
     * 我的待预约检查申请：已缴费或急诊已提交、且尚无在办预约的申请单。
     *
     * @param patientId 就诊人ID（调用方从患者登录态解析，不接受前端自报）
     */
    PageResult<PatientExamApplyVO> pendingApplies(Long patientId, int pageNum, int pageSize);

    /**
     * 可选时段：按申请单项目的可承接设备与时长，给出近 3 天内的空档。
     *
     * @param examDate  期望起始日期（可空 = 今天）
     * @param afterTime 只看该时刻之后（HH:mm，可空）
     */
    List<PatientExamSlotVO> recommendSlots(Long patientId, Long applyId, LocalDate examDate, String afterTime);

    /**
     * 占号预约。
     *
     * @return 预约成功回执；失败抛业务异常（号源冲突、设备停用、申请单状态不可约等）
     */
    PatientExamApptVO book(Long patientId, Long applyId, Long deviceId, LocalDate examDate, String startTime);

    /**
     * 自助取消：只放开「已预约」，退号并回退申请单状态。
     */
    void cancel(Long patientId, Long apptId, String reason);

    /**
     * 我的检查预约记录（含已终结单）。
     */
    PageResult<PatientExamApptVO> myAppointments(Long patientId, int pageNum, int pageSize);
}
