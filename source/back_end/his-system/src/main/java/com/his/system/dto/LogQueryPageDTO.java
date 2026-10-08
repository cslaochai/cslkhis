package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日志查询入参（操作/登录/审计三本账共用，各页签只填自己关心的字段）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogQueryPageDTO extends PageParam {

    /**
     * 日志类型（导出时用：1-操作 2-登录 3-审计）
     */
    private Integer logType;

    /**
     * 关键字：操作日志匹配 URL/方法名，登录日志匹配用户名/IP，审计日志匹配内容/对象ID
     */
    private String keyword;

    /**
     * 操作人姓名（模糊）
     */
    private String operator;

    /**
     * 状态：操作日志/审计日志 0-正常(审计为1-成功) 见各表口径；登录日志 0-成功 1-失败
     */
    private Integer status;

    /**
     * 业务类型（操作日志 0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）
     */
    private Integer businessType;

    /**
     * 模块（审计日志 module，操作日志 title）
     */
    private String module;

    /**
     * 操作类型（审计日志 operation）
     */
    private String operation;

    /**
     * 对象类型（审计日志 target_type）
     */
    private String targetType;

    /**
     * 对象ID（审计日志的目标标识，如患者ID/处方ID；字段变更日志取业务标识）
     */
    private String targetId;

    /**
     * 字段名（仅字段变更日志用：同时匹配 field_name 与 field_label）。
     *
     * <p>审计问得最多的一句是"过敏史什么时候被改过"，不能让人在几千行里翻。
     */
    private String fieldName;

    /**
     * 开始日期 yyyy-MM-dd
     */
    private String beginTime;

    /**
     * 结束日期 yyyy-MM-dd（含当天 23:59:59）
     */
    private String endTime;
}
