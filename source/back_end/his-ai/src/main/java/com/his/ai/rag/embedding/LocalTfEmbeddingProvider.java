package com.his.ai.rag.embedding;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 TF 向量 embedding（零外部依赖，开发环境默认，可直接跑通 RAG 全链路）。
 */
@Component
public class LocalTfEmbeddingProvider implements EmbeddingProvider {

    /**
     * 固定维度（hashing trick 目标维度，远程实现由模型决定维度，二者不混用）
     */
    private static final int DIM = 1024;

    private static final Pattern TOKEN_EN = Pattern.compile("[a-zA-Z0-9]+");

    private static boolean isCjk(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }

    @Override
    public String name() {
        return "local-tf";
    }

    @Override
    public float[] embed(String text) {
        float[] vec = new float[DIM];
        if (text == null || text.isEmpty()) {
            return vec;
        }
        Map<String, Float> tf = new HashMap<>();
        int total = 0;

        // 英文 / 数字整词
        Matcher m = TOKEN_EN.matcher(text);
        while (m.find()) {
            tf.merge(m.group().toLowerCase(), 1f, Float::sum);
            total++;
        }

        // 中文 unigram + bigram
        StringBuilder zh = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (isCjk(text.charAt(i))) {
                zh.append(text.charAt(i));
            }
        }
        String s = zh.toString();
        for (int i = 0; i < s.length(); i++) {
            tf.merge("zh:" + s.charAt(i), 1f, Float::sum);
            total++;
            if (i + 1 < s.length()) {
                tf.merge("zh2:" + s.substring(i, i + 2), 1f, Float::sum);
                total++;
            }
        }

        // 特征哈希到固定维度 + TF 加权
        for (Map.Entry<String, Float> e : tf.entrySet()) {
            int idx = Math.floorMod(e.getKey().hashCode(), DIM);
            vec[idx] += e.getValue() / total;
        }

        // L2 归一
        float n = 0f;
        for (float v : vec) {
            n += v * v;
        }
        n = (float) Math.sqrt(n);
        if (n > 0f) {
            for (int i = 0; i < vec.length; i++) {
                vec[i] /= n;
            }
        }
        return vec;
    }
}
