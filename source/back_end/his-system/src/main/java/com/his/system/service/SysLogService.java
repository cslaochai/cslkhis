package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.LogQueryPageDTO;
import com.his.system.vo.AuditLogVO;
import com.his.system.vo.FieldChangeVO;
import com.his.system.vo.LogStatVO;
import com.his.system.vo.LoginLogVO;
import com.his.system.vo.OperLogDetailVO;
import com.his.system.vo.OperLogListVO;

/**
 * 日志审计服务（操作日志操作日志 / 登录日志登录日志 / 审计日志审计日志）。
 *
 * <p>三本账放在一起是有意的：等保三级查的是"审计覆盖到每个用户、记录重要安全事件"，
 * 分开三个页面会让"登录失败 5 次之后紧接着来了 3 次越权操作"这类串联证据永远拼不起来。
 *
 * <p><b>只读服务</b>：不提供任何删除/清空接口 —— 审计记录不得被应用侧删改（等保 8.1.4.3），
 * 归档走 DBA 按时间转储。
 */
public interface SysLogService {

    PageResult<OperLogListVO> operLogListPage(LogQueryPageDTO query);

    OperLogDetailVO operLogDetail(Long id);

    PageResult<LoginLogVO> loginLogListPage(LogQueryPageDTO query);

    PageResult<AuditLogVO> auditLogListPage(LogQueryPageDTO query);

    AuditLogVO auditLogDetail(Long id);

    /**
     * 字段级修改日志分页（字段级修改日志，sql/159）。
     *
     * <p>第四本账：前三本回答"谁调了接口 / 谁登录了 / 谁对哪个对象做了什么"，
     * 这本回答"哪个字段从什么值改成了什么值"。
     * 入参复用 {@link LogQueryPageDTO}：targetType 传对象类型、targetId 传对象ID、
     * keyword 匹配对象名/字段中文名/新旧值。
     */
    PageResult<FieldChangeVO> fieldChangeListPage(LogQueryPageDTO query);

    /**
     * 按批次号取同一次保存的全部字段变更。
     *
     * <p>一次保存改 5 个字段会落 5 行，列表里散着看不出是"同一个人同一分钟改的" ——
     * 详情弹窗按 batchNo 拉，一次性看完这一刀到底动了什么。
     */
    java.util.List<FieldChangeVO> fieldChangeBatch(String batchNo);

    LogStatVO stat();

    /**
     * 导出 CSV（当前筛选条件下的记录，最多 5000 行）。
     * 返回的是文件文本，前端转 Blob 下载 —— 不落服务器磁盘，不留临时文件。
     */
    String exportCsv(LogQueryPageDTO query);
}
