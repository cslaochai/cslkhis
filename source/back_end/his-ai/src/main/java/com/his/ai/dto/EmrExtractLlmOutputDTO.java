package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 病历文本结构化抽取的模型输出结构（与 prompts/emr-extract.md 的 JSON 契约一一对应）。
 * <p>
 * 刻意**不含体征字段**：体温/脉搏/呼吸/血压由 {@code EmrFieldCatalog} 的正则提取，
 * 不让模型经手 —— 数值一旦被模型"顺"一下，就是一条错误记录。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmrExtractLlmOutputDTO {

    private List<Field> fields = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Field {

        /**
         * 字段 key，必须落在 EmrFieldCatalog 白名单内
         */
        private String field;

        /**
         * 字段值（尽量保留原文措辞）
         */
        private String value;

        /**
         * 原文片段，用于回原文做子串比对
         */
        private String evidence;
    }
}
