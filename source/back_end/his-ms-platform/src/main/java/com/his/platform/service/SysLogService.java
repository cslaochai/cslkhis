package com.his.platform.service;

import com.his.common.base.PageResult;
import com.his.platform.dto.LogPageQueryDTO;
import com.his.platform.vo.*;

/**
 * 日志审计服务：操作、登录、审计、字段变更四本账的查询、统计与导出收口。
 */
public interface SysLogService {

    /**
     * 操作日志分页：按操作人、模块、时间等条件筛选，只记写动作，读接口不入库。
     * 用于系统管理「日志审计」页的「操作日志」页签表格。
     */
    PageResult<OperLogListVO> operLogListPage(LogPageQueryDTO query);

    /**
     * 单条操作日志的完整现场：请求参数与返回内容，凭据类字段已打码。
     * 用于「操作日志」页签行内「详情」弹框，追查一次写操作到底提交了什么。
     */
    OperLogDetailVO operLogDetail(Long id);

    /**
     * 登录日志分页：成功与失败同表，靠登录状态区分。
     * 用于「日志审计」页的「登录日志」页签，排查账号被爆破或异常登录。
     */
    PageResult<LoginLogVO> loginLogListPage(LogPageQueryDTO query);

    /**
     * 审计日志分页：按模块、操作、对象等条件筛选业务侧显式写入的「谁对哪个对象做了什么」。
     * 用于「日志审计」页的「审计日志」页签。
     */
    PageResult<AuditLogVO> auditLogListPage(LogPageQueryDTO query);

    /**
     * 单条审计日志的完整内容，含业务侧写入的详情文本与失败原因。
     * 用于「审计日志」页签行内「详情」弹框。
     */
    AuditLogVO auditLogDetail(Long id);

    /**
     * 字段级修改日志分页（字段级修改日志，sql/159）。
     *
     * <p>第四本账：前三本回答"谁调了接口 / 谁登录了 / 谁对哪个对象做了什么"，
     * 这本回答"哪个字段从什么值改成了什么值"。
     * 入参复用 {@link LogPageQueryDTO}：targetType 传对象类型、targetId 传对象ID、
     * keyword 匹配对象名/字段中文名/新旧值。
     */
    PageResult<FieldChangeVO> fieldChangeListPage(LogPageQueryDTO query);

    /**
     * 按批次号取同一次保存的全部字段变更。
     *
     * <p>一次保存改 5 个字段会落 5 行，列表里散着看不出是"同一个人同一分钟改的" ——
     * 详情弹窗按 batchNo 拉，一次性看完这一刀到底动了什么。
     */
    java.util.List<FieldChangeVO> fieldChangeBatch(String batchNo);

    /**
     * 汇总操作、登录、审计、字段变更四本账的量，并给出近 24 小时口令爆破嫌疑账号。
     * 用于「日志审计」页顶部的统计卡与安全提醒条。
     */
    LogStatVO stat();

    /**
     * 导出 CSV（当前筛选条件下的记录，最多 5000 行）。
     * 返回的是文件文本，前端转 Blob 下载 —— 不落服务器磁盘，不留临时文件。
     */
    String exportCsv(LogPageQueryDTO query);
}
