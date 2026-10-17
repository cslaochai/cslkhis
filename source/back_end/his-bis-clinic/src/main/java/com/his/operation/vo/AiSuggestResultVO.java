package com.his.operation.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 排台结果：建议列表 + 无法排入的受阻列表。
 */
@Data
public class AiSuggestResultVO {

    /** 排台目标日期 */
    private String date;

    /** 单间单日营业分钟（口径同利用率：08:00~18:00 = 600） */
    private Long capacityMinutesPerRoom;

    /** 可排建议（按急诊优先、申请先后排序） */
    private List<AiSuggestionVO> suggestions = new ArrayList<>();

    /** 当日无可用空档的单（reason 说明原因） */
    private List<AiSuggestionVO> blocked = new ArrayList<>();
}
