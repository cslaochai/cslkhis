package com.his.app.service;

import com.his.app.dto.ServiceTraceDTO;

/**
 * 客服页自助行为埋点。
 */
public interface MiniServiceTraceService {

    void record(ServiceTraceDTO dto);
}
