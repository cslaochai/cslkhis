package com.his.ai.support;

import com.his.patient.vo.NursingVitalFactVO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 病情恶化评分规则（MEWS 主体 + NEWS 的 SpO2 分档，G-12）。
 * <p>
 * <b>评分是事实层，全部代码算</b>：分档表固化如下，预警级只由总分决定，模型无权改。
 * MEWS 标准五参数里的「意识（AVPU）」没有结构化字段，本实现不含该项 ——
 * 宁可少评一项也不猜意识状态，注释留痕防止后人"补全"成拍脑袋分档。
 * </p>
 */
public final class DeteriorationScoreRules {

    /**
     * 关注阈值：≥4 提示关注（MEWS 常用预警线）
     */
    public static final int WATCH_SCORE = 4;
    /**
     * 高危阈值：≥6 提示高危（MEWS ≥5~6 常对应需评估升级处置）
     */
    public static final int CRITICAL_SCORE = 6;
    /**
     * 无体征数据时的占位总分
     */
    public static final int NO_DATA_SCORE = -1;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private DeteriorationScoreRules() {
    }

    /**
     * 对一条体征行评分。行内没有任何体征值时 totalScore=NO_DATA（不预警也不给建议）。
     */
    public static DeteriorationScore score(NursingVitalFactVO vital) {
        DeteriorationScore s = new DeteriorationScore();
        if (vital == null
                || (vital.getSystolicPressure() == null && vital.getPulse() == null
                && vital.getRespiration() == null && vital.getTemperature() == null
                && vital.getSpo2() == null)) {
            s.setTotalScore(NO_DATA_SCORE);
            s.setNoData(true);
            return s;
        }
        Integer sbp = systolicScore(vital.getSystolicPressure());
        Integer pulse = pulseScore(vital.getPulse());
        Integer rr = respirationScore(vital.getRespiration());
        Integer temp = temperatureScore(vital.getTemperature());
        Integer spo2 = spo2Score(vital.getSpo2());
        s.setSystolicPressure(sbp);
        s.setPulse(pulse);
        s.setRespiration(rr);
        s.setTemperature(temp);
        s.setSpo2(spo2);
        s.setTotalScore(nz(sbp) + nz(pulse) + nz(rr) + nz(temp) + nz(spo2));
        return s;
    }

    /**
     * 预警级：0-未触发 1-关注 2-高危
     */
    public static int alertLevel(int totalScore) {
        if (totalScore >= CRITICAL_SCORE) {
            return 2;
        }
        if (totalScore >= WATCH_SCORE) {
            return 1;
        }
        return 0;
    }

    /**
     * 分项明细（评分表渲染 + 提示词事实共用一份口径），未测量的项不出现。
     */
    public static List<ScoreItem> items(NursingVitalFactVO vital) {
        List<ScoreItem> items = new ArrayList<>();
        if (vital == null) {
            return items;
        }
        Integer sbp = systolicScore(vital.getSystolicPressure());
        if (sbp != null) {
            items.add(new ScoreItem("收缩压", vital.getSystolicPressure() + "mmHg", sbp));
        }
        Integer pulse = pulseScore(vital.getPulse());
        if (pulse != null) {
            items.add(new ScoreItem("脉搏", vital.getPulse() + "次/分", pulse));
        }
        Integer rr = respirationScore(vital.getRespiration());
        if (rr != null) {
            items.add(new ScoreItem("呼吸", vital.getRespiration() + "次/分", rr));
        }
        Integer temp = temperatureScore(vital.getTemperature());
        if (temp != null) {
            items.add(new ScoreItem("体温", vital.getTemperature() + "℃", temp));
        }
        Integer spo2 = spo2Score(vital.getSpo2());
        if (spo2 != null) {
            items.add(new ScoreItem("血氧", vital.getSpo2() + "%", spo2));
        }
        return items;
    }

    /**
     * 体征越阈事件（交接班聚合用）：只报事实不解释，阈值与评分分档同源本类。
     */
    public static List<String> abnormalEvents(List<NursingVitalFactVO> rows) {
        List<String> events = new ArrayList<>();
        for (NursingVitalFactVO r : rows) {
            List<String> marks = new ArrayList<>();
            if (r.getTemperature() != null && r.getTemperature().compareTo(new BigDecimal("38.5")) >= 0) {
                marks.add("体温 " + r.getTemperature() + "℃（≥38.5）");
            }
            if (r.getSystolicPressure() != null && r.getSystolicPressure() <= 90) {
                marks.add("收缩压 " + r.getSystolicPressure() + "mmHg（≤90）");
            }
            if (r.getPulse() != null && (r.getPulse() >= 120 || r.getPulse() <= 50)) {
                marks.add("脉搏 " + r.getPulse() + "次/分（≥120 或 ≤50）");
            }
            if (r.getRespiration() != null && r.getRespiration() >= 24) {
                marks.add("呼吸 " + r.getRespiration() + "次/分（≥24）");
            }
            if (r.getSpo2() != null && r.getSpo2() <= 93) {
                marks.add("血氧 " + r.getSpo2() + "%（≤93）");
            }
            if (marks.isEmpty()) {
                continue;
            }
            String who = (r.getBedNo() == null ? "" : r.getBedNo() + "床 ")
                    + (r.getPatientName() == null ? "" : r.getPatientName() + " ");
            String when = r.getMeasureTime() == null ? "" : "（" + r.getMeasureTime().toLocalDate() + " "
                    + r.getMeasureTime().format(CLOCK) + "）";
            events.add(who + String.join("、", marks) + when);
        }
        return events;
    }

    // ---- 分项分档（一处一表，注释即口径）----

    private static Integer systolicScore(Integer sbp) {
        if (sbp == null) return null;
        if (sbp <= 70) return 3;
        if (sbp <= 80) return 2;
        if (sbp <= 100) return 1;
        if (sbp <= 199) return 0;
        return 2;
    }

    private static Integer pulseScore(Integer pulse) {
        if (pulse == null) return null;
        if (pulse < 40) return 2;
        if (pulse <= 50) return 1;
        if (pulse <= 100) return 0;
        if (pulse <= 110) return 1;
        if (pulse <= 129) return 2;
        return 3;
    }

    private static Integer respirationScore(Integer rr) {
        if (rr == null) return null;
        if (rr < 9) return 2;
        if (rr <= 14) return 0;
        if (rr <= 20) return 1;
        if (rr <= 29) return 2;
        return 3;
    }

    private static Integer temperatureScore(BigDecimal temp) {
        if (temp == null) return null;
        if (temp.compareTo(new BigDecimal("35")) < 0) return 2;
        if (temp.compareTo(new BigDecimal("38.5")) < 0) return 0;
        return 2;
    }

    /**
     * SpO2 沿用 NEWS 分档（MEWS 原版无此项，血氧是护理监测的常规项）
     */
    private static Integer spo2Score(Integer spo2) {
        if (spo2 == null) return null;
        if (spo2 < 91) return 3;
        if (spo2 <= 93) return 2;
        if (spo2 <= 95) return 1;
        return 0;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 单患者评分结果（分项 + 总分 + 预警级），由 VO 透传给前端与提示词
     */
    @Getter
    @Setter
    public static class DeteriorationScore {
        private Integer systolicPressure;
        private Integer pulse;
        private Integer respiration;
        private Integer temperature;
        private Integer spo2;
        private int totalScore;
        private boolean noData;
    }

    /**
     * 分项明细行
     */
    public record ScoreItem(String name, String valueText, int score) {
    }
}
