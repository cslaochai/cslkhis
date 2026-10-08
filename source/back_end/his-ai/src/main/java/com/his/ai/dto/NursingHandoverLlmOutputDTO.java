package com.his.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 护理交接班摘要模型输出（G-13）。
 */
@Data
public class NursingHandoverLlmOutputDTO {

    /**
     * SBAR 式交班摘要（≤400 字，禁诊断结论与处置医嘱）
     */
    private String summary;

    /**
     * 重点关注条目：直接引用输入事实里已有的「床号+患者」标识，不许发明新患者
     */
    private List<String> focus;
}
