package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志详情（含请求参数与返回内容，密码类字段在落库时已打码）。
 */
@Data
public class OperLogDetailVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 操作模块
     */
    private String title;
    /**
     * 业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）
     */
    private Integer businessType;
    private String businessTypeText;
    /**
     * 方法名称
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
     * 操作地点
     */
    private String operLocation;

    /**
     * 请求参数（脱敏后）
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
     * 状态文本
     */
    private String statusText;
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
