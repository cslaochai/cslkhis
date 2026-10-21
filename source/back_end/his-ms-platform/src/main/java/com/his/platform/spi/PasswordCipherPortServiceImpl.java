package com.his.platform.spi;

import com.his.platform.support.PasswordCipherService;
import com.his.spi.service.PasswordCipherFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link PasswordCipherFacadeService} 窄端口实现：委托平台内部 {@link PasswordCipherService#decrypt}。
 */
@Service
@RequiredArgsConstructor
public class PasswordCipherPortServiceImpl implements PasswordCipherFacadeService {

    private final PasswordCipherService passwordCipherService;

    @Override
    public String decrypt(String cipherText) {
        return passwordCipherService.decrypt(cipherText);
    }
}
