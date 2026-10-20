package com.his.spi.service;

import com.his.spi.vo.BizAppointInfoListVO;
import com.his.spi.vo.EmergencyBriefVO;
import com.his.spi.vo.RegistBriefVO;
import com.his.spi.vo.StopImpactItemVO;
import com.his.spi.vo.TodayVisitVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 挂号事实端口：挂号、候诊队列、急诊三类就诊事实的跨域读取，以及排班停诊引起的诊室级联改写。
 * <p>
 * 本端口只承诺「数据级」能力：实现方仅依赖本模块 mapper，不牵挂号/排队的编排服务，
 * 因此任何模块都能依赖它而不与挂号域构成 Bean 环。需要编排（挂号建单、退号退款）请走
 * {@link AppointFacadeService}，需要排队动作请走 {@link QueueFacadeService}。
 */
public interface RegistFacadeService {

    /**
     * 按挂号ID取挂号摘要。
     *
     * @param registId 挂号ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    RegistBriefVO findRegist(Long registId);

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
     * 按就诊日期区间取挂号明细（报表统计用，闭区间，任一端可空）。
     */
    List<BizAppointInfoListVO> listRegists(String startDate, String endDate);

    /**
     * 查挂号记录归属的就诊人ID；记录不存在返回 {@code null}。
     * 患者端写操作前的归属校验锚点。
     */
    Long patientIdOfRegist(Long registId);

    /**
     * 排班诊室变更 → 级联更新当日队列诊室。
     */
    void updateQueueRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName);

    /**
     * 排班诊室变更 → 级联更新当日挂号诊室。
     */
    void updateAppointRoomBySchedule(Long scheduleId, LocalDate visitDate, Long roomId, String roomName);

    /**
     * 急诊记录摘要（AI 分诊建议的事实来源）；不存在返回 {@code null}
     */
    EmergencyBriefVO findEmergency(Long emergencyId);
}
