package com.his.appoint.support;

import com.his.patient.mapper.BizPatientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 患者主档「首次 / 最近就诊」冗余字段回写器。
 *
 * <p>触发时机只有一种：挂号单结诊（regist_status → 4 已就诊）。挂号 / 签到 / 接诊
 * 都不算「已就诊」—— 患者中心列表上的「最近就诊 / 首次就诊」必须是诊疗事实，
 * 不是预约意向。就诊时间取**结诊时刻**（含时分秒），不是 visit_date（纯日期）。
 *
 * <p>口径（与 {@link BizPatientMapper#markFirstVisit} / {@link BizPatientMapper#markLastVisit}
 * 的 WHERE 条件一致，SQL 一处定义、不在这里重复判断）：
 * <ul>
 *   <li>最近就诊 = 已结诊里最晚的结诊时刻（并发时后结诊的覆盖）</li>
 *   <li>首次就诊 = 已结诊里最早的结诊时刻（同日不互相覆盖，科室/医生取该单快照）</li>
 * </ul>
 * 历史数据不回填：存量患者这些列是 NULL，前端渲染「—」。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PatientVisitSummaryUpdater {

    private final BizPatientMapper patientMapper;

    /**
     * 结诊回写。deptId / doctorId 允许为 null（历史挂号单可能缺快照），名称同样照存；
     * 失败只记日志、不抛出：主档冗余字段少写一次不阻断诊疗流程，
     * 后续任何一次结诊都会把最近就诊追平（首次就诊列暂缺则由下一次更早结诊补上）。
     */
    public void onVisitCompleted(Long patientId, LocalDateTime visitTime,
                                 Long deptId, String deptName, Long doctorId, String doctorName) {
        if (patientId == null || visitTime == null) {
            return;
        }
        try {
            patientMapper.markLastVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
            patientMapper.markFirstVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
        } catch (Exception e) {
            log.warn("患者主档就诊信息回写失败（不阻断结诊流程）：patientId={}, visitTime={}, error={}",
                    patientId, visitTime, e.getMessage());
        }
    }
}
