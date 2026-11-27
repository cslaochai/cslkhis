package com.his.iot.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监护波形内存缓冲：只留最近一段滚动窗，不落 TDengine。
 *
 * <p>口径与真实监护网关一致：心率/血压/血氧/呼吸/体温这些离散数值按时序库归档；
 * ECG、脉搏波、呼吸波是高频连续曲线，归档没有临床价值还会把时序库撑爆，因此只在内存留
 * 4 秒心电/脉搏波、8 秒呼吸波，供护士站大屏轮询取窗重绘。服务重启缓冲清空，不影响历史数值。
 *
 * <p>设备号为键；MQTT 回调串行到达，查询线程并发读取，每台设备一把锁保护环形数组。
 */
@Slf4j
@Component
public class WaveformBuffer {

    /** ECG/脉搏波：4 秒 × 100Hz */
    private static final int ECG_CAP = 400;
    /** 呼吸波：8 秒 × 20Hz */
    private static final int RESP_CAP = 160;

    private final Map<String, DeviceWave> waves = new ConcurrentHashMap<>();

    /**
     * 追加一批波形采样。任一通道为空就跳过该通道，不影响其余通道。
     */
    public void offer(String deviceNo, List<Double> ecg, List<Double> pleth, List<Double> resp,
                      String rhythm, Integer hr, Integer rr, String ts) {
        if (deviceNo == null || deviceNo.isBlank()) {
            return;
        }
        waves.computeIfAbsent(deviceNo, DeviceWave::new)
                .append(ecg, pleth, resp, rhythm, hr, rr, ts);
    }

    /** 单台设备最近波形窗；缓冲里没有该设备返回 null。 */
    public Map<String, Object> snapshot(String deviceNo) {
        DeviceWave w = waves.get(deviceNo);
        return w == null ? null : w.snapshot();
    }

    /** 批量取窗：传入设备编号清单只返回缓冲里存在的设备；清单为空返回全部在缓冲中的设备。 */
    public List<Map<String, Object>> snapshotBatch(List<String> deviceNos) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (deviceNos == null || deviceNos.isEmpty()) {
            for (DeviceWave w : waves.values()) {
                out.add(w.snapshot());
            }
            return out;
        }
        for (String no : deviceNos) {
            if (no == null || no.isBlank()) {
                continue;
            }
            Map<String, Object> snap = snapshot(no);
            if (snap != null) {
                out.add(snap);
            }
        }
        return out;
    }

    private static final class DeviceWave {
        private final String deviceNo;
        private final double[] ecg = new double[ECG_CAP];
        private final double[] pleth = new double[ECG_CAP];
        private final double[] resp = new double[RESP_CAP];
        private int ecgIdx;
        private int plethIdx;
        private int respIdx;
        private int ecgCount;
        private int plethCount;
        private int respCount;
        private String rhythm;
        private Integer hr;
        private Integer rr;
        private String ts;

        private DeviceWave(String deviceNo) {
            this.deviceNo = deviceNo;
        }

        private synchronized void append(List<Double> e, List<Double> p, List<Double> r,
                                         String rhythm, Integer hr, Integer rr, String ts) {
            if (e != null) {
                for (Double v : e) {
                    ecg[ecgIdx] = v == null ? 0 : v;
                    ecgIdx = (ecgIdx + 1) % ECG_CAP;
                    ecgCount++;
                }
            }
            if (p != null) {
                for (Double v : p) {
                    pleth[plethIdx] = v == null ? 0 : v;
                    plethIdx = (plethIdx + 1) % ECG_CAP;
                    plethCount++;
                }
            }
            if (r != null) {
                for (Double v : r) {
                    resp[respIdx] = v == null ? 0 : v;
                    respIdx = (respIdx + 1) % RESP_CAP;
                    respCount++;
                }
            }
            this.rhythm = rhythm;
            this.hr = hr;
            this.rr = rr;
            this.ts = ts;
        }

        private synchronized Map<String, Object> snapshot() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("deviceNo", deviceNo);
            m.put("rhythm", rhythm);
            m.put("hr", hr);
            m.put("rr", rr);
            m.put("ts", ts);
            m.put("ecg", drain(ecg, ecgCount, ECG_CAP));
            m.put("pleth", drain(pleth, plethCount, ECG_CAP));
            m.put("resp", drain(resp, respCount, RESP_CAP));
            m.put("ecgHz", 100);
            m.put("waveHz", 20);
            return m;
        }

        /** 从下一个写槽位绕一圈，即时间顺序；未写满时前段为初始 0。 */
        private static List<Double> drain(double[] buf, int count, int cap) {
            List<Double> out = new ArrayList<>(cap);
            int writeIdx = count < cap ? count : count % cap;
            for (int k = 0; k < cap; k++) {
                int pos = (writeIdx + k) % cap;
                out.add(Math.round(buf[pos] * 1000.0) / 1000.0);
            }
            return out;
        }
    }
}
