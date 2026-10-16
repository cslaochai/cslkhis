package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.util.SensitiveMaskUtil;
import com.his.emr.dto.OutpatientLogQueryDTO;
import com.his.emr.entity.SysInfectiousDisease;
import com.his.emr.mapper.OutpatientLogMapper;
import com.his.emr.mapper.SysInfectiousDiseaseMapper;
import com.his.emr.service.OutpatientLogService;
import com.his.emr.vo.OutpatientLogListVO;
import com.his.emr.vo.OutpatientLogStatsVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 门诊日志实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutpatientLogServiceImpl extends ServiceImpl<SysInfectiousDiseaseMapper, SysInfectiousDisease> implements OutpatientLogService {

    /**
     * 目录为空时的正则哨兵：^$ 永不命中任何诊断编码（空串命中会被当成"什么都能报"）
     */
    private static final String NO_MATCH_REGEX = "^$";

    private final OutpatientLogMapper outpatientLogMapper;
    private final SysInfectiousDiseaseMapper sysInfectiousDiseaseMapper;
    private final DeptScopeService deptScopeService;

    @Override
    public PageResult<OutpatientLogListVO> listPage(OutpatientLogQueryDTO query) {
        Map<String, String> prefixes = loadReportablePrefixes();
        Page<OutpatientLogListVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<OutpatientLogListVO> result =
                outpatientLogMapper.selectLogPage(page, query, toRegex(prefixes), deptScopeService.scopedDeptIds(query.getDeptId()));
        List<OutpatientLogListVO> records = result.getRecords();
        for (OutpatientLogListVO vo : records) {
            vo.setPhoneMasked(SensitiveMaskUtil.maskPhone(vo.getPhone()));
            vo.setPhone(null);
            if (Boolean.TRUE.equals(vo.getReportable())) {
                vo.setMatchedDiseaseName(matchDisease(vo.getDiagnosisCode(), prefixes));
            }
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getPages(), records);
    }

    @Override
    public OutpatientLogStatsVO stats(OutpatientLogQueryDTO query) {
        OutpatientLogStatsVO stats =
                outpatientLogMapper.selectLogStats(query, toRegex(loadReportablePrefixes()),
                        deptScopeService.scopedDeptIds(query.getDeptId()));
        return stats == null ? new OutpatientLogStatsVO() : stats;
    }

    @Override
    public PageResult<OutpatientLogListVO> myPendingPage(OutpatientLogQueryDTO query) {
        applySelfScope(query);
        return listPage(query);
    }

    @Override
    public OutpatientLogStatsVO myPendingStats(OutpatientLogQueryDTO query) {
        applySelfScope(query);
        return stats(query);
    }

    private void applySelfScope(OutpatientLogQueryDTO query) {
        CurrentUser user = UserUtils.getCurrentUser();
        query.setDoctorId(user == null || user.getEmployeeId() == null ? -1L : user.getEmployeeId());
        query.setDeptId(null);
        query.setReportableOnly(true);
        query.setReportedFilter(1);
    }

    /**
     * ICD 前缀 → 病种名（同名多前缀时后者覆盖无所谓，展示用 best effort）
     */
    private Map<String, String> loadReportablePrefixes() {
        List<SysInfectiousDisease> diseases = sysInfectiousDiseaseMapper.selectList(
                new LambdaQueryWrapper<SysInfectiousDisease>()
                        .eq(SysInfectiousDisease::getStatus, 1));
        Map<String, String> prefixes = new LinkedHashMap<>();
        for (SysInfectiousDisease d : diseases) {
            for (String seg : String.valueOf(d.getIcd10()).split(",")) {
                expand(seg.trim()).forEach(p -> prefixes.putIfAbsent(p, d.getDiseaseName()));
            }
        }
        return prefixes;
    }

    /**
     * {@code B15-B19} 摊成 B15..B19；{@code U07.1} 原样；非法段忽略（脏字典不该让整个日志页 500）
     */
    private List<String> expand(String seg) {
        List<String> out = new ArrayList<>();
        if (seg.isEmpty()) {
            return out;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^([A-Z])(\\d{2})-\\1(\\d{2})$").matcher(seg);
        if (m.matches()) {
            int from = Integer.parseInt(m.group(2)), to = Integer.parseInt(m.group(3));
            if (from <= to) {
                for (int i = from; i <= to; i++) {
                    out.add(m.group(1) + String.format("%02d", i));
                }
            }
            return out;
        }
        if (seg.matches("^[A-Z]\\d{2}([.]\\d+)?$")) {
            out.add(seg);
        } else if (!seg.equals("null")) {
            log.warn("法定传染病目录 ICD 段无法解析，已忽略：{}", seg);
        }
        return out;
    }

    private String toRegex(Map<String, String> prefixes) {
        if (prefixes.isEmpty()) {
            return NO_MATCH_REGEX;
        }
        return prefixes.keySet().stream()
                .map(p -> p.replace(".", "\\."))
                .reduce((a, b) -> a + "|" + b)
                .orElse(NO_MATCH_REGEX);
    }

    /**
     * 前缀可能被多个病种共享（如 A00/A01 都是腹泻类），命中几个拼几个
     */
    private String matchDisease(String diagnosisCode, Map<String, String> prefixes) {
        if (diagnosisCode == null) {
            return null;
        }
        List<String> names = prefixes.entrySet().stream()
                .filter(e -> diagnosisCode.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .distinct()
                .toList();
        return names.isEmpty() ? null : String.join("、", names);
    }
}
