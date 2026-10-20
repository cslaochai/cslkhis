package com.his.medicaltech.spi;

import com.his.medicaltech.service.EmployeeTechAuthService;
import com.his.spi.dto.TechAuthGateDTO;
import com.his.spi.service.EmployeeTechAuthFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 技术授权准入闸端口实现：转调医技域授权服务的准入判定。
 */
@Service
@RequiredArgsConstructor
public class EmployeeTechAuthFacadeServiceImpl implements EmployeeTechAuthFacadeService {

    private final EmployeeTechAuthService employeeTechAuthService;

    @Override
    public void gate(TechAuthGateDTO gate) {
        employeeTechAuthService.gate(gate);
    }
}
