package com.his.appoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交班明细入参（一条 = 一个「这个人现在归你」）。
 */
@Data
public class EmergencyHandoverItemDTO {

    /**
     * 急诊记录ID
     */
    @NotNull(message = "急诊记录不能为空")
    private Long emergencyId;

    /**
     * 接续责任人（不传则归整单接班人）
     */
    private Long takeDoctorId;

    /**
     * 去向/处置交代（必填：交班的核心信息就是"下一步该干什么"）
     */
    @NotBlank(message = "请填写去向/处置交代")
    private String disposition;

    /**
     * 逐条补充交代（过敏史/管路/家属联系方式等，截到 300）
     */
    private String handoverNote;
}
