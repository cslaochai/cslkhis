package com.his.clinical.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病历草稿移交台账查询入参（管理侧）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmrHandoverPageQueryDTO extends PageParam {

    /**
     * 状态（1-待代补 2-已完成 3-已撤销）
     */
    private Integer status;

    /**
     * 承接人员工ID（按承接人筛）
     */
    private Long receiverId;

    /**
     * 关键词（单号/病历号/患者姓名）
     */
    private String keyword;
}
