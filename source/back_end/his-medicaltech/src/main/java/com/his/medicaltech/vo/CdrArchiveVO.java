package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * CDR 里出现的"另一份档案"（EMPI 影子档案）。
 *
 * <p>用于告诉看页面的人：这条时间轴里有一部分数据其实挂在另一份档案号下。
 */
@Data
@Schema(description = "CDR 关联档案（影子）")
public class CdrArchiveVO {

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "档案ID")
    private Long patientId;

    /** 患者号 */
    @Schema(description = "患者号")
    private String patientNo;

    /** 患者姓名 */
    @Schema(description = "姓名")
    private String patientName;

    @Schema(description = "并入主档时间")
    private String mergeTime;

    @Schema(description = "该档案贡献的事件数")
    private Integer eventCount;
}
