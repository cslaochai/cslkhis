package com.his.appoint.support;

import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.enums.AppointStatusEnum;
import com.his.appoint.enums.QueueStatusEnum;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.mapper.BizQueueMapper;
import com.his.patient.entity.BizPatient;
import com.his.patient.service.BizPatientService;
import com.his.system.api.service.EmrVisitApi;
import com.his.system.api.vo.AppointBriefVO;
import com.his.system.api.vo.PatientBriefVO;
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

    private final BizPatientService bizPatientService;

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
        BizPatient patient = bizPatientService.getById(patientId);
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
            bizAppointInfoMapper.updateById(appoint);
        }

        BizQueue queue = queueId == null ? null : bizQueueMapper.selectById(queueId);
        if (queue == null) {
            return null;
        }
        queue.setQueueStatus(QueueStatusEnum.COMPLETED.getCode());
        queue.setEndTime(LocalDateTime.now());
        bizQueueMapper.updateById(queue);
        return queue.getDoctorId();
    }
}
