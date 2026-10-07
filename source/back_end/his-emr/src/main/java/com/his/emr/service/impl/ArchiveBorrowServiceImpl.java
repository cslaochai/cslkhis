package com.his.emr.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.DelFlagEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.ArchiveBorrowApplyDTO;
import com.his.emr.dto.ArchiveBorrowAuditDTO;
import com.his.emr.dto.ArchiveBorrowQueryPageDTO;
import com.his.emr.entity.BizArchiveBorrow;
import com.his.emr.entity.BizMedicalRecordArchive;
import com.his.emr.enums.ArchiveStatusEnum;
import com.his.emr.enums.BorrowStatusEnum;
import com.his.emr.enums.BorrowTypeEnum;
import com.his.emr.mapper.BizArchiveBorrowMapper;
import com.his.emr.mapper.BizMedicalRecordArchiveMapper;
import com.his.emr.service.ArchiveBorrowService;
import com.his.emr.vo.ArchiveBorrowStatsVO;
import com.his.emr.vo.ArchiveBorrowVO;
import com.his.emr.vo.MessagePayloadVO;
import com.his.system.entity.SysMessage;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * 病案借阅/复印服务实现
 * <p>
 * 状态机：1 待审核 → 2 已借出 → 3 已归还；拒绝 → 4；复印审核通过直接 → 5 已复印。
 * 每步流转留「操作人 id + 姓名 + 意见 + 时间」痕迹链。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveBorrowServiceImpl extends ServiceImpl<BizArchiveBorrowMapper, BizArchiveBorrow> implements ArchiveBorrowService {

    private final BizArchiveBorrowMapper bizArchiveBorrowMapper;
    private final BizMedicalRecordArchiveMapper bizMedicalRecordArchiveMapper;
    private final RedisSequenceService redisSequenceService;
    private final SysMessageService sysMessageService;

    @Override
    public PageResult<ArchiveBorrowVO> page(ArchiveBorrowQueryPageDTO q) {
        Page<ArchiveBorrowVO> page = bizArchiveBorrowMapper.selectBorrowPage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                q.getBorrowNo(), q.getBorrowType(), q.getStatus(), q.getKeyword());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public ArchiveBorrowVO getDetailById(Long id) {
        ArchiveBorrowVO vo = bizArchiveBorrowMapper.selectBorrowById(id);
        if (vo == null) {
            throw new BusinessException("借阅/复印单不存在或已删除");
        }
        return vo;
    }

    @Override
    public ArchiveBorrowStatsVO stats() {
        return bizArchiveBorrowMapper.selectStats();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(ArchiveBorrowApplyDTO dto) {
        if (dto.getBorrowType() != BorrowTypeEnum.BORROW.getCode() && dto.getBorrowType() != BorrowTypeEnum.COPY.getCode()) {
            throw new BusinessException("类型只能为 1 借阅 / 2 复印");
        }
        BizMedicalRecordArchive archive = bizMedicalRecordArchiveMapper.selectById(dto.getArchiveId());
        if (archive == null || archive.getDelFlag() != 0) {
            throw new BusinessException("归档记录不存在");
        }
        // 借阅只能借「已归档」原件；封存病历是法律封存态，原件一律不外借（复印开放）
        if (dto.getBorrowType() == BorrowTypeEnum.BORROW.getCode() && archive.getArchiveStatus() != ArchiveStatusEnum.ARCHIVED.getCode()) {
            throw new BusinessException("借阅仅限已归档病历；待归档病历请先归档，封存病历原件不外借");
        }
        if (dto.getBorrowType() == BorrowTypeEnum.COPY.getCode()
                && archive.getArchiveStatus() != ArchiveStatusEnum.ARCHIVED.getCode()
                && archive.getArchiveStatus() != ArchiveStatusEnum.SEALED.getCode()) {
            throw new BusinessException("复印仅限已归档/已封存病历");
        }
        // 应还日期：借阅必填且不得早于今日（历史日期的申请单只会制造永久超期）
        LocalDate expectReturn = dto.getExpectReturnDate();
        if (dto.getBorrowType() == BorrowTypeEnum.BORROW.getCode()) {
            // B 类保留：条件必填——仅借阅类型要求应还日期（@NotNull 一刀切会挡掉合法的复印申请）
            if (expectReturn == null) {
                throw new BusinessException("借阅必须填写应归还日期");
            }
            if (expectReturn.isBefore(LocalDate.now())) {
                throw new BusinessException("应归还日期不能早于今天");
            }
        } else if (expectReturn != null) {
            throw new BusinessException("复印无需填写应归还日期");
        }
        // 同一份病历同类型存在未结单（待审核/已借出）不允许重复申请
        if (bizArchiveBorrowMapper.countOpenByArchive(dto.getArchiveId(), dto.getBorrowType()) > 0) {
            throw new BusinessException("该病历已有同类型在途申请（待审核或未归还），请勿重复申请");
        }

        Long operatorId = UserUtils.getCurrentUser().getEmployeeId();
        String operatorName = UserUtils.getCurrentUser().getRealName();
        LocalDateTime now = TimeUtil.nowSeconds();

        BizArchiveBorrow b = new BizArchiveBorrow();
        b.setBorrowNo(redisSequenceService.generateArchiveBorrowNo());
        b.setBorrowType(dto.getBorrowType());
        b.setArchiveId(archive.getId());
        b.setRecordNo(archive.getRecordNo());
        b.setPatientName(archive.getPatientName());
        b.setDeptName(archive.getDeptName());
        b.setApplicantId(operatorId);
        b.setApplicantName(operatorName);
        b.setPurpose(dto.getPurpose().trim());
        b.setExpectReturnDate(expectReturn);
        b.setStatus(BorrowStatusEnum.PENDING.getCode());
        b.setDelFlag(DelFlagEnum.NORMAL.getCode());
        b.setCreateTime(now);
        b.setUpdateTime(now);
        if (bizArchiveBorrowMapper.insert(b) != 1) {
            throw new BusinessException("申请失败");
        }
        return b.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(ArchiveBorrowAuditDTO dto) {
        BizArchiveBorrow b = bizArchiveBorrowMapper.selectByIdForUpdate(dto.getId());
        if (b == null || b.getDelFlag() != 0) {
            throw new BusinessException("借阅/复印单不存在或已删除");
        }
        if (b.getStatus() == null || b.getStatus() != BorrowStatusEnum.PENDING.getCode()) {
            throw new BusinessException("当前状态不允许审核（期望状态=1，实际状态=" + b.getStatus() + "）");
        }
        boolean approve = Boolean.TRUE.equals(dto.getApprove());
        // B 类保留：条件必填——仅拒绝时要求审核意见，通过可不填
        if (!approve && (dto.getRemark() == null || dto.getRemark().isBlank())) {
            throw new BusinessException("拒绝必须填写审核意见");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        b.setAuditById(UserUtils.getCurrentUser().getEmployeeId());
        b.setAuditByName(UserUtils.getCurrentUser().getRealName());
        b.setAuditRemark(TextUtil.trimToNull(dto.getRemark()));
        b.setAuditTime(now);
        if (approve) {
            if (b.getBorrowType() == BorrowTypeEnum.BORROW.getCode()) {
                b.setStatus(BorrowStatusEnum.LENT.getCode());
                b.setLendTime(now);
            } else {
                b.setStatus(BorrowStatusEnum.COPIED.getCode());
            }
        } else {
            b.setStatus(BorrowStatusEnum.REJECTED.getCode());
        }
        b.setUpdateTime(now);
        if (bizArchiveBorrowMapper.updateById(b) != 1) {
            throw new BusinessException("审核失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void giveBack(Long id) {
        BizArchiveBorrow b = bizArchiveBorrowMapper.selectByIdForUpdate(id);
        if (b == null || b.getDelFlag() != 0) {
            throw new BusinessException("借阅/复印单不存在或已删除");
        }
        if (b.getStatus() == null || b.getStatus() != BorrowStatusEnum.LENT.getCode()) {
            throw new BusinessException("当前状态不允许归还（期望状态=2 已借出，实际状态=" + b.getStatus() + "）");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        b.setStatus(BorrowStatusEnum.RETURNED.getCode());
        b.setReturnTime(now);
        b.setUpdateTime(now);
        if (bizArchiveBorrowMapper.updateById(b) != 1) {
            throw new BusinessException("归还失败");
        }
    }

    // 借阅超期提醒（发送方，定时 + 手工补跑双路径，可重入）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizArchiveBorrow b = bizArchiveBorrowMapper.selectById(id);
        if (b == null || b.getDelFlag() != 0) {
            throw new BusinessException("借阅/复印单不存在或已删除");
        }
        if (b.getStatus() != null && b.getStatus() != BorrowStatusEnum.PENDING.getCode()) {
            throw new BusinessException("已审核的单据不能删除（留痕完整性要求）");
        }
        // Objects.equals 而不是 .equals()：employeeId 可能为 null（管理账号无员工档），
        // 直接 .equals 会 NPE 变成 500，看起来像服务端坏了
        if (!Objects.equals(UserUtils.getCurrentUser().getEmployeeId(), b.getApplicantId())) {
            throw new BusinessException("只有申请人本人可以删除");
        }
        // ⚠ MP 全局 logic-delete-field=delFlag：updateById 不允许 set del_flag（静默跳过），
        // 软删必须走 MP 的 deleteById → UPDATE ... SET del_flag=1 WHERE id=? AND del_flag=0
        if (bizArchiveBorrowMapper.deleteById(id) != 1) {
            throw new BusinessException("删除失败");
        }
    }

    // 内部

    @Override
    public int notifyOverdue() {
        List<BizArchiveBorrow> overdueList = bizArchiveBorrowMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizArchiveBorrow>()
                        .eq(BizArchiveBorrow::getStatus, BorrowStatusEnum.LENT.getCode())
                        .eq(BizArchiveBorrow::getDelFlag, 0)
                        .isNotNull(BizArchiveBorrow::getExpectReturnDate)
                        .lt(BizArchiveBorrow::getExpectReturnDate, LocalDate.now())
                        .orderByAsc(BizArchiveBorrow::getExpectReturnDate));
        if (overdueList.isEmpty()) {
            return 0;
        }

        LocalDateTime sinceToday = LocalDate.now().atStartOfDay();
        int sent = 0;
        for (BizArchiveBorrow b : overdueList) {
            try {
                // 每张单每天最多一条：定时 + 手动补跑共用，必须可重入
                long todaySent = sysMessageService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getBizType, BizTypeEnum.ARCHIVE_BORROW.getType())
                        .eq(SysMessage::getBizId, b.getId())
                        .ge(SysMessage::getSendTime, sinceToday));
                if (todaySent > 0) {
                    continue;
                }
                long overdueDays = ChronoUnit.DAYS.between(b.getExpectReturnDate(), LocalDate.now());
                String content = String.format(
                        "您申请借阅的病历 %s（患者 %s，科室 %s）应于 %s 归还，已超期 %d 天仍未归还，请尽快归还病案室。",
                        b.getRecordNo(),
                        b.getPatientName() == null ? "未知" : b.getPatientName(),
                        b.getDeptName() == null ? "未知" : b.getDeptName(),
                        b.getExpectReturnDate(), overdueDays);
                MessagePayloadVO msg = new MessagePayloadVO();
                msg.setRecordNo(b.getRecordNo());
                msg.setPatientName(b.getPatientName());
                msg.setOverdueDays(overdueDays);
                msg.setHandlerName(b.getApplicantName());
                String payload = JSONUtil.toJsonStr(msg);
                boolean ok = sysMessageService.sendSystemMessage(b.getApplicantId(), b.getApplicantName(),
                        "病案借阅超期：" + b.getBorrowNo(), content,
                        BizTypeEnum.ARCHIVE_BORROW.getType(), b.getId(), "warning", payload, null);
                if (ok) {
                    sent++;
                }
            } catch (Exception ex) {
                // 单条失败不中断整轮：超期提醒是催办手段，不能因为一条脏数据丢掉其余提醒
                log.warn("[病案借阅] 超期提醒发送失败 borrowId={} applicantId={}", b.getId(), b.getApplicantId(), ex);
            }
        }
        log.info("[病案借阅] 超期扫描完成：超期未还 {} 张，发送提醒 {} 条", overdueList.size(), sent);
        return sent;
    }
}
