package com.his.emr.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.provider.service.SignCoverageProvider;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 门诊病历的签名覆盖率。
 */
@Component
@RequiredArgsConstructor
public class OutpatientRecordSignCoverageProviderImpl implements SignCoverageProvider {

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
