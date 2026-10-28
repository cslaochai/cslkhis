package com.his.outpatient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 急诊绿色通道设置入参——医生工作站「一键开通/变更绿色通道」用。
 * 与登记（BizEmergencyUpsertDTO.greenChannel）共用同一套 @AllowedValues 四值口径。
 */
@Data
public class EmergencyGreenChannelDTO {

    /** 急诊记录 id */
    @NotNull(message = "急诊记录 id 不能为空")
    private Long id;

    /** 绿色通道：胸痛中心 / 卒中中心 / 创伤中心 / 无 */
    @AllowedValues(values = {"胸痛中心", "卒中中心", "创伤中心", "无"}, message = "绿色通道取值不合法")
    private String greenChannel;
}
