package com.his.outpatient.spi;

import com.his.outpatient.service.DoctorStatusCacheService;
import com.his.outpatient.service.QueueService;
import com.his.spi.dto.AppointCheckInUpdateDTO;
import com.his.spi.service.QueueFacadeService;
import com.his.spi.vo.PatientQueueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 候诊队列端口实现：排队查询与签到下沉 {@code QueueService}，接诊状态收尾走本模块缓存服务。
 */
@Service
@RequiredArgsConstructor
public class QueueFacadeServiceImpl implements QueueFacadeService {

    private final QueueService queueService;

    private final DoctorStatusCacheService doctorStatusCacheService;

    @Override
    public List<PatientQueueVO> myQueue(Long patientId) {
        return queueService.myQueue(patientId);
    }

    @Override
    public boolean checkInByRegistId(AppointCheckInUpdateDTO dto) {
        return queueService.checkInByRegistId(dto);
    }

    @Override
    public void finishConsulting(Long doctorId) {
        doctorStatusCacheService.finishConsulting(doctorId);
    }
}
