package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.medicaltech.dto.StatReportDTO;
import com.his.medicaltech.entity.BizStatReport;
import com.his.medicaltech.mapper.BizStatReportMapper;
import com.his.medicaltech.mapper.StatReportAggMapper;
import com.his.medicaltech.service.StatReportService;
import com.his.medicaltech.vo.StatReportVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 病案统计上报服务（打印预留）。
 */
@Service
@RequiredArgsConstructor
public class StatReportServiceImpl implements StatReportService {

    private final BizStatReportMapper reportMapper;

    private final StatReportAggMapper aggMapper;

    private final ObjectMapper objectMapper;

    private DictCacheService dictCacheService;

    /**
     * selectPage 排除列后字段为 null，聚合 map 兜底空 Map
     */
    private static Map<String, Object> nz(Map<String, Object> m) {
        return m == null ? Map.of() : m;
    }

    // 报出 / 作废
    private static long toLong(Object v) {
        return v == null ? 0L : ((Number) v).longValue();
    }

    private static BigDecimal toDecimal(Object v) {
        return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
    }

    // 查询
    private static String cut(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    @Transactional(rollbackFor = Exception.class)
    public StatReportVO.Detail generate(StatReportDTO.Generate dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        if (dto.getPeriodType() == null || (dto.getPeriodType() != 1 && dto.getPeriodType() != 2)) {
            throw new BusinessException("期间类型只能是 1-月报 或 2-年报");
        }
        String period = dto.getPeriodValue().trim();
        LocalDate start;
        LocalDate end;
        if (dto.getPeriodType() == 1) {
            if (!period.matches("\\d{4}-(0[1-9]|1[0-2])")) {
                throw new BusinessException("月报期间格式应为 yyyy-MM，例如 2026-09");
            }
            YearMonth ym = YearMonth.parse(period);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        } else {
            if (!period.matches("\\d{4}")) {
                throw new BusinessException("年报期间格式应为 yyyy，例如 2026");
            }
            int y = Integer.parseInt(period);
            start = LocalDate.of(y, 1, 1);
            end = LocalDate.of(y, 12, 31);
        }
        String startStr = start.atStartOfDay().format(DateFormats.DATETIME);
        String endStr = end.atTime(LocalTime.MAX).format(DateFormats.DATETIME);

        Long deptId = dto.getDeptId();
        BizStatReport dup = reportMapper.selectOne(new LambdaQueryWrapper<BizStatReport>()
                .eq(BizStatReport::getReportType, dto.getReportType())
                .eq(BizStatReport::getPeriodValue, period)
                .in(BizStatReport::getStatus, 0, 1)
                .eq(deptId != null, BizStatReport::getDeptId, deptId)
                .isNull(deptId == null, BizStatReport::getDeptId)
                .last("LIMIT 1"));
        if (dup != null) {
            throw new BusinessException("同期间同类型已存在未闭环的上报台账（" + dup.getReportNo() + "），"
                    + "请先作废或调整期间");
        }

        String deptName = null;
        if (deptId != null) {
            deptName = aggMapper.selectDeptName(deptId);
            if (!StringUtils.hasText(deptName)) {
                throw new BusinessException("所选科室不存在");
            }
        }
        Long deptFilter = deptId == null ? 0L : deptId;

        Map<String, Object> summary = nz(aggMapper.cohortSummary(startStr, endStr, deptFilter));
        Map<String, Object> opStats = nz(aggMapper.operationStats(startStr, endStr, deptFilter));
        Map<String, Object> fees = nz(aggMapper.cohortFees(startStr, endStr, deptFilter));
        List<Map<String, Object>> levelDist = aggMapper.operationLevelDist(startStr, endStr, deptFilter);
        List<Map<String, Object>> insurance = aggMapper.insuranceDist(startStr, endStr, deptFilter);
        List<Map<String, Object>> topDx = aggMapper.topDiagnoses(startStr, endStr, deptFilter);
        List<Map<String, Object>> cases = aggMapper.cohortCases(startStr, endStr, deptFilter);

        String typeName = dictCacheService.getDicDataLabel("biz_medicaltech_statReportTypeEnum", dto.getReportType());
        String title = (deptName == null ? "" : deptName) + typeName + "（" + period + "）";
        String operator = operatorUser.getRealName();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reportKind", "WS4-STAT-" + dto.getReportType() + "-" + period);
        payload.put("reportName", title);
        Map<String, Object> org = new LinkedHashMap<>();
        org.put("orgName", "（预留：机构全称，真实对接时填写）");
        org.put("orgCode", "（预留：卫生统计机构代码）");
        org.put("regionCode", "（预留：行政区划代码）");
        payload.put("org", org);
        Map<String, Object> periodMap = new LinkedHashMap<>();
        periodMap.put("type", dto.getPeriodType());
        periodMap.put("typeLabel", dto.getPeriodType() == 1 ? "月报" : "年报");
        periodMap.put("value", period);
        periodMap.put("start", startStr);
        periodMap.put("end", endStr);
        payload.put("period", periodMap);
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("deptId", deptId == null ? null : String.valueOf(deptId));
        scope.put("deptName", deptName == null ? "全院" : deptName);
        payload.put("scope", scope);

        Map<String, Object> indicators = new LinkedHashMap<>();
        indicators.put("dischargeCount", toLong(summary.get("dischargeCount")));
        indicators.put("deathCount", toLong(summary.get("deathCount")));
        indicators.put("avgLosDays", toDecimal(summary.get("avgLosDays")));
        indicators.put("operationCount", toLong(opStats.get("operationCount")));
        indicators.put("level3upCount", toLong(opStats.get("level3upCount")));
        indicators.put("settleCount", toLong(fees.get("settleCount")));
        indicators.put("totalAmount", toDecimal(fees.get("totalAmount")));
        indicators.put("insuranceAmount", toDecimal(fees.get("insuranceAmount")));
        indicators.put("patientPayAmount", toDecimal(fees.get("patientPayAmount")));
        indicators.put("arrearsAmount", toDecimal(fees.get("arrearsAmount")));
        payload.put("indicators", indicators);

        List<Map<String, Object>> levels = new ArrayList<>();
        for (Map<String, Object> row : levelDist) {
            Map<String, Object> item = new LinkedHashMap<>();
            long lv = toLong(row.get("level"));
            item.put("level", lv);
            item.put("levelLabel", lv == 0 ? "未录级别" : lv + "级");
            item.put("count", toLong(row.get("cnt")));
            levels.add(item);
        }
        payload.put("operationLevels", levels);

        List<Map<String, Object>> ins = new ArrayList<>();
        for (Map<String, Object> row : insurance) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", String.valueOf(row.get("insuranceType")));
            item.put("count", toLong(row.get("cnt")));
            item.put("amount", toDecimal(row.get("amount")));
            ins.add(item);
        }
        payload.put("insuranceTypes", ins);

        List<Map<String, Object>> dxs = new ArrayList<>();
        for (Map<String, Object> row : topDx) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", row.get("diagnosisCode"));
            item.put("name", row.get("diagnosisName"));
            item.put("count", toLong(row.get("cnt")));
            dxs.add(item);
        }
        payload.put("topDiagnoses", dxs);
        payload.put("cases", cases);

        Map<String, Object> reserved = new LinkedHashMap<>();
        reserved.put("sendChannel", "打印预留：未对接外部平台。报出（submit）仅冻结留痕，"
                + "真实对接时把报出动作换成 http 上报即可，埋点就在 submit。");
        reserved.put("receipt", "回执落库与月度对账口径：预留（本台账 payload 冻结即对外承诺内容，可回看）");
        reserved.put("printTip", "本报文可打印成纸质报表，加盖机构公章后作为上报留档。");
        payload.put("reserved", reserved);
        payload.put("operator", operator);
        payload.put("generatedAt", now.format(DateFormats.DATETIME));

        String payloadJson;
        try {
            payloadJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        } catch (Exception e) {
            throw new BusinessException("上报报文序列化失败：" + cut(e.getMessage(), 200));
        }

        BizStatReport r = new BizStatReport();
        r.setReportNo("TJ" + now.format(DateFormats.COMPACT_DATETIME) + ThreadLocalRandom.current().nextInt(100, 1000));
        r.setReportType(dto.getReportType());
        r.setPeriodType(dto.getPeriodType());
        r.setPeriodValue(period);
        r.setDeptId(deptId);
        r.setDeptName(deptName);
        r.setTitle(title);
        r.setDischargeCount((int) toLong(summary.get("dischargeCount")));
        r.setDeathCount((int) toLong(summary.get("deathCount")));
        r.setOperationCount((int) toLong(opStats.get("operationCount")));
        r.setLevel3upCount((int) toLong(opStats.get("level3upCount")));
        r.setAvgLosDays(toDecimal(summary.get("avgLosDays")));
        r.setTotalAmount(toDecimal(fees.get("totalAmount")));
        r.setPayload(payloadJson);
        r.setStatus(0);
        r.setGenerateTime(now);
        r.setOperatorName(operator);
        r.setRemark(cut(dto.getRemark(), 500));
        r.setCreateBy(operator);
        reportMapper.insert(r);
        return toDetail(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public StatReportVO.Row submit(Long id) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizStatReport r = mustGet(id);
        if (r.getStatus() != 0) {
            throw new BusinessException("只有草稿可报出（当前状态：" + dictCacheService.getDicDataLabel("biz_medicaltech_statReportStatusEnum", r.getStatus()) + "）");
        }
        r.setStatus(1);
        r.setSubmitTime(LocalDateTime.now());
        r.setSubmitByName(operatorUser.getRealName());
        reportMapper.updateById(r);
        return toRow(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public StatReportVO.Row voidReport(Long id, String reason) {
        BizStatReport r = mustGet(id);
        if (r.getStatus() == 2) {
            throw new BusinessException("该台账已作废");
        }
        r.setStatus(2);
        r.setVoidTime(LocalDateTime.now());
        r.setVoidReason(cut(reason, 200));
        reportMapper.updateById(r);
        return toRow(r);
    }

    public IPage<StatReportVO.Row> listPage(StatReportDTO.QueryPage dto) {
        StatReportDTO.QueryPage q = dto == null ? new StatReportDTO.QueryPage() : dto;
        LambdaQueryWrapper<BizStatReport> qw = new LambdaQueryWrapper<BizStatReport>()
                .select(BizStatReport.class, fi -> !"payload".equals(fi.getProperty()))
                .and(StringUtils.hasText(q.getKeyword()), w -> w
                        .like(BizStatReport::getReportNo, q.getKeyword().trim())
                        .or().like(BizStatReport::getTitle, q.getKeyword().trim()))
                .eq(q.getReportType() != null, BizStatReport::getReportType, q.getReportType())
                .eq(q.getStatus() != null, BizStatReport::getStatus, q.getStatus())
                .eq(StringUtils.hasText(q.getPeriodValue()), BizStatReport::getPeriodValue,
                        q.getPeriodValue() == null ? null : q.getPeriodValue().trim())
                .ge(q.getStartDate() != null, BizStatReport::getGenerateTime, q.getStartDate())
                .le(q.getEndDate() != null, BizStatReport::getGenerateTime, q.getEndDate())
                .orderByDesc(BizStatReport::getGenerateTime)
                .orderByDesc(BizStatReport::getId);
        return reportMapper.selectPage(Page.of(q.getPageNum(), q.getPageSize()), qw)
                .convert(this::toRow);
    }

    public StatReportVO.Detail getDetailById(Long id) {
        return toDetail(mustGet(id));
    }

    private BizStatReport mustGet(Long id) {
        BizStatReport r = reportMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("上报台账不存在");
        }
        return r;
    }

    private StatReportVO.Row toRow(BizStatReport r) {
        StatReportVO.Row v = new StatReportVO.Row();
        copy(r, v);
        return v;
    }

    private StatReportVO.Detail toDetail(BizStatReport r) {
        StatReportVO.Detail v = new StatReportVO.Detail();
        copy(r, v);
        v.setPayload(r.getPayload());
        v.setVoidTime(r.getVoidTime());
        return v;
    }

    private void copy(BizStatReport r, StatReportVO.Row v) {
        v.setId(r.getId());
        v.setReportNo(r.getReportNo());
        v.setReportType(r.getReportType());
        v.setPeriodType(r.getPeriodType());
        v.setPeriodValue(r.getPeriodValue());
        v.setDeptId(r.getDeptId());
        v.setDeptName(r.getDeptName());
        v.setTitle(r.getTitle());
        v.setDischargeCount(r.getDischargeCount());
        v.setDeathCount(r.getDeathCount());
        v.setOperationCount(r.getOperationCount());
        v.setLevel3upCount(r.getLevel3upCount());
        v.setAvgLosDays(r.getAvgLosDays());
        v.setTotalAmount(r.getTotalAmount());
        v.setStatus(r.getStatus());
        v.setGenerateTime(r.getGenerateTime());
        v.setSubmitTime(r.getSubmitTime());
        v.setOperatorName(r.getOperatorName());
        v.setSubmitByName(r.getSubmitByName());
        v.setVoidReason(r.getVoidReason());
        v.setRemark(r.getRemark());
    }
}