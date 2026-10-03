package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编码任务查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodeTaskQueryPageDTO extends PageParam {

    /** 任务号 CT+yyyyMMdd+4位 */
    private String taskNo;

    /** 状态（1待编码 2已提交 3已完成 4已退修） */
    private Integer status;

    /** 编码人员工ID */
    private Long coderId;

    /** 关键词（任务号/病历号/患者姓名） */
    private String keyword;
}
