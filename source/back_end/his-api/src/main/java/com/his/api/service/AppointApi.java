package com.his.api.service;

import com.his.api.vo.DeptRefVO;
import com.his.api.vo.RegistBriefVO;
import com.his.api.vo.TodayVisitVO;
import com.his.api.vo.StopImpactItemVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 挂号域对外提供的端口（依赖倒置，实现落在 his-appoint）：
 * 挂号摘要与科室反查、今日就诊、停诊/改诊室级联。
 *
 * <p>号源排班迁到 system 模块后，停诊/改诊室这类跨域动作不得直接依赖 appoint 模块
 * （否则 system → appoint 与 appoint → system 形成循环依赖），统一走本端口。
 */
public interface AppointApi {

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
}
