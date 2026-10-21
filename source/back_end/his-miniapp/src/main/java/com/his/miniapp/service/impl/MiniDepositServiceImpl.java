package com.his.miniapp.service.impl;

import com.his.common.utils.UserUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.PrepayPageQueryDTO;
import com.his.spi.service.ChargeFacadeService;
import com.his.spi.vo.PrepayBalanceVO;
import com.his.spi.vo.PrepayVO;
import com.his.common.exception.BusinessException;
import com.his.miniapp.mapper.MiniAdmissionMapper;
import com.his.miniapp.service.MiniDepositService;
import com.his.miniapp.vo.MiniAdmiSelectListVO;
import com.his.spi.service.PatientFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniDepositServiceImpl implements MiniDepositService {

    private final MiniAdmissionMapper miniAdmissionMapper;
    private final ChargeFacadeService chargeFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public List<MiniAdmiSelectListVO> myAdmissions(Long patientId) {
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查询该就诊人的住院记录");
        }
        return miniAdmissionMapper.selectByPatientId(patientId);
    }

    @Override
    public PrepayBalanceVO balance(Long admissionId) {
        requireOwnAdmission(admissionId);
        return chargeFacadeService.prepayBalance(admissionId);
    }

    @Override
    public IPage<PrepayVO> prepayListPage(PrepayPageQueryDTO queryDTO) {
        requireOwnAdmission(queryDTO.getAdmissionId());
        return chargeFacadeService.prepayPage(queryDTO);
    }

    private void requireOwnAdmission(Long admissionId) {
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
        patientFacadeService.canAccessPatient(patientId);
    }

}
