package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.api.dto.PrepayQueryPageDTO;
import com.his.api.service.ChargeProvider;
import com.his.api.vo.PrepayBalanceVO;
import com.his.api.vo.PrepayVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniAdmissionMapper;
import com.his.miniapp.service.MiniDepositService;
import com.his.miniapp.vo.MiniAdmiSelectListVO;
import com.his.api.service.AuthContextProvider;
import com.his.api.service.PatientProvider;
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
        // C-非 web 入参：私有 helper 被 balance（@RequestParam）与 prepayListPage 共用，后者的分页 DTO 属收费模块、
        // admissionId 对全院流水查询是可空的公共条件，加注解会挡掉那边的合法请求，Bean Validation 也覆盖不到本入口，保留
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
