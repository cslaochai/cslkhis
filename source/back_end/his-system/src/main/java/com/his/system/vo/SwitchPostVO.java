package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 切换岗位结果出参。
 *
 * <p>角色与科室一起返回：岗位是成对切换的，前端拿一次就能把顶栏两个标签都更新掉，
 * 不用切换后再请求一次 /auth/info。
 */
@Data
public class SwitchPostVO {

    /**
     * 新登录令牌。
     *
     * <p>岗位是会话级选择（只活在这个 token 里），前端必须**立即写入 localStorage**，
     * 否则下一个请求又按员工主科室 + 库里默认角色算，等于白切。
     */
    private String token;

    private String currentRole;

    private String roleName;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;
}
