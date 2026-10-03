package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 会诊应答（接诊）入参：会诊方确认"我来做这次会诊"。
 *
 * <p>应答是**必须的一步**，不是可选动作：它把"申请方说有这件事"变成"会诊方承认要负责"，
 * 也是"未应答不可完成"这条铁律的物理载体。
 *
 * <p><b>刻意不接收接诊医生ID</b>：接诊人一律是当前登录用户 —— 系统不允许"替别人接诊"。
 * 允许代填，等于让任何人都能把一次会诊挂到别人名下，留痕当场失去意义
 * （与"死者的病历不许别人代签"同一类问题）。
 */
@Data
public class ConsultationAcceptDTO implements Serializable {

    /**
     * 会诊ID（必填）
     */
    @NotNull(message = "会诊ID不能为空")
    private Long consultationId;

    /**
     * 接诊说明（可空）
     */
    private String remark;
}
