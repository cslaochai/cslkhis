package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.NursingQcDTO;
import com.his.patient.entity.BizNursingQcCheck;
import com.his.patient.entity.BizNursingQcCheckItem;
import com.his.patient.entity.BizNursingQcIndicator;
import com.his.patient.enums.QcIndicatorSourceEnum;
import com.his.patient.mapper.BizNursingQcCheckItemMapper;
import com.his.patient.mapper.BizNursingQcCheckMapper;
import com.his.patient.mapper.BizNursingQcIndicatorMapper;
import com.his.patient.mapper.SysNursingQcItemMapper;
import com.his.patient.service.NursingQcService;
import com.his.patient.vo.NurseQcVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 护理质控服务实现（sql/168）。
 *
 * <p>口径（与 {@code sql/168} 文件头逐条对齐，改这里必须同步改那里的铺底 SQL 与自检）：
 * <ol>
 *   <li><b>主表六个数字一律由明细求和</b>（口径 d）：抽查/合格/合格率/应得分/实得分/得分率
 *       没有任何手填口子，前端传什么都不认。这样台账重算读主表和读明细必然是同一个数。</li>
 *   <li><b>一张单 = 病区 × 月 × 类别由唯一键保证</b>（{@code uk_check_ward_month_cat}）：
 *       服务层按唯一键定位后整单覆盖明细，不做「先查再插」的竞态判断。</li>
 *   <li><b>已确认的单调明细一律拒绝</b>：要改必须先退回草稿。台账会把已确认的数带进月度通报，
 *       悄悄改掉等于事后无痕修订。</li>
 *   <li><b>检查项按目录重查快照</b>：{@code itemId} 必须在本次类别的启用目录里，
 *       项目名与应得分取库里的当前值写成快照（标准改版不能重算历史检查单）。</li>
 *   <li><b>重算与上报分离</b>：确认检查单<b>不</b>自动改台账（少一条隐式写入路径），
 *       护理部点「重算」才落账；{@code report_status=2} 的行由 SQL 侧闸门挡住不被覆盖。</li>
 *   <li><b>合格率类指标：本月没有检查单就不写行</b>：写 0 会让「没查」在看板上长得像「合格率为 0」。
 *       千床日类照写（住院床日是客观事实，0 床日就落 {@code rate_value = NULL}）。</li>
 *   <li><b>数据范围按病区所属科室收口</b>：读侧 LIST 过滤，写侧逐条 {@code canAccessDept}，
 *       越权直接报错而不是静默改条件。</li>
 *   <li><b>删除全走物理删</b>：三张表唯一键都不含 del_flag（AGENTS §3）。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NursingQcServiceImpl implements NursingQcService {
    private static final int TEXT_MAX = 500;
    private static final int OPERATOR_MAX = 64;
    private static final int INSPECTOR_LIMIT = 200;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String REMARK_AUTO = "合格率与得分由检查明细求和生成（sql/168 口径 d）";
    private final DeptScopeProvider deptScopeProvider;
    private final BizNursingQcCheckMapper checkMapper;
    private final BizNursingQcCheckItemMapper checkItemMapper;
    private final SysNursingQcItemMapper itemMapper;
    private final BizNursingQcIndicatorMapper indicatorMapper;
    private final DictCacheService dictCacheService;

    // 参照数据

    private static String requireMonth(String month, String label) {
        // ②非web入口：共用守卫，主要职责是 yyyy-MM 解析；HTTP 侧非空已由 DTO @NotNull + @Valid 收口
        String value = trimToNull(month);
        if (value == null) {
            throw new BusinessException("请选择" + label);
        }
        parseMonthWithLabel(value, label);
        return value;
    }

    private static YearMonth parseMonth(String month) {
        return parseMonthWithLabel(month, "统计月份");
    }

    private static YearMonth parseMonthWithLabel(String month, String label) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new BusinessException(label + "格式必须为 yyyy-MM");
        }
    }

    // 检查单

    /**
     * 单号：QC + yyyyMM + 两位类别 + 病区ID 后五位（唯一键已保证三元组唯一，所以号码不会重复）
     */
    private static String buildCheckNo(String checkMonth, int category, Long wardId) {
        long tail = wardId == null ? 0L : Math.floorMod(wardId, 100000L);
        return "QC" + checkMonth.replace("-", "") + String.format("%02d", category) + String.format("%05d", tail);
    }

    private static String autoSummary(int category, int sampleCount, int qualifiedCount, BigDecimal qualifiedRate,
                                      BigDecimal scoreRate, List<BizNursingQcCheckItem> rows) {
        long badItems = rows.stream()
                .filter(r -> r.getQualifiedNum() != null && r.getCheckedNum() != null
                        && r.getQualifiedNum() < r.getCheckedNum())
                .count();
        return "本轮" + NursingQcCategoryEnum.getText(category) + "抽查 " + sampleCount + " 例，合格 "
                + qualifiedCount + " 例，合格率 " + qualifiedRate.toPlainString() + "%，得分率 "
                + scoreRate.toPlainString() + "%；重点问题 " + badItems + " 项";
    }

    /**
     * 达标判定：分母 0 或无目标 → NULL（页面上显示「—」，不是「未达标」）
     */
    private static Integer reachedFlag(NursingIndicatorEnum e, BigDecimal rate, BigDecimal target) {
        if (e == null || rate == null) {
            return null;
        }
        BigDecimal t = target != null ? target : e.targetDecimal();
        if (t == null) {
            return null;
        }
        return (e.higherIsBetter() ? rate.compareTo(t) >= 0 : rate.compareTo(t) <= 0) ? 1 : 0;
    }

    private static String reachedText(Integer flag) {
        if (flag == null) {
            return "无目标或无数据";
        }
        return flag == 1 ? "达标" : "未达标";
    }

    /**
     * 百分数：分子/分母×100 保留两位；分母 0 返回 0（检查单没有明细根本存不进来）
     */
    private static BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.multiply(HUNDRED).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    // 台账与看板

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static int nvl(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    @Override
    public List<NurseQcVO.Ward> wardSelectList(String keyword) {
        return checkMapper.selectWardOptions(scopedDeptIds(null), trimToNull(keyword));
    }

    @Override
    public List<NurseQcVO.Inspector> inspectorSelectList(Long wardId, String keyword) {
        NurseQcVO.Ward ward = requireWard(wardId);
        return checkMapper.selectInspectors(ward.getDeptId(), trimToNull(keyword), INSPECTOR_LIMIT);
    }

    @Override
    public List<NurseQcVO.ItemDef> itemSelectList(Integer category) {
        List<NurseQcVO.ItemDef> items = category == null
                ? itemMapper.selectAllEnabledItems()
                : itemMapper.selectItemsByCategory(requireCategory(category));
        items.forEach(this::fillItemDefText);
        return items;
    }

    // 台账行构建（口径 a/b/c/d 的 Java 侧唯一实现）

    @Override
    public PageResult<NurseQcVO.CheckRow> checkListPage(NursingQcDTO.CheckQueryPage query) {
        NursingQcDTO.CheckQueryPage q = query == null ? new NursingQcDTO.CheckQueryPage() : query;
        IPage<NurseQcVO.CheckRow> page = new Page<>(nvl(q.getPageNum(), 1), nvl(q.getPageSize(), 10));
        List<NurseQcVO.CheckRow> records = checkMapper.selectCheckPage(page, trimToNull(q.getKeyword()),
                q.getWardId(), deptScopeProvider.resolveDeptId(q.getDeptId()), q.getCategory(), q.getStatus(),
                trimToNull(q.getStartMonth()), trimToNull(q.getEndMonth()), scopedDeptIds(q.getDeptId()));
        records.forEach(this::fillCheckText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 校验与文本填充

    @Override
    public NurseQcVO.CheckDetail getDetailById(Long id) {
        NurseQcVO.CheckRow row = requireCheck(id);
        List<NurseQcVO.CheckItemRow> items = checkItemMapper.selectItemsByCheckId(row.getId());
        List<NurseQcVO.ItemDef> catalog = itemMapper.selectItemsByCategory(row.getCategory());
        catalog.forEach(this::fillItemDefText);
        Set<Long> recorded = new HashSet<>();
        items.forEach(i -> recorded.add(i.getItemId()));
        NurseQcVO.CheckDetail detail = new NurseQcVO.CheckDetail();
        detail.setCheck(row);
        detail.setItems(items);
        detail.setCatalog(catalog);
        // 目录里已被停用但单上仍有快照的项不会计入 missing：missing 只说「这轮漏查了几个现行项」
        detail.setMissingItemCount((int) catalog.stream().filter(c -> !recorded.contains(c.getItemId())).count());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseQcVO.SaveResult checkUpsert(NursingQcDTO.CheckUpsert dto) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了
        if (dto == null) {
            throw new BusinessException("缺少检查单内容");
        }
        int category = requireCategory(dto.getCategory());
        NurseQcVO.Ward ward = requireWard(dto.getWardId());
        String checkMonth = requireMonth(dto.getCheckMonth(), "检查月份");
        NurseQcVO.CheckRow existing = checkMapper.selectCheckByUk(ward.getWardId(), checkMonth, category);
        if (existing != null && NursingQcStatusEnum.CONFIRMED.getCode() == nvl(existing.getStatus(), 1)) {
            throw new BusinessException("「" + ward.getWardName() + "」" + checkMonth + " 的"
                    + NursingQcCategoryEnum.getText(category) + "检查单已确认，请先退回草稿再修改");
        }

        Map<Long, NurseQcVO.ItemDef> catalog = new LinkedHashMap<>();
        itemMapper.selectItemsByCategory(category).forEach(i -> catalog.put(i.getItemId(), i));

        int sampleCount = 0;
        int qualifiedCount = 0;
        BigDecimal fullScore = BigDecimal.ZERO;
        BigDecimal totalScore = BigDecimal.ZERO;
        List<BizNursingQcCheckItem> rows = new ArrayList<>(dto.getItems().size());
        Set<Long> used = new HashSet<>();
        for (NursingQcDTO.CheckItemInput input : dto.getItems()) {
            NurseQcVO.ItemDef def = catalog.get(input.getItemId());
            if (def == null) {
                throw new BusinessException("检查项目不存在、已停用或不属于「"
                        + NursingQcCategoryEnum.getText(category) + "」类别，请刷新后重选");
            }
            if (!used.add(def.getItemId())) {
                throw new BusinessException("「" + def.getItemName() + "」重复录入，同一项目一行只能记一次");
            }
            int checked = nvl(input.getCheckedNum(), 0);
            int qualified = nvl(input.getQualifiedNum(), 0);
            if (qualified > checked) {
                throw new BusinessException("「" + def.getItemName() + "」合格例数（" + qualified
                        + "）不能大于抽查例数（" + checked + "）");
            }
            BigDecimal score = checked <= 0 ? BigDecimal.ZERO
                    : def.getFullScore().multiply(BigDecimal.valueOf(qualified))
                    .divide(BigDecimal.valueOf(checked), 1, RoundingMode.HALF_UP);
            BizNursingQcCheckItem item = new BizNursingQcCheckItem();
            item.setItemId(def.getItemId());
            item.setItemCode(def.getItemCode());
            item.setItemName(def.getItemName());
            item.setCategory(category);
            item.setCheckedNum(checked);
            item.setQualifiedNum(qualified);
            item.setFullScore(def.getFullScore());
            item.setScore(score);
            item.setProblem(cut(input.getProblem(), TEXT_MAX));
            item.setCauseAnalysis(cut(input.getCauseAnalysis(), TEXT_MAX));
            item.setRectifyMeasure(cut(input.getRectifyMeasure(), TEXT_MAX));
            item.setRemark(cut(input.getRemark(), TEXT_MAX));
            rows.add(item);
            sampleCount += checked;
            qualifiedCount += qualified;
            fullScore = fullScore.add(def.getFullScore());
            totalScore = totalScore.add(score);
        }
        if (sampleCount <= 0) {
            throw new BusinessException("抽查例数合计为 0，这样的检查单不能入台账");
        }

        BigDecimal qualifiedRate = percent(BigDecimal.valueOf(qualifiedCount), BigDecimal.valueOf(sampleCount));
        BigDecimal scoreRate = percent(totalScore, fullScore);
        String summary = trimToNull(dto.getSummary());

        BizNursingQcCheck entity = new BizNursingQcCheck();
        if (existing != null) {
            entity.setId(existing.getId());
            entity.setCheckNo(existing.getCheckNo());
        } else {
            entity.setId(IdWorker.getId());
            entity.setCheckNo(buildCheckNo(checkMonth, category, ward.getWardId()));
        }
        NurseQcVO.Inspector inspector = resolveInspector(dto.getInspectorId());
        entity.setWardId(ward.getWardId());
        entity.setWardName(ward.getWardName());
        entity.setDeptId(ward.getDeptId());
        entity.setDeptName(ward.getDeptName());
        entity.setCheckMonth(checkMonth);
        entity.setCheckDate(dto.getCheckDate());
        entity.setCategory(category);
        entity.setInspectorId(inspector == null ? null : inspector.getEmployeeId());
        entity.setInspectorName(inspector == null ? null : inspector.getEmpName());
        entity.setSampleCount(sampleCount);
        entity.setQualifiedCount(qualifiedCount);
        entity.setQualifiedRate(qualifiedRate);
        entity.setFullScore(fullScore);
        entity.setTotalScore(totalScore);
        entity.setScoreRate(scoreRate);
        entity.setStatus(existing == null ? NursingQcStatusEnum.DRAFT.getCode() : nvl(existing.getStatus(), 1));
        entity.setSummary(cut(summary == null ? autoSummary(category, sampleCount, qualifiedCount, qualifiedRate,
                scoreRate, rows) : summary, TEXT_MAX));
        entity.setRemark(cut(REMARK_AUTO, TEXT_MAX));
        if (existing != null) {
            checkMapper.updateById(entity);
        } else {
            checkMapper.insert(entity);
        }

        // 唯一键不含 del_flag ⇒ 先物理清明细再重插，软删会让第二步撞键
        checkItemMapper.purgeByCheckId(entity.getId());
        for (BizNursingQcCheckItem item : rows) {
            item.setId(IdWorker.getId());
            item.setCheckId(entity.getId());
            checkItemMapper.insert(item);
        }

        NurseQcVO.SaveResult result = new NurseQcVO.SaveResult();
        result.setId(entity.getId());
        result.setCheckNo(entity.getCheckNo());
        result.setItemCount(rows.size());
        result.setSampleCount(sampleCount);
        result.setQualifiedCount(qualifiedCount);
        result.setQualifiedRate(qualifiedRate);
        result.setFullScore(fullScore);
        result.setTotalScore(totalScore);
        result.setScoreRate(scoreRate);
        result.setStatus(entity.getStatus());
        result.setStatusText(NursingQcStatusEnum.getText(entity.getStatus()));
        result.setMessage((existing == null ? "检查单已创建：" : "检查单已更新：") + entity.getCheckNo()
                + "（抽查 " + sampleCount + " 例，合格率 " + qualifiedRate.toPlainString() + "%）"
                + "；台账需在" + checkMonth + "重算后更新");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseQcVO.SaveResult checkStatus(NursingQcDTO.CheckStatus dto) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了（id 必填已收口到 DTO @NotNull + @Valid）
        if (dto == null) {
            throw new BusinessException("缺少检查单ID");
        }
        NursingQcStatusEnum target = NursingQcStatusEnum.fromCode(dto.getStatus());
        // ③业务规则：状态码值合法性（"不能为空"已由 DTO @NotNull 收口，fromCode 只判非法值）
        if (target == null) {
            throw new BusinessException("状态只能是 1-草稿 或 2-已确认");
        }
        NurseQcVO.CheckRow row = requireCheck(dto.getId());
        if (target == NursingQcStatusEnum.CONFIRMED) {
            // ③业务规则：确认闸门查的是库里已存的明细，不是本次入参，DTO 注解表达不了
            if (checkItemMapper.selectItemsByCheckId(row.getId()).isEmpty()) {
                throw new BusinessException("这张单还没有明细，不能确认");
            }
        }
        BizNursingQcCheck entity = new BizNursingQcCheck();
        entity.setId(row.getId());
        entity.setStatus(target.getCode());
        checkMapper.updateById(entity);

        NurseQcVO.SaveResult result = new NurseQcVO.SaveResult();
        result.setId(row.getId());
        result.setCheckNo(row.getCheckNo());
        result.setStatus(target.getCode());
        result.setStatusText(target.getLabel());
        result.setMessage(target == NursingQcStatusEnum.CONFIRMED
                ? "已确认，明细冻结（要改请先退回草稿）"
                : "已退回草稿，可以改明细了");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkDeleteById(Long id) {
        NurseQcVO.CheckRow row = requireCheck(id);
        checkItemMapper.purgeByCheckId(row.getId());
        checkMapper.purgeById(row.getId());
    }

    @Override
    public PageResult<NurseQcVO.LedgerRow> ledgerListPage(NursingQcDTO.LedgerQueryPage query) {
        NursingQcDTO.LedgerQueryPage q = query == null ? new NursingQcDTO.LedgerQueryPage() : query;
        IPage<NurseQcVO.LedgerRow> page = new Page<>(nvl(q.getPageNum(), 1), nvl(q.getPageSize(), 10));
        List<NurseQcVO.LedgerRow> records = indicatorMapper.selectLedgerPage(page, trimToNull(q.getKeyword()),
                q.getWardId(), deptScopeProvider.resolveDeptId(q.getDeptId()), trimToNull(q.getIndicatorCode()),
                q.getReportStatus(), trimToNull(q.getStatMonth()), trimToNull(q.getStartMonth()),
                trimToNull(q.getEndMonth()), scopedDeptIds(q.getDeptId()));
        records.forEach(this::fillLedgerText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public List<NurseQcVO.Kpi> monthMetrics(NursingQcDTO.MonthQuery query) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了（统计月份必填已收口到 DTO @NotNull + @Valid）
        if (query == null) {
            throw new BusinessException("请选择统计月份");
        }
        String statMonth = requireMonth(query.getStatMonth(), "统计月份");
        NurseQcVO.Ward ward = query.getWardId() == null ? null : requireVisibleWard(query.getWardId());
        Map<String, NurseQcVO.Kpi> byCode = new LinkedHashMap<>();
        indicatorMapper.selectMonthKpi(statMonth, ward == null ? null : ward.getWardId(),
                        ward == null ? scopedDeptIds(null) : null)
                .forEach(k -> byCode.put(k.getIndicatorCode(), k));
        // 四条指标永远都在看板上：没有台账的月份显示「未重算」而不是消失
        List<NurseQcVO.Kpi> kpis = new ArrayList<>(NursingIndicatorEnum.values().length);
        for (NursingIndicatorEnum e : NursingIndicatorEnum.values()) {
            NurseQcVO.Kpi k = byCode.get(e.getCode());
            if (k == null) {
                k = new NurseQcVO.Kpi();
                k.setIndicatorCode(e.getCode());
                k.setNumerator(BigDecimal.ZERO);
                k.setDenominator(BigDecimal.ZERO);
                k.setWardCount(0);
                k.setNotReachedCount(0);
                k.setReportedCount(0);
                k.setUnreportedCount(0);
            }
            k.setIndicatorName(e.getLabel());
            k.setUnit(e.getUnit());
            k.setTargetValue(e.targetDecimal());
            k.setSourceType(e.getSourceType());
            k.setStatMonth(statMonth);
            fillKpiText(k);
            kpis.add(k);
        }
        return kpis;
    }

    @Override
    public List<NurseQcVO.Kpi> trend(NursingQcDTO.TrendQuery query) {
        // ②非web入口：整个入参对象的判空。此接口未加 @Valid：TrendQuery.indicatorCode 上挂着
        // 错位的 yyyy-MM @Pattern（把指标码当月份校），一旦开校验任何合法指标都会被拒，故保持 service 兜底
        if (query == null) {
            throw new BusinessException("请选择统计指标");
        }
        NursingIndicatorEnum indicator = requireIndicator(query.getIndicatorCode());
        NurseQcVO.Ward ward = query.getWardId() == null ? null : requireVisibleWard(query.getWardId());
        String start = trimToNull(query.getStartMonth());
        String end = trimToNull(query.getEndMonth());
        if (start != null && end != null && start.compareTo(end) > 0) {
            throw new BusinessException("起始月份不能晚于结束月份");
        }
        List<NurseQcVO.Kpi> points = indicatorMapper.selectTrend(indicator.getCode(),
                ward == null ? null : ward.getWardId(), start, end, ward == null ? scopedDeptIds(null) : null);
        points.forEach(p -> {
            p.setIndicatorName(indicator.getLabel());
            p.setUnit(indicator.getUnit());
            p.setTargetValue(indicator.targetDecimal());
            p.setSourceType(indicator.getSourceType());
            fillKpiText(p);
        });
        return points;
    }

    @Override
    public List<NurseQcVO.LedgerRow> wardCompare(NursingQcDTO.CompareQuery query) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了（月份+指标必填已收口到 DTO @NotNull + @Valid）
        if (query == null) {
            throw new BusinessException("请选择统计月份与指标");
        }
        String statMonth = requireMonth(query.getStatMonth(), "统计月份");
        NursingIndicatorEnum indicator = requireIndicator(query.getIndicatorCode());
        List<NurseQcVO.LedgerRow> rows = indicatorMapper.selectWardCompare(statMonth, indicator.getCode(),
                scopedDeptIds(null));
        rows.forEach(this::fillLedgerText);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseQcVO.RecalcResult recalc(NursingQcDTO.RecalcCommand command) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了（月份必填已收口到 DTO @NotNull + @Valid）
        if (command == null) {
            throw new BusinessException("请选择要重算的月份");
        }
        String statMonth = requireMonth(command.getStatMonth(), "统计月份");
        YearMonth month = parseMonth(statMonth);
        LocalDate monthStart = month.atDay(1);
        // 统计末日 = LEAST(月末, 今天)：当月算的是「至今为止」的活数（口径 a），未来月份没有事实可算
        LocalDate statEnd = month.atEndOfMonth().isAfter(LocalDate.now()) ? LocalDate.now() : month.atEndOfMonth();
        if (monthStart.isAfter(LocalDate.now())) {
            throw new BusinessException("统计月份 " + statMonth + " 还没开始，重算不出任何事实");
        }

        List<NurseQcVO.Ward> wards = command.getWardId() == null
                ? indicatorMapper.selectCalcWards(scopedDeptIds(null))
                : List.of(requireVisibleWard(command.getWardId()));
        // 「全部病区」是护理部一键月度动作，全院 49 个启用病区里多数当月既没收过检查也没有住院事实：
        // 给它们写「0 床日、发生率 NULL」的空行不会说谎，但会把病区对比图挤满四十多个无意义落点。
        // 所以整扫时跳过无据病区；指定病区时不跳（这个病区就是要自己的这一月入账，哪怕是零）。
        boolean sweep = command.getWardId() == null;
        int calculated = 0;
        int skipped = 0;
        int bedDaysTotal = 0;
        int wardDone = 0;
        int skippedEmptyWard = 0;
        for (NurseQcVO.Ward ward : wards) {
            int bedDays = indicatorMapper.selectBedDays(ward.getWardId(), monthStart, statEnd);
            if (sweep && bedDays <= 0 && checkMapper.countByWardMonth(ward.getWardId(), statMonth) == 0) {
                skippedEmptyWard++;
                continue;
            }
            wardDone++;
            bedDaysTotal += bedDays;
            Set<String> reported = new HashSet<>(indicatorMapper.selectReportedCodes(statMonth, ward.getWardId()));
            for (NursingIndicatorEnum e : NursingIndicatorEnum.values()) {
                if (reported.contains(e.getCode())) {
                    skipped++;
                    continue;
                }
                BizNursingQcIndicator row = buildIndicator(e, ward, statMonth, monthStart, statEnd, bedDays);
                if (row == null) {
                    continue;
                }
                indicatorMapper.upsertIndicator(row);
                calculated++;
            }
        }
        NurseQcVO.RecalcResult result = new NurseQcVO.RecalcResult();
        result.setStatMonth(statMonth);
        result.setWardCount(wardDone);
        result.setCalculatedCount(calculated);
        result.setSkippedReportedCount(skipped);
        result.setSkippedEmptyWardCount(skippedEmptyWard);
        result.setTotalBedDays(bedDaysTotal);
        result.setMessage(statMonth + "：重算 " + wardDone + " 个病区，写入/覆盖 " + calculated
                + " 行台账，跳过已上报 " + skipped + " 行（床日合计 " + bedDaysTotal + "）"
                + (skippedEmptyWard > 0 ? "；另有 " + skippedEmptyWard + " 个病区本月无住院事实也无检查单，未入账" : "")
                + (skipped > 0 ? "；已上报的行要先退回才能改" : ""));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NurseQcVO.ReportResult report(NursingQcDTO.ReportCommand command) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了（月份与状态必填已收口到 DTO @NotNull + @Valid）
        if (command == null) {
            throw new BusinessException("请选择统计月份");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String statMonth = requireMonth(command.getStatMonth(), "统计月份");
        NursingQcReportEnum target = NursingQcReportEnum.fromCode(command.getReportStatus());
        // ③业务规则：码值合法性（非空已由 DTO @NotNull 收口）
        if (target == null) {
            throw new BusinessException("上报状态只能是 1-退回未上报 或 2-已上报");
        }
        NurseQcVO.Ward ward = command.getWardId() == null ? null : requireVisibleWard(command.getWardId());
        int affected = indicatorMapper.updateReportStatus(statMonth, ward == null ? null : ward.getWardId(),
                target.getCode(), ward == null ? scopedDeptIds(null) : null, cut(operatorUser.getRealName(), OPERATOR_MAX));
        NurseQcVO.ReportResult result = new NurseQcVO.ReportResult();
        result.setStatMonth(statMonth);
        result.setAffectedCount(affected);
        result.setReportStatus(target.getCode());
        result.setReportStatusText(target.getLabel());
        result.setMessage(affected == 0
                ? statMonth + " 没有需要" + (target == NursingQcReportEnum.REPORTED ? "上报" : "退回")
                + "的台账行（先重算再上报）"
                : statMonth + "：" + affected + " 行台账已" + target.getLabel()
                + (target == NursingQcReportEnum.REPORTED ? "，重算不再覆盖这些行" : "，可以重算了"));
        return result;
    }

    @Override
    public void ledgerDeleteById(Long id) {
        // ②非web入口：service 入参守卫（HTTP 侧 @RequestParam 本身必填，DTO 注解表达不了）
        if (id == null) {
            throw new BusinessException("缺少台账行ID");
        }
        NurseQcVO.LedgerRow row = indicatorMapper.selectLedgerById(id);
        if (row == null) {
            throw new BusinessException("台账行不存在");
        }
        if (!deptScopeProvider.canAccessDept(row.getDeptId())) {
            throw new BusinessException("无权删除「" + row.getWardName() + "」的台账（不在当前岗位的数据范围内）");
        }
        indicatorMapper.purgeById(id);
    }

    /**
     * 一条指标在一个病区一个月的台账行；返回 null 表示本月不写行（合格率类且没有检查单）。
     *
     * <p>分子分母的取数表达式与 {@code sql/168} 第 13 节铺底逐字一致，所以「重算」对铺底数据应当零变化。
     */
    private BizNursingQcIndicator buildIndicator(NursingIndicatorEnum e, NurseQcVO.Ward ward, String statMonth,
                                                 LocalDate monthStart, LocalDate statEnd, int bedDays) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BigDecimal numerator;
        BigDecimal denominator;
        String remark;
        if (e.getSourceType() == QcIndicatorSourceEnum.CHECK.getCode()) {
            NurseQcVO.CheckRow check = checkMapper.selectCheckByUk(ward.getWardId(), statMonth, e.getCheckCategory());
            if (check == null) {
                return null;
            }
            numerator = BigDecimal.valueOf(nvl(check.getQualifiedCount(), 0));
            denominator = BigDecimal.valueOf(nvl(check.getSampleCount(), 0));
            remark = "来源：检查单 " + check.getCheckNo() + "（类别 " + check.getCategory()
                    + "、" + NursingQcStatusEnum.getText(check.getStatus()) + "）由明细求和";
        } else {
            int count = indicatorMapper.selectEventCount(ward.getWardId(), monthStart, statEnd,
                    e.getAdverseEventType(),
                    e == NursingIndicatorEnum.UPPR_RATE ? AdverseAcquiredEnum.HOSPITAL_ACQUIRED.getCode() : null);
            numerator = BigDecimal.valueOf(count);
            denominator = BigDecimal.valueOf(bedDays);
            remark = "分母=该病区 " + statMonth + " 实际占用床日 " + bedDays + "（统计末日 " + statEnd + "）；分子="
                    + (e == NursingIndicatorEnum.FALL_RATE
                    ? "跌倒/坠床 " + count + " 例"
                    : "院内获得压疮 " + count + " 例（入院带入留档但不进分子）");
        }
        BigDecimal rate = e.rate(numerator, denominator);
        BigDecimal target = e.targetDecimal();

        BizNursingQcIndicator row = new BizNursingQcIndicator();
        row.setId(IdWorker.getId());
        row.setWardId(ward.getWardId());
        row.setWardName(ward.getWardName());
        row.setDeptId(ward.getDeptId());
        row.setDeptName(ward.getDeptName());
        row.setStatMonth(statMonth);
        row.setIndicatorCode(e.getCode());
        row.setIndicatorName(e.getLabel());
        row.setUnit(e.getUnit());
        row.setNumerator(numerator);
        row.setDenominator(denominator);
        row.setRateValue(rate);
        row.setTargetValue(target);
        row.setReachedFlag(rate == null || target == null ? null : (rate.compareTo(target) >= 0 ? 1 : 0));
        row.setSourceType(e.getSourceType());
        row.setReportStatus(NursingQcReportEnum.UNREPORTED.getCode());
        row.setCalcTime(LocalDateTime.now());
        String operator = cut(operatorUser.getRealName(), OPERATOR_MAX);
        row.setCreateBy(operator);
        row.setUpdateBy(operator);
        row.setRemark(cut(remark, TEXT_MAX));
        return row;
    }

    private NurseQcVO.CheckRow requireCheck(Long id) {
        // ②非web入口：多个 service 方法共用的参数守卫，HTTP 必填已由 DTO @NotNull + @Valid / @RequestParam 收口
        if (id == null) {
            throw new BusinessException("缺少检查单ID");
        }
        NurseQcVO.CheckRow row = checkMapper.selectCheckById(id);
        if (row == null) {
            throw new BusinessException("检查单不存在");
        }
        if (!deptScopeProvider.canAccessDept(row.getDeptId())) {
            throw new BusinessException("无权操作「" + row.getWardName() + "」的检查单（不在当前岗位的数据范围内）");
        }
        fillCheckText(row);
        return row;
    }

    private NurseQcVO.Ward requireWard(Long wardId) {
        // ②非web入口：多个 service 方法共用的参数守卫，HTTP 必填已由各 DTO @NotNull + @Valid 收口
        if (wardId == null) {
            throw new BusinessException("请选择病区");
        }
        NurseQcVO.Ward ward = checkMapper.selectWard(wardId);
        if (ward == null) {
            throw new BusinessException("病区不存在或已停用");
        }
        return ward;
    }

    /**
     * 可操作的病区：存在、启用，且在当前岗位数据范围内
     */
    private NurseQcVO.Ward requireVisibleWard(Long wardId) {
        NurseQcVO.Ward ward = requireWard(wardId);
        if (!deptScopeProvider.canAccessDept(ward.getDeptId())) {
            throw new BusinessException("无权操作「" + ward.getWardName() + "」的护理质控数据（不在当前岗位的数据范围内）");
        }
        return ward;
    }

    /**
     * 检查人：空=当前登录人；传了就必须是在职人员，姓名一律取库里的快照。
     *
     * <p>留空分支也必须过 {@link #checkMapper} 的 selectInspector 同一道在职闸 —— 曾经直接拿
     * {@code UserUtils.getCurrentUser().getEmployeeId()} 落库，admin 的 emp_id=1 在员工里是
     * <b>status=0 的停用员工</b>，写出一张 inspector_id 过不了自己校验的检查单：
     * 下次编辑回传就被「检查人不存在或已停用」拒掉，这张单从此改不动。写入侧与校验侧必须同一把尺子，
     * 过不了闸就不记检查人（留 NULL），而不是写一个注定非法的值。
     */
    private NurseQcVO.Inspector resolveInspector(Long inspectorId) {
        if (inspectorId == null) {
            Long currentEmpId = UserUtils.getCurrentUser().getEmployeeId();
            return currentEmpId == null ? null : checkMapper.selectInspector(currentEmpId);
        }
        NurseQcVO.Inspector inspector = checkMapper.selectInspector(inspectorId);
        if (inspector == null) {
            throw new BusinessException("检查人不存在或已停用");
        }
        return inspector;
    }

    private int requireCategory(Integer category) {
        NursingQcCategoryEnum e = NursingQcCategoryEnum.fromCode(category);
        if (e == null) {
            throw new BusinessException("检查类别非法（" + NursingQcCategoryEnum.whitelistText() + "）");
        }
        return e.getCode();
    }

    private NursingIndicatorEnum requireIndicator(String code) {
        NursingIndicatorEnum e = NursingIndicatorEnum.fromCode(trimToNull(code));
        if (e == null) {
            throw new BusinessException("统计指标非法（" + NursingIndicatorEnum.whitelistText() + "）");
        }
        return e;
    }

    private void fillItemDefText(NurseQcVO.ItemDef item) {
        item.setCategoryName(NursingQcCategoryEnum.getText(item.getCategory()));
    }

    private void fillCheckText(NurseQcVO.CheckRow row) {
        row.setCategoryName(NursingQcCategoryEnum.getText(row.getCategory()));
        row.setStatusText(NursingQcStatusEnum.getText(row.getStatus()));
    }

    private void fillLedgerText(NurseQcVO.LedgerRow row) {
        NursingIndicatorEnum e = NursingIndicatorEnum.fromCode(row.getIndicatorCode());
        if (e != null) {
            row.setIndicatorName(e.getLabel());
            row.setUnit(e.getUnit());
            row.setTargetValue(e.targetDecimal());
            row.setReachedFlag(reachedFlag(e, row.getRateValue(), row.getTargetValue()));
        }
        row.setReachedText(reachedText(row.getReachedFlag()));
        row.setReportStatusText(NursingQcReportEnum.getText(row.getReportStatus()));
    }

    private void fillKpiText(NurseQcVO.Kpi kpi) {
        NursingIndicatorEnum e = NursingIndicatorEnum.fromCode(kpi.getIndicatorCode());
        boolean higherIsBetter = e == null || e.higherIsBetter();
        kpi.setHigherIsBetter(higherIsBetter);
        kpi.setReachedFlag(reachedFlag(e, kpi.getRateValue(), kpi.getTargetValue()));
        if (kpi.getWardCount() == null || kpi.getWardCount() == 0) {
            kpi.setReachedText("本月未重算");
            return;
        }
        kpi.setReachedText(reachedText(kpi.getReachedFlag()));
    }

    /**
     * 当前岗位可见科室；null=不收口（全院），空集合用 -1 兜住，避免 IN () 语法错
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = deptScopeProvider.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return null;
        }
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        return allowed.isEmpty() ? List.of(-1L) : List.copyOf(allowed);
    }
}