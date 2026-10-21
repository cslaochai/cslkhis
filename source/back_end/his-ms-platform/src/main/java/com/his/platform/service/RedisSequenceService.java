package com.his.platform.service;

/**
 * 基于 Redis 的分布式序列号 / 业务单号生成服务。
 * 内部接口：继承对外窄端口 {@link com.his.spi.service.RedisSequenceService}。
 */
public interface RedisSequenceService extends com.his.spi.service.RedisSequenceService {
}
