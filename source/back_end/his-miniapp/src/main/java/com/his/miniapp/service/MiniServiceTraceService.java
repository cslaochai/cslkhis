package com.his.miniapp.service;

import com.his.miniapp.dto.ServiceTraceDTO;

/**
 * 客服页自助行为埋点。
 */
public interface MiniServiceTraceService {

    void record(ServiceTraceDTO dto);
}
