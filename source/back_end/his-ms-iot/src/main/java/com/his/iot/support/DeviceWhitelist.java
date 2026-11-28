package com.his.iot.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.his.iot.entity.IotDevice;
import com.his.iot.mapper.IotDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设备接入白名单：报文里的 {@code deviceNo} 必须能在 {@code biz_iot_device} 查到且未停用，
 * 声明的 {@code deviceType} 还必须与台账一致——HIS 没登记的设备，不允许把数据写进时序库和告警表，
 * 否则模拟器/第三方随手发来的报文会变成"真实遥测"。
 *
 * <p>台账缓存 60 秒刷新一次。查询失败时沿用上一份缓存；从未成功加载过则放行并告警，
 * 不让台账数据库抖动把整条设备接入打断（接入失败的代价比多收几条数据大）。
 */
@Component
@RequiredArgsConstructor
public class DeviceWhitelist {

    public enum Verdict { PASS, UNKNOWN, TYPE_MISMATCH }

    private static final Logger log = LoggerFactory.getLogger(DeviceWhitelist.class);

    private static final long REFRESH_MS = 60_000;
    /** 同一个设备编号的拒收日志最多每分钟一条，避免一台野设备刷满日志。 */
    private static final long LOG_INTERVAL_MS = 60_000;

    private final IotDeviceMapper mapper;

    /** deviceNo → deviceType；null 表示从未成功加载过台账。 */
    private volatile Map<String, String> ledger;
    private volatile long loadedAt;

    private final AtomicLong unknownDrops = new AtomicLong();
    private final AtomicLong typeDrops = new AtomicLong();
    private final Map<String, Long> lastLogAt = new ConcurrentHashMap<>();

    /**
     * 判定一条报文是否可以入库。
     *
     * @param deviceNo   报文里的设备编号
     * @param deviceType 报文里声明的设备类型
     */
    public Verdict check(String deviceNo, String deviceType) {
        if (deviceNo == null || deviceNo.isBlank()) {
            return Verdict.UNKNOWN;
        }
        Map<String, String> snapshot = ensureLoaded();
        if (snapshot == null) {
            return Verdict.PASS;
        }
        String expectType = snapshot.get(deviceNo);
        if (expectType == null) {
            unknownDrops.incrementAndGet();
            warnThrottled(deviceNo, "未在白名单内的设备报文，已丢弃");
            return Verdict.UNKNOWN;
        }
        if (deviceType != null && !deviceType.isBlank() && !expectType.equals(deviceType)) {
            typeDrops.incrementAndGet();
            warnThrottled(deviceNo, "设备类型与台账不一致（台账 " + expectType + "，报文 " + deviceType + "），已丢弃");
            return Verdict.TYPE_MISMATCH;
        }
        return Verdict.PASS;
    }

    /** 运维口径：被白名单拒掉的报文数量。 */
    public Map<String, Object> stats() {
        Map<String, Object> m = new HashMap<>();
        Map<String, String> snapshot = ledger;
        m.put("ledgerSize", snapshot == null ? -1 : snapshot.size());
        m.put("loadedAt", loadedAt);
        m.put("unknownDrops", unknownDrops.get());
        m.put("typeMismatchDrops", typeDrops.get());
        return m;
    }

    private Map<String, String> ensureLoaded() {
        Map<String, String> current = ledger;
        if (current != null && System.currentTimeMillis() - loadedAt < REFRESH_MS) {
            return current;
        }
        synchronized (this) {
            Map<String, String> reloaded = ledger;
            if (reloaded != null && System.currentTimeMillis() - loadedAt < REFRESH_MS) {
                return reloaded;
            }
            try {
                List<IotDevice> devices = mapper.selectList(
                        Wrappers.<IotDevice>lambdaQuery().eq(IotDevice::getEnabled, 1));
                Map<String, String> map = new HashMap<>(devices.size() * 2);
                for (IotDevice d : devices) {
                    if (d.getDeviceNo() != null && !d.getDeviceNo().isBlank()) {
                        map.put(d.getDeviceNo(), d.getDeviceType());
                    }
                }
                ledger = map;
                loadedAt = System.currentTimeMillis();
                return map;
            } catch (Exception e) {
                log.warn("设备白名单刷新失败，沿用上一次缓存（{} 条）：{}",
                        reloaded == null ? 0 : reloaded.size(), e.getMessage());
                return reloaded;
            }
        }
    }

    private void warnThrottled(String deviceNo, String message) {
        long now = System.currentTimeMillis();
        Long last = lastLogAt.get(deviceNo);
        if (last != null && now - last < LOG_INTERVAL_MS) {
            return;
        }
        lastLogAt.put(deviceNo, now);
        log.warn("{} {}", deviceNo, message);
    }
}
