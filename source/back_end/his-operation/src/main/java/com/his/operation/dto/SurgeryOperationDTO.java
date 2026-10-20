package com.his.operation.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 手术闭环回写病案首页手术明细的入参。
 * 重复闸、序号重排、is_surgery 置位都在提供方做（它是这张表的所有者）。
 */
@Data
public class SurgeryOperationDTO {

    private Long admissionId;

    private Long applyId;

    private Integer isMain;

    private String operationCode;

    private String operationName;

    private LocalDateTime operationDate;

    private Integer operationLevel;

    private Integer incisionLevel;

    private Integer anesthesiaType;

    private Long surgeonId;

    private String surgeonName;

    private String assistantName;

    private String operationBasis;

    /** 备注 */
    private String remark;
}
