package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 按膳食方案批量生成订餐（营养科/食堂侧）。
 */
@Data
public class MealGenerateDTO {

    @NotNull(message = "就餐日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mealDate;

    /** 按病区批量生成（食堂按病区送饭）；为空 = 全部病区 */
    private List<Long> wardIds;

    /** 只给这几个住院患者生成（膳食方案页单人补生成）；与 wardIds 二选一 */
    private List<Long> admissionIds;

    /** 是否覆盖重生成（true 时先物理清该日未配送行） */
    private Boolean overwrite;
}
