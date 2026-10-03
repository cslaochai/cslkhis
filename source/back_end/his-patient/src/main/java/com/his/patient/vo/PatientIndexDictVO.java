package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * EMPI 字典出参（匹配级别 + 档案关键字段清单）。
 *
 * <p>放服务端而不是前端硬编码：级别文案与关键字段清单必须与后端的完整度口径同源，
 * 前端各存一份必然漂移（改了口径页面还在显示老文案）。
 */
@Data
public class PatientIndexDictVO implements Serializable {

    /** 匹配级别清单 */
    private List<PatientMatchLevelSelectListVO> matchLevels;

    /**
     * 关键字段的"字段名 → 中文名"对照。
     *
     * <p>刻意保持对象形状而不是裁成列表：前端按字段名直接取标签
     * （{@code Record<string, string>}），改成数组会让所有取值点变成查找。
     */
    private Map<String, String> profileFields;
}
