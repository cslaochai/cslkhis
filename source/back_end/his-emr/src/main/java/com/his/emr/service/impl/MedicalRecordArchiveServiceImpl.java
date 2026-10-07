package com.his.emr.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.emr.entity.BizMedicalRecordArchive;
import com.his.emr.enums.ArchiveStatusEnum;
import com.his.emr.mapper.BizMedicalRecordArchiveMapper;
import com.his.emr.service.MedicalRecordArchiveService;
import com.his.emr.vo.BizMedicalRecordArchiveVO;
import com.his.emr.vo.MedicalRecordArchiveCountVO;
import com.his.emr.vo.MessagePayloadVO;
import com.his.system.entity.SysMessage;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 病历归档服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordArchiveServiceImpl extends ServiceImpl<BizMedicalRecordArchiveMapper, BizMedicalRecordArchive> implements MedicalRecordArchiveService {

    /**
     * 门诊病历归档时限（天）：就诊后超过该天数仍未归档即提醒。
     * <p>依据是病历管理规定「门诊病历即时归档、最迟不超过患者下一次就诊」的
     * 工程化近似 —— 用固定天数而不是"下一次就诊"，因为下一次就诊无法预测。
     */
    private static final int OVERDUE_DAYS = 3;

    private final SysMessageService sysMessageService;

    @Override
    public PageResult<BizMedicalRecordArchiveVO> selectArchivePage(Long patientId, Integer archiveStatus, String keyword,
                                                                   int pageNum, int pageSize) {
        LambdaQueryWrapper<BizMedicalRecordArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizMedicalRecordArchive::getPatientId, patientId)
                .eq(archiveStatus != null, BizMedicalRecordArchive::getArchiveStatus, archiveStatus)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(BizMedicalRecordArchive::getRecordNo, keyword)
                        .or().like(BizMedicalRecordArchive::getPatientName, keyword))
                .orderByDesc(BizMedicalRecordArchive::getCreateTime)
                .orderByDesc(BizMedicalRecordArchive::getId);

        Page<BizMedicalRecordArchive> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizMedicalRecordArchiveVO> voList = page.getRecords().stream()
                .map(this::toVo).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public BizMedicalRecordArchiveVO getArchiveDetail(Long archiveId) {
        return toVo(this.getById(archiveId));
    }

    private BizMedicalRecordArchiveVO toVo(BizMedicalRecordArchive entity) {
        BizMedicalRecordArchiveVO vo = new BizMedicalRecordArchiveVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archive(Long archiveId) {
        BizMedicalRecordArchive archive = this.getById(archiveId);
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        if (archive.getArchiveStatus() != 1) {
            throw new BusinessException("当前状态不允许归档");
        }

        archive.setArchiveStatus(ArchiveStatusEnum.ARCHIVED.getCode());
        archive.setArchiveTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return this.updateById(archive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean seal(Long archiveId) {
        BizMedicalRecordArchive archive = this.getById(archiveId);
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        if (archive.getArchiveStatus() != 2) {
            throw new BusinessException("当前状态不允许封存");
        }

        archive.setArchiveStatus(ArchiveStatusEnum.SEALED.getCode());
        archive.setSealTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        return this.updateById(archive);
    }

    // 三态计数

    @Override
    public MedicalRecordArchiveCountVO statusCount() {
        // 只查状态列，在内存里归并：本表量级（病案）远达不到需要 SQL 聚合调优的程度，
        // 且列少、单表，避免为 3 个数字写 3 条 count(*)。
        List<BizMedicalRecordArchive> all = this.list(new LambdaQueryWrapper<BizMedicalRecordArchive>()
                .select(BizMedicalRecordArchive::getArchiveStatus)
                .eq(BizMedicalRecordArchive::getDelFlag, 0));
        int pending = 0, archived = 0, sealed = 0;
        for (BizMedicalRecordArchive a : all) {
            Integer st = a.getArchiveStatus();
            if (st == null) {
                continue; // 状态为空的行不计入任何一态，避免伪装成"待归档"
            }
            if (st == ArchiveStatusEnum.PENDING.getCode()) {
                pending++;
            } else if (st == 2) {
                archived++;
            } else if (st == 3) {
                sealed++;
            }
        }
        MedicalRecordArchiveCountVO vo = new MedicalRecordArchiveCountVO();
        vo.setTotal(all.size());
        vo.setPending(pending);
        vo.setArchived(archived);
        vo.setSealed(sealed);
        return vo;
    }

    // emr-arch 发送方：归档超期提醒

    @Override
    public int notifyOverdueArchives() {
        LocalDate deadline = LocalDate.now().minusDays(OVERDUE_DAYS);
        List<BizMedicalRecordArchive> overdueList = this.list(new LambdaQueryWrapper<BizMedicalRecordArchive>()
                .eq(BizMedicalRecordArchive::getArchiveStatus, ArchiveStatusEnum.PENDING.getCode())
                .eq(BizMedicalRecordArchive::getDelFlag, 0)
                .isNotNull(BizMedicalRecordArchive::getDoctorId)
                .le(BizMedicalRecordArchive::getVisitDate, deadline)
                .orderByAsc(BizMedicalRecordArchive::getVisitDate));
        if (overdueList.isEmpty()) {
            return 0;
        }

        LocalDateTime sinceToday = LocalDate.now().atStartOfDay();
        int sent = 0;
        for (BizMedicalRecordArchive archive : overdueList) {
            try {
                // 每份病历每天最多一条：这是每日提醒，不是每次调用都发（定时 + 手动补跑共用，必须可重入）
                long todaySent = sysMessageService.count(new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getBizType, BizTypeEnum.EMR_ARCHIVE.getType())
                        .eq(SysMessage::getBizId, archive.getId())
                        .ge(SysMessage::getSendTime, sinceToday));
                if (todaySent > 0) {
                    continue;
                }
                long overdueDays = archive.getVisitDate() == null
                        ? OVERDUE_DAYS : ChronoUnit.DAYS.between(archive.getVisitDate(), LocalDate.now());
                String content = String.format(
                        "您于 %s 为患者 %s（病历号 %s，科室 %s）书写的门诊病历已超过 %d 天未归档（超期 %d 天），请及时完成归档。",
                        archive.getVisitDate(),
                        archive.getPatientName() == null ? "未知" : archive.getPatientName(),
                        archive.getRecordNo(),
                        archive.getDeptName() == null ? "未知" : archive.getDeptName(),
                        OVERDUE_DAYS, overdueDays);
                MessagePayloadVO msg = new MessagePayloadVO();
                msg.setPatientName(archive.getPatientName());
                msg.setRecordNo(archive.getRecordNo());
                msg.setVisitDate(archive.getVisitDate() == null ? null : archive.getVisitDate().toString());
                msg.setOverdueDays(overdueDays);
                String payload = JSONUtil.toJsonStr(msg);
                boolean ok = sysMessageService.sendSystemMessage(archive.getDoctorId(), archive.getDoctorName(),
                        "病历归档超期：" + archive.getRecordNo(), content,
                        BizTypeEnum.EMR_ARCHIVE.getType(), archive.getId(), "warning", payload, null);
                if (ok) {
                    sent++;
                }
            } catch (Exception ex) {
                // 单条失败不中断整轮：超期提醒是催办手段，不能因为一条脏数据丢掉其余提醒
                log.warn("[病历归档] 超期提醒发送失败 archiveId={} doctorId={}",
                        archive.getId(), archive.getDoctorId(), ex);
            }
        }
        log.info("[病历归档] 超期扫描完成：待归档超期 {} 份，发送提醒 {} 条（时限 {} 天）",
                overdueList.size(), sent, OVERDUE_DAYS);
        return sent;
    }
}
