package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 患者留言提交。
 */
@Data
@Schema(name = "ServiceMessageUpsertDTO", description = "患者留言提交")
public class ServiceMessageUpsertDTO {

    /**
     * 留言内容
     */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容最多500字")
    private String content;

    /**
     * 联系电话（留空则用建档手机号）
     */
    @Size(max = 20, message = "联系电话过长")
    private String contactPhone;

    /**
     * 留言分类（同 sys_faq.category_code，可空）
     */
    @Size(max = 32, message = "分类编码过长")
    private String categoryCode;
}
