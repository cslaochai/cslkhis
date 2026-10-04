package com.his.ai.rag.embedding;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 TF 向量 embedding（零外部依赖，开发环境默认，可直接跑通 RAG 全链路）。
 *
 * <p>分词策略：
 * <ul>
 *   <li>英文 / 数字：按 {@code [a-zA-Z0-9]+} 整词提取（小写）；</li>
 *   <li>中文：按字符 unigram（{@code zh:字}）+ 相邻 bigram（{@code zh2:字词}），覆盖「血常规」「空腹」等短语；</li>
 * </ul>
 * 词频（TF）经<b>特征哈希（hashing trick）</b>落到固定 {@link #DIM} 维 float 数组，再 L2 归一。
 * 这样输出是定长稠密向量，与远程 embedding 同构，内存库 / 将来 Milvus 都能直接用。
 *
 * <p><b>局限（刻意的）：</b>哈希碰撞会让「字面相近」的召回足够用，但<b>不是语义召回</b>——
 * 同义词（「发烧」vs「发热」）不会自动对齐。需要语义对齐的（药品别名、同义词缩写）走结构化知识表，不靠向量。
 * 真正要语义检索时，把 {@code ai.rag.embedding-provider} 切到 {@code remote} 即可。
 */
@Component
public class LocalTfEmbeddingProvider implements EmbeddingProvider {

    /**
     * 固定维度（hashing trick 目标维度，远程实现由模型决定维度，二者不混用）
     */
    private static final int DIM = 1024;

    private static final Pattern TOKEN_EN = Pattern.compile("[a-zA-Z0-9]+");

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

    private static boolean isCjk(char c) {
        return c >= 0x4E00 && c <= 0x9FFF;
    }
}
