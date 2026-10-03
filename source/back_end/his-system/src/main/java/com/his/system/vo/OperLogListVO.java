package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志列表行（列表不带 operParam / jsonResult —— 请求体动辄几 KB，一页 100 行就把带宽吃没了，
 * 要看内容点开详情）。
 */
@Data
public class OperLogListVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 操作模块 */
    private String title;

    /** 业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空） */
    private Integer businessType;

    /** 业务类型文案（服务端算，前端不自备一份口径） */
    private String businessTypeText;

    /** Controller 方法名 */
    private String method;

    /** 请求方式（GET/POST/PUT/DELETE） */
    private String requestMethod;

    /** 操作人员 */
    private String operName;

    /** 操作人员ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operId;

    /** 部门名称 */
    private String deptName;

    /** 请求URL */
    private String operUrl;

    /** 操作IP */
    private String operIp;

    /** 操作地点 */
    private String operLocation;

    /** 操作状态（0-正常 1-异常） */
    private Integer status;

    /** 状态文本 */
    private String statusText;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    /** 消耗时间（毫秒） */
    private Long costTime;

    /** 失败原因摘要（列表只给一句话，完整报文在详情里） */
    private String errorMsg;
}
