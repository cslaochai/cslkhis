package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录账号摘要：跨模块按账号取身份快照的出参（不含口令等敏感列）。
 */
@Data
@NoArgsConstructor
public class UserBriefVO {

    /**
     * 账号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 登录账号名
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 关联员工ID（院外账号为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long empId;
}
