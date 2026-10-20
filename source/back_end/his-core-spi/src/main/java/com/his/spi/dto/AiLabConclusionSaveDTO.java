package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检验解读结论写回入参：把解读出来的结论与建议落到对应的检验执行记录上。
 */
@Data
@NoArgsConstructor
public class AiLabConclusionSaveDTO {

    /**
     * 检验记录ID
     */
    private Long recordId;

    /**
     * 检验结论（覆盖写入）
     */
    private String conclusion;

    /**
     * 建议正文（为 null 时保留记录上原有的建议，不抹平）
     */
    private String suggestions;
}
