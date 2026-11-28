package com.his.pharmacy.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 临床科室发起器械包回收申请（护士站 → 供应室）。
 *
 * <p>与 {@code CssdDTO.Receive} 的区别：Receive 是供应室坐台登记（直接落 1-已回收），
 * 这里是科室把「用完的器械要送回消毒供应中心」这件事先报上来，落 0-待回收，等供应室上门回收再确认。
 */
@Data
public class CssdPackApplyDTO implements Serializable {

    /** 器械包名称（从器械包模板目录选，也可临时新增） */
    @NotBlank(message = "器械包名称不能为空")
    private String packName;

    /** 申领/归属科室ID（不填取当前岗位科室） */
    private Long deptId;

    /** 科室名称快照 */
    private String deptName;

    /** 灭菌方式:1-高压蒸汽 2-环氧乙烷 3-低温等离子（选模板时自动带出，仍可由科室确认） */
    @NotNull(message = "请选择灭菌方式")
    @AllowedValues(values = {"1", "2", "3"}, message = "灭菌方式取值不合法")
    private Integer sterilizeMethod;

    /** 备注（器械数量、污染情况等交接信息） */
    private String remark;
}
