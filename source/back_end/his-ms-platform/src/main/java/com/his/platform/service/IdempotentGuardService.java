package com.his.platform.service;

/**
 * 请求幂等防重服务：SET key NX EX，同一 (scene, key) 在 TTL 内只放行第一次。
 * 内部接口：继承对外窄端口 {@link com.his.spi.service.IdempotentGuardService}。
 */
public interface IdempotentGuardService extends com.his.spi.service.IdempotentGuardService {
}
