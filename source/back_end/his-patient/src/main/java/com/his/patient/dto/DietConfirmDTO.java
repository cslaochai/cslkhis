package com.his.patient.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 营养科接收/退回膳食方案（批量，全成功或全不生效）。
 *
 * <p>接收才是"膳食医嘱开始执行"：医生开了医嘱而营养科没接单，患者一顿饭都吃不上，
 * 所以膳食医嘱执行率的分子只数 confirm_status=1。
 * 退回必须带原因（如"该患者需先重评 NRS2002""饮食类型与病情不符"），
 * 退回去让开嘱医师改，而不是营养师顺手把医嘱改了 —— 改医嘱是医师的权限。
 */
@Data
public class DietConfirmDTO {

    /** 主键ID集合 */
    @NotEmpty(message = "请选择要处理的膳食方案")
    private List<Long> ids;

    /** true-接收 false-退回 */
    @NotNull(message = "接收/退回不能为空")
    private Boolean accept;

    /** 退回原因（accept=false 必填） */
    private String rejectReason;
}
