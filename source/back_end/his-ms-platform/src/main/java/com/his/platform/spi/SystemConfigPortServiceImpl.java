package com.his.platform.spi;

import com.his.platform.service.SysConfigService;
import com.his.spi.service.SystemConfigFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link SystemConfigFacadeService} 窄端口实现：委托平台内部 {@link SysConfigService}。
 */
@Service
@RequiredArgsConstructor
public class SystemConfigPortServiceImpl implements SystemConfigFacadeService {

    private final SysConfigService sysConfigService;

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        return sysConfigService.getConfigValue(configKey, defaultValue);
    }

    @Override
    public void setConfigValue(String configKey, String configName, String configValue) {
        sysConfigService.setConfigValue(configKey, configName, configValue);
    }
}
