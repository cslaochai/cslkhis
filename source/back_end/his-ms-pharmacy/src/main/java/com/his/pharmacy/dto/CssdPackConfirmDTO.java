package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 供应室确认回收（0-待回收 → 1-已回收）。
 */
@Data
public class CssdPackConfirmDTO implements Serializable {

    @NotNull(message = "器械包ID不能为空")
    private Long packId;

    /** 回收备注（如「器械缺失 1 把」） */
    private String remark;
}
