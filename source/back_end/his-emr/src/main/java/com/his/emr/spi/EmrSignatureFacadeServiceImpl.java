package com.his.emr.spi;

import com.his.emr.service.EmrSignatureService;
import com.his.spi.dto.SignCommandDTO;
import com.his.spi.service.EmrSignatureFacadeService;
import com.his.spi.vo.SignatureVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 电子签名端口实现：把签名动作转给病历域自己的签名服务。
 */
@Service
@RequiredArgsConstructor
public class EmrSignatureFacadeServiceImpl implements EmrSignatureFacadeService {

    private final EmrSignatureService emrSignatureService;

    @Override
    public SignatureVO sign(SignCommandDTO command) {
        return emrSignatureService.sign(command);
    }

    @Override
    public SignatureVO invalidate(Long signId, String reason, Long operatorId, String operatorName) {
        return emrSignatureService.invalidate(signId, reason, operatorId, operatorName);
    }
}
