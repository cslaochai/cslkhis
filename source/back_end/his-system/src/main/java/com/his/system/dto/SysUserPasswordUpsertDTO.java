package com.his.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户重置密码入参
 */
@Data
public class SysUserPasswordUpsertDTO {

    /**
     * 用户ID
     */
    private Long userId;
}
