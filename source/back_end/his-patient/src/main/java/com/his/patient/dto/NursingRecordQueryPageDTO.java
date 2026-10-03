package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 护理文书分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NursingRecordQueryPageDTO extends PageParam implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 文书类型：1-三测单 2-护理记录单 3-生命体征监测
     */
    private Integer nursingType;

    /**
     * 文书状态：1-草稿 2-已提交 3-已归档
     */
    private Integer recordStatus;

    /**
     * 起始测量时间（含）
     */
    private String beginTime;

    /**
     * 结束测量时间（含）
     */
    private String endTime;

    /**
     * 科室数据权限收敛集合（M6）—— 只由服务端按 {@code DeptScopeGuard} 填充，
     * listPage 入口先置 null，前端传什么都忽略。受限时非空。
     */
    private List<Long> scopeDeptIds;
}
