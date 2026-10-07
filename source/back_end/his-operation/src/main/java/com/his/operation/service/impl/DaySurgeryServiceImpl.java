package com.his.operation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.Constants;
import com.his.common.base.PageResult;
import com.his.common.enums.TechAuthCategoryEnum;
import com.his.common.enums.TechOverrideSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.operation.dto.*;
import com.his.operation.entity.BizDaySurgeryApply;
import com.his.operation.entity.BizDaySurgeryFollow;
import com.his.operation.entity.BizDaySurgeryItem;
import com.his.operation.mapper.BizDaySurgeryApplyMapper;
import com.his.operation.mapper.BizDaySurgeryFollowMapper;
import com.his.operation.mapper.BizDaySurgeryItemMapper;
import com.his.operation.service.DaySurgeryService;
import com.his.operation.vo.DaySurgeryApplyVO;
import com.his.operation.vo.DaySurgeryItemCountVO;
import com.his.operation.vo.DaySurgeryItemVO;
import com.his.operation.vo.DaySurgeryStatVO;
import com.his.patient.entity.BizPatient;
import com.his.patient.service.PatientService;
import com.his.system.dto.TechAuthGateDTO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 日间手术服务实现。
 *
 * <p>口径：
 * <ol>
 *   <li><b>准入是闸门</b>：只有启用中的目录术式能预约；停用只影响新预约，存量单照常推进。</li>
 *   <li>状态机单向：1待评估 → 2评估通过 → 3已安排 → 4术后观察 → 5已出院（终态）；
 *       未终态 → 6已取消（原因必填）；术后观察 → 7已转住院（住院号必填）。</li>
 *   <li><b>评估未通过不得安排、未安排不得登记完成</b> —— 评审必查的两道硬闸门。</li>
 *   <li>超期 / 随访时限是<b>服务端派生不落库</b>：滞留超 maxStayHours 判 overdue，
 *       离院 + 24h 为随访时限，过期且零随访判 followOverdue。</li>
 *   <li>转住院必须回填 admission_id —— 那是医保与病案口径的分界点。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DaySurgeryServiceImpl implements DaySurgeryService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizDaySurgeryItemMapper itemMapper;
    private final BizDaySurgeryApplyMapper applyMapper;
    private final BizDaySurgeryFollowMapper followMapper;
    private final PatientService patientService;
    private final RedisSequenceService sequenceService;
    /**
     * 手术分级授权闸门（G21）：his-system 提供，择期手术不够级别直接拒单
     */
    private final EmployeeTechAuthService techAuthService;

    // 准入目录

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        return new BigDecimal(String.valueOf(v)).longValue();
    }

    private static LocalDate parseDate(String v) {
        String s = trimToNull(v);
        if (s == null) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim());
        } catch (Exception e) {
            throw new BusinessException("日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    // 登记单

    private static LocalDateTime parseDateTime(String v) {
        String s = trimToNull(v);
        if (s == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            throw new BusinessException("时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }

    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    @Override
    public PageResult<DaySurgeryItemVO> itemListPage(DaySurgeryItemQueryPageDTO dto) {
        Page<DaySurgeryItemVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<DaySurgeryItemVO> records = itemMapper.selectItemPage(page, trimToNull(dto.getKeyword()),
                dto.getDeptId(), dto.getEnabledOnly());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public List<DaySurgeryItemVO> itemSelectList(Long deptId) {
        return itemMapper.selectEnabledList(deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryItemVO itemUpsert(DaySurgeryItemUpsertDTO dto) {
        String code = trimToNull(dto.getItemCode());
        BizDaySurgeryItem entity;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            entity = new BizDaySurgeryItem();
            entity.setItemCode(code);
            entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        } else {
            entity = requireItem(dto.getId());
            if (StringUtils.hasText(code) && !Objects.equals(code, entity.getItemCode())) {
                if (itemMapper.countByCode(code, entity.getId()) > 0) {
                    throw new BusinessException("术式编码已存在：" + code);
                }
                entity.setItemCode(code);
            }
            if (dto.getStatus() != null) {
                entity.setStatus(dto.getStatus());
            }
        }
        entity.setItemName(dto.getItemName().trim());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(dto.getDeptId() == null ? null : itemMapper.selectDeptName(dto.getDeptId()));
        // maxStayHours 不填按 48：0 会让每一床都判超期，绝不能静默落到 0
        Integer hours = dto.getMaxStayHours() == null || dto.getMaxStayHours() <= 0 ? 48 : dto.getMaxStayHours();
        entity.setMaxStayHours(hours);
        entity.setAnesthesiaType(dto.getAnesthesiaType());
        entity.setStandardFee(dto.getStandardFee());
        entity.setOperationLevel(dto.getOperationLevel());
        entity.setRemark(cut(dto.getRemark(), 512));
        if (isNew) {
            if (itemMapper.countByCode(code, 0L) > 0) {
                throw new BusinessException("术式编码已存在：" + code);
            }
            itemMapper.insert(entity);
        } else {
            itemMapper.updateById(entity);
        }
        return requireItemVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryItemVO itemUpdateStatus(Long id, Integer status) {
        BizDaySurgeryItem entity = requireItem(id);
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态只能为 1（启用）或 0（停用）");
        }
        entity.setStatus(status);
        itemMapper.updateById(entity);
        return requireItemVo(entity.getId());
    }

    @Override
    public PageResult<DaySurgeryApplyVO> listPage(DaySurgeryQueryPageDTO dto) {
        Page<DaySurgeryApplyVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        List<DaySurgeryApplyVO> records = applyMapper.selectApplyPage(page, trimToNull(dto.getKeyword()),
                dto.getStatus(), dto.getItemId(), dto.getDeptId(), dto.getOpenOnly(), dto.getOverdueOnly(),
                trimToNull(dto.getDateFrom()), trimToNull(dto.getDateTo()));
        records.forEach(this::decorate);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public DaySurgeryApplyVO getDetailById(Long id) {
        DaySurgeryApplyVO vo = requireApplyVo(id);
        vo.setFollows(followMapper.selectByApplyId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO applyUpsert(DaySurgeryApplyUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDaySurgeryItem item = requireItem(dto.getItemId());
        if (!Objects.equals(item.getStatus(), 1)) {
            throw new BusinessException("术式「" + item.getItemName() + "」已停用，不可新预约日间手术");
        }
        BizPatient patient = requirePatient(dto.getPatientId());
        BizDaySurgeryApply entity;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            entity = new BizDaySurgeryApply();
            entity.setApplyNo(nextApplyNo());
            entity.setStatus(BizDaySurgeryApply.STATUS_WAIT_EVAL);
            entity.setFollowCount(0);
        } else {
            entity = requireApply(dto.getId());
            if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_WAIT_EVAL)) {
                throw new BusinessException("仅「待评估」的登记单允许修改（当前：" + statusName(entity.getStatus()) + "）");
            }
        }
        entity.setItemId(item.getId());
        entity.setItemCode(item.getItemCode());
        entity.setItemName(item.getItemName());
        entity.setMaxStayHours(item.getMaxStayHours() == null ? 48 : item.getMaxStayHours());
        entity.setPatientId(patient.getId());
        entity.setPatientNo(patient.getPatientNo());
        entity.setPatientName(patient.getPatientName());
        entity.setDeptId(dto.getDeptId());
        entity.setDeptName(dto.getDeptId() == null ? null : applyMapper.selectDeptName(dto.getDeptId()));
        entity.setDoctorId(dto.getDoctorId());
        entity.setDoctorName(operatorUser.getRealName());
        entity.setPlanSurgeryDate(parseDate(dto.getPlanSurgeryDate()));
        entity.setRemark(cut(dto.getRemark(), 512));
        // G21 手术分级授权：日间手术全是择期，术者没有该类别授权或级别不够 → 直接拒单，不留越权通道
        gateTechAuth(item, entity);
        if (isNew) {
            applyMapper.insert(entity);
        } else {
            applyMapper.updateById(entity);
        }
        return requireApplyVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO evaluate(DaySurgeryEvalDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDaySurgeryApply entity = requireApply(dto.getId());
        if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_WAIT_EVAL)) {
            throw new BusinessException("仅「待评估」的登记单可做术前评估（当前：" + statusName(entity.getStatus()) + "）");
        }
        Integer result = dto.getEvalResult();
        if (!Objects.equals(result, BizDaySurgeryApply.EVAL_PASS) && !Objects.equals(result, BizDaySurgeryApply.EVAL_FAIL)) {
            throw new BusinessException("评估结论只能为 1（通过）或 2（不通过）");
        }
        entity.setEvalResult(result);
        entity.setEvalBy(operatorUser.getRealName());
        entity.setEvalTime(now());
        entity.setEvalRemark(cut(dto.getEvalRemark(), 500));
        // 不通过仍留在「待评估」可重评；通过才推进到「评估通过」（安排手术的前置条件）
        entity.setStatus(Objects.equals(result, BizDaySurgeryApply.EVAL_PASS)
                ? BizDaySurgeryApply.STATUS_EVAL_PASSED : BizDaySurgeryApply.STATUS_WAIT_EVAL);
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO arrange(DaySurgeryArrangeDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDaySurgeryApply entity = requireApply(dto.getId());
        if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_EVAL_PASSED)) {
            if (Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_WAIT_EVAL)) {
                throw new BusinessException("术前评估未通过或尚未评估，不得安排日间手术");
            }
            throw new BusinessException("仅「评估通过」的登记单可安排（当前：" + statusName(entity.getStatus()) + "）");
        }
        entity.setStatus(BizDaySurgeryApply.STATUS_ARRANGED);
        entity.setSurgeryTime(parseDateTime(dto.getSurgeryTime()));
        entity.setOperatingRoom(cut(dto.getOperatingRoom(), 64));
        entity.setSeqNo(dto.getSeqNo());
        entity.setAnesthesiaType(dto.getAnesthesiaType());
        entity.setSurgeon(cut(dto.getSurgeon(), 64));
        entity.setArrangeBy(operatorUser.getRealName());
        entity.setArrangeTime(now());
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO finishSurgery(DaySurgeryFinishDTO dto) {
        BizDaySurgeryApply entity = requireApply(dto.getId());
        if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_ARRANGED)) {
            throw new BusinessException("仅「已安排」的登记单可登记完成（当前：" + statusName(entity.getStatus()) + "）");
        }
        entity.setStatus(BizDaySurgeryApply.STATUS_OBSERVING);
        entity.setSurgeryEndTime(dto.getSurgeryEndTime() == null ? now() : parseDateTime(dto.getSurgeryEndTime()));
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO discharge(DaySurgeryDischargeDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDaySurgeryApply entity = requireApply(dto.getId());
        if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_OBSERVING)) {
            throw new BusinessException("仅「术后观察」的登记单可登记离院（当前：" + statusName(entity.getStatus()) + "）");
        }
        Integer leaveType = dto.getLeaveType();
        if (!Objects.equals(leaveType, BizDaySurgeryApply.LEAVE_NORMAL)
                && !Objects.equals(leaveType, BizDaySurgeryApply.LEAVE_READMIT)) {
            throw new BusinessException("离院方式只能为 1（按时离院）或 3（非计划再入院）；转普通住院请走转住院动作");
        }
        entity.setStatus(BizDaySurgeryApply.STATUS_DISCHARGED);
        entity.setLeaveType(leaveType);
        entity.setDischargeTime(dto.getDischargeTime() == null ? now() : parseDateTime(dto.getDischargeTime()));
        entity.setDischargeBy(operatorUser.getRealName());
        entity.setDischargeRemark(cut(dto.getDischargeRemark(), 500));
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO transferToIpd(DaySurgeryTransferDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDaySurgeryApply entity = requireApply(dto.getId());
        if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_OBSERVING)) {
            throw new BusinessException("仅「术后观察」的登记单可转住院（当前：" + statusName(entity.getStatus()) + "）");
        }
        if (applyMapper.countAdmission(dto.getTransferAdmissionId()) == 0) {
            throw new BusinessException("转住院的住院记录不存在或已删除");
        }
        entity.setStatus(BizDaySurgeryApply.STATUS_TRANSFERRED);
        entity.setLeaveType(BizDaySurgeryApply.LEAVE_TRANSFER);
        entity.setTransferAdmissionId(dto.getTransferAdmissionId());
        entity.setTransferRemark(cut(dto.getTransferRemark(), 500));
        entity.setDischargeTime(now());
        entity.setDischargeBy(operatorUser.getRealName());
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO cancel(DaySurgeryActionDTO dto) {
        BizDaySurgeryApply entity = requireApply(dto.getId());
        if (isTerminal(entity.getStatus())) {
            throw new BusinessException("已出院/已取消/已转住院的登记单不可再取消");
        }
        String reason = trimToNull(dto.getContent());
        entity.setStatus(BizDaySurgeryApply.STATUS_CANCELED);
        entity.setCancelReason(cut(reason, 500));
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySurgeryApplyVO follow(DaySurgeryFollowDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDaySurgeryApply entity = requireApply(dto.getId());
        Integer st = entity.getStatus();
        if (!Objects.equals(st, BizDaySurgeryApply.STATUS_DISCHARGED)
                && !Objects.equals(st, BizDaySurgeryApply.STATUS_TRANSFERRED)) {
            throw new BusinessException("仅「已出院 / 已转住院」的登记单可登记随访（当前：" + statusName(st) + "）");
        }
        BizDaySurgeryFollow follow = new BizDaySurgeryFollow();
        follow.setApplyId(entity.getId());
        follow.setFollowType(dto.getFollowType());
        follow.setResult(dto.getResult());
        follow.setContent(cut(dto.getContent(), 500));
        follow.setOperatorId(operatorUser.getEmployeeId());
        follow.setOperator(operatorUser.getRealName());
        follow.setFollowTime(now());
        follow.setDelFlag(0);
        followMapper.insert(follow);
        entity.setFollowCount(followMapper.countByApplyId(entity.getId()));
        applyMapper.updateById(entity);
        return requireApplyVo(entity.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        BizDaySurgeryApply entity = requireApply(id);
        if (!Objects.equals(entity.getStatus(), BizDaySurgeryApply.STATUS_WAIT_EVAL)) {
            throw new BusinessException("仅「待评估」的登记单可删除");
        }
        if (followMapper.countByApplyId(id) > 0) {
            throw new BusinessException("已有随访记录，不允许删除");
        }
        return applyMapper.deleteById(id) > 0;
    }

    @Override
    public DaySurgeryStatVO stat() {
        DaySurgeryStatVO vo = new DaySurgeryStatVO();
        long w = 0, e = 0, a = 0, o = 0, d = 0, c = 0, t = 0;
        for (Map<String, Object> row : applyMapper.countByStatus()) {
            long cnt = toLong(row.get("c"));
            switch ((int) toLong(row.get("k"))) {
                case 1 -> w = cnt;
                case 2 -> e = cnt;
                case 3 -> a = cnt;
                case 4 -> o = cnt;
                case 5 -> d = cnt;
                case 6 -> c = cnt;
                case 7 -> t = cnt;
                default -> {
                }
            }
        }
        vo.setWaitEvalCount(w);
        vo.setEvalPassedCount(e);
        vo.setArrangedCount(a);
        vo.setObservingCount(o);
        vo.setDischargedCount(d);
        vo.setCanceledCount(c);
        vo.setTransferredCount(t);
        vo.setTotal(w + e + a + o + d + c + t);
        vo.setOverdueCount(applyMapper.countOverdue());
        vo.setFollowOverdueCount(applyMapper.countFollowOverdue());
        vo.setReadmitCount(applyMapper.countReadmit());
        BigDecimal rate = applyMapper.onTimeLeaveRate();
        vo.setOnTimeLeaveRate(rate == null ? BigDecimal.ZERO : rate);

        List<DaySurgeryItemCountVO> top = new ArrayList<>();
        for (Map<String, Object> row : applyMapper.countByItemTop()) {
            DaySurgeryItemCountVO item = new DaySurgeryItemCountVO();
            item.setItemId(toLong(row.get("i")));
            item.setName(String.valueOf(row.get("n")));
            item.setCount(toLong(row.get("c")));
            top.add(item);
        }
        vo.setByItemTop(top);
        return vo;
    }

    /**
     * 超期 / 随访时限 / 按钮可用性一律服务端派生，不落库
     */
    private void decorate(DaySurgeryApplyVO vo) {
        Integer st = vo.getStatus();
        boolean waitEval = Objects.equals(st, BizDaySurgeryApply.STATUS_WAIT_EVAL);
        boolean evalPassed = Objects.equals(st, BizDaySurgeryApply.STATUS_EVAL_PASSED);
        boolean arranged = Objects.equals(st, BizDaySurgeryApply.STATUS_ARRANGED);
        boolean observing = Objects.equals(st, BizDaySurgeryApply.STATUS_OBSERVING);
        boolean left = Objects.equals(st, BizDaySurgeryApply.STATUS_DISCHARGED)
                || Objects.equals(st, BizDaySurgeryApply.STATUS_TRANSFERRED);
        boolean terminal = left || Objects.equals(st, BizDaySurgeryApply.STATUS_CANCELED);

        boolean overdue = observing && vo.getSurgeryEndTime() != null
                && ChronoUnit.HOURS.between(vo.getSurgeryEndTime(), LocalDateTime.now())
                > (vo.getMaxStayHours() == null ? 48 : vo.getMaxStayHours());
        vo.setOverdue(overdue);

        if (left && vo.getDischargeTime() != null) {
            LocalDateTime due = vo.getDischargeTime().plusHours(BizDaySurgeryApply.FOLLOW_DUE_HOURS);
            vo.setFollowDue(due);
            int fc = vo.getFollowCount() == null ? 0 : vo.getFollowCount();
            vo.setFollowOverdue(fc == 0 && LocalDateTime.now().isAfter(due));
        } else {
            vo.setFollowOverdue(false);
        }

        vo.setCanEdit(waitEval);
        vo.setCanEvaluate(waitEval);
        vo.setCanArrange(evalPassed);
        vo.setCanFinish(arranged);
        vo.setCanDischarge(observing);
        vo.setCanTransfer(observing);
        vo.setCanFollow(left);
        vo.setCanCancel(!terminal);
        vo.setCanDelete(waitEval);
    }

    /**
     * G21 手术分级授权闸门：术者须在计划手术当天持有「手术」类授权，且级别覆盖目录上登记的分级。
     * <p>要求级别取自术式目录（{@code operation_level}）而不是写死 1 级 —— 日间手术里
     * 白内障、腹腔镜胆囊是三级术式，用二、一级授权的人不该能预约它们。
     * 无急诊通道：日间手术本身就是把「择期」写进名字里的门诊化手术。
     */
    private void gateTechAuth(BizDaySurgeryItem item, BizDaySurgeryApply entity) {
        if (entity.getDoctorId() == null) {
            throw new BusinessException("请先指定手术医师——日间手术须按术者的技术授权准入");
        }
        TechAuthGateDTO gate = new TechAuthGateDTO();
        gate.setEmployeeId(entity.getDoctorId());
        gate.setAuthCategory(TechAuthCategoryEnum.SURGERY.getCode());
        gate.setRequiredLevel(item.getOperationLevel());
        gate.setItemCode(item.getItemCode());
        gate.setOperateDate(entity.getPlanSurgeryDate());
        gate.setSourceType(TechOverrideSourceEnum.DAY_SURGERY.getCode());
        gate.setSourceId(entity.getId());
        gate.setSourceNo(entity.getApplyNo());
        try {
            techAuthService.gate(gate);
        } catch (BusinessException e) {
            throw new BusinessException("术式「" + item.getItemName() + "」" + e.getMessage());
        }
    }

    private BizDaySurgeryItem requireItem(Long id) {
        BizDaySurgeryItem item = itemMapper.selectById(id);
        if (item == null || !Objects.equals(item.getDelFlag(), 0)) {
            throw new BusinessException("日间手术准入术式不存在或已删除");
        }
        return item;
    }

    private DaySurgeryItemVO requireItemVo(Long id) {
        DaySurgeryItemVO vo = itemMapper.selectItemById(id);
        if (vo == null) {
            throw new BusinessException("日间手术准入术式不存在或已删除");
        }
        return vo;
    }

    private DaySurgeryApplyVO requireApplyVo(Long id) {
        DaySurgeryApplyVO vo = applyMapper.selectApplyById(id);
        if (vo == null) {
            throw new BusinessException("日间手术登记单不存在或已删除");
        }
        decorate(vo);
        return vo;
    }

    private BizDaySurgeryApply requireApply(Long id) {
        BizDaySurgeryApply entity = applyMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getDelFlag(), 0)) {
            throw new BusinessException("日间手术登记单不存在或已删除");
        }
        return entity;
    }

    private BizPatient requirePatient(Long patientId) {
        BizPatient patient = patientService.getById(patientId);
        if (patient == null || !Objects.equals(patient.getDelFlag(), 0)) {
            throw new BusinessException("患者不存在或已删除");
        }
        return patient;
    }

    private boolean isTerminal(Integer status) {
        return Objects.equals(status, BizDaySurgeryApply.STATUS_DISCHARGED)
                || Objects.equals(status, BizDaySurgeryApply.STATUS_CANCELED)
                || Objects.equals(status, BizDaySurgeryApply.STATUS_TRANSFERRED);
    }

    private String statusName(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "待评估";
            case 2 -> "评估通过";
            case 3 -> "已安排";
            case 4 -> "术后观察";
            case 5 -> "已出院";
            case 6 -> "已取消";
            case 7 -> "已转住院";
            default -> "未知";
        };
    }

    private String nextApplyNo() {
        return Constants.DAY_SURGERY_NO_PREFIX + LocalDate.now().format(NO_DATE)
                + String.format("%04d", sequenceService.next("DAY_SURGERY"));
    }
}
