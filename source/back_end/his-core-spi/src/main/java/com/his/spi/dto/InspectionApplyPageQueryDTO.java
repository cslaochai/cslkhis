package com.his.spi.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 检查申请单跨域分页查询入参：按状态档位、患者、急诊标志与就诊日期区间筛申请单。
 *
 * <p>计数场景复用同一份筛选条件（分页参数不参与计数），避免两条近义入参各写一遍口径。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionApplyPageQueryDTO extends PageParam {

    /**
     * 申请状态集合（命中其中任一档才算；空 = 不限状态）
     */
    private List<Integer> applyStatusIn;

    /**
     * 排除的申请单ID（调用方按自己域的事实算出「不该再出现在这个列表里」的单；空 = 不排除）
     */
    private List<Long> excludeApplyIds;

    /**
     * 关键字：申请单号 / 患者姓名 / 检查项目名任一命中
     */
    private String keyword;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 是否急诊（1-是 0-否）
     */
    private Integer isEmergency;

    /**
     * 就诊日期区间起（含当日）
     */
    private LocalDate startDate;

    /**
     * 就诊日期区间止（含当日）
     */
    private LocalDate endDate;
}
