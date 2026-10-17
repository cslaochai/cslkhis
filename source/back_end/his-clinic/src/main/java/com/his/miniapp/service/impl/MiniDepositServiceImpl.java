package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.provider.dto.PrepayQueryPageDTO;
import com.his.provider.service.ChargeProvider;
import com.his.provider.vo.PrepayBalanceVO;
import com.his.provider.vo.PrepayVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniAdmissionMapper;
import com.his.miniapp.service.MiniDepositService;
import com.his.miniapp.vo.MiniAdmiSelectListVO;
import com.his.provider.service.AuthContextProvider;
import com.his.provider.service.PatientProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniDepositServiceImpl implements MiniDepositService {

    private final MiniAdmissionMapper miniAdmissionMapper;
    private final ChargeProvider chargeProvider;
    private final PatientProvider patientProvider;
    private final AuthContextProvider authContextProvider;

    @Override
    public List<MiniAdmiSelectListVO> myAdmissions(Long patientId) {
        if (!patientProvider.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人的住院记录");
        }
        return miniAdmissionMapper.selectByPatientId(patientId);
    }

    @Override
    public PrepayBalanceVO balance(Long admissionId) {
        requireOwnAdmission(admissionId);
        return chargeProvider.prepayBalance(admissionId);
    }

    @Override
    public IPage<PrepayVO> prepayListPage(PrepayQueryPageDTO query) {
        requireOwnAdmission(query.getAdmissionId());
        return chargeProvider.prepayPage(query);
    }

    private void requireOwnAdmission(Long admissionId) {
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        Long patientId = authContextProvider.getCurrentUser().getPatientId();
        // 归属从住院记录反查，不看前端传的是谁
        List<MiniAdmiSelectListVO> mine = miniAdmissionMapper.selectByPatientId(patientId);
        String target = String.valueOf(admissionId);
        boolean own = mine.stream().anyMatch(m -> Objects.equals(m.getAdmissionId(), target));
        if (!own) {
            throw new BusinessException("无权操作该住院记录的押金");
        }
        patientProvider.canAccessPatient(patientId);
    }

}
