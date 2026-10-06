package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 撤销已上传结算清单入参（G7 报盘撤销）
 */
@Data
public class CancelUploadDTO {

    /**
     * 结算清单ID
     */
    @NotNull(message = "结算清单ID不能为空")
    private Long id;

    /**
     * 撤销原因（写入报文台账 remark，列宽 500，前端限 200）
     */
    @Size(max = 200, message = "撤销原因不能超过200字")
    private String reason;
}
