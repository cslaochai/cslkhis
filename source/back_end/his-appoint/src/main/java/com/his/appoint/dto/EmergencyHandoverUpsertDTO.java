package com.his.appoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 急诊交班提交入参
 */
@Data
public class EmergencyHandoverUpsertDTO {

    /**
     * 交班科室（不传 = 当前登录岗位所在科室）。
     * 急诊台/护士长一次交多个急诊科室时逐科提交，每科一张单，
     * 因为台账上的计数与责任人是按科室算的，混在一张单上就说不清"谁科的池子被清了"。
     */
    private Long deptId;

    /**
     * 整单接班人（明细未单独指定接续医生时归他）
     */
    @NotNull(message = "请选择接班人")
    private Long takeEmpId;

    /**
     * 整单交代备注
     */
    private String remark;

    /**
     * 逐条点名的明细：必须完整覆盖本科室当前「该我负责 + 无人负责」的未闭环急诊，
     * 漏一条服务端直接拒绝（清零的含义就在这里）。
     */
    @Valid
    @NotEmpty(message = "待交班清单不能为空")
    private List<EmergencyHandoverItemDTO> items;
}
