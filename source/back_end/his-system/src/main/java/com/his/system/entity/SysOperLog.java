package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志（操作日志，建表见 sql/10，查看页见 sql/158）。
 *
 * <p><b>只记写动作</b>：POST/DELETE 里排除 listPage / selectList / getById 这类约定命名的读接口
 * （{@code OperLogInterceptor} 的读动作死表），其余一律留痕。
 * 读病历这类敏感查阅由业务模块自己写审计日志，两本账不重复。
 *
 * <p>本表<b>只读</b>：不提供删除接口（等保三级要求审计记录不得被未预期删除），
 * 归档走 DBA 按 oper_time 分区/转储，不在应用侧开口子。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oper_log")
public class SysOperLog extends BaseEntity {

    /**
     * 操作模块（如"用户管理"）
     */
    private String title;

    /**
     * 业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）
     */
    private Integer businessType;

    /**
     * 方法名称（Controller 方法名）
     */
    private String method;

    /**
     * 请求方式（GET/POST/PUT/DELETE）
     */
    private String requestMethod;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 操作人员ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 请求URL
     */
    private String operUrl;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 操作地点（内网/外网，不做 IP 库猜测）
     */
    private String operLocation;

    /**
     * 请求参数（密码类字段已打码）
     */
    private String operParam;

    /**
     * 返回参数
     */
    private String jsonResult;

    /**
     * 操作状态（0-正常 1-异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    /**
     * 消耗时间（毫秒）
     */
    private Long costTime;
}
