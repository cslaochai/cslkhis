package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
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
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.OUTPATIENT_RECORD;
    }

    @Override
    public SignCoverage coverage() {
        long total = recordMapper.selectCount(null);
        long signed = recordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.SIGNED.getCode()));
        long invalidated = recordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode()));
        long pending = recordMapper.selectCount(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.UNSIGNED.getCode()));
        return new SignCoverage(SignBizTypeEnum.OUTPATIENT_RECORD.getCode(), SignBizTypeEnum.OUTPATIENT_RECORD.getText(),
                total, signed, pending, invalidated);
    }
}
