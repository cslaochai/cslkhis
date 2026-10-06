package com.his.medicaltech.support;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 12 导联心电波形合成器（sql/173）。
 *
 * <p>真实波形应由设备经 /collectWave 推送；这个合成器是设备对接就位前的联调/演示
 * 入口（/simulateWave）。它按「每导联一组 P/Q/R/S/T 峰值参数 + 心拍时序」合成 10 秒
 * 250Hz 采样，产出结构与设备导出 JSON 完全一致 —— 设备接进来换掉推送方，模型不动。
 *
 * <p>波形学上刻意不追求病理细节：四种节律（窦速/窦缓/房颤/室早）足够把
 * 「报告闸门」与「波形阅读」跑真 —— 房颤无 P 波且 RR 绝对不齐、室早宽大畸形
 * 且有代偿间歇，这两条在渲染出来的图上一眼可辨。
 */
@Component
public class EcgWaveSimulator {

    public static final int SAMPLE_RATE = 250;
    public static final double DURATION_SEC = 10.0;
    public static final int GAIN_MM_PER_MV = 10;
    public static final int PAPER_SPEED_MM_PER_S = 25;

    /**
     * 导联顺序（标准 12 导联）
     */
    private static final String[] LEADS = {
            "I", "II", "III", "aVR", "aVL", "aVF", "V1", "V2", "V3", "V4", "V5", "V6"
    };

    /**
     * 每导联波形参数：P / Q / R / S / T 峰值（mV）。
     * aVR 全倒置；V1~V2 呈 rS（S 深）；V4~V6 R 渐高 —— 大致还原正常心电轴下的形态。
     */
    private static final double[][] LEAD_AMP = {
            // P      Q       R       S       T
            {0.08, -0.02, 0.80, -0.10, 0.25},  // I
            {0.12, -0.03, 1.10, -0.12, 0.30},  // II
            {0.06, -0.02, 0.50, -0.10, 0.12},  // III
            {-0.10, 0.02, -0.90, 0.10, -0.25}, // aVR
            {0.04, -0.01, 0.40, -0.15, 0.15},  // aVL
            {0.10, -0.02, 0.90, -0.10, 0.25},  // aVF
            {0.04, 0.00, 0.30, -1.00, 0.10},   // V1
            {0.05, 0.00, 0.50, -1.30, 0.35},   // V2
            {0.06, 0.00, 0.90, -0.80, 0.50},   // V3
            {0.08, -0.02, 1.50, -0.40, 0.45},  // V4
            {0.08, -0.03, 1.30, -0.15, 0.35},  // V5
            {0.07, -0.03, 1.00, -0.05, 0.25},  // V6
    };

    /**
     * 每秒心跳数（按节律）
     */
    private static final Map<Integer, double[]> RHYTHM = Map.of(
            1, new double[]{75, 1.0, 1.0},    // 窦性心律 75bpm
            2, new double[]{115, 1.0, 1.0},   // 窦性心动过速
            3, new double[]{48, 1.0, 1.0},    // 窦性心动过缓
            4, new double[]{88, 0.72, 1.28},  // 房颤：RR 在 0.72~1.28 倍间漂移
            5, new double[]{75, 1.0, 1.0}     // 室早：基础 75bpm + 每 4 拍插入
    );

    private static final String[] RHYTHM_NAME = {
            null, "窦性心律", "窦性心动过速", "窦性心动过缓", "心房颤动", "室性早搏"
    };

    /**
     * 高斯波形：amp * exp(-(dt-mu)^2 / (2 sigma^2))
     */
    private static double gauss(double dt, double mu, double sigma, double amp) {
        double d = dt - mu;
        return amp * Math.exp(-(d * d) / (2 * sigma * sigma));
    }

    /**
     * 合成 12 导联波形 JSON。
     *
     * @param rhythmCode 字典 his_ecg_rhythm（1~5）
     */
    public String simulate(int rhythmCode) {
        Random rnd = new Random(rhythmCode * 7919L);
        int n = (int) Math.round(SAMPLE_RATE * DURATION_SEC);

        double baseBpm = RHYTHM.get(rhythmCode)[0];
        double rrMin = RHYTHM.get(rhythmCode)[1];
        double rrMax = RHYTHM.get(rhythmCode)[2];

        // 心拍时序（秒）：每拍的 QRS 起点 + 类型（0=窦 1=室早）
        List<double[]> beats = new ArrayList<>();
        double t = 0.15;
        while (t < DURATION_SEC - 0.3) {
            boolean pvc = rhythmCode == 5 && beats.size() % 4 == 3;
            beats.add(new double[]{t, pvc ? 1 : 0});
            double rr = 60.0 / baseBpm;
            if (rhythmCode == 4) {
                rr *= rrMin + rnd.nextDouble() * (rrMax - rrMin);
            }
            // 室早后代偿间歇
            if (pvc) {
                rr *= 1.6;
            }
            t += rr;
        }

        List<double[]> all = new ArrayList<>();
        for (int li = 0; li < LEADS.length; li++) {
            double[] amp = LEAD_AMP[li];
            double[] s = new double[n];
            for (int i = 0; i < n; i++) {
                double time = i / (double) SAMPLE_RATE;
                double v = 0;
                for (double[] b : beats) {
                    v += beatWave(time - b[0], b[1] == 1, amp, rhythmCode == 4);
                }
                // 基线噪声（肌电/呼吸漂移的极小模拟），房颤加 f 波
                v += (rnd.nextDouble() - 0.5) * 0.015;
                if (rhythmCode == 4) {
                    v += Math.sin(2 * Math.PI * 6.0 * time + li) * 0.04;
                }
                s[i] = Math.round(v * 10000.0) / 10000.0;
            }
            all.add(s);
        }

        StringBuilder sb = new StringBuilder(1 << 20);
        sb.append("{\"sampleRate\":").append(SAMPLE_RATE)
                .append(",\"durationSec\":").append(DURATION_SEC)
                .append(",\"gainMmPerMv\":").append(GAIN_MM_PER_MV)
                .append(",\"paperSpeedMmPerS\":").append(PAPER_SPEED_MM_PER_S)
                .append(",\"rhythmName\":\"").append(RHYTHM_NAME[rhythmCode]).append('"')
                .append(",\"leads\":[");
        for (int li = 0; li < LEADS.length; li++) {
            if (li > 0) {
                sb.append(',');
            }
            sb.append("{\"name\":\"").append(LEADS[li]).append("\",\"samples\":[");
            double[] s = all.get(li);
            for (int i = 0; i < s.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(s[i]);
            }
            sb.append("]}");
        }
        // 节律条：取导联 II 全长 10 秒（设备端惯例是加一条长 II 导联）
        sb.append("],\"rhythm\":{\"name\":\"II\",\"samples\":[");
        double[] s2 = all.get(1);
        for (int i = 0; i < s2.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(s2[i]);
        }
        sb.append("]}}");
        return sb.toString();
    }

    /**
     * 单个心拍在导联上的电位贡献（时间差 dt 秒；pvc=室早形态；afib=无 P 波）
     */
    private double beatWave(double dt, boolean pvc, double[] amp, boolean afib) {
        if (dt < -0.1 || dt > 0.7) {
            return 0;
        }
        if (pvc) {
            // 室早：无 P，宽大畸形 QRS（0.14s），T 与 QRS 主波方向相反
            return gauss(dt, 0.00, 0.035, amp[2] * 1.45)
                    + gauss(dt, 0.075, 0.035, amp[3] * 0.9)
                    - gauss(dt, 0.30, 0.085, amp[4] * 1.6);
        }
        double v = 0;
        if (!afib) {
            v += gauss(dt, 0.06, 0.022, amp[0]);                    // P
        }
        v += gauss(dt, 0.155, 0.010, amp[1]);                       // Q
        v += gauss(dt, 0.20, 0.012, amp[2]);                        // R
        v += gauss(dt, 0.243, 0.012, amp[3]);                       // S
        v += gauss(dt, 0.40, 0.050, amp[4]);                        // T
        return v;
    }
}
