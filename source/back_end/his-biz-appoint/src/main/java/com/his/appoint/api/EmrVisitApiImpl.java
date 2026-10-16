package com.his.appoint.api;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizQueue;
import com.his.common.enums.AppointStatusEnum;
import com.his.appoint.enums.QueueStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.api.service.PatientApi;
import com.his.api.vo.PatientMasterVO;
import com.his.api.service.EmrVisitApi;
import com.his.api.vo.AppointBriefVO;
import com.his.api.vo.PatientBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 病历-就诊联动端口实现（SPI：接口声明在 his-system.api，本类是 his-appoint 侧实现）。
 *
 * <p>病历域不再直连本模块 Mapper；患者摘要经 his-patient 的 service 读取（跨模块只走 service）。
 */
@Component
@RequiredArgsConstructor
public class EmrVisitApiImpl implements EmrVisitApi {

    private final BizAppointInfoMapper bizAppointInfoMapper;

    private final BizQueueMapper bizQueueMapper;

    private final PatientApi patientApi;

    @Override
    public AppointBriefVO getAppointById(Long registId) {
        BizAppointInfo appoint = bizAppointInfoMapper.selectById(registId);
        if (appoint == null) {
            return null;
        }
        AppointBriefVO vo = new AppointBriefVO();
        BeanUtils.copyProperties(appoint, vo);
        return vo;
    }

    @Override
    public PatientBriefVO getPatientById(Long patientId) {
        PatientMasterVO patient = patientApi.getPatient(patientId);
        if (patient == null) {
            return null;
        }
        PatientBriefVO vo = new PatientBriefVO();
        BeanUtils.copyProperties(patient, vo);
        return vo;
    }

    @Override
    public Long completeVisit(Long registId, Long queueId) {
        BizAppointInfo appoint = bizAppointInfoMapper.selectById(registId);
        if (appoint != null) {
            appoint.setRegistStatus(AppointStatusEnum.COMPLETED.getCode());
            // 守卫下沉到 WHERE：结诊回写仅限「已接诊/已就诊」，防并发退号/过号/结转后用陈旧快照整行覆盖
            int rows = bizAppointInfoMapper.update(appoint, new LambdaUpdateWrapper<BizAppointInfo>()
                    .eq(BizAppointInfo::getId, appoint.getId())
                    .in(BizAppointInfo::getRegistStatus,
                            AppointStatusEnum.ACCEPTED.getCode(), AppointStatusEnum.COMPLETED.getCode()));
            if (rows == 0) {
                throw new BusinessException("该挂号状态已变更，请刷新后重试");
            }
        }

        BizQueue queue = queueId == null ? null : bizQueueMapper.selectById(queueId);
        if (queue == null) {
            return null;
        }
        queue.setQueueStatus(QueueStatusEnum.COMPLETED.getCode());
        queue.setEndTime(LocalDateTime.now());
        // 守卫下沉到 WHERE：完成回写仅限「就诊中/已就诊」队列（后者为幂等重提），防并发过号/退号后快照复活
        int rows = bizQueueMapper.update(queue, new LambdaUpdateWrapper<BizQueue>()
                .eq(BizQueue::getId, queue.getId())
                .in(BizQueue::getQueueStatus,
                        QueueStatusEnum.CONSULTING.getCode(), QueueStatusEnum.COMPLETED.getCode()));
        if (rows == 0) {
            throw new BusinessException("该队列状态已变更，请刷新后重试");
        }
        return queue.getDoctorId();
    }
}
