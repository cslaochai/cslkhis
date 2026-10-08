package com.his.appoint.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分诊卡回显：当前生效值 + 完整历史。
 */
@Data
@Schema(description = "分诊详情（当前值 + 历史）")
public class TriageDetailVO {

    @Schema(description = "当前生效的分诊记录，未分诊时为 null")
    private TriageRecordVO latest;

    @Schema(description = "历史分诊记录，按时间倒序")
    private List<TriageRecordVO> history = new ArrayList<>();
}
