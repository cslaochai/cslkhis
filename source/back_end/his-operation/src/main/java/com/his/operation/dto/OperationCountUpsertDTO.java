package com.his.operation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 建立手术清点单入参（同时登记术前基数）。
 *
 * <p>要求一次把"清单 + 术前基数"给完：先建一张空清单再逐条往里加，
 * 中途那段时间里的空清单会被当成"这台手术没用器械"—— 那比没有清单更糟。
 * 允许 {@code items} 为空只为"整台手术零器械"留口子，
 * 服务端会把它标成"零器械清点"，不会让它看起来像"还没开始数"。
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
