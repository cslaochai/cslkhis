package com.his.system.service;


/**
 * 业务操作审计服务：记下「谁、在哪个模块、对哪个对象做了什么、成没成」。
 */
public interface SysAuditLogService {

    /**
     * 记一条操作审计：成败都落账，失败时带原因，取值长度按各字段可容纳的上限收口。
     * 用于敏感动作留痕——报告提交/审核/退回/发布、代煎与胶片的改状态、影像删除、切换岗位、患者端就诊人维护。
     *
     * @param userName   操作人姓名，取不到登录态时留空而不是补一个假名字
     * @param targetId   被操作对象的主键，按文本留存
     * @param success    本次动作成没成；false 时 errorMsg 写清为什么没成
     */
    void record(Long userId, String userName, String module, String operation, String targetType, Object targetId, String content, boolean success, String errorMsg);
}
