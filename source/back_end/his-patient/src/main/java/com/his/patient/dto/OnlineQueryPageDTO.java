package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 线上问诊分页查询入参（listPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OnlineQueryPageDTO extends PageParam implements Serializable {

    /**
     * 单号/患者姓名模糊
     */
    private String keyword;

    /**
     * 问诊方式:1-图文 2-电话 3-视频
     */
    private Integer consultType;

    /**
     * 状态:1-待接诊 2-接诊中 3-已完成 4-已退诊
     */
    private Integer status;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 接诊医生（员工ID）
     */
    private Long doctorId;

    /**
     * 仅待接诊（工作台待办用）
     */
    private Boolean waitingOnly;
}
