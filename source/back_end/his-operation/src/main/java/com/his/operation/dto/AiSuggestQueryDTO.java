package com.his.operation.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 排台建议入参：目标日期 + 可选手术间集合（空=全部启用手术间）。
 */
@Data
public class AiSuggestQueryDTO {

    /**
     * 排台目标日期（yyyy-MM-dd，空按今天）
     */
    private String date;

    /**
     * 限定手术间 id 集合（空=全部启用手术间）
     */
    private List<Long> roomIds;
}
