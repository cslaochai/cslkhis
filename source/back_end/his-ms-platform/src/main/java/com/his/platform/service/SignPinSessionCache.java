package com.his.platform.service;

/**
 * 软签「签名 PIN 免输入窗口」的 Redis 读缓存。
 *
 * <p>一次 PIN 校验通过后，在窗口时长内继续签名不必再让签名人重复输口令；
 * 每次成功签名都顺延窗口。口令本身不经本缓存，这里只存「这个人这张证书近期确认过」。
 */
public interface SignPinSessionCache {

    String KEY_PREFIX = "his:sign:pin:";

    /**
     * 该员工用这张证书是否已处于免输入窗口内。
     *
     * @return true=窗口内，本次签名可不输 PIN；Redis 不可用时返回 false（宁可多要一次确认）
     */
    boolean isActive(Long empId, Long certId);

    /**
     * 开启或顺延免输入窗口：PIN 校验通过时调用，窗口内完成一次签名时也调用。
     *
     * @param ttlMinutes 窗口时长（分钟），由调用方按系统参数算好后传入；非正数表示不写窗口
     */
    void renew(Long empId, Long certId, int ttlMinutes);
}
