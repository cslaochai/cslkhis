package com.his.clinical.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.spi.service.SignCoverageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 门诊病历的签名覆盖率。
 */
@Component
@RequiredArgsConstructor
public class OutpatientRecordSignCoverageServiceImpl extends ServiceImpl<BizMedicalRecordMapper, BizMedicalRecord> implements SignCoverageService {

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.OUTPATIENT_RECORD;
    }

    @Override
    public SignCoverage coverage() {
        long total = bizMedicalRecordMapper.selectCount(null);
        long signed = bizMedicalRecordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.SIGNED.getCode()));
        long invalidated = bizMedicalRecordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode()));
        long pending = bizMedicalRecordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.UNSIGNED.getCode()));
        return new SignCoverage(SignBizTypeEnum.OUTPATIENT_RECORD.getCode(), SignBizTypeEnum.OUTPATIENT_RECORD.getLabel(),
                total, signed, pending, invalidated);
    }
}
