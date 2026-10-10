package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.StatReportDTO;
import com.his.medicaltech.entity.BizStatReport;
import com.his.medicaltech.mapper.BizStatReportMapper;
import com.his.medicaltech.mapper.StatReportAggMapper;
import com.his.medicaltech.service.StatReportService;
import com.his.medicaltech.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 病案统计上报服务（打印预留）。
 */
@Service
@RequiredArgsConstructor
public class StatReportServiceImpl extends ServiceImpl<BizStatReportMapper, BizStatReport> implements StatReportService {

    private final BizStatReportMapper bizStatReportMapper;

    private final StatReportAggMapper statReportAggMapper;

    private final ObjectMapper objectMapper;

    private final RedisSequenceService redisSequenceService;

    private final DictCacheService dictCacheService;

    /**
     * selectPage 排除列后聚合结果可能为 null
     */
    private static StatCohortSummaryRowVO emptySummary() {
        StatCohortSummaryRowVO vo = new StatCohortSummaryRowVO();
        vo.setDischargeCount(0L);
        vo.setDeathCount(0L);
        vo.setAvgLosDays(BigDecimal.ZERO);
        return vo;
    }

    private static StatOperationRowVO emptyOperation() {
        StatOperationRowVO vo = new StatOperationRowVO();
        vo.setOperationCount(0L);
        vo.setLevel3upCount(0L);
        return vo;
    }

    private static StatCohortFeesRowVO emptyFees() {
        StatCohortFeesRowVO vo = new StatCohortFeesRowVO();
        vo.setSettleCount(0L);
        vo.setTotalAmount(BigDecimal.ZERO);
        vo.setInsuranceAmount(BigDecimal.ZERO);
        vo.setPatientPayAmount(BigDecimal.ZERO);
        vo.setArrearsAmount(BigDecimal.ZERO);
        return vo;
    }

    // 查询

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
        String startStr = TimeUtil.dayStart(start).format(DateFormats.DATETIME);
        String endStr = TimeUtil.dayEnd(end).format(DateFormats.DATETIME);

        Long deptId = dto.getDeptId();
        BizStatReport dup = bizStatReportMapper.selectOne(new LambdaQueryWrapper<BizStatReport>()
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
            deptName = statReportAggMapper.selectDeptName(deptId);
            if (!TextUtil.hasText(deptName)) {
                throw new BusinessException("所选科室不存在");
            }
        }
        Long deptFilter = deptId == null ? 0L : deptId;

        StatCohortSummaryRowVO summary = statReportAggMapper.cohortSummary(startStr, endStr, deptFilter);
        if (summary == null) {
            summary = emptySummary();
        }
        StatOperationRowVO opStats = statReportAggMapper.operationStats(startStr, endStr, deptFilter);
        if (opStats == null) {
            opStats = emptyOperation();
        }
        StatCohortFeesRowVO fees = statReportAggMapper.cohortFees(startStr, endStr, deptFilter);
        if (fees == null) {
            fees = emptyFees();
        }
        List<StatOperationLevelRowVO> levelDist = statReportAggMapper.operationLevelDist(startStr, endStr, deptFilter);
        List<StatInsuranceDistRowVO> insurance = statReportAggMapper.insuranceDist(startStr, endStr, deptFilter);
        List<StatTopDiagnosisRowVO> topDx = statReportAggMapper.topDiagnoses(startStr, endStr, deptFilter);
        List<StatCohortCaseRowVO> cases = statReportAggMapper.cohortCases(startStr, endStr, deptFilter);

        String typeName = dictCacheService.getDicDataLabel(DictType.STAT_REPORT_TYPE, dto.getReportType());
        String title = (deptName == null ? "" : deptName) + typeName + "（" + period + "）";
        String operator = operatorUser.getRealName();
        LocalDateTime now = LocalDateTime.now();

        // 报文结构见 StatReportPayloadVO：字段名是前端预览/打印与未来对接平台的契约
        StatReportPayloadVO.Org org = new StatReportPayloadVO.Org();
        org.setOrgName("（预留：机构全称，真实对接时填写）");
        org.setOrgCode("（预留：卫生统计机构代码）");
        org.setRegionCode("（预留：行政区划代码）");

        StatReportPayloadVO.Period periodInfo = new StatReportPayloadVO.Period();
        periodInfo.setType(dto.getPeriodType());
        periodInfo.setTypeLabel(dto.getPeriodType() == 1 ? "月报" : "年报");
        periodInfo.setValue(period);
        periodInfo.setStart(startStr);
        periodInfo.setEnd(endStr);

        StatReportPayloadVO.Scope scope = new StatReportPayloadVO.Scope();
        scope.setDeptId(deptId);
        scope.setDeptName(deptName == null ? "全院" : deptName);

        StatReportPayloadVO.Indicators indicators = new StatReportPayloadVO.Indicators();
        indicators.setDischargeCount(summary.getDischargeCount());
        indicators.setDeathCount(summary.getDeathCount());
        indicators.setAvgLosDays(summary.getAvgLosDays());
        indicators.setOperationCount(opStats.getOperationCount());
        indicators.setLevel3upCount(opStats.getLevel3upCount());
        indicators.setSettleCount(fees.getSettleCount());
        indicators.setTotalAmount(fees.getTotalAmount());
        indicators.setInsuranceAmount(fees.getInsuranceAmount());
        indicators.setPatientPayAmount(fees.getPatientPayAmount());
        indicators.setArrearsAmount(fees.getArrearsAmount());

        List<StatReportPayloadVO.OperationLevelItem> levels = new ArrayList<>();
        for (StatOperationLevelRowVO row : levelDist) {
            StatReportPayloadVO.OperationLevelItem item = new StatReportPayloadVO.OperationLevelItem();
            long lv = row.getLevel() == null ? 0L : row.getLevel();
            item.setLevel(lv);
            item.setLevelLabel(lv == 0 ? "未录级别" : lv + "级");
            item.setCount(row.getCnt());
            levels.add(item);
        }

        List<StatReportPayloadVO.InsuranceItem> ins = new ArrayList<>();
        for (StatInsuranceDistRowVO row : insurance) {
            StatReportPayloadVO.InsuranceItem item = new StatReportPayloadVO.InsuranceItem();
            item.setType(String.valueOf(row.getInsuranceType()));
            item.setCount(row.getCnt());
            item.setAmount(row.getAmount());
            ins.add(item);
        }

        List<StatReportPayloadVO.TopDiagnosisItem> dxs = new ArrayList<>();
        for (StatTopDiagnosisRowVO row : topDx) {
            StatReportPayloadVO.TopDiagnosisItem item = new StatReportPayloadVO.TopDiagnosisItem();
            item.setCode(row.getDiagnosisCode());
            item.setName(row.getDiagnosisName());
            item.setCount(row.getCnt());
            dxs.add(item);
        }

        StatReportPayloadVO.Reserved reserved = new StatReportPayloadVO.Reserved();
        reserved.setSendChannel("打印预留：未对接外部平台。报出（submit）仅冻结留痕，"
                + "真实对接时把报出动作换成 http 上报即可，埋点就在 submit。");
        reserved.setReceipt("回执落库与月度对账口径：预留（本台账 payload 冻结即对外承诺内容，可回看）");
        reserved.setPrintTip("本报文可打印成纸质报表，加盖机构公章后作为上报留档。");

        StatReportPayloadVO payload = new StatReportPayloadVO();
        payload.setReportKind("WS4-STAT-" + dto.getReportType() + "-" + period);
        payload.setReportName(title);
        payload.setOrg(org);
        payload.setPeriod(periodInfo);
        payload.setScope(scope);
        payload.setIndicators(indicators);
        payload.setOperationLevels(levels);
        payload.setInsuranceTypes(ins);
        payload.setTopDiagnoses(dxs);
        payload.setCases(cases);
        payload.setReserved(reserved);
        payload.setOperator(operator);
        payload.setGeneratedAt(now.format(DateFormats.DATETIME));

        String payloadJson;
        try {
            payloadJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        } catch (Exception e) {
            throw new BusinessException("上报报文序列化失败：" + TextUtil.cut(e.getMessage(), 200));
        }

        BizStatReport r = new BizStatReport();
        r.setReportNo(redisSequenceService.generateStatReportNo());
        r.setReportType(dto.getReportType());
        r.setPeriodType(dto.getPeriodType());
        r.setPeriodValue(period);
        r.setDeptId(deptId);
        r.setDeptName(deptName);
        r.setTitle(title);
        r.setDischargeCount(summary.getDischargeCount().intValue());
        r.setDeathCount(summary.getDeathCount().intValue());
        r.setOperationCount(opStats.getOperationCount().intValue());
        r.setLevel3upCount(opStats.getLevel3upCount().intValue());
        r.setAvgLosDays(summary.getAvgLosDays());
        r.setTotalAmount(fees.getTotalAmount());
        r.setPayload(payloadJson);
        r.setStatus(0);
        r.setGenerateTime(now);
        r.setOperatorName(operator);
        r.setRemark(TextUtil.cut(dto.getRemark(), 500));
        r.setCreateBy(operator);
        bizStatReportMapper.insert(r);
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
            throw new BusinessException("只有草稿可报出（当前状态：" + dictCacheService.getDicDataLabel(DictType.STAT_REPORT_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(1);
        r.setSubmitTime(LocalDateTime.now());
        r.setSubmitByName(operatorUser.getRealName());
        bizStatReportMapper.updateById(r);
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
        r.setVoidReason(TextUtil.cut(reason, 200));
        bizStatReportMapper.updateById(r);
        return toRow(r);
    }

    public IPage<StatReportVO.Row> listPage(StatReportDTO.QueryPage dto) {
        LambdaQueryWrapper<BizStatReport> qw = new LambdaQueryWrapper<BizStatReport>()
                .select(BizStatReport.class, fi -> !"payload".equals(fi.getProperty()))
                .and(TextUtil.hasText(dto.getKeyword()), w -> w
                        .like(BizStatReport::getReportNo, dto.getKeyword().trim())
                        .or().like(BizStatReport::getTitle, dto.getKeyword().trim()))
                .eq(dto.getReportType() != null, BizStatReport::getReportType, dto.getReportType())
                .eq(dto.getStatus() != null, BizStatReport::getStatus, dto.getStatus())
                .eq(TextUtil.hasText(dto.getPeriodValue()), BizStatReport::getPeriodValue,
                        dto.getPeriodValue() == null ? null : dto.getPeriodValue().trim())
                .ge(dto.getStartDate() != null, BizStatReport::getGenerateTime, dto.getStartDate())
                .le(dto.getEndDate() != null, BizStatReport::getGenerateTime, dto.getEndDate())
                .orderByDesc(BizStatReport::getGenerateTime)
                .orderByDesc(BizStatReport::getId);
        return bizStatReportMapper.selectPage(Page.of(dto.getPageNum(), dto.getPageSize()), qw)
                .convert(this::toRow);
    }

    public StatReportVO.Detail getDetailById(Long id) {
        return toDetail(mustGet(id));
    }

    private BizStatReport mustGet(Long id) {
        BizStatReport r = bizStatReportMapper.selectById(id);
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