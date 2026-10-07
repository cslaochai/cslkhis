package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.enums.QueueStatusEnum;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.patient.service.PatientTodayVisit;
import com.his.patient.service.PatientTodayVisitProvider;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 「今日就诊」提供者实现（就诊域）。
 *
 * <p>实现 {@link PatientTodayVisitProvider}（接口在患者域），让全局患者搜索能把
 * 「今天真的在就诊」的患者排到最前。his-appoint 已依赖 his-patient，方向正确。
 *
 * <p>口径与医生站今日队列（{@code QueueServiceImpl#getTodayQueueList}）保持一致：
 * <b>按候诊队列的就诊日期 = 今天</b>，而不是 arrive_time 区间 —— 后者在跨零点的
 * 夜班场景下会把昨天的患者算成今天。
 *
 * <p>只覆盖**门诊**（候诊队列）。住院在院的标注属另一笔账：住院患者有独立工作区，
 * 且入院记录在患者域的住院包里，放到这里会让就诊域反向依赖住院包。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatientTodayVisitProviderImpl implements PatientTodayVisitProvider {

    private final BizQueueMapper bizQueueMapper;

    @Override
    public Map<Long, PatientTodayVisit> todayVisits() {
        LambdaQueryWrapper<BizQueue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizQueue::getVisitDate, LocalDate.now())
                // 已退号(5)/已过号(6) 不算「今日就诊」——写了病历也轮不到他
                .in(BizQueue::getQueueStatus,
                        QueueStatusEnum.WAITING.getCode(),
                        QueueStatusEnum.CONSULTING.getCode(),
                        QueueStatusEnum.COMPLETED.getCode())
                .orderByDesc(BizQueue::getArriveTime);
        List<BizQueue> list = bizQueueMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        CurrentUser currentUser = UserUtils.getCurrentUser();
        Long myEmpId = currentUser != null ? currentUser.getEmployeeId() : null;
        Long myDeptId = currentUser != null ? currentUser.getDeptId() : null;

        Map<Long, PatientTodayVisit> result = new HashMap<>();
        for (BizQueue q : list) {
            if (q.getPatientId() == null) {
                continue;
            }
            PatientTodayVisit visit = toVisit(q, myEmpId, myDeptId);
            PatientTodayVisit exist = result.get(q.getPatientId());
            if (exist == null) {
                result.put(q.getPatientId(), visit);
                continue;
            }
            // 同一患者今天可能有多条队列记录（多次挂号/签到）：
            // 查询已按 arrive_time DESC，先到的更新，所以默认保留先到的；
            // 唯一例外——「我的人」优先，否则医生会看到患者在别科的最新状态而漏掉自己这单。
            if (Boolean.TRUE.equals(visit.getMine()) && !Boolean.TRUE.equals(exist.getMine())) {
                result.put(q.getPatientId(), visit);
            }
        }
        return result;
    }

    private PatientTodayVisit toVisit(BizQueue q, Long myEmpId, Long myDeptId) {
        PatientTodayVisit visit = new PatientTodayVisit();
        visit.setPatientId(q.getPatientId());
        visit.setStatus(q.getQueueStatus());
        QueueStatusEnum status = q.getQueueStatus() == null
                ? null : QueueStatusEnum.fromCode(q.getQueueStatus());
        // 状态文案由提供方给：前端不再自造码值映射（未知码值宁可显示原始码，也不回落成看似合法的值）
        visit.setStatusText(status != null ? status.getLabel() : "状态(" + q.getQueueStatus() + ")");
        visit.setDeptName(q.getDeptName());
        visit.setDoctorName(q.getDoctorName());
        visit.setQueueNo(q.getSequenceNo() != null ? q.getSequenceNo() + " 号" : null);
        visit.setMine((myEmpId != null && myEmpId.equals(q.getDoctorId()))
                || (myDeptId != null && myDeptId.equals(q.getDeptId())));
        return visit;
    }
}
