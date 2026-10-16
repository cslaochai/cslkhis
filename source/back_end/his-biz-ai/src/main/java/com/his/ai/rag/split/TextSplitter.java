package com.his.ai.rag.split;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本切块（滑动窗口，带重叠）。
 */
@Component
public class TextSplitter {

    /**
     * 将长文切成若干重叠窗口。
     *
     * @param text      原文
     * @param chunkSize 单块最大字符数
     * @param overlap   相邻块重叠字符数（建议 chunkSize 的 10%~20%）
     * @return 切块列表（每块已 trim，空块已剔除）
     */
    public List<String> split(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        String clean = text.replace("\r\n", "\n").trim();
        if (clean.length() <= chunkSize) {
            chunks.add(clean);
            return chunks;
        }
        int step = Math.max(1, chunkSize - overlap);
        for (int i = 0; i < clean.length(); i += step) {
            int end = Math.min(clean.length(), i + chunkSize);
            String piece = clean.substring(i, end).trim();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
        }
        return chunks;
    }
}
