package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 模板动作入参（发布 / 停用，共用 pathway:add 权限）。
 */
@Data
public class PathwayActionDTO implements Serializable {

    @NotNull(message = "模板ID不能为空")
    private Long id;
}
