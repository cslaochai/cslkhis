package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.operation.entity.BizOperationCount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 手术清点单出参。
 *
 * <p>{@code can*} 由后端判定：三阶段的推进顺序是硬规则，
 * 前端只负责把按钮显出来，不在本地写"phase 小于某个值就禁用"这类判断 ——
 * 本地判断会掩盖后端规则的失效：真正的规则只有一处说了算。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationCountVO extends BizOperationCount {

    private String admissionNo;

    /** 患者编号 */
    private String patientNo;

    private String surgeonName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime plannedStartTime;

    private Integer operationStatus;

    private String operationStatusText;

    // 文案
    private String phaseText;
    /** 状态文本 */
    private String statusText;
    private String beforeResultText;
    private String closureResultText;
    private String finalResultText;

    /** 明细项集合 */
    private List<CountItemVO> items;

    private Integer itemCount;

    /** 术前总数（各明细术前数量之和，服务端复算，不累加存储列） */
    private Integer totalBefore;

    private Integer totalClosure;

    private Integer totalFinal;

    // 能力位
    private Boolean canCountBefore;
    private Boolean canCountClosure;
    private Boolean canCountFinal;
    private Boolean canAddItem;

    private String warningText;
}
