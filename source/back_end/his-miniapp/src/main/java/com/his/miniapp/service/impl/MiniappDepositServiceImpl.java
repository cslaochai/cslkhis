package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.vo.PrepayBalanceVO;
import com.his.charge.vo.PrepayVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniappAdmissionMapper;
import com.his.miniapp.service.MiniappDepositService;
import com.his.miniapp.vo.AdmissionSelectListVO;
import com.his.patient.service.PatientGuardianService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniappDepositServiceImpl implements MiniappDepositService {

    private final MiniappAdmissionMapper miniappAdmissionMapper;
    private final InpatientAccountService inpatientAccountService;
    private final PatientGuardianService patientGuardianService;

    @Override
    public List<AdmissionSelectListVO> myAdmissions(Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人的住院记录");
        }
        return miniappAdmissionMapper.selectByPatientId(patientId);
    }

    @Override
    public PrepayBalanceVO balance(Long admissionId) {
        requireOwnAdmission(admissionId);
        return inpatientAccountService.balance(admissionId);
    }

    @Override
    public IPage<PrepayVO> prepayListPage(PrepayQueryPageDTO query) {
        requireOwnAdmission(query.getAdmissionId());
        return inpatientAccountService.prepayListPage(query);
    }

    private void requireOwnAdmission(Long admissionId) {
        // C 类保留：归属闸被两处复用 —— 一处入参是 GET @RequestParam 标量，一处是住院收费窗口共用的分页查询 DTO
        //（在窗口侧该字段是可选筛选条件），挂必填注解会把另一个模块的合法查询挡成 400，注解做不到
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        Long patientId = UserUtils.getCurrentUser().getPatientId();
        // 归属从住院记录反查，不看前端传的是谁
        List<AdmissionSelectListVO> mine = miniappAdmissionMapper.selectByPatientId(patientId);
        String target = String.valueOf(admissionId);
        boolean own = mine.stream().anyMatch(m -> Objects.equals(m.getAdmissionId(), target));
        if (!own) {
            throw new BusinessException("无权操作该住院记录的押金");
        }
        patientGuardianService.canAccessPatient(patientId);
    }

}
