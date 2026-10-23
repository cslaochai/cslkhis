package com.his.outpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 改派结果VO：前端据此提示护士「已改派到 X 医生 / Y 诊室，新排队号 Z，当前排第 N 位」。
 */
@Data
@Schema(description = "改派结果")
public class QueueReassignVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "队列ID")
    private Long queueId;

    /**
     * 改派后的排队号（换诊室时按新诊室前缀重新生成）
     */
    @Schema(description = "排队号")
    private String queueNo;

    /**
     * 改派后的顺序号（目标医生候诊队尾 + 1）
     */
    @Schema(description = "顺序号")
    private Integer sequenceNo;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "目标医生ID")
    private Long doctorId;

    @Schema(description = "目标医生姓名")
    private String doctorName;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "目标诊室ID")
    private Long roomId;

    @Schema(description = "目标诊室名称")
    private String roomName;
}
