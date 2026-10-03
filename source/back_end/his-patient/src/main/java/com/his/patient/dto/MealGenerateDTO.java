package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 按膳食方案批量生成订餐（营养科/食堂侧）。
 *
 * <p>生成规则全部在服务端：只取 plan_status=1-执行中且 route=1-口服的方案，
 * 按方案 meal_types 拆成一日多餐；管饲与肠外营养不进订餐。
 *
 * <p>{@code overwrite}：uk_meal_order(admission_id, meal_date, meal_type) 不含 del_flag，
 * 重生成必须先物理删旧行 —— 但只删「还没配送」的行（0-待配餐/1-已配餐），
 * 已配送/已签收的餐是既成事实，不许被一次点击抹掉，那些患者当天就跳过不重生成。
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
