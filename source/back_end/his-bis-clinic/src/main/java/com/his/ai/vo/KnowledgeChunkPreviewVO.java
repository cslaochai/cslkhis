package com.his.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 知识文档切块预览出参（只按当前切块参数计算，不落库不灌向量）。
 */
@Data
public class KnowledgeChunkPreviewVO {

    /** 切块数量 */
    private Integer chunkCount;

    /** 原文总字符数 */
    private Integer totalChars;

    /** 当前切块参数：单块字符数 */
    private Integer chunkSize;

    /** 当前切块参数：块间重叠字符数 */
    private Integer chunkOverlap;

    /** 每块开头预览（每块最多 200 字符，避免大文本回传） */
    private List<String> chunks;
}
