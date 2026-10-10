package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.patient.enums.VtePreventStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预防措施登记/落实。
 */
@Data
public class VtePreventUpsertDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 入院ID
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 措施码
     */
    @NotBlank(message = "措施码不能为空")
    private String measureCode;

    /**
     * 措施名称
     */
    @Size(max = 200, message = "措施名称超长")
    private String measureName;

    /**
     * 计划执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝）
     */
    @NotNull(message = "落实状态不能为空")
    @InEnum(value = VtePreventStatusEnum.class, message = "落实状态取值不合法（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝）")
    private Integer executeStatus;

    /**
     * 落实时间；executeStatus=1 时为空则服务端取当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 未落实原因（禁忌/拒绝必填，服务端截到 500）
     */
    private String reason;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注超长")
    private String remark;
}
