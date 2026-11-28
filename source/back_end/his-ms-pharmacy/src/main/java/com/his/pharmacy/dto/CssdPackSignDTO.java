package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 申领科室签收已发放的无菌包（6-已发放 → 7-已签收）。
 */
@Data
public class CssdPackSignDTO implements Serializable {

    @NotNull(message = "器械包ID不能为空")
    private Long packId;

    /** 签收备注（如「包破损，已退回」需写清） */
    private String remark;
}
