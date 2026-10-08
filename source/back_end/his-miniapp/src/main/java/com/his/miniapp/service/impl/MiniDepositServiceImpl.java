package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.vo.PrepayBalanceVO;
import com.his.charge.vo.PrepayVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniAdmissionMapper;
import com.his.miniapp.service.MiniDepositService;
import com.his.miniapp.vo.MiniAdmiSelectListVO;
import com.his.patient.service.PatientGuardianService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniDepositServiceImpl implements MiniDepositService {

    private final MiniAdmissionMapper miniAdmissionMapper;
    private final InpatientAccountService inpatientAccountService;
    private final PatientGuardianService patientGuardianService;

    @Override
    public List<MiniAdmiSelectListVO> myAdmissions(Long patientId) {
        if (!patientGuardianService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人的住院记录");
        }
        return miniAdmissionMapper.selectByPatientId(patientId);
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
        // C-非 web 入参：私有 helper 被 balance（@RequestParam）与 prepayListPage 共用，后者的分页 DTO 属收费模块、
        // admissionId 对全院流水查询是可空的公共条件，加注解会挡掉那边的合法请求，Bean Validation 也覆盖不到本入口，保留
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        Long patientId = UserUtils.getCurrentUser().getPatientId();
        // 归属从住院记录反查，不看前端传的是谁
        List<MiniAdmiSelectListVO> mine = miniAdmissionMapper.selectByPatientId(patientId);
        String target = String.valueOf(admissionId);
        boolean own = mine.stream().anyMatch(m -> Objects.equals(m.getAdmissionId(), target));
        if (!own) {
            throw new BusinessException("无权操作该住院记录的押金");
        }
        patientGuardianService.canAccessPatient(patientId);
    }

}
