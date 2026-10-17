package com.his.provider.service;

import com.his.provider.dto.AppointCheckInUpdateDTO;
import com.his.provider.dto.AppointQueryDTO;
import com.his.provider.dto.AppointUpsertDTO;
import com.his.provider.dto.RevisitFeePreviewDTO;
import com.his.provider.vo.BizAppointInfoListVO;
import com.his.provider.vo.DeptRefVO;
import com.his.provider.vo.EmergencyBriefVO;
import com.his.provider.vo.PatientQueueVO;
import com.his.provider.vo.RegistBriefVO;
import com.his.provider.vo.RevisitFeePreviewVO;
import com.his.provider.vo.RevisitRecordSelectVO;
import com.his.provider.vo.TodayVisitVO;
import com.his.provider.vo.StopImpactItemVO;
import com.his.common.base.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 挂号域对外提供的端口
 */
public interface AppointProvider {

    /**
     * 按挂号ID取挂号摘要。
     *
     * @param registId 挂号ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    RegistBriefVO findRegist(Long registId);

    /**
     * 按挂号单号反查开单科室（收费明细的科室归属反查入口）。
     *
     * @param registNo 挂号单号
     * @return 查不到返回 {@code null}（调用方保持科室为空，<b>不得兜底成默认科室</b>）
     */
    DeptRefVO findDeptByRegistNo(String registNo);

    /**
     * 当前登录用户视角下、今天有就诊记录的患者（患者主页「今日就诊」用）。
     *
     * @return patientId → 今日就诊信息；无今日就诊则不返回该 key（不是返回 null 值）
     */
    Map<Long, TodayVisitVO> todayVisits();

    /**
     * 按排班取在挂挂号（停诊影响名单）。
     *
     * @param scheduleId 排班 ID
     * @return 不含已退号/已爽约；查不到返回空列表（不抛异常）
     */
    List<StopImpactItemVO> listRegsBySchedule(Long scheduleId);

    /**
     * 退号（含退款编排），返回是否成功。
     *
     * @param id     挂号 ID
     * @param reason 退号原因
     */
    boolean cancelRegist(Long id, String reason);

    /**
     * 排班诊室变更 → 级联更新当日队列诊室。
     */
    void updateQueueRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName);

    /**
     * 排班诊室变更 → 级联更新当日挂号诊室。
     */
    void updateAppointRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName);

    // ---------- 挂号单读写 ----------

    /**
     * 患者挂号（预约池扣号 + 收费策略判定 + 判重 + 建挂号费账单，全在提供方口径内）。
     *
     * @return 落库后的挂号详情
     */
    BizAppointInfoListVO addAppoint(AppointUpsertDTO dto);

    /**
     * 挂号分页查询（患者端「我的预约」用；归属校验归调用方）
     */
    PageResult<BizAppointInfoListVO> listRegistPage(AppointQueryDTO query);

    /**
     * 按就诊日期区间取挂号明细（报表统计用，闭区间，任一端可空）。
     */
    List<BizAppointInfoListVO> listRegists(String startDate, String endDate);

    /**
     * 查挂号记录归属的就诊人ID；记录不存在返回 {@code null}。
     * 患者端写操作前的归属校验锚点。
     */
    Long patientIdOfRegist(Long registId);

    // ---------- 复诊 ----------

    /**
     * 复诊费用预估（按复诊收费策略，只读）
     */
    RevisitFeePreviewVO revisitFeePreview(RevisitFeePreviewDTO dto);

    /**
     * 复诊「原病历」候选：按患者取最近若干次真正就诊过的病历
     */
    List<RevisitRecordSelectVO> revisitRecordSelectList(Long patientId, int limit);

    // ---------- 队列 ----------

    /**
     * 患者端「我的排队」：近 3 日挂号 + 队列状态 + 前方等待人数
     */
    List<PatientQueueVO> myQueue(Long patientId);

    /**
     * 按挂号记录签到（签到时创建排队记录）
     */
    boolean checkInByRegistId(AppointCheckInUpdateDTO dto);

    // ---------- 医生接诊状态 ----------

    /**
     * 结诊后清医生接诊状态缓存
     */
    void finishConsulting(Long doctorId);

    // ---------- 急诊 ----------

    /**
     * 急诊记录摘要（AI 分诊建议的事实来源）；不存在返回 {@code null}
     */
    EmergencyBriefVO findEmergency(Long emergencyId);
}
