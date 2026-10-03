package com.his.miniapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 患者留言提交。
 *
 * <p>不接收 patientId：归属由登录态决定，前端说了不算
 * （照抄 {@code MiniappEmrController} 的口径）。
 */
@Data
@Schema(name = "ServiceMessageUpsertDTO", description = "患者留言提交")
public class ServiceMessageUpsertDTO {

    /** 留言内容 */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容最多500字")
    private String content;

    /** 联系电话（留空则用建档手机号） */
    @Size(max = 20, message = "联系电话过长")
    private String contactPhone;

    /** 留言分类（同 sys_faq.category_code，可空） */
    @Size(max = 32, message = "分类编码过长")
    private String categoryCode;
}
