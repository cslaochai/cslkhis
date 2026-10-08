package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 麻醉随访新增/修改入参（id 为空 = 新增草稿；不为空 = 修改，仅允许「草稿」）。
 */
@Data
public class AnesthesiaFollowupUpsertDTO implements Serializable {

    /**
     * 随访单ID（为空 = 新增；不为空 = 修改草稿）
     */
    private Long id;

    /**
     * 麻醉记录ID（必填；必须已提交/已审核才能随访）
     */
    @NotNull(message = "麻醉记录ID不能为空（随访必须挂在一次麻醉上）")
    private Long recordId;

    /**
     * 随访时间（必填；不得早于麻醉结束时间）
     */
    @NotNull(message = "随访时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followupTime;

    /**
     * 疼痛评分 NRS 0~10（草稿可空，完成必填）
     */
    @Min(value = 0, message = "疼痛评分不能小于0")
    @Max(value = 10, message = "疼痛评分不能大于10")
    private Integer painScore;

    /**
     * 麻醉恢复情况：1-良好 2-一般 3-差（草稿可空，完成必填）
     */
    @Min(value = 1, message = "恢复情况取值不合法（1-良好 2-一般 3-差）")
    @Max(value = 3, message = "恢复情况取值不合法（1-良好 2-一般 3-差）")
    private Integer recovery;

    /**
     * 并发症码值（逗号分隔，如 "1,3"；无并发症留空）
     */
    @Size(max = 200, message = "并发症码值串过长")
    private String adverseItems;

    /**
     * 并发症经过描述（勾选任何并发症时必填）
     */
    private String adverseNote;

    /**
     * 处理措施与转归（有并发症时必填）
     */
    private String handling;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}
