package com.his.inpatient.spi;

import com.his.inpatient.entity.BizAdmission;
import com.his.inpatient.mapper.BizAdmissionMapper;
import com.his.spi.service.AdmissionQueryFacadeService;
import com.his.spi.vo.AdmissionActiveBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 入院登记只读取数端口实现（数据宿主=住院域 biz_admission）。
 * <p>叶子端口：只依赖本域 mapper，不注入编排服务，避免与住院域构成 Bean 环。
 */
@Service
@RequiredArgsConstructor
public class AdmissionQueryFacadeServiceImpl implements AdmissionQueryFacadeService {

    private final BizAdmissionMapper bizAdmissionMapper;

    @Override
    public AdmissionActiveBriefVO findAdmissionBrief(Long admissionId) {
        if (admissionId == null) {
            return null;
        }
        BizAdmission admission = bizAdmissionMapper.selectById(admissionId);
        if (admission == null) {
            return null;
        }
        AdmissionActiveBriefVO vo = new AdmissionActiveBriefVO();
        vo.setAdmissionId(admission.getAdmissionId());
        vo.setPatientId(admission.getPatientId());
        vo.setDeptId(admission.getDeptId());
        vo.setWardId(admission.getWardId());
        vo.setBedId(admission.getBedId());
        vo.setAdmitTime(admission.getAdmitTime());
        // 状态直通：0-已出院 1-在院（AdmitStatusEnum），调用方按 ==1 判在院
        vo.setAdmitStatus(admission.getAdmitStatus());
        return vo;
    }
}
