package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标本标签打印入参。
 * <p>补打原因不在 DTO 上加长度校验（§3）：超长由服务端截断，不把粘贴长说明变成请求失败。</p>
 */
@Data
public class SpecimenLabelPrintDTO {

    /**
     * 检验记录ID
     */
    @NotNull(message = "检验记录ID不能为空")
    private Long recordId;

    /**
     * 补打原因（第 2 次及以后必填，首次打印可空）
     */
    private String printReason;

    /**
     * 打印机名称（终端绑定，可空）
     */
    private String printerName;
}
