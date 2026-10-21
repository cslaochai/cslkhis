package com.his.outpatient.spi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.his.outpatient.entity.AppointInfo;
import com.his.outpatient.entity.Queue;
import com.his.outpatient.mapper.AppointInfoMapper;
import com.his.outpatient.mapper.QueueMapper;
import com.his.spi.service.RegistStateFacadeService;
import com.his.spi.vo.QueueStateRowVO;
import com.his.spi.vo.RegistStateRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 挂号与队列状态端口（宿主=outpatient）：结诊联动「读挂号行、推进挂号状态、结束队列」的写入出口。
 *
 * <p>叶子实现：只注入本模块 mapper，不依赖任何编排服务。状态推进一律带「期望状态集合」在
 * UPDATE 条件里做乐观校验（原子），当前状态不在集合内不写、返回 false 让调用方提示状态已变更。
 */
@Service
@RequiredArgsConstructor
public class RegistStateFacadeServiceImpl implements RegistStateFacadeService {

    private final AppointInfoMapper appointInfoMapper;
    private final QueueMapper queueMapper;

    @Override
    public RegistStateRowVO getRegistRow(Long registId) {
        if (registId == null) {
            return null;
        }
        AppointInfo e = appointInfoMapper.selectById(registId);
        if (e == null) {
            return null;
        }
        RegistStateRowVO vo = new RegistStateRowVO();
        vo.setId(e.getId());
        vo.setRegistNo(e.getRegistNo());
        vo.setRegistStatus(e.getRegistStatus());
        vo.setDeptId(e.getDeptId());
        vo.setDeptName(e.getDeptName());
        vo.setDoctorId(e.getDoctorId());
        vo.setDoctorName(e.getDoctorName());
        vo.setVisitDate(e.getVisitDate());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRegistStatus(Long registId, Integer targetStatus, List<Integer> expectStatuses) {
        if (registId == null || targetStatus == null || expectStatuses == null || expectStatuses.isEmpty()) {
            return false;
        }
        int n = appointInfoMapper.update(null,
                Wrappers.<AppointInfo>lambdaUpdate()
                        .set(AppointInfo::getRegistStatus, targetStatus)
                        .eq(AppointInfo::getId, registId)
                        .in(AppointInfo::getRegistStatus, expectStatuses));
        return n > 0;
    }

    @Override
    public QueueStateRowVO getQueueRow(Long queueId) {
        if (queueId == null) {
            return null;
        }
        Queue e = queueMapper.selectById(queueId);
        if (e == null) {
            return null;
        }
        QueueStateRowVO vo = new QueueStateRowVO();
        vo.setId(e.getId());
        vo.setQueueNo(e.getQueueNo());
        vo.setQueueStatus(e.getQueueStatus());
        vo.setDoctorId(e.getDoctorId());
        vo.setEndTime(e.getEndTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishQueue(Long queueId, Integer targetStatus, LocalDateTime endTime, List<Integer> expectStatuses) {
        if (queueId == null || targetStatus == null || expectStatuses == null || expectStatuses.isEmpty()) {
            return false;
        }
        int n = queueMapper.update(null,
                Wrappers.<Queue>lambdaUpdate()
                        .set(Queue::getQueueStatus, targetStatus)
                        .set(Queue::getEndTime, endTime)
                        .eq(Queue::getId, queueId)
                        .in(Queue::getQueueStatus, expectStatuses));
        return n > 0;
    }
}
