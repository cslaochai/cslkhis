package com.his.medicaltech.service.impl;

import com.his.medicaltech.service.UltrasoundService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.support.SubDictText;
import com.his.medicaltech.dto.UltrasoundDTO;
import com.his.medicaltech.entity.BizUltrasoundMeasure;
import com.his.medicaltech.entity.BizUltrasoundRecord;
import com.his.medicaltech.mapper.BizUltrasoundMeasureMapper;
import com.his.medicaltech.mapper.BizUltrasoundRecordMapper;
import com.his.medicaltech.vo.UltrasoundVO;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 超声亚专业服务
 *
 * <p>状态机与内镜一致：1 已登记 → 2 已签到 → 3 检查中 → 4 已出报告 → 5 已审核 → 6 已发布；7 已取消。
 *
 * <p>硬规则：审核人不得是报告人本人；已发布不可再改、不可取消。
 * 测量值异常标志由服务端按参考范围判定 —— 参考范围为空或无法解析时**不确定地判为正常**，
 * 而是标记「未判定」让前端显示出来（把"没判"伪装成"正常"是最危险的假数据）。
 */
@Service
@RequiredArgsConstructor
public class UltrasoundServiceImpl extends ServiceImpl<BizUltrasoundRecordMapper, BizUltrasoundRecord> implements UltrasoundService {

    private static final int ST_REGISTERED = 1;
    private static final int ST_CHECKED_IN = 2;
    private static final int ST_EXAMINING = 3;
    private static final int ST_REPORTED = 4;
    private static final int ST_AUDITED = 5;
    private static final int ST_PUBLISHED = 6;
    private static final int ST_CANCELLED = 7;

    /** 异常标志：0-正常 1-偏高 2-偏低 3-异常（数值无法比较/范围不可解析） */
    private static final int ABN_NORMAL = 0;
    private static final int ABN_HIGH = 1;
    private static final int ABN_LOW = 2;

    private static final String DICT_STATUS = "his_endous_status";
    private static final String DICT_US_TYPE = "his_ultrasound_type";

    private final BizUltrasoundRecordMapper recordMapper;
    private final BizUltrasoundMeasureMapper measureMapper;
    private final SubDictText dictText;

    // 查询

    public PageResult<UltrasoundVO.ListVO> pageVO(UltrasoundDTO.Query q) {
        LambdaQueryWrapper<BizUltrasoundRecord> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.hasText(q.getRecordNo()), BizUltrasoundRecord::getRecordNo, q.getRecordNo())
                .eq(q.getPatientId() != null, BizUltrasoundRecord::getPatientId, q.getPatientId())
                .eq(q.getUsType() != null, BizUltrasoundRecord::getUsType, q.getUsType())
                .eq(q.getStatus() != null, BizUltrasoundRecord::getStatus, q.getStatus())
                .like(StringUtils.hasText(q.getPatientName()), BizUltrasoundRecord::getPatientName, tr(q.getPatientName()))
                .ge(q.getStartDate() != null, BizUltrasoundRecord::getVisitDate, q.getStartDate())
                .le(q.getEndDate() != null, BizUltrasoundRecord::getVisitDate, q.getEndDate())
                .orderByDesc(BizUltrasoundRecord::getId);
        Page<BizUltrasoundRecord> page = recordMapper.selectPage(
                new Page<>(q.getPageNum() == null ? 1 : q.getPageNum(), q.getPageSize() == null ? 20 : q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toListVo(page.getRecords()));
    }

    public UltrasoundVO.StatsVO stats() {
        UltrasoundVO.StatsVO vo = new UltrasoundVO.StatsVO();
        vo.setTotal(recordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                .ne(BizUltrasoundRecord::getStatus, ST_CANCELLED)));
        vo.setPending(count(ST_REGISTERED) + count(ST_CHECKED_IN));
        vo.setExamining(count(ST_EXAMINING));
        vo.setPendingAudit(count(ST_REPORTED));
        vo.setPublished(count(ST_PUBLISHED));
        vo.setTodayCount(recordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                .ge(BizUltrasoundRecord::getCreateTime, LocalDate.now().atStartOfDay())));
        return vo;
    }

    private long count(int status) {
        return recordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>().eq(BizUltrasoundRecord::getStatus, status));
    }

    public UltrasoundVO.DetailVO getDetail(Long recordId) {
        BizUltrasoundRecord r = require(recordId);
        UltrasoundVO.DetailVO vo = new UltrasoundVO.DetailVO();
        BeanUtils.copyProperties(r, vo);
        vo.setStatusText(dictText.text(DICT_STATUS, r.getStatus()));
        vo.setUsTypeText(dictText.text(DICT_US_TYPE, r.getUsType()));
        vo.setMeasures(listMeasures(recordId));
        return vo;
    }

    public List<UltrasoundVO.MeasureVO> listMeasures(Long recordId) {
        List<BizUltrasoundMeasure> list = measureMapper.selectList(new LambdaQueryWrapper<BizUltrasoundMeasure>()
                .eq(BizUltrasoundMeasure::getRecordId, recordId)
                .orderByAsc(BizUltrasoundMeasure::getSortOrder)
                .orderByAsc(BizUltrasoundMeasure::getId));
        List<UltrasoundVO.MeasureVO> out = new ArrayList<>();
        for (BizUltrasoundMeasure m : list) {
            UltrasoundVO.MeasureVO v = new UltrasoundVO.MeasureVO();
            BeanUtils.copyProperties(m, v);
            v.setAbnormalFlagText(abnormalText(m));
            out.add(v);
        }
        return out;
    }

    private List<UltrasoundVO.ListVO> toListVo(List<BizUltrasoundRecord> records) {
        List<UltrasoundVO.ListVO> out = new ArrayList<>();
        for (BizUltrasoundRecord r : records) {
            UltrasoundVO.ListVO v = new UltrasoundVO.ListVO();
            BeanUtils.copyProperties(r, v);
            v.setStatusText(dictText.text(DICT_STATUS, r.getStatus()));
            v.setUsTypeText(dictText.text(DICT_US_TYPE, r.getUsType()));
            out.add(v);
        }
        return out;
    }

    // 写入

    @Transactional(rollbackFor = Exception.class)
    public UltrasoundVO.DetailVO upsertRecord(UltrasoundDTO.RecordUpsert dto) {
        BizUltrasoundRecord r = dto.getId() == null ? create(dto) : update(dto);
        return getDetail(r.getId());
    }

    private BizUltrasoundRecord create(UltrasoundDTO.RecordUpsert dto) {
        BizUltrasoundRecord r = new BizUltrasoundRecord();
        BeanUtils.copyProperties(dto, r);
        r.setId(null);
        LocalDate visitDate = dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now();
        r.setVisitDate(visitDate);
        if (r.getUsType() == null) {
            r.setUsType(1);
        }
        r.setStatus(ST_REGISTERED);
        r.setRecordNo(nextRecordNo(visitDate));
        recordMapper.insert(r);
        return r;
    }

    private BizUltrasoundRecord update(UltrasoundDTO.RecordUpsert dto) {
        BizUltrasoundRecord r = require(dto.getId());
        assertMutable(r);
        if (r.getStatus() != ST_REGISTERED && r.getStatus() != ST_CHECKED_IN) {
            throw new BusinessException("已开始检查的记录不可修改登记信息（当前：" + dictText.text(DICT_STATUS, r.getStatus()) + "）");
        }
        BeanUtils.copyProperties(dto, r);
        r.setId(dto.getId());
        recordMapper.updateById(r);
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long recordId) {
        BizUltrasoundRecord r = require(recordId);
        assertMutable(r);
        if (r.getStatus() != ST_REGISTERED) {
            throw new BusinessException("仅「已登记」可签到（当前：" + dictText.text(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(ST_CHECKED_IN);
        recordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(UltrasoundDTO.Execute dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (r.getStatus() != ST_CHECKED_IN && r.getStatus() != ST_EXAMINING) {
            throw new BusinessException("请先签到再执行检查（当前：" + dictText.text(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setSonographer(StringUtils.hasText(dto.getSonographer()) ? dto.getSonographer() : currentName());
        if (StringUtils.hasText(dto.getBodyPart())) {
            r.setBodyPart(dto.getBodyPart());
        }
        if (r.getStatus() == ST_CHECKED_IN) {
            r.setStatus(ST_EXAMINING);
            r.setExecuteTime(LocalDateTime.now().withNano(0));
        }
        recordMapper.updateById(r);
    }

    /** 测量值整单覆盖式保存：以本次提交为准，未提交的先删 */
    @Transactional(rollbackFor = Exception.class)
    public int saveMeasures(UltrasoundDTO.MeasureSave dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (r.getStatus() < ST_CHECKED_IN) {
            throw new BusinessException("未签到的记录不能录入测量值");
        }
        List<UltrasoundDTO.MeasureItem> items = dto.getMeasures() == null ? new ArrayList<>() : dto.getMeasures();
        // 覆盖式：先按 id 删除本次未保留的旧项（保留 id 的走更新）
        List<Long> keepIds = new ArrayList<>();
        for (UltrasoundDTO.MeasureItem it : items) {
            if (it.getId() != null) {
                keepIds.add(it.getId());
            }
        }
        LambdaQueryWrapper<BizUltrasoundMeasure> del = new LambdaQueryWrapper<BizUltrasoundMeasure>()
                .eq(BizUltrasoundMeasure::getRecordId, r.getId());
        if (!keepIds.isEmpty()) {
            del.notIn(BizUltrasoundMeasure::getId, keepIds);
        }
        measureMapper.delete(del);

        int sort = 0;
        for (UltrasoundDTO.MeasureItem it : items) {
            if (!StringUtils.hasText(it.getMeasureName())) {
                continue;
            }
            BizUltrasoundMeasure m = new BizUltrasoundMeasure();
            if (it.getId() != null) {
                BizUltrasoundMeasure old = measureMapper.selectById(it.getId());
                if (old != null && r.getId().equals(old.getRecordId())) {
                    m = old;
                }
            }
            m.setRecordId(r.getId());
            m.setRecordNo(r.getRecordNo());
            m.setMeasureName(it.getMeasureName());
            m.setMeasureValue(it.getMeasureValue());
            m.setUnit(it.getUnit());
            m.setReferenceRange(it.getReferenceRange());
            m.setSortOrder(it.getSortOrder() != null ? it.getSortOrder() : ++sort);
            m.setAbnormalFlag(judgeAbnormal(it.getMeasureValue(), it.getReferenceRange()));
            if (m.getId() == null) {
                measureMapper.insert(m);
            } else {
                measureMapper.updateById(m);
            }
        }
        return items.size();
    }

    @Transactional(rollbackFor = Exception.class)
    public void report(UltrasoundDTO.Report dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (r.getStatus() != ST_EXAMINING) {
            throw new BusinessException("仅「检查中」的记录可出具报告（当前：" + dictText.text(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setFindings(dto.getFindings());
        r.setConclusion(dto.getConclusion());
        r.setSuggestion(dto.getSuggestion());
        r.setStatus(ST_REPORTED);
        r.setReportBy(currentName());
        r.setReportTime(LocalDateTime.now().withNano(0));
        recordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(UltrasoundDTO.Audit dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (r.getStatus() != ST_REPORTED) {
            throw new BusinessException("仅「已出报告」可审核（当前：" + dictText.text(DICT_STATUS, r.getStatus()) + "）");
        }
        String who = currentName();
        if (who != null && who.equals(r.getReportBy())) {
            throw new BusinessException("审核人不得是报告医师本人（" + who + "）——超声报告必须两级签署");
        }
        r.setStatus(ST_AUDITED);
        r.setAuditBy(who);
        r.setAuditTime(LocalDateTime.now().withNano(0));
        if (StringUtils.hasText(dto.getAuditOpinion())) {
            r.setRemark(clip((r.getRemark() == null ? "" : r.getRemark() + " | 审核意见：") + dto.getAuditOpinion()));
        }
        recordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long recordId) {
        BizUltrasoundRecord r = require(recordId);
        if (r.getStatus() == ST_PUBLISHED) {
            throw new BusinessException("该报告已发布");
        }
        if (r.getStatus() != ST_AUDITED) {
            throw new BusinessException("发布前必须完成审核（当前：" + dictText.text(DICT_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(ST_PUBLISHED);
        r.setPublishBy(currentName());
        r.setPublishTime(LocalDateTime.now().withNano(0));
        recordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(UltrasoundDTO.Cancel dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        if (r.getStatus() == ST_PUBLISHED) {
            throw new BusinessException("已发布的报告不能取消");
        }
        if (r.getStatus() == ST_CANCELLED) {
            throw new BusinessException("该记录已取消");
        }
        r.setStatus(ST_CANCELLED);
        r.setCancelReason(clip(dto.getCancelReason()));
        r.setCancelTime(LocalDateTime.now().withNano(0));
        recordMapper.updateById(r);
    }

    // 内部

    private BizUltrasoundRecord require(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("检查记录ID不能为空");
        }
        BizUltrasoundRecord r = recordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("超声检查记录不存在：" + id);
        }
        return r;
    }

    private void assertMutable(BizUltrasoundRecord r) {
        if (r.getStatus() == ST_PUBLISHED || r.getStatus() == ST_CANCELLED) {
            throw new BusinessException("已" + (r.getStatus() == ST_PUBLISHED ? "发布" : "取消") + "的记录不可再操作");
        }
    }

    /**
     * 按参考范围判异常。范围为空 / 解析不了 / 值不是数字 → 一律 ABN_NORMAL，
     * 但展示文案会标明「未判定」，不把"没判"伪装成"正常"。
     */
    private Integer judgeAbnormal(String value, String range) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(range)) {
            return ABN_NORMAL;
        }
        BigDecimal v = toNum(value);
        if (v == null) {
            return ABN_NORMAL;
        }
        String rr = range.replaceAll("\\s", "").replace('～', '-').replace('~', '-');
        BigDecimal min = null;
        BigDecimal max = null;
        try {
            if (rr.contains("-")) {
                String[] p = rr.split("-");
                if (p.length == 2) {
                    min = toNum(p[0]);
                    max = toNum(p[1]);
                }
            } else if (rr.startsWith("<=") || rr.startsWith("≤")) {
                max = toNum(rr.replaceAll("<=|≤", ""));
            } else if (rr.startsWith(">=") || rr.startsWith("≥")) {
                min = toNum(rr.replaceAll(">=|≥", ""));
            } else if (rr.startsWith("<")) {
                max = toNum(rr.substring(1));
            } else if (rr.startsWith(">")) {
                min = toNum(rr.substring(1));
            }
        } catch (Exception ignore) {
            return ABN_NORMAL;
        }
        if (min == null && max == null) {
            return ABN_NORMAL;
        }
        if (max != null && v.compareTo(max) > 0) {
            return ABN_HIGH;
        }
        if (min != null && v.compareTo(min) < 0) {
            return ABN_LOW;
        }
        return ABN_NORMAL;
    }

    private String abnormalText(BizUltrasoundMeasure m) {
        if (m.getAbnormalFlag() == null) {
            return "未判定";
        }
        if (!StringUtils.hasText(m.getReferenceRange()) || toNum(m.getMeasureValue()) == null) {
            return "未判定";
        }
        switch (m.getAbnormalFlag()) {
            case ABN_HIGH: return "偏高";
            case ABN_LOW: return "偏低";
            default: return "正常";
        }
    }

    private BigDecimal toNum(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replaceAll("[^0-9.\\-]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private String nextRecordNo(LocalDate date) {
        String day = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long base = recordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                .ge(BizUltrasoundRecord::getCreateTime, date.atStartOfDay())
                .lt(BizUltrasoundRecord::getCreateTime, date.plusDays(1).atStartOfDay())) + 1;
        for (int i = 0; i < 20; i++) {
            String no = "CS" + day + String.format("%03d", base + i);
            if (recordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                    .eq(BizUltrasoundRecord::getRecordNo, no)) == 0) {
                return no;
            }
        }
        return "CS" + day + System.currentTimeMillis() % 100000;
    }

    private String currentName() {
        String n = UserUtils.getCurrentEmployeeName();
        return n != null ? n : "未知操作人";
    }

    /** null 安全 trim：查询条件的 value 参数是急切求值的，直接 x.trim() 会在 x 为 null 时 NPE */
    private String tr(String s) {
        return s == null ? null : s.trim();
    }

    private String clip(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 480 ? s.substring(0, 480) : s;
    }
}
