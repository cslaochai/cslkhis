package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ObjectSignStatus;
import com.his.common.enums.SignBizType;
import com.his.common.service.SignCoverageProvider;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.service.OutpatientRecordSignCoverageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 门诊病历的签名覆盖率。
 */
@Component
@RequiredArgsConstructor
public class OutpatientRecordSignCoverageProviderImpl implements SignCoverageProvider, OutpatientRecordSignCoverageProvider {

    private final BizMedicalRecordMapper recordMapper;

    @Override
    public SignBizType bizType() {
        return SignBizType.OUTPATIENT_RECORD;
    }

    @Override
    public SignCoverage coverage() {
        long total = recordMapper.selectCount(null);
        long signed = recordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatus.SIGNED.getCode()));
        long invalidated = recordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatus.INVALIDATED.getCode()));
        long pending = recordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatus.UNSIGNED.getCode()));
        return new SignCoverage(SignBizType.OUTPATIENT_RECORD.getCode(), SignBizType.OUTPATIENT_RECORD.getText(),
                total, signed, pending, invalidated);
    }
}
