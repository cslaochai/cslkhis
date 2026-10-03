package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 入 PACU 登记入参。
 *
 * <p>必须先有已提交/已审核的麻醉记录：PACU 是"麻醉的一段延续"，
 * 没有麻醉记录却有一段 PACU 停留，等于凭空出现一节监护。
 */
@Data
public class PacuEnterDTO implements Serializable {

    /** 麻醉记录ID */
    @NotNull(message = "麻醉记录单ID不能为空")
    private Long recordId;

    /** 复苏护士ID（员工ID，姓名服务端查） */
    private Long nurseId;

    /** 负责麻醉医师ID（员工ID） */
    private Long anesthetistId;

    /** 备注 */
    private String remark;
}
