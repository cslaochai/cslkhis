package com.his.emr.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** 公示入参：只允许公示"已点评且结论 2/3/4"的明细；公示只增不可撤 */
@Data
public class RxReviewPublicityDTO {

    @NotEmpty(message = "请选择要公示的点评明细")
    private List<Long> itemIds;
}
