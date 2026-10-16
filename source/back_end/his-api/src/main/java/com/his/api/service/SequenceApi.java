package com.his.api.service;

/**
 * 分布式序列号端口（his-system 实现）。
 *
 * <p>业务单号统一出口在 his-system 的 {@code RedisSequenceService}，miniapp 只依赖本端口。
 */
public interface SequenceApi {

    /**
     * 取下一序列号（从 1 开始，按 module 维度自增）。
     *
     * @param module 模块标识，如 PATIENT、QUEUE
     * @return 自增后的序列号
     */
    long next(String module);
}
