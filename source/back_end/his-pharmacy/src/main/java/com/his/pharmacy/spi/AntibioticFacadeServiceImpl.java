package com.his.pharmacy.spi;

import com.his.pharmacy.service.AntibioticService;
import com.his.spi.service.AntibioticFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 抗菌药处方权端口实现：硬校验直接委托内部 {@link AntibioticService#assertCanPrescribe}，
 * 无有效授权由内部抛业务异常让整张处方回滚。
 */
@Service
@RequiredArgsConstructor
public class AntibioticFacadeServiceImpl implements AntibioticFacadeService {

    private final AntibioticService antibioticService;

    @Override
    public void assertCanPrescribe(Long doctorId, List<Long> antibioticDrugIds) {
        antibioticService.assertCanPrescribe(doctorId, antibioticDrugIds);
    }
}
