package com.his.emr.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.Constants;
import com.his.common.base.PageResult;
import com.his.common.enums.DelFlagEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.*;
import com.his.emr.dto.*;
import com.his.emr.entity.BizDisputeCase;
import com.his.emr.entity.BizDisputeFlow;
import com.his.emr.enums.DisputeStatusEnum;
import com.his.emr.enums.SealStatusEnum;
import com.his.emr.mapper.BizDisputeCaseMapper;
import com.his.emr.mapper.BizDisputeFlowMapper;
import com.his.emr.service.DisputeService;
import com.his.emr.service.MedicalRecordArchiveService;
import com.his.emr.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 医疗纠纷 / 投诉登记服务实现。
 *
 * <p>口径：
 * <ol>
 *   <li>状态机单向：1待受理 → 2调查中 → 3处理中 → 4已结案（终态）；未结案 → 5已撤销（终态）。
 *       终态一律不可再编辑 —— 改历史结论等于把台账变成草稿本。</li>
 *   <li>受理即联动封存：need_seal=1 时自动封存该患者「已归档」病案；
 *       暂无已归档病历 → seal_status=2（待归档后封存），主单上明确标注，杜绝"以为封了其实没封"。</li>
 *   <li>结案必须收口：处理途径 + 责任认定 + 赔偿金额（无赔偿填 0）+ 结论，缺一项不结案。</li>
 *   <li>跟踪流水只增不改，状态只在主单；按钮可用性（can*）一律服务端派生。</li>
 *   <li>投诉人电话库里存明文、出参脱敏；操作人服务端取当前登录人。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl extends ServiceImpl<BizDisputeCaseMapper, BizDisputeCase> implements DisputeService {

    private final BizDisputeCaseMapper bizDisputeCaseMapper;
    private final BizDisputeFlowMapper bizDisputeFlowMapper;
    private final RedisSequenceService redisSequenceService;
    private final MedicalRecordArchiveService medicalRecordArchiveService;

    // 查询

    // 登记 / 修改

    // 受理（联动封存）

    private static LocalDateTime parseDateTime(String v) {
        String s = TextUtil.trimToNull(v);
        if (s == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    // 处理跟踪

    // 结案 / 撤销 / 删除

    @Override
    public PageResult<DisputeCaseVO> listPage(DisputeQueryPageDTO dto) {
        Page<DisputeCaseVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<DisputeCaseVO> records = bizDisputeCaseMapper.selectCasePage(page, TextUtil.trimToNull(dto.getKeyword()),
                dto.getCaseType(), dto.getStatus(), dto.getLevel(), dto.getDeptId(),
                dto.getOpenOnly(), TextUtil.trimToNull(dto.getDateFrom()), TextUtil.trimToNull(dto.getDateTo()));
        records.forEach(this::decorate);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public DisputeCaseVO getDetailById(Long id) {
        DisputeCaseVO vo = requireVo(id);
        vo.setFlows(bizDisputeFlowMapper.selectByCaseId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeCaseVO caseUpsert(DisputeCaseUpsertDTO dto) {
        BizDisputeCase entity;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            entity = new BizDisputeCase();
            entity.setCaseNo(nextCaseNo());
            entity.setStatus(DisputeStatusEnum.PENDING.getCode());
            entity.setSealStatus(SealStatusEnum.NONE.getCode());
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            entity.setRegisterBy(operatorUser.getRealName());
            entity.setRegisterTime(TimeUtil.nowSeconds());
        } else {
            entity = requireEntity(dto.getId());
            if (!Objects.equals(entity.getStatus(), DisputeStatusEnum.PENDING.getCode())) {
                throw new BusinessException("仅「待受理」单据允许修改（" + statusName(entity.getStatus()) + "已锁定）");
            }
        }
        entity.setCaseType(dto.getCaseType());
        entity.setSourceType(dto.getSourceType());
        entity.setLevel(dto.getLevel() == null ? 1 : dto.getLevel());
        entity.setPatientId(dto.getPatientId());
        entity.setAdmissionId(dto.getAdmissionId());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(dto.getDeptId() == null ? null : bizDisputeCaseMapper.selectDeptName(dto.getDeptId()));
        entity.setInvolvedStaff(TextUtil.cut(dto.getInvolvedStaff(), 255));
        entity.setComplainant(TextUtil.cut(dto.getComplainant(), 64));
        entity.setComplainantRel(dto.getComplainantRel());
        entity.setComplainantTel(TextUtil.cut(dto.getComplainantTel(), 32));
        entity.setOccurTime(parseDateTime(dto.getOccurTime()));
        entity.setOccurPlace(TextUtil.cut(dto.getOccurPlace(), 128));
        entity.setContent(TextUtil.cut(dto.getContent(), 1000));
        entity.setDemand(TextUtil.cut(dto.getDemand(), 500));
        entity.setNeedSeal(dto.getNeedSeal() == null ? 0 : dto.getNeedSeal());
        entity.setRemark(TextUtil.cut(dto.getRemark(), 512));
        // 患者快照服务端重查，不信任前端传的姓名（改名/改名号都能对上）
        if (dto.getPatientId() != null) {
            entity.setPatientNo(bizDisputeCaseMapper.selectPatientNo(dto.getPatientId()));
            entity.setPatientName(bizDisputeCaseMapper.selectPatientName(dto.getPatientId()));
            if (entity.getPatientName() == null) {
                throw new BusinessException("患者不存在或已删除");
            }
        }
        if (isNew) {
            bizDisputeCaseMapper.insert(entity);
        } else {
            bizDisputeCaseMapper.updateById(entity);
        }
        return requireVo(entity.getId());
    }

    // 统计

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeCaseVO accept(DisputeActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDisputeCase entity = requireEntity(dto.getId());
        if (!Objects.equals(entity.getStatus(), DisputeStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅「待受理」单据可受理（当前：" + statusName(entity.getStatus()) + "）");
        }
        String operator = operatorUser.getRealName();
        entity.setStatus(DisputeStatusEnum.INVESTIGATING.getCode());
        entity.setAcceptBy(operator);
        entity.setAcceptTime(TimeUtil.nowSeconds());
        // 封存联动：需封存 → 找该患者已归档病案 → 有则封，无则标「待归档后封存」
        if (Objects.equals(entity.getNeedSeal(), 1)) {
            sealIfPossible(entity, operator, "受理");
        }
        bizDisputeCaseMapper.updateById(entity);
        addFlow(entity.getId(), "受理", DisputeStatusEnum.PENDING.getCode(),
                DisputeStatusEnum.INVESTIGATING.getCode(), TextUtil.cut(dto.getContent(), 1000), operator);
        return requireVo(entity.getId());
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeCaseVO sealNow(DisputeActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDisputeCase entity = requireEntity(dto.getId());
        if (!Objects.equals(entity.getNeedSeal(), 1)) {
            throw new BusinessException("该单据未申请封存病历，无需补封");
        }
        if (Objects.equals(entity.getSealStatus(), SealStatusEnum.DONE.getCode())) {
            throw new BusinessException("该单据病历已封存，无需重复封存");
        }
        if (isTerminal(entity.getStatus())) {
            throw new BusinessException("已结案/已撤销单据不允许再封存病历");
        }
        String operator = operatorUser.getRealName();
        boolean sealed = sealIfPossible(entity, operator, "补封存");
        if (!sealed) {
            throw new BusinessException("该患者暂无「已归档」病历，请先完成病案归档再补封");
        }
        bizDisputeCaseMapper.updateById(entity);
        addFlow(entity.getId(), "封存病历", entity.getStatus(), entity.getStatus(),
                "手动补封存，病案ID " + entity.getArchiveId(), operator);
        return requireVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeCaseVO follow(DisputeFollowDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDisputeCase entity = requireEntity(dto.getId());
        Integer from = entity.getStatus();
        if (isTerminal(from)) {
            throw new BusinessException("已结案/已撤销单据不可再登记处理跟踪");
        }
        Integer to = from;
        if (dto.getToStatus() != null) {
            if (!Objects.equals(dto.getToStatus(), DisputeStatusEnum.INVESTIGATING.getCode())
                    && !Objects.equals(dto.getToStatus(), DisputeStatusEnum.HANDLING.getCode())) {
                throw new BusinessException("处理跟踪只能推进到「调查中」或「处理中」；结案请走结案（需收口赔偿与责任）");
            }
            if (Objects.equals(from, DisputeStatusEnum.PENDING.getCode())
                    && !Objects.equals(dto.getToStatus(), DisputeStatusEnum.INVESTIGATING.getCode())) {
                throw new BusinessException("待受理单据只能推进到「调查中」（即受理）；受理请走受理动作以便联动封存");
            }
            to = dto.getToStatus();
            entity.setStatus(to);
            if (Objects.equals(to, DisputeStatusEnum.INVESTIGATING.getCode()) && entity.getAcceptTime() == null) {
                entity.setAcceptBy(operatorUser.getRealName());
                entity.setAcceptTime(TimeUtil.nowSeconds());
            }
            bizDisputeCaseMapper.updateById(entity);
        }
        String action = TextUtil.cut(dto.getAction(), 64);
        addFlow(entity.getId(), action, from, to, TextUtil.cut(dto.getContent(), 1000), operatorUser.getRealName());
        return requireVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeCaseVO close(DisputeCloseDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDisputeCase entity = requireEntity(dto.getId());
        Integer from = entity.getStatus();
        if (!Objects.equals(from, DisputeStatusEnum.INVESTIGATING.getCode())
                && !Objects.equals(from, DisputeStatusEnum.HANDLING.getCode())) {
            throw new BusinessException("仅「调查中 / 处理中」单据可结案（当前：" + statusName(from) + "）");
        }
        // D 取值规则（非空校验已下沉 DTO @NotNull）：赔偿金额不得为负，无赔偿填 0
        if (dto.getCompensation().signum() < 0) {
            throw new BusinessException("赔偿金额不能为负（无赔偿请填 0）");
        }
        String operator = operatorUser.getRealName();
        entity.setStatus(DisputeStatusEnum.CLOSED.getCode());
        entity.setDealType(dto.getDealType());
        entity.setDutyType(dto.getDutyType());
        entity.setCompensation(dto.getCompensation());
        entity.setConclusion(TextUtil.cut(dto.getConclusion(), 1000));
        entity.setCloseBy(operator);
        entity.setCloseTime(TimeUtil.nowSeconds());
        bizDisputeCaseMapper.updateById(entity);
        addFlow(entity.getId(), "结案", from, DisputeStatusEnum.CLOSED.getCode(),
                TextUtil.cut(dto.getConclusion(), 1000), operator);
        return requireVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisputeCaseVO revoke(DisputeActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDisputeCase entity = requireEntity(dto.getId());
        Integer from = entity.getStatus();
        if (isTerminal(from)) {
            throw new BusinessException("已结案/已撤销单据不可再撤销");
        }
        String reason = TextUtil.trimToNull(dto.getContent());
        // B 类保留：动作 DTO 为受理/补封存/撤销共用，说明只有撤销必填，@NotBlank 一刀切会挡掉合法动作
        if (reason == null) {
            throw new BusinessException("撤销原因必填");
        }
        String operator = operatorUser.getRealName();
        entity.setStatus(DisputeStatusEnum.REVOKED.getCode());
        entity.setRevokeReason(TextUtil.cut(reason, 500));
        bizDisputeCaseMapper.updateById(entity);
        addFlow(entity.getId(), "撤销", from, DisputeStatusEnum.REVOKED.getCode(), TextUtil.cut(reason, 500), operator);
        return requireVo(entity.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        BizDisputeCase entity = requireEntity(id);
        if (!Objects.equals(entity.getStatus(), DisputeStatusEnum.PENDING.getCode())) {
            throw new BusinessException("仅「待受理」单据可删除");
        }
        if (bizDisputeFlowMapper.countByCaseId(id) > 0) {
            throw new BusinessException("已有处理跟踪记录，不允许删除（撤销走撤销动作留痕）");
        }
        // 软删走 MP deleteById（updateById set del_flag 会被 @TableLogic 静默跳过）
        return bizDisputeCaseMapper.deleteById(id) > 0;
    }

    @Override
    public DisputeStatVO stat(String dateFrom, String dateTo) {
        String from = TextUtil.trimToNull(dateFrom);
        String to = TextUtil.trimToNull(dateTo);
        DisputeStatVO vo = new DisputeStatVO();
        long pending = 0, investigating = 0, handling = 0, closed = 0, revoked = 0;
        for (DisputeCodeCountVO row : bizDisputeCaseMapper.countByStatus(from, to)) {
            long c = NumUtil.orZero(row.getC());
            int k = NumUtil.orZero(row.getK());
            if (k == DisputeStatusEnum.PENDING.getCode()) {
                pending = c;
            } else if (k == DisputeStatusEnum.INVESTIGATING.getCode()) {
                investigating = c;
            } else if (k == DisputeStatusEnum.HANDLING.getCode()) {
                handling = c;
            } else if (k == DisputeStatusEnum.CLOSED.getCode()) {
                closed = c;
            } else if (k == DisputeStatusEnum.REVOKED.getCode()) {
                revoked = c;
            }
        }
        vo.setPendingCount(pending);
        vo.setInvestigatingCount(investigating);
        vo.setHandlingCount(handling);
        vo.setClosedCount(closed);
        vo.setRevokedCount(revoked);
        vo.setOpenCount(pending + investigating + handling);
        vo.setTotal(pending + investigating + handling + closed + revoked);

        List<DisputeStatItemVO> byType = new ArrayList<>();
        for (DisputeCodeCountVO row : bizDisputeCaseMapper.countByCaseType(from, to)) {
            DisputeStatItemVO item = new DisputeStatItemVO();
            item.setKey(String.valueOf(row.getK()));
            item.setCount(NumUtil.orZero(row.getC()));
            byType.add(item);
        }
        vo.setByCaseType(byType);

        List<DisputeStatItemVO> byDept = new ArrayList<>();
        for (DisputeDeptCountVO row : bizDisputeCaseMapper.countByDeptTop(from, to)) {
            DisputeStatItemVO item = new DisputeStatItemVO();
            item.setDeptId(row.getD());
            item.setName(row.getN());
            item.setCount(NumUtil.orZero(row.getC()));
            byDept.add(item);
        }
        vo.setByDeptTop(byDept);

        DisputeCloseSumVO sum = bizDisputeCaseMapper.sumClosed(from, to);
        vo.setCompensationTotal(sum == null || sum.getTotal() == null ? BigDecimal.ZERO : sum.getTotal());
        vo.setAvgCloseDays(sum == null || sum.getAvgDays() == null ? BigDecimal.ZERO : sum.getAvgDays());
        return vo;
    }

    /**
     * 尝试封存：有已归档病案则封并回写；无则返回 false（调用方决定是标记待封还是报错）
     */
    private boolean sealIfPossible(BizDisputeCase entity, String operator, String scene) {
        Long archiveId = entity.getPatientId() == null
                ? null : bizDisputeCaseMapper.selectSealableArchiveId(entity.getPatientId());
        if (archiveId == null) {
            entity.setSealStatus(SealStatusEnum.PENDING_ARCHIVE.getCode());
            log.warn("[纠纷封存] {}未封存：患者{} 暂无已归档病历，标记待归档后封存", scene, entity.getPatientId());
            return false;
        }
        medicalRecordArchiveService.seal(archiveId);
        entity.setSealStatus(SealStatusEnum.DONE.getCode());
        entity.setArchiveId(archiveId);
        entity.setSealTime(TimeUtil.nowSeconds());
        return true;
    }

    private void addFlow(Long caseId, String action, Integer from, Integer to, String content, String operator) {
        BizDisputeFlow flow = new BizDisputeFlow();
        flow.setCaseId(caseId);
        flow.setAction(action);
        flow.setFromStatus(from);
        flow.setToStatus(to);
        flow.setContent(content);
        flow.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());
        flow.setOperator(operator);
        flow.setOperateTime(TimeUtil.nowSeconds());
        flow.setDelFlag(DelFlagEnum.NORMAL.getCode());
        bizDisputeFlowMapper.insert(flow);
    }

    /**
     * 按钮可用性与派生字段一律服务端算，前端不许按 status switch
     */
    private void decorate(DisputeCaseVO vo) {
        Integer st = vo.getStatus();
        boolean pending = Objects.equals(st, DisputeStatusEnum.PENDING.getCode());
        boolean open = pending || Objects.equals(st, DisputeStatusEnum.INVESTIGATING.getCode())
                || Objects.equals(st, DisputeStatusEnum.HANDLING.getCode());
        boolean terminal = Objects.equals(st, DisputeStatusEnum.CLOSED.getCode())
                || Objects.equals(st, DisputeStatusEnum.REVOKED.getCode());
        vo.setCanEdit(pending);
        vo.setCanAccept(pending);
        vo.setCanFollow(open && !pending);
        vo.setCanClose(Objects.equals(st, DisputeStatusEnum.INVESTIGATING.getCode())
                || Objects.equals(st, DisputeStatusEnum.HANDLING.getCode()));
        vo.setCanRevoke(open);
        vo.setCanSeal(!terminal && Objects.equals(vo.getNeedSeal(), 1)
                && !Objects.equals(vo.getSealStatus(), SealStatusEnum.DONE.getCode()));
        vo.setCanDelete(pending);
        vo.setComplainantTel(SensitiveMaskUtil.maskPhone(vo.getComplainantTel()));
        // 受理天数：未受理 0，已结案取受理→结案，其余取受理→今天
        if (vo.getAcceptTime() == null) {
            vo.setOpenDays(0);
        } else {
            LocalDate end = vo.getCloseTime() != null
                    ? vo.getCloseTime().toLocalDate() : LocalDate.now();
            vo.setOpenDays((int) ChronoUnit.DAYS.between(vo.getAcceptTime().toLocalDate(), end));
        }
    }

    private DisputeCaseVO requireVo(Long id) {
        DisputeCaseVO vo = bizDisputeCaseMapper.selectCaseById(id);
        if (vo == null) {
            throw new BusinessException("纠纷/投诉单据不存在或已删除");
        }
        decorate(vo);
        return vo;
    }

    private BizDisputeCase requireEntity(Long id) {
        BizDisputeCase entity = bizDisputeCaseMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDelFlag(), 0)) {
            throw new BusinessException("纠纷/投诉单据不存在或已删除");
        }
        return entity;
    }

    private boolean isTerminal(Integer status) {
        return Objects.equals(status, DisputeStatusEnum.CLOSED.getCode())
                || Objects.equals(status, DisputeStatusEnum.REVOKED.getCode());
    }

    private String statusName(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "待受理";
            case 2 -> "调查中";
            case 3 -> "处理中";
            case 4 -> "已结案";
            case 5 -> "已撤销";
            default -> "未知";
        };
    }

    private String nextCaseNo() {
        return Constants.DISPUTE_NO_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next("DISPUTE"));
    }
}
