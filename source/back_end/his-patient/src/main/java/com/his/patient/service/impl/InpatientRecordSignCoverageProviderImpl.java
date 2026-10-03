package com.his.patient.service.impl;

import com.his.patient.service.InpatientRecordSignCoverageProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.ObjectSignStatus;
import com.his.common.enums.SignBizType;
import com.his.common.service.SignCoverageProvider;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.mapper.BizInpatientRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 住院病历文书的签名覆盖率（数的是业务表，所以实现在这里而不是 his-common）。
 *
 * <p>三种状态分别计数、**互不回落**：未签名 / 已签名 / 签名已失效。
 * 存量文书（本能力上线前归档的）会全部落在"未签名"里 —— 这是事实，不是 bug，
 * 把它们藏起来才是问题。
 */
@Component
@RequiredArgsConstructor
public class InpatientRecordSignCoverageProviderImpl implements SignCoverageProvider, InpatientRecordSignCoverageProvider {

    private final BizInpatientRecordMapper recordMapper;

    @Override
    public SignBizType bizType() {
        return SignBizType.INPATIENT_RECORD;
    }

    @Override
    public SignCoverage coverage() {
        long total = recordMapper.selectCount(null);
        long signed = recordMapper.selectCount(new LambdaQueryWrapper<BizInpatientRecord>()
                .eq(BizInpatientRecord::getSignStatus, ObjectSignStatus.SIGNED.getCode()));
        long invalidated = recordMapper.selectCount(new LambdaQueryWrapper<BizInpatientRecord>()
                .eq(BizInpatientRecord::getSignStatus, ObjectSignStatus.INVALIDATED.getCode()));
        long pending = recordMapper.selectCount(new LambdaQueryWrapper<BizInpatientRecord>()
                .eq(BizInpatientRecord::getSignStatus, ObjectSignStatus.UNSIGNED.getCode()));
        return new SignCoverage(SignBizType.INPATIENT_RECORD.getCode(), SignBizType.INPATIENT_RECORD.getText(),
                total, signed, pending, invalidated);
    }
}
