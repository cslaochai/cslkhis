package com.his.medicaltech.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工作站上传影像帧入参（文件本身走 multipart 的 file 部件，这里只带定位信息）
 */
@Data
public class ExamImageUploadDTO {

    /**
     * 单据类型（1-检查 2-检验）
     */
    @NotNull(message = "单据类型不能为空")
    @AllowedValues(values = {"1", "2"}, message = "单据类型取值不合法")
    private Integer bizType;

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单不能为空")
    private Long applyId;

    /**
     * 影像模态（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）
     */
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7"}, message = "影像模态取值不合法")
    private Integer modality;
}
