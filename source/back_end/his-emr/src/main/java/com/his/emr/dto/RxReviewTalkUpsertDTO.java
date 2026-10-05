package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 医师约谈 upsert 入参。
 *
 * <p>关联点评明细（relatedReviewIds）必须同属一位医师且结论为不合理（2/3/4）；
 * doctorId/科室从明细推出。约谈人缺省取当前登录人。医师确认后禁改。
 */
@Data
public class RxReviewTalkUpsertDTO {

    /**
     * id 为空 = 新建；非空 = 修改（医师已确认的禁改）
     */
    private Long id;

    /**
     * 被约谈医师ID（有关联明细时从明细推出，入参值会被校验一致性）
     */
    private Long doctorId;

    /**
     * 被约谈医师姓名
     */
    @NotBlank(message = "被约谈医师姓名不能为空")
    private String doctorName;

    /**
     * 医师所在科室（可空，有关联明细时自动补）
     */
    private String deptName;

    /**
     * 约谈类型（1-首次约谈 2-警告约谈 3-限制处方权 4-取消处方权 5-恢复处方权）
     */
    @NotNull(message = "约谈类型不能为空")
    @Min(value = 1, message = "约谈类型非法")
    @Max(value = 5, message = "约谈类型非法")
    private Integer talkType;

    /**
     * 约谈时间
     */
    @NotNull(message = "约谈时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime talkTime;

    /**
     * 约谈人姓名（缺省当前登录人）
     */
    private String talkerName;

    /**
     * 约谈部门（医务科/药学部）
     */
    private String talkerOrg;

    /**
     * 关联点评明细ID（约谈依据；可空=独立约谈）
     */
    private List<Long> relatedReviewIds;

    /**
     * 问题摘要
     */
    private String problemSummary;

    /**
     * 约谈内容
     */
    private String talkContent;

    /**
     * 整改要求
     */
    private String rectifyRequire;

    /**
     * 整改状态（1-待整改 2-已整改）
     */
    @Min(value = 1, message = "整改状态非法")
    @Max(value = 2, message = "整改状态非法")
    private Integer rectifyStatus;

    /**
     * 整改情况说明
     */
    private String rectifyRemark;

    /**
     * 备注
     */
    private String remark;
}
