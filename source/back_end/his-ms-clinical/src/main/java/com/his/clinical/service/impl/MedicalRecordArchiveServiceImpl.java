package com.his.clinical.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.utils.UserUtils;
import com.his.clinical.dto.PaperSignVerifyDTO;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizMedicalRecordArchive;
import com.his.common.enums.ArchiveStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.clinical.mapper.BizMedicalRecordArchiveMapper;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.clinical.service.MedicalRecordArchiveService;
import com.his.clinical.vo.BizMedicalRecordArchiveVO;
import com.his.clinical.vo.MedicalRecordArchiveCountVO;
import com.his.clinical.vo.MessagePayloadVO;
import com.his.common.enums.BizTypeEnum;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.SysAuditLogService;
import com.his.spi.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 病历归档服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordArchiveServiceImpl extends ServiceImpl<BizMedicalRecordArchiveMapper, BizMedicalRecordArchive> implements MedicalRecordArchiveService {

    /**
     * 门诊病历归档截止日：诊疗结束后的首个工作日（跳过周六、周日）。
     * <p>依据《医疗机构病历管理规定》第十二条——门诊病历由接诊医师书写后，就诊结束即应由医疗机构归档。
     * 工程口径取「就诊次日；次日遇周末顺延到周一」，不再用固定 3 天。
     */
    private static LocalDate archiveDeadline(LocalDate visitDate) {
        if (visitDate == null) {
            return null;
        }
        LocalDate day = visitDate.plusDays(1);
        while (day.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || day.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            day = day.plusDays(1);
        }
        return day;
    }

    /**
     * 纸质签名核验状态：已核验。纸质核验是替代凭证，不写回病历 sign_status。
     */
    private static final int PAPER_VERIFIED = 1;
    /**
     * 归档签名方式：电子签名 / 纸质签名核验
     */
    private static final int SIGN_METHOD_ELECTRONIC = 1;
    private static final int SIGN_METHOD_PAPER = 2;

    private final SysMessageService sysMessageService;
    private final SysAuditLogService sysAuditLogService;
    private final DeptScopeService deptScopeService;
    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    @Override
    public PageResult<BizMedicalRecordArchiveVO> selectArchivePage(Long patientId, Integer archiveStatus, String keyword,
                                                                   int pageNum, int pageSize) {
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        LambdaQueryWrapper<BizMedicalRecordArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(scope != null, BizMedicalRecordArchive::getDeptId, scope)
                .eq(patientId != null, BizMedicalRecordArchive::getPatientId, patientId)
                .eq(archiveStatus != null, BizMedicalRecordArchive::getArchiveStatus, archiveStatus)
                .and(TextUtil.hasText(keyword), w -> w
                        .like(BizMedicalRecordArchive::getRecordNo, keyword)
                        .or().like(BizMedicalRecordArchive::getPatientName, keyword))
                .orderByDesc(BizMedicalRecordArchive::getCreateTime)
                .orderByDesc(BizMedicalRecordArchive::getId);

        Page<BizMedicalRecordArchive> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizMedicalRecordArchiveVO> voList = page.getRecords().stream()
                .map(this::toVo).collect(Collectors.toList());
        fillSignStatus(voList);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public BizMedicalRecordArchiveVO getArchiveDetail(Long archiveId) {
        BizMedicalRecordArchive entity = this.getById(archiveId);
        if (entity == null) {
            return null;
        }
        deptScopeService.assertDeptAccessible(entity.getDeptId());
        BizMedicalRecordArchiveVO vo = toVo(entity);
        fillSignStatus(java.util.Collections.singletonList(vo));
        return vo;
    }

    /**
     * 列表/详情补签名状态：锚点在病历行（biz_medical_record.sign_status），归档表不冗余，
     * 批量回查一次，供前端把「待补签」行标出来。
     */
    private void fillSignStatus(List<BizMedicalRecordArchiveVO> voList) {
        List<Long> recordIds = voList.stream()
                .map(BizMedicalRecordArchiveVO::getRecordId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (recordIds.isEmpty()) {
            return;
        }
        Map<Long, Integer> signMap = bizMedicalRecordMapper.selectBatchIds(recordIds).stream()
                .collect(Collectors.toMap(BizMedicalRecord::getId,
                        r -> r.getSignStatus() == null ? ObjectSignStatusEnum.UNSIGNED.getCode() : r.getSignStatus()));
        for (BizMedicalRecordArchiveVO vo : voList) {
            vo.setSignStatus(signMap.get(vo.getRecordId()));
        }
    }

    private BizMedicalRecordArchiveVO toVo(BizMedicalRecordArchive entity) {
        BizMedicalRecordArchiveVO vo = new BizMedicalRecordArchiveVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyPaperSign(PaperSignVerifyDTO dto) {
        BizMedicalRecordArchive archive = this.getById(dto.getArchiveId());
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        deptScopeService.assertDeptAccessible(archive.getDeptId());
        if (archive.getArchiveStatus() == null || archive.getArchiveStatus() != ArchiveStatusEnum.PENDING.getCode()) {
            throw new BusinessException("仅待归档病历可登记纸质签名核验");
        }
        if (archive.getPaperVerifyStatus() != null && archive.getPaperVerifyStatus() == PAPER_VERIFIED) {
            throw new BusinessException("该病历已完成纸质签名核验，不能重复登记");
        }
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getEmployeeId() == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizMedicalRecord record = bizMedicalRecordMapper.selectById(archive.getRecordId());
        if (record != null && record.getSignStatus() != null
                && record.getSignStatus() == ObjectSignStatusEnum.SIGNED.getCode()) {
            throw new BusinessException("该病历已完成电子签名，无需纸质核验");
        }

        // 只登记纸质核验凭证，不改 biz_medical_record.sign_status：纸质签名与电子签名证据形态不同，
        // 回写等于伪造电子签名，归档后无法验签。
        archive.setPaperVerifyStatus(PAPER_VERIFIED);
        archive.setPaperCertNo(dto.getPaperCertNo());
        archive.setPaperVerifyById(user.getEmployeeId());
        archive.setPaperVerifyByName(user.getRealName());
        archive.setPaperVerifyTime(TimeUtil.nowSeconds());
        archive.setPaperVerifyRemark(dto.getRemark());
        archive.setSignMethod(SIGN_METHOD_PAPER);
        boolean success = this.updateById(archive);
        if (!success) {
            throw new BusinessException("纸质签名核验登记失败");
        }
        sysAuditLogService.record(user.getEmployeeId(), user.getRealName(), "病历归档", "纸质签名核验",
                "biz_medical_record_archive", archive.getId(),
                String.format("归档单号=%s；纸质件编号=%s；核验说明=%s",
                        archive.getArchiveNo(), dto.getPaperCertNo(), dto.getRemark()), true, null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archive(Long archiveId) {
        BizMedicalRecordArchive archive = this.getById(archiveId);
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        deptScopeService.assertDeptAccessible(archive.getDeptId());
        if (archive.getArchiveStatus() != 1) {
            throw new BusinessException("当前状态不允许归档");
        }
        if (!doArchive(archive)) {
            throw new BusinessException("该病历未完成医师电子签名，也未登记纸质签名核验，暂不允许归档");
        }
        return true;
    }

    /**
     * 归档签名闸门与状态推进：电子签名已签，或纸质签名已由病案管理科核验，二者之一即可放行。
     * <p>两条都缺失说明签名证据链不完整，保持「待补签」不进正式病案库。
     *
     * @return false 表示签名证据不足、本次未归档；true 表示已推进为已归档
     */
    private boolean doArchive(BizMedicalRecordArchive archive) {
        BizMedicalRecord record = bizMedicalRecordMapper.selectById(archive.getRecordId());
        boolean signed = record != null && record.getSignStatus() != null
                && record.getSignStatus() == ObjectSignStatusEnum.SIGNED.getCode();
        boolean paperVerified = archive.getPaperVerifyStatus() != null
                && archive.getPaperVerifyStatus() == PAPER_VERIFIED;
        if (record != null && !signed && !paperVerified) {
            return false;
        }
        archive.setSignMethod(signed ? SIGN_METHOD_ELECTRONIC
                : (paperVerified ? SIGN_METHOD_PAPER : archive.getSignMethod()));

        archive.setArchiveStatus(ArchiveStatusEnum.ARCHIVED.getCode());
        archive.setArchiveTime(TimeUtil.nowSeconds());
        return this.updateById(archive);
    }

    @Override
    public int autoArchiveSigned() {
        // 当日就诊留出医生补正时间：只处理就诊日期在昨天及以前的病案，次日零点后的定时任务自动归档。
        List<BizMedicalRecordArchive> pending = this.list(new LambdaQueryWrapper<BizMedicalRecordArchive>()
                .eq(BizMedicalRecordArchive::getArchiveStatus, ArchiveStatusEnum.PENDING.getCode())
                .eq(BizMedicalRecordArchive::getDelFlag, 0)
                .le(BizMedicalRecordArchive::getVisitDate, LocalDate.now().minusDays(1))
                .orderByAsc(BizMedicalRecordArchive::getVisitDate));
        if (pending.isEmpty()) {
            return 0;
        }
        int archived = 0;
        for (BizMedicalRecordArchive archive : pending) {
            try {
                // 不加整体事务：逐条独立提交，一条脏数据失败不会把同一轮其他病案的归档一起回滚
                if (doArchive(archive)) {
                    archived++;
                    log.info("[病历归档] 自动归档成功 archiveId={} recordNo={} signMethod={}",
                            archive.getId(), archive.getRecordNo(), archive.getSignMethod());
                }
            } catch (Exception ex) {
                // 单条失败不中断整轮：缺签名证据/脏数据的病案留存待归档，继续走人工与催办
                log.warn("[病历归档] 自动归档失败 archiveId={} recordNo={}",
                        archive.getId(), archive.getRecordNo(), ex);
            }
        }
        log.info("[病历归档] 自动归档扫描完成：待归档 {} 份，本轮归档 {} 份", pending.size(), archived);
        return archived;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean seal(Long archiveId) {
        BizMedicalRecordArchive archive = this.getById(archiveId);
        if (archive == null) {
            throw new BusinessException("归档记录不存在");
        }
        deptScopeService.assertDeptAccessible(archive.getDeptId());
        if (archive.getArchiveStatus() != 2) {
            throw new BusinessException("当前状态不允许封存");
        }

        archive.setArchiveStatus(ArchiveStatusEnum.SEALED.getCode());
        archive.setSealTime(TimeUtil.nowSeconds());
        return this.updateById(archive);
    }

    // 三态计数

    @Override
    public MedicalRecordArchiveCountVO statusCount() {
        // 只查状态列，在内存里归并：本表量级（病案）远达不到需要 SQL 聚合调优的程度，
        // 且列少、单表，避免为 3 个数字写 3 条 count(*)。
        List<Long> scope = deptScopeService.scopedDeptIds(null);
        List<BizMedicalRecordArchive> all = this.list(new LambdaQueryWrapper<BizMedicalRecordArchive>()
                .select(BizMedicalRecordArchive::getArchiveStatus)
                .eq(BizMedicalRecordArchive::getDelFlag, 0)
                .in(scope != null, BizMedicalRecordArchive::getDeptId, scope));
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
        LocalDate today = LocalDate.now();
        // 先粗筛：截止日最早也是就诊次日，所以就诊日期在今天及以后的病案不可能超期
        List<BizMedicalRecordArchive> pendingList = this.list(new LambdaQueryWrapper<BizMedicalRecordArchive>()
                .eq(BizMedicalRecordArchive::getArchiveStatus, ArchiveStatusEnum.PENDING.getCode())
                .eq(BizMedicalRecordArchive::getDelFlag, 0)
                .isNotNull(BizMedicalRecordArchive::getDoctorId)
                .le(BizMedicalRecordArchive::getVisitDate, today.minusDays(1))
                .orderByAsc(BizMedicalRecordArchive::getVisitDate));
        if (pendingList.isEmpty()) {
            return 0;
        }

        LocalDateTime sinceToday = TimeUtil.dayStart(today);
        int sent = 0;
        int overdueCount = 0;
        for (BizMedicalRecordArchive archive : pendingList) {
            try {
                // 截止日 = 诊疗结束后的首个工作日；今天等于截止日还算「按期」，次日起才算超期
                LocalDate deadline = archiveDeadline(archive.getVisitDate());
                if (deadline == null || !today.isAfter(deadline)) {
                    continue;
                }
                overdueCount++;
                // 每份病历每天最多一条：这是每日提醒，不是每次调用都发（定时 + 手动补跑共用，必须可重入）
                long todaySent = sysMessageService.countSentSince(BizTypeEnum.EMR_ARCHIVE.getType(), archive.getId(), sinceToday);
                if (todaySent > 0) {
                    continue;
                }
                long overdueDays = ChronoUnit.DAYS.between(deadline, today);
                String content = String.format(
                        "您于 %s 为患者 %s（病历号 %s，科室 %s）书写的门诊病历，归档截止日为 %s（诊疗结束后首个工作日），"
                                + "现已超期 %d 天未归档，请及时完成归档。",
                        archive.getVisitDate(),
                        archive.getPatientName() == null ? "未知" : archive.getPatientName(),
                        archive.getRecordNo(),
                        archive.getDeptName() == null ? "未知" : archive.getDeptName(),
                        deadline, overdueDays);
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
        log.info("[病历归档] 超期扫描完成：待归档 {} 份，其中超期 {} 份，发送提醒 {} 条（截止规则：诊疗结束后首个工作日）",
                pendingList.size(), overdueCount, sent);
        return sent;
    }
}
