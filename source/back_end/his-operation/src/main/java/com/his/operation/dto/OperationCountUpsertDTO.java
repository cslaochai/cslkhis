package com.his.operation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 建立手术清点单入参（同时登记术前基数）。
 */
@Data
public class OperationCountUpsertDTO implements Serializable {

    /**
     * 手术申请单ID
     */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /**
     * 器械（洗手）护士ID（员工ID）
     */
    private Long instrumentNurseId;

    /**
     * 巡回护士ID（员工ID）
     */
    private Long circulateNurseId;

    /**
     * 明细项集合
     */
    @Valid
    private List<CountItemInputUpsertDTO> items;

    /**
     * 备注
     */
    private String remark;
}
