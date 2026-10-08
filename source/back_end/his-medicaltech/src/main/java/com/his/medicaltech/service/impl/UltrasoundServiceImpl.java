package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.UltrasoundDTO;
import com.his.medicaltech.entity.BizUltrasoundMeasure;
import com.his.medicaltech.entity.BizUltrasoundRecord;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.enums.UltrasoundAbnormalFlagEnum;
import com.his.medicaltech.mapper.BizUltrasoundMeasureMapper;
import com.his.medicaltech.mapper.BizUltrasoundRecordMapper;
import com.his.medicaltech.service.UltrasoundService;
import com.his.medicaltech.vo.UltrasoundVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 超声亚专业服务
 */
@Service
@RequiredArgsConstructor
public class UltrasoundServiceImpl extends ServiceImpl<BizUltrasoundRecordMapper, BizUltrasoundRecord> implements UltrasoundService {


    private final BizUltrasoundRecordMapper bizUltrasoundRecordMapper;
    private final BizUltrasoundMeasureMapper bizUltrasoundMeasureMapper;
    private final DictCacheService dictCacheService;

    // 查询

    public PageResult<UltrasoundVO.ListVO> pageVO(UltrasoundDTO.Query q) {
        LambdaQueryWrapper<BizUltrasoundRecord> w = new LambdaQueryWrapper<>();
        w.eq(TextUtil.hasText(q.getRecordNo()), BizUltrasoundRecord::getRecordNo, q.getRecordNo())
                .eq(q.getPatientId() != null, BizUltrasoundRecord::getPatientId, q.getPatientId())
                .eq(q.getUsType() != null, BizUltrasoundRecord::getUsType, q.getUsType())
                .eq(q.getStatus() != null, BizUltrasoundRecord::getStatus, q.getStatus())
                .like(TextUtil.hasText(q.getPatientName()), BizUltrasoundRecord::getPatientName, TextUtil.trim(q.getPatientName()))
                .ge(q.getStartDate() != null, BizUltrasoundRecord::getVisitDate, q.getStartDate())
                .le(q.getEndDate() != null, BizUltrasoundRecord::getVisitDate, q.getEndDate())
                .orderByDesc(BizUltrasoundRecord::getId);
        Page<BizUltrasoundRecord> page = bizUltrasoundRecordMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toListVo(page.getRecords()));
    }

    public UltrasoundVO.StatsVO stats() {
        UltrasoundVO.StatsVO vo = new UltrasoundVO.StatsVO();
        vo.setTotal(bizUltrasoundRecordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                .ne(BizUltrasoundRecord::getStatus, InsRecordStatusEnum.CANCELLED.getCode())));
        vo.setPending(count(InsRecordStatusEnum.REGISTERED.getCode()) + count(InsRecordStatusEnum.SIGNED_IN.getCode()));
        vo.setExamining(count(InsRecordStatusEnum.CHECKING.getCode()));
        vo.setPendingAudit(count(InsRecordStatusEnum.RESULTED.getCode()));
        vo.setPublished(count(InsRecordStatusEnum.PUBLISHED.getCode()));
        vo.setTodayCount(bizUltrasoundRecordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                .ge(BizUltrasoundRecord::getCreateTime, TimeUtil.dayStart(LocalDate.now()))));
        return vo;
    }

    private long count(int status) {
        return bizUltrasoundRecordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>().eq(BizUltrasoundRecord::getStatus, status));
    }

    public UltrasoundVO.DetailVO getDetail(Long recordId) {
        BizUltrasoundRecord r = require(recordId);
        UltrasoundVO.DetailVO vo = new UltrasoundVO.DetailVO();
        BeanUtils.copyProperties(r, vo);
        vo.setStatusText(dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()));
        vo.setUsTypeText(dictCacheService.getDicDataLabel(DictType.ULTRASOUND_TYPE, r.getUsType()));
        vo.setMeasures(listMeasures(recordId));
        return vo;
    }

    public List<UltrasoundVO.MeasureVO> listMeasures(Long recordId) {
        List<BizUltrasoundMeasure> list = bizUltrasoundMeasureMapper.selectList(new LambdaQueryWrapper<BizUltrasoundMeasure>()
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
            v.setStatusText(dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()));
            v.setUsTypeText(dictCacheService.getDicDataLabel(DictType.ULTRASOUND_TYPE, r.getUsType()));
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
        r.setStatus(InsRecordStatusEnum.REGISTERED.getCode());
        r.setRecordNo(nextRecordNo(visitDate));
        bizUltrasoundRecordMapper.insert(r);
        return r;
    }

    private BizUltrasoundRecord update(UltrasoundDTO.RecordUpsert dto) {
        BizUltrasoundRecord r = require(dto.getId());
        assertMutable(r);
        if (!InsRecordStatusEnum.REGISTERED.is(r.getStatus()) && !InsRecordStatusEnum.SIGNED_IN.is(r.getStatus())) {
            throw new BusinessException("已开始检查的记录不可修改登记信息（当前：" + dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()) + "）");
        }
        BeanUtils.copyProperties(dto, r);
        r.setId(dto.getId());
        bizUltrasoundRecordMapper.updateById(r);
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long recordId) {
        BizUltrasoundRecord r = require(recordId);
        assertMutable(r);
        if (!InsRecordStatusEnum.REGISTERED.is(r.getStatus())) {
            throw new BusinessException("仅「已登记」可签到（当前：" + dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(InsRecordStatusEnum.SIGNED_IN.getCode());
        bizUltrasoundRecordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(UltrasoundDTO.Execute dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (!InsRecordStatusEnum.SIGNED_IN.is(r.getStatus()) && !InsRecordStatusEnum.CHECKING.is(r.getStatus())) {
            throw new BusinessException("请先签到再执行检查（当前：" + dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()) + "）");
        }
        r.setSonographer(TextUtil.hasText(dto.getSonographer()) ? dto.getSonographer() : operatorUser.getRealName());
        if (TextUtil.hasText(dto.getBodyPart())) {
            r.setBodyPart(dto.getBodyPart());
        }
        if (InsRecordStatusEnum.SIGNED_IN.is(r.getStatus())) {
            r.setStatus(InsRecordStatusEnum.CHECKING.getCode());
            r.setExecuteTime(LocalDateTime.now().withNano(0));
        }
        bizUltrasoundRecordMapper.updateById(r);
    }

    /**
     * 测量值整单覆盖式保存：以本次提交为准，未提交的先删
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveMeasures(UltrasoundDTO.MeasureSave dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (r.getStatus() < InsRecordStatusEnum.SIGNED_IN.getCode()) {
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
        bizUltrasoundMeasureMapper.delete(del);

        int sort = 0;
        for (UltrasoundDTO.MeasureItem it : items) {
            if (!TextUtil.hasText(it.getMeasureName())) {
                continue;
            }
            BizUltrasoundMeasure m = new BizUltrasoundMeasure();
            if (it.getId() != null) {
                BizUltrasoundMeasure old = bizUltrasoundMeasureMapper.selectById(it.getId());
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
                bizUltrasoundMeasureMapper.insert(m);
            } else {
                bizUltrasoundMeasureMapper.updateById(m);
            }
        }
        return items.size();
    }

    @Transactional(rollbackFor = Exception.class)
    public void report(UltrasoundDTO.Report dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (!InsRecordStatusEnum.CHECKING.is(r.getStatus())) {
            throw new BusinessException("仅「检查中」的记录可出具报告（当前：" + dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()) + "）");
        }
        r.setFindings(dto.getFindings());
        r.setConclusion(dto.getConclusion());
        r.setSuggestion(dto.getSuggestion());
        r.setStatus(InsRecordStatusEnum.RESULTED.getCode());
        r.setReportBy(operatorUser.getRealName());
        r.setReportTime(LocalDateTime.now().withNano(0));
        bizUltrasoundRecordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void audit(UltrasoundDTO.Audit dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizUltrasoundRecord r = require(dto.getRecordId());
        assertMutable(r);
        if (!InsRecordStatusEnum.RESULTED.is(r.getStatus())) {
            throw new BusinessException("仅「已出报告」可审核（当前：" + dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()) + "）");
        }
        String who = operatorUser.getRealName();
        if (who != null && who.equals(r.getReportBy())) {
            throw new BusinessException("审核人不得是报告医师本人（" + who + "）——超声报告必须两级签署");
        }
        r.setStatus(InsRecordStatusEnum.REVIEWED.getCode());
        r.setAuditBy(who);
        r.setAuditTime(LocalDateTime.now().withNano(0));
        if (TextUtil.hasText(dto.getAuditOpinion())) {
            r.setRemark(TextUtil.cut((r.getRemark() == null ? "" : r.getRemark() + " | 审核意见：") + dto.getAuditOpinion(), 480));
        }
        bizUltrasoundRecordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long recordId) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizUltrasoundRecord r = require(recordId);
        if (InsRecordStatusEnum.PUBLISHED.is(r.getStatus())) {
            throw new BusinessException("该报告已发布");
        }
        if (!InsRecordStatusEnum.REVIEWED.is(r.getStatus())) {
            throw new BusinessException("发布前必须完成审核（当前：" + dictCacheService.getDicDataLabel(DictType.ENDOUS_STATUS, r.getStatus()) + "）");
        }
        r.setStatus(InsRecordStatusEnum.PUBLISHED.getCode());
        r.setPublishBy(operatorUser.getRealName());
        r.setPublishTime(LocalDateTime.now().withNano(0));
        bizUltrasoundRecordMapper.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(UltrasoundDTO.Cancel dto) {
        BizUltrasoundRecord r = require(dto.getRecordId());
        if (InsRecordStatusEnum.PUBLISHED.is(r.getStatus())) {
            throw new BusinessException("已发布的报告不能取消");
        }
        if (InsRecordStatusEnum.CANCELLED.is(r.getStatus())) {
            throw new BusinessException("该记录已取消");
        }
        r.setStatus(InsRecordStatusEnum.CANCELLED.getCode());
        r.setCancelReason(TextUtil.cut(dto.getCancelReason(), 480));
        r.setCancelTime(LocalDateTime.now().withNano(0));
        bizUltrasoundRecordMapper.updateById(r);
    }

    // 内部

    private BizUltrasoundRecord require(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("检查记录ID不能为空");
        }
        BizUltrasoundRecord r = bizUltrasoundRecordMapper.selectById(id);
        if (r == null) {
            throw new BusinessException("超声检查记录不存在：" + id);
        }
        return r;
    }

    private void assertMutable(BizUltrasoundRecord r) {
        if (InsRecordStatusEnum.PUBLISHED.is(r.getStatus()) || InsRecordStatusEnum.CANCELLED.is(r.getStatus())) {
            throw new BusinessException("已" + (InsRecordStatusEnum.PUBLISHED.is(r.getStatus()) ? "发布" : "取消") + "的记录不可再操作");
        }
    }

    /**
     * 按参考范围判异常。范围为空 / 解析不了 / 值不是数字 → 一律 ABN_NORMAL，
     * 但展示文案会标明「未判定」，不把"没判"伪装成"正常"。
     */
    private Integer judgeAbnormal(String value, String range) {
        if (!TextUtil.hasText(value) || !TextUtil.hasText(range)) {
            return UltrasoundAbnormalFlagEnum.NORMAL.getCode();
        }
        BigDecimal v = toNum(value);
        if (v == null) {
            return UltrasoundAbnormalFlagEnum.NORMAL.getCode();
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
            return UltrasoundAbnormalFlagEnum.NORMAL.getCode();
        }
        if (min == null && max == null) {
            return UltrasoundAbnormalFlagEnum.NORMAL.getCode();
        }
        if (max != null && v.compareTo(max) > 0) {
            return UltrasoundAbnormalFlagEnum.HIGH.getCode();
        }
        if (min != null && v.compareTo(min) < 0) {
            return UltrasoundAbnormalFlagEnum.LOW.getCode();
        }
        return UltrasoundAbnormalFlagEnum.NORMAL.getCode();
    }

    private String abnormalText(BizUltrasoundMeasure m) {
        if (m.getAbnormalFlag() == null) {
            return "未判定";
        }
        if (!TextUtil.hasText(m.getReferenceRange()) || toNum(m.getMeasureValue()) == null) {
            return "未判定";
        }
        if (UltrasoundAbnormalFlagEnum.HIGH.is(m.getAbnormalFlag())) {
            return "偏高";
        }
        if (UltrasoundAbnormalFlagEnum.LOW.is(m.getAbnormalFlag())) {
            return "偏低";
        }
        return "正常";
    }

    private BigDecimal toNum(String s) {
        if (!TextUtil.hasText(s)) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replaceAll("[^0-9.\\-]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private String nextRecordNo(LocalDate date) {
        String day = date.format(DateFormats.COMPACT_DATE);
        long base = bizUltrasoundRecordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                .ge(BizUltrasoundRecord::getCreateTime, TimeUtil.dayStart(date))
                .lt(BizUltrasoundRecord::getCreateTime, TimeUtil.dayStart(date.plusDays(1)))) + 1;
        for (int i = 0; i < 20; i++) {
            String no = "CS" + day + String.format("%03d", base + i);
            if (bizUltrasoundRecordMapper.selectCount(new LambdaQueryWrapper<BizUltrasoundRecord>()
                    .eq(BizUltrasoundRecord::getRecordNo, no)) == 0) {
                return no;
            }
        }
        return "CS" + day + System.currentTimeMillis() % 100000;
    }

}
