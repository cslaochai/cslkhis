package com.his.miniapp.service;

import com.his.miniapp.dto.ServiceTraceDTO;

/**
 * 客服页自助行为埋点。
 *
 * <p>写入失败一律吞掉只记日志：<b>埋点挂了不能让客服页报错</b> ——
 * 患者是来解决问题的，不是来帮我们排查埋点的。
 */
public interface MiniServiceTraceService {

    void record(ServiceTraceDTO dto);
}
