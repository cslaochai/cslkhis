package com.his.medicaltech.enums;

/**
 * 数据质量问题的严重度（P5.3）。
 */
public enum QualitySeverity {

    HIGH(3, "严重"),
    MEDIUM(2, "警告"),
    LOW(1, "提示");

    private final int level;
    private final String text;

    QualitySeverity(int level, String text) {
        this.level = level;
        this.text = text;
    }

    public static String textOf(Integer level) {
        if (level == null) {
            return "—";
        }
        for (QualitySeverity s : values()) {
            if (s.level == level) {
                return s.text;
            }
        }
        return "";
    }

    public int getLevel() {
        return level;
    }

    public String getText() {
        return text;
    }
}
