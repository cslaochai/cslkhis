package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.dto.LogQueryPageDTO;
import com.his.system.entity.SysAuditLog;
import com.his.system.entity.SysFieldChangeLog;
import com.his.system.entity.SysLoginLog;
import com.his.system.entity.SysOperLog;
import com.his.system.mapper.SysAuditLogMapper;
import com.his.system.mapper.SysFieldChangeLogMapper;
import com.his.system.mapper.SysLoginLogMapper;
import com.his.system.mapper.SysOperLogMapper;
import com.his.system.service.SysLogService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.AuditLogVO;
import com.his.system.vo.FieldChangeVO;
import com.his.system.vo.LogStatVO;
import com.his.system.vo.LoginLogVO;
import com.his.system.vo.OperLogDetailVO;
import com.his.system.vo.OperLogListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 日志审计服务实现（sql/158）。
 *
 * <p>三张表都是<b>只增不删</b>的业务留痕表，所以这里没有任何写方法 ——
 * 写入分别在 {@code OperLogInterceptor}（操作日志）、{@code SysLoginLogService}（登录日志）、
 * {@code SysAuditLogService}（审计日志，业务模块显式调用）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl implements SysLogService {
    @Autowired
    private DictCacheService dictText;

    /** CSV 导出行上限：审计导出是给检查人员看的，不是给数据库做全量备份。 */
    private static final int EXPORT_MAX = 5000;

    /** 口令爆破嫌疑阈值（近24小时失败次数） */
    private static final int BRUTE_FORCE_THRESHOLD = 5;


    private final SysOperLogMapper operLogMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final SysAuditLogMapper auditLogMapper;
    private final SysFieldChangeLogMapper fieldChangeLogMapper;

    // 操作日志

    @Override
    public PageResult<OperLogListVO> operLogListPage(LogQueryPageDTO query) {
        LogQueryPageDTO q = orEmpty(query);
        String operator = trim(q.getOperator());
        String module = trim(q.getModule());
        String keyword = trim(q.getKeyword());
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOperLog::getDelFlag, 0)
                .like(operator != null, SysOperLog::getOperName, operator)
                .like(module != null, SysOperLog::getTitle, module)
                .eq(q.getBusinessType() != null, SysOperLog::getBusinessType, q.getBusinessType())
                .eq(q.getStatus() != null, SysOperLog::getStatus, q.getStatus());
        if (keyword != null) {
            wrapper.and(w -> w.like(SysOperLog::getOperUrl, keyword)
                    .or().like(SysOperLog::getMethod, keyword)
                    .or().like(SysOperLog::getOperIp, keyword));
        }
        applyRange(wrapper, q, SysOperLog::getOperTime);
        wrapper.orderByDesc(SysOperLog::getOperTime).orderByDesc(SysOperLog::getId);

        IPage<SysOperLog> page = operLogMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        List<OperLogListVO> records = page.getRecords().stream().map(this::toOperVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public OperLogDetailVO operLogDetail(Long id) {
        // C 类保留：入参是 GET @RequestParam 标量而非 request DTO，Bean Validation 注解无处安放
        if (id == null) {
            throw new BusinessException("日志ID不能为空");
        }
        SysOperLog row = operLogMapper.selectById(id);
        if (row == null || (row.getDelFlag() != null && row.getDelFlag() == 1)) {
            throw new BusinessException("操作日志不存在：" + id);
        }
        OperLogDetailVO vo = new OperLogDetailVO();
        vo.setId(row.getId());
        vo.setTitle(row.getTitle());
        vo.setBusinessType(row.getBusinessType());
        vo.setBusinessTypeText(OperBusinessTypeEnum.getText(row.getBusinessType()));
        vo.setMethod(row.getMethod());
        vo.setRequestMethod(row.getRequestMethod());
        vo.setOperName(row.getOperName());
        vo.setOperId(row.getOperId());
        vo.setDeptName(row.getDeptName());
        vo.setDeptId(row.getDeptId());
        vo.setOperUrl(row.getOperUrl());
        vo.setOperIp(row.getOperIp());
        vo.setOperLocation(row.getOperLocation());
        vo.setOperParam(row.getOperParam());
        vo.setJsonResult(row.getJsonResult());
        vo.setStatus(row.getStatus());
        vo.setStatusText(OperStatusEnum.getText(row.getStatus()));
        vo.setErrorMsg(row.getErrorMsg());
        vo.setOperTime(row.getOperTime());
        vo.setCostTime(row.getCostTime());
        return vo;
    }

    // 登录日志

    @Override
    public PageResult<LoginLogVO> loginLogListPage(LogQueryPageDTO query) {
        LogQueryPageDTO q = orEmpty(query);
        String operator = trim(q.getOperator());
        String keyword = trim(q.getKeyword());
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginLog::getDelFlag, 0)
                .eq(q.getStatus() != null, SysLoginLog::getLoginStatus, q.getStatus());
        if (operator != null) {
            wrapper.and(w -> w.like(SysLoginLog::getUserName, operator).or().like(SysLoginLog::getRealName, operator));
        }
        if (keyword != null) {
            wrapper.and(w -> w.like(SysLoginLog::getLoginIp, keyword).or().like(SysLoginLog::getMsg, keyword));
        }
        applyRange(wrapper, q, SysLoginLog::getLoginTime);
        wrapper.orderByDesc(SysLoginLog::getLoginTime).orderByDesc(SysLoginLog::getId);

        IPage<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        List<LoginLogVO> records = page.getRecords().stream().map(this::toLoginVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 审计日志

    @Override
    public PageResult<AuditLogVO> auditLogListPage(LogQueryPageDTO query) {
        LogQueryPageDTO q = orEmpty(query);
        String operator = trim(q.getOperator());
        String module = trim(q.getModule());
        String operation = trim(q.getOperation());
        String targetType = trim(q.getTargetType());
        String targetId = trim(q.getTargetId());
        String keyword = trim(q.getKeyword());
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAuditLog::getDelFlag, 0)
                .like(module != null, SysAuditLog::getModule, module)
                .like(operation != null, SysAuditLog::getOperation, operation)
                .like(targetType != null, SysAuditLog::getTargetType, targetType)
                .like(targetId != null, SysAuditLog::getTargetId, targetId)
                .like(operator != null, SysAuditLog::getUserName, operator);
        if (q.getStatus() != null) {
            // 审计日志 status 口径：1-成功 0-失败（与操作日志相反，入参仍按"看结果"给）
            wrapper.eq(SysAuditLog::getStatus, q.getStatus());
        }
        if (keyword != null) {
            wrapper.and(w -> w.like(SysAuditLog::getContent, keyword)
                    .or().like(SysAuditLog::getTargetId, keyword)
                    .or().like(SysAuditLog::getIp, keyword));
        }
        applyRange(wrapper, q, SysAuditLog::getCreateTime);
        wrapper.orderByDesc(SysAuditLog::getCreateTime).orderByDesc(SysAuditLog::getId);

        IPage<SysAuditLog> page = auditLogMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        List<AuditLogVO> records = page.getRecords().stream().map(this::toAuditVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public AuditLogVO auditLogDetail(Long id) {
        // C 类保留：入参是 GET @RequestParam 标量而非 request DTO，Bean Validation 注解无处安放
        if (id == null) {
            throw new BusinessException("日志ID不能为空");
        }
        SysAuditLog row = auditLogMapper.selectById(id);
        if (row == null || (row.getDelFlag() != null && row.getDelFlag() == 1)) {
            throw new BusinessException("审计日志不存在：" + id);
        }
        return toAuditVO(row);
    }

    // 字段级修改日志（第四本账，sql/159）

    @Override
    public PageResult<FieldChangeVO> fieldChangeListPage(LogQueryPageDTO query) {
        LogQueryPageDTO q = orEmpty(query);
        String bizType = trim(q.getTargetType());
        String bizId = trim(q.getTargetId());
        String field = trim(q.getFieldName());
        String operator = trim(q.getOperator());
        String keyword = trim(q.getKeyword());
        LambdaQueryWrapper<SysFieldChangeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFieldChangeLog::getDelFlag, 0)
                .eq(bizType != null, SysFieldChangeLog::getBizType, bizType)
                .eq(bizId != null, SysFieldChangeLog::getBizId, bizId)
                .like(operator != null, SysFieldChangeLog::getOperatorName, operator);
        if (field != null) {
            wrapper.and(w -> w.like(SysFieldChangeLog::getFieldName, field)
                    .or().like(SysFieldChangeLog::getFieldLabel, field));
        }
        // 关键字跨对象名 / 对象编号 / 字段中文名 / 新旧值 —— 审计问得最多的是
        // "某某患者的某某字段什么时候被改过"，只匹配一个列会让人以为没改过。
        if (keyword != null) {
            wrapper.and(w -> w.like(SysFieldChangeLog::getBizName, keyword)
                    .or().like(SysFieldChangeLog::getBizNo, keyword)
                    .or().like(SysFieldChangeLog::getFieldLabel, keyword)
                    .or().like(SysFieldChangeLog::getOldValue, keyword)
                    .or().like(SysFieldChangeLog::getNewValue, keyword));
        }
        applyRange(wrapper, q, SysFieldChangeLog::getChangeTime);
        // 分页必须补唯一二级键：同一毫秒落的多行靠 id 兜底，否则翻页会出现重复行/丢行
        wrapper.orderByDesc(SysFieldChangeLog::getChangeTime).orderByDesc(SysFieldChangeLog::getId);

        IPage<SysFieldChangeLog> page =
                fieldChangeLogMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        List<FieldChangeVO> records = page.getRecords().stream().map(this::toFieldChangeVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public List<FieldChangeVO> fieldChangeBatch(String batchNo) {
        // C 类保留：入参是 GET @RequestParam 标量（纯空格也进得来）而非 request DTO，Bean Validation 注解无处安放
        if (!StringUtils.hasText(batchNo)) {
            throw new BusinessException("批次号不能为空");
        }
        LambdaQueryWrapper<SysFieldChangeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFieldChangeLog::getDelFlag, 0)
                .eq(SysFieldChangeLog::getBatchNo, batchNo.trim())
                .orderByAsc(SysFieldChangeLog::getId);
        return fieldChangeLogMapper.selectList(wrapper).stream().map(this::toFieldChangeVO).toList();
    }

    // 统计

    @Override
    public LogStatVO stat() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();

        LogStatVO vo = new LogStatVO();
        vo.setOperTotal(count(new LambdaQueryWrapper<SysOperLog>().eq(SysOperLog::getDelFlag, 0), operLogMapper));
        vo.setOperToday(count(new LambdaQueryWrapper<SysOperLog>().eq(SysOperLog::getDelFlag, 0)
                .ge(SysOperLog::getOperTime, dayStart), operLogMapper));
        vo.setOperFailToday(count(new LambdaQueryWrapper<SysOperLog>().eq(SysOperLog::getDelFlag, 0)
                .eq(SysOperLog::getStatus, 1).ge(SysOperLog::getOperTime, dayStart), operLogMapper));
        vo.setOper7d(count(new LambdaQueryWrapper<SysOperLog>().eq(SysOperLog::getDelFlag, 0)
                .ge(SysOperLog::getOperTime, weekStart), operLogMapper));

        vo.setLoginTotal(count(new LambdaQueryWrapper<SysLoginLog>().eq(SysLoginLog::getDelFlag, 0), loginLogMapper));
        vo.setLoginToday(count(new LambdaQueryWrapper<SysLoginLog>().eq(SysLoginLog::getDelFlag, 0)
                .ge(SysLoginLog::getLoginTime, dayStart), loginLogMapper));
        vo.setLoginFailToday(count(new LambdaQueryWrapper<SysLoginLog>().eq(SysLoginLog::getDelFlag, 0)
                .eq(SysLoginLog::getLoginStatus, 1).ge(SysLoginLog::getLoginTime, dayStart), loginLogMapper));
        vo.setLogin7d(count(new LambdaQueryWrapper<SysLoginLog>().eq(SysLoginLog::getDelFlag, 0)
                .ge(SysLoginLog::getLoginTime, weekStart), loginLogMapper));

        vo.setAuditTotal(count(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getDelFlag, 0), auditLogMapper));
        vo.setAuditToday(count(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getDelFlag, 0)
                .ge(SysAuditLog::getCreateTime, dayStart), auditLogMapper));
        vo.setAuditFailToday(count(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getDelFlag, 0)
                .eq(SysAuditLog::getStatus, 0).ge(SysAuditLog::getCreateTime, dayStart), auditLogMapper));
        vo.setAudit7d(count(new LambdaQueryWrapper<SysAuditLog>().eq(SysAuditLog::getDelFlag, 0)
                .ge(SysAuditLog::getCreateTime, weekStart), auditLogMapper));

        vo.setFieldChangeTotal(count(new LambdaQueryWrapper<SysFieldChangeLog>()
                .eq(SysFieldChangeLog::getDelFlag, 0), fieldChangeLogMapper));
        vo.setFieldChangeToday(count(new LambdaQueryWrapper<SysFieldChangeLog>()
                .eq(SysFieldChangeLog::getDelFlag, 0)
                .ge(SysFieldChangeLog::getChangeTime, dayStart), fieldChangeLogMapper));
        vo.setFieldChange7d(count(new LambdaQueryWrapper<SysFieldChangeLog>()
                .eq(SysFieldChangeLog::getDelFlag, 0)
                .ge(SysFieldChangeLog::getChangeTime, weekStart), fieldChangeLogMapper));

        vo.setRiskyAccounts(riskyAccounts(now.minusHours(24)));
        return vo;
    }

    /** 近24小时登录失败次数达到阈值的账号（口令爆破嫌疑）。 */
    private List<LogStatVO.RiskAccount> riskyAccounts(LocalDateTime since) {
        try {
            QueryWrapper<SysLoginLog> w = new QueryWrapper<>();
            w.select("user_name", "COUNT(*) AS fail_count", "MAX(login_time) AS last_fail_time")
                    .eq("del_flag", 0)
                    .eq("login_status", 1)
                    .ge("login_time", since)
                    .isNotNull("user_name")
                    .ne("user_name", "")
                    .groupBy("user_name")
                    .having("COUNT(*) >= {0}", BRUTE_FORCE_THRESHOLD)
                    .orderByDesc("fail_count");
            List<Map<String, Object>> maps = loginLogMapper.selectMaps(w);
            List<LogStatVO.RiskAccount> list = new ArrayList<>();
            for (Map<String, Object> m : maps) {
                LogStatVO.RiskAccount a = new LogStatVO.RiskAccount();
                a.setUserName(String.valueOf(m.get("user_name")));
                Object c = m.get("fail_count");
                a.setFailCount(c == null ? 0L : Long.valueOf(String.valueOf(c)));
                Object t = m.get("last_fail_time");
                a.setLastFailTime(t instanceof LocalDateTime l ? l : null);
                list.add(a);
            }
            return list;
        } catch (Exception e) {
            // 统计卡里的"嫌疑账号"算不出来不该让整页统计 500
            log.error("登录失败账号统计失败", e);
            return List.of();
        }
    }

    private <T> long count(LambdaQueryWrapper<T> wrapper, com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper) {
        Long n = mapper.selectCount(wrapper);
        return n == null ? 0L : n;
    }

    // 导出

    @Override
    public String exportCsv(LogQueryPageDTO query) {
        LogQueryPageDTO q = orEmpty(query);
        int type = q.getLogType() == null ? 1 : q.getLogType();
        q.forExport(EXPORT_MAX);
        StringBuilder sb = new StringBuilder();
        // BOM：Excel 打开 UTF-8 CSV 不加 BOM 会全屏乱码
        sb.append('\uFEFF');
        switch (type) {
            case 2 -> {
                sb.append("用户名,姓名,登录IP,地点,浏览器,操作系统,状态,提示,登录时间\n");
                for (LoginLogVO r : loginLogListPage(q).getRecords()) {
                    sb.append(csv(r.getUserName())).append(',')
                            .append(csv(r.getRealName())).append(',')
                            .append(csv(r.getLoginIp())).append(',')
                            .append(csv(r.getLoginLocation())).append(',')
                            .append(csv(r.getBrowser())).append(',')
                            .append(csv(r.getOs())).append(',')
                            .append(csv(r.getLoginStatusText())).append(',')
                            .append(csv(r.getMsg())).append(',')
                            .append(r.getLoginTime() == null ? "" : r.getLoginTime()).append('\n');
                }
            }
            case 3 -> {
                sb.append("时间,操作人,模块,操作,对象类型,对象ID,内容,IP,结果\n");
                for (AuditLogVO r : auditLogListPage(q).getRecords()) {
                    sb.append(r.getCreateTime() == null ? "" : r.getCreateTime()).append(',')
                            .append(csv(r.getUserName())).append(',')
                            .append(csv(r.getModule())).append(',')
                            .append(csv(r.getOperation())).append(',')
                            .append(csv(r.getTargetType())).append(',')
                            .append(csv(r.getTargetId())).append(',')
                            .append(csv(r.getContent())).append(',')
                            .append(csv(r.getIp())).append(',')
                            .append(csv(r.getStatusText())).append('\n');
                }
            }
            case 4 -> {
                sb.append("时间,对象类型,对象编号,对象名称,字段,变更前,变更后,变更类型,操作人,科室,批次号\n");
                for (FieldChangeVO r : fieldChangeListPage(q).getRecords()) {
                    sb.append(r.getChangeTime() == null ? "" : r.getChangeTime()).append(',')
                            .append(csv(r.getBizTypeText())).append(',')
                            .append(csv(r.getBizNo())).append(',')
                            .append(csv(r.getBizName())).append(',')
                            .append(csv(r.getFieldLabel())).append(',')
                            .append(csv(r.getOldValue())).append(',')
                            .append(csv(r.getNewValue())).append(',')
                            .append(csv(r.getChangeTypeText())).append(',')
                            .append(csv(r.getOperatorName())).append(',')
                            .append(csv(r.getDeptName())).append(',')
                            .append(csv(r.getBatchNo())).append('\n');
                }
            }
            default -> {
                sb.append("时间,模块,业务类型,方法,请求方式,操作人,科室,URL,IP,地点,状态,耗时(ms),失败原因\n");
                for (OperLogListVO r : operLogListPage(q).getRecords()) {
                    sb.append(r.getOperTime() == null ? "" : r.getOperTime()).append(',')
                            .append(csv(r.getTitle())).append(',')
                            .append(csv(r.getBusinessTypeText())).append(',')
                            .append(csv(r.getMethod())).append(',')
                            .append(csv(r.getRequestMethod())).append(',')
                            .append(csv(r.getOperName())).append(',')
                            .append(csv(r.getDeptName())).append(',')
                            .append(csv(r.getOperUrl())).append(',')
                            .append(csv(r.getOperIp())).append(',')
                            .append(csv(r.getOperLocation())).append(',')
                            .append(csv(r.getStatusText())).append(',')
                            .append(r.getCostTime() == null ? "" : r.getCostTime()).append(',')
                            .append(csv(r.getErrorMsg())).append('\n');
                }
            }
        }
        sb.append("\n# 本文件由系统导出，最多 ").append(EXPORT_MAX)
                .append(" 行；导出人：").append(csv(operatorName())).append('\n');
        return sb.toString();
    }

    private String operatorName() {
        var user = UserUtils.getCurrentUser();
        if (user == null) {
            return "";
        }
        return org.springframework.util.StringUtils.hasText(user.getEmployeeName())
                ? user.getEmployeeName() : user.getRealName();
    }

    // 转换

    private OperLogListVO toOperVO(SysOperLog row) {
        OperLogListVO vo = new OperLogListVO();
        vo.setId(row.getId());
        vo.setTitle(row.getTitle());
        vo.setBusinessType(row.getBusinessType());
        vo.setBusinessTypeText(OperBusinessTypeEnum.getText(row.getBusinessType()));
        vo.setMethod(row.getMethod());
        vo.setRequestMethod(row.getRequestMethod());
        vo.setOperName(row.getOperName());
        vo.setOperId(row.getOperId());
        vo.setDeptName(row.getDeptName());
        vo.setOperUrl(row.getOperUrl());
        vo.setOperIp(row.getOperIp());
        vo.setOperLocation(row.getOperLocation());
        vo.setStatus(row.getStatus());
        vo.setStatusText(OperStatusEnum.getText(row.getStatus()));
        vo.setOperTime(row.getOperTime());
        vo.setCostTime(row.getCostTime());
        vo.setErrorMsg(row.getErrorMsg() == null ? null : cut(row.getErrorMsg(), 200));
        return vo;
    }

    private LoginLogVO toLoginVO(SysLoginLog row) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(row.getId());
        vo.setUserName(row.getUserName());
        vo.setUserId(row.getUserId());
        vo.setRealName(row.getRealName());
        vo.setLoginIp(row.getLoginIp());
        vo.setLoginLocation(row.getLoginLocation());
        vo.setBrowser(row.getBrowser());
        vo.setOs(row.getOs());
        vo.setLoginStatus(row.getLoginStatus());
        vo.setLoginStatusText(LoginStatusEnum.getText(row.getLoginStatus()));
        vo.setMsg(row.getMsg());
        vo.setLoginTime(row.getLoginTime());
        vo.setUserAgent(row.getUserAgent());
        return vo;
    }

    private AuditLogVO toAuditVO(SysAuditLog row) {
        AuditLogVO vo = new AuditLogVO();
        vo.setId(row.getId());
        vo.setUserId(row.getUserId());
        vo.setUserName(row.getUserName());
        vo.setModule(row.getModule());
        vo.setOperation(row.getOperation());
        vo.setTargetId(row.getTargetId());
        vo.setTargetType(row.getTargetType());
        vo.setContent(row.getContent());
        vo.setIp(row.getIp());
        vo.setStatus(row.getStatus());
        vo.setStatusText(AuditLogStatusEnum.getText(row.getStatus()));
        vo.setErrorMsg(row.getErrorMsg());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }

    private FieldChangeVO toFieldChangeVO(SysFieldChangeLog row) {
        FieldChangeVO vo = new FieldChangeVO();
        vo.setId(row.getId());
        vo.setBizType(row.getBizType());
        vo.setBizTypeText(bizTypeText(row.getBizType()));
        vo.setBizId(row.getBizId());
        vo.setBizNo(row.getBizNo());
        vo.setBizName(row.getBizName());
        vo.setFieldName(row.getFieldName());
        vo.setFieldLabel(row.getFieldLabel());
        vo.setOldValue(row.getOldValue());
        vo.setNewValue(row.getNewValue());
        vo.setChangeType(row.getChangeType());
        vo.setChangeTypeText(changeTypeText(row.getChangeType()));
        vo.setBatchNo(row.getBatchNo());
        vo.setOperatorId(row.getOperatorId());
        vo.setOperatorName(row.getOperatorName());
        vo.setDeptName(row.getDeptName());
        vo.setChangeTime(row.getChangeTime());
        vo.setRemark(row.getRemark());
        return vo;
    }

    // 工具

    private static LogQueryPageDTO orEmpty(LogQueryPageDTO query) {
        return query == null ? new LogQueryPageDTO() : query;
    }

    /**
     * 先 trim 再判空：MP 的 {@code like(condition, column, value)} 是普通方法调用，
     * 实参里的 {@code xx.trim()} 无论 condition 真假都会先求值 —— 传 null 就 NPE（本轮实测踩到）。
     */
    private static String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 日期区间：入参只到天，上界必须补到 23:59:59 ——
     * 直接拿日期字符串比大小会把当天全部时点滤掉（G12 已踩）。
     */
    private static <T> void applyRange(LambdaQueryWrapper<T> wrapper, LogQueryPageDTO q,
                                       com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column) {
        if (StringUtils.hasText(q.getBeginTime())) {
            wrapper.ge(column, parseDay(q.getBeginTime(), LocalTime.MIN));
        }
        if (StringUtils.hasText(q.getEndTime())) {
            wrapper.le(column, parseDay(q.getEndTime(), LocalTime.MAX.withNano(0)));
        }
    }

    private static LocalDateTime parseDay(String day, LocalTime time) {
        String d = day.trim();
        if (d.length() > 10) {
            d = d.substring(0, 10);
        }
        try {
            return LocalDateTime.of(LocalDate.parse(d), time);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd：" + day);
        }
    }

    /**
     * 对象类型文本。认不出的码值**原样返回**而不是落成"未知"：
     * 新接一个对象忘了补这里，界面上会直接显示原始码值（一眼看出该补）；
     * 回落成"未知"的话这个缺口永远不会被发现（MEMORY：未知值不得渲染成某个合法值）。
     */
    static String bizTypeText(String v) {
        if (v == null) {
            return "未知";
        }
        return switch (v) {
            case "PATIENT" -> "患者主档";
            case "USER" -> "系统用户";
            case "EMPLOYEE" -> "员工档案";
            case "MEDICAL_RECORD" -> "门诊病历";
            case "INPATIENT_RECORD" -> "住院文书";
            default -> v;
        };
    }

    /** 变更类型：INSERT-建档 UPDATE-修改 ACTION-操作留痕（无字段级新旧值）。 */
    static String changeTypeText(String v) {
        if (v == null) {
            return "未知";
        }
        return switch (v) {
            case "INSERT" -> "建档";
            case "UPDATE" -> "修改";
            case "ACTION" -> "操作";
            default -> v;
        };
    }

    private static String csv(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v).replace("\r", " ").replace("\n", " ");
        return s.contains(",") || s.contains("\"") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}