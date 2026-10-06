package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.DelFlagEnum;
import com.his.common.enums.ReviewStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.PublicHealthSubmitDTO;
import com.his.emr.entity.BizPublicHealthReport;
import com.his.emr.mapper.BizPublicHealthReportMapper;
import com.his.emr.service.PublicHealthReportService;
import com.his.emr.vo.BizPublicHealthReportVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 公卫上报服务实现
 */
@Service
@RequiredArgsConstructor
public class PublicHealthReportServiceImpl extends ServiceImpl<BizPublicHealthReportMapper, BizPublicHealthReport> implements PublicHealthReportService {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Override
    public PageResult<BizPublicHealthReportVO> selectReportPage(Long patientId, Integer reportType, Integer reportStatus,
                                                                String patientName, String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizPublicHealthReport> wrapper = new LambdaQueryWrapper<>();
        // 本实体不是 BaseEntity 子类，没有 @TableLogic —— del_flag 必须显式收口，否则软删的行照样查出来
        wrapper.eq(BizPublicHealthReport::getDelFlag, 0)
                .eq(patientId != null, BizPublicHealthReport::getPatientId, patientId)
                .eq(reportType != null, BizPublicHealthReport::getReportType, reportType)
                .eq(reportStatus != null, BizPublicHealthReport::getReportStatus, reportStatus)
                .orderByDesc(BizPublicHealthReport::getCreateTime);
        // MP 的 like(cond, col, v) 是普通方法调用，实参先求值 —— 必须先 trim 成局部变量再判空，
        // 否则未传参时 getX().trim() 直接 NPE 兜成 500
        String nameKw = patientName == null ? null : patientName.trim();
        wrapper.like(nameKw != null && !nameKw.isEmpty(), BizPublicHealthReport::getPatientName, nameKw);
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && !kw.isEmpty()) {
            wrapper.and(w -> w.like(BizPublicHealthReport::getReportNo, kw)
                    .or().like(BizPublicHealthReport::getDiagnosis, kw)
                    .or().like(BizPublicHealthReport::getDiagnosisCode, kw));
        }

        Page<BizPublicHealthReport> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizPublicHealthReportVO> voList = page.getRecords().stream()
                .map(this::toVo).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public BizPublicHealthReportVO getReportDetail(Long reportId) {
        return toVo(this.getById(reportId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizPublicHealthReportVO submitReport(PublicHealthSubmitDTO submitDTO) {
        BizPublicHealthReport report = new BizPublicHealthReport();
        BeanUtils.copyProperties(submitDTO, report);
        report.setReportNo("PH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", SEQ.incrementAndGet() % 10000));
        report.setReportStatus(ReviewStatusEnum.PENDING.getCode());
        report.setReportTime(LocalDateTime.now());
        if (report.getReportBy() == null || report.getReportBy().isBlank()) {
            report.setReportBy(UserUtils.getCurrentEmployeeName());
        }
        // 本实体不是 BaseEntity 子类，三列不会自动填充，必须显式写
        LocalDateTime now = LocalDateTime.now();
        report.setCreateBy(report.getReportBy());
        report.setCreateTime(now);
        report.setUpdateBy(report.getReportBy());
        report.setUpdateTime(now);
        if (report.getDelFlag() == null) {
            report.setDelFlag(DelFlagEnum.NORMAL.getCode());
        }
        this.save(report);
        return toVo(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditReport(Long reportId, boolean approved, String auditBy, String remark) {
        String auditor = auditBy;
        if (auditor == null || auditor.isBlank()) {
            var current = UserUtils.getCurrentUser();
            auditor = current == null ? null : current.getRealName();
        }
        BizPublicHealthReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("上报记录不存在");
        }
        if (report.getReportStatus() != 1) {
            throw new BusinessException("仅「待审核」的记录可审核（当前状态 " + report.getReportStatus() + "）");
        }

        // 2-审核通过 / 3-审核驳回（驳回后可修改再提，但不再发新号）
        report.setReportStatus(approved ? ReviewStatusEnum.APPROVED.getCode() : ReviewStatusEnum.REJECTED.getCode());
        report.setAuditBy(auditor);
        report.setAuditTime(LocalDateTime.now());
        report.setUpdateBy(auditor);
        report.setUpdateTime(LocalDateTime.now());
        report.setAuditRemark(remark);
        return this.updateById(report);
    }

    private BizPublicHealthReportVO toVo(BizPublicHealthReport entity) {
        BizPublicHealthReportVO vo = new BizPublicHealthReportVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
