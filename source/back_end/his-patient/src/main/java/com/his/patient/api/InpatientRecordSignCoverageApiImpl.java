package com.his.patient.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.system.api.service.SignCoverageApi;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.mapper.BizInpatientRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 住院病历文书的签名覆盖率
 */
@Component
@RequiredArgsConstructor
public class InpatientRecordSignCoverageApiImpl implements SignCoverageApi {

    private final BizInpatientRecordMapper bizInpatientRecordMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.INPATIENT_RECORD;
    }

    @Override
    public SignCoverage coverage() {
        long total = bizInpatientRecordMapper.selectCount(null);
        long signed = bizInpatientRecordMapper.selectCount(new LambdaQueryWrapper<BizInpatientRecord>()
                .eq(BizInpatientRecord::getSignStatus, ObjectSignStatusEnum.SIGNED.getCode()));
        long invalidated = bizInpatientRecordMapper.selectCount(new LambdaQueryWrapper<BizInpatientRecord>()
                .eq(BizInpatientRecord::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode()));
        long pending = bizInpatientRecordMapper.selectCount(new LambdaQueryWrapper<BizInpatientRecord>()
                .eq(BizInpatientRecord::getSignStatus, ObjectSignStatusEnum.UNSIGNED.getCode()));
        return new SignCoverage(SignBizTypeEnum.INPATIENT_RECORD.getCode(), SignBizTypeEnum.INPATIENT_RECORD.getText(),
                total, signed, pending, invalidated);
    }
}
