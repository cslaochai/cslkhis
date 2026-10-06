package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟 DICOM 导入入参（学习阶段不接真设备，由后端现画几帧灰阶测试图）
 */
@Data
public class ExamImageMockImportDTO {

    /**
     * 影像来源单据类型
     */
    @NotNull(message = "单据类型不能为空")
    private Integer bizType;

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单不能为空")
    private Long applyId;

    /**
     * 影像模态（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）
     */
    private Integer modality;

    /**
     * 导入帧数，缺省 6；上限由服务端兜（一次导入几十帧会把工作站渲染拖死）
     */
    private Integer frameCount;
}
