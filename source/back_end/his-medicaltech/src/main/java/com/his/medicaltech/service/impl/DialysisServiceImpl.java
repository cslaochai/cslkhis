package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.BaseEntity;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.DialysisDTO;
import com.his.medicaltech.entity.BizDialysisMachine;
import com.his.medicaltech.entity.BizDialysisPatient;
import com.his.medicaltech.entity.BizDialysisPrescription;
import com.his.medicaltech.entity.BizDialysisSession;
import com.his.medicaltech.enums.*;
import com.his.medicaltech.mapper.BizDialysisMachineMapper;
import com.his.medicaltech.mapper.BizDialysisPatientMapper;
import com.his.medicaltech.mapper.BizDialysisPrescriptionMapper;
import com.his.medicaltech.mapper.BizDialysisSessionMapper;
import com.his.medicaltech.service.DialysisService;
import com.his.medicaltech.vo.DialysisVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 血液净化（透析）中心服务。
 *
 * <p>口径：
 * <ol>
 *   <li>档案＝患者级唯一（uk_dp_patient）；患者姓名/编号/电话一律服务端按 patientId
 *       重查患者基本信息取快照，不信前端传值。暂停/退出必须填原因，且不能有未结束的透析单。</li>
 *   <li>处方＝档案级「同时只允许一张有效」。排班时把干体重/时长/血流速/透析器/抗凝整套快照进
 *       透析单，之后改处方不影响已排的单。</li>
 *   <li>排班＝日期+时段+机位，撞 uk_session_slot_machine 直接拒绝并指出占用者；机位维修/停用不可排。
 *       日期不允许是未来之后（最多今天）。</li>
 *   <li>单状态机：1已排班 →（上机：透前体重+通路评估）2透析中 →（下机：透后体重）3已完成；
 *       1 →（取消：原因）4已取消。超滤量 =（透前-透后）×1000、实际时长 = 下机-上机，均服务端回算。</li>
 *   <li>本域只出治疗过程记录与台账，不生成收费单、不扣耗材库存（计费走治疗医嘱主链，
 *       与 L12 PIVAS 同一口径，避免双计）。</li>
 *   <li>操作人取当前登录人；原因/描述类文本服务端截列宽。电话在展示接口出参脱敏，
 *       编辑回显（archiveGetById）保持明文。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialysisServiceImpl extends ServiceImpl<BizDialysisSessionMapper, BizDialysisSession> implements DialysisService {

    /**
     * 原因/描述类文本落库上限（列宽 255，留余量）
     */
    private static final int REASON_MAX = 200;

    private static final int MAX_TEXT = 255;

    private final BizDialysisPatientMapper bizDialysisPatientMapper;
    private final BizDialysisPrescriptionMapper bizDialysisPrescriptionMapper;
    private final BizDialysisMachineMapper bizDialysisMachineMapper;
    private final BizDialysisSessionMapper bizDialysisSessionMapper;
    private final RedisSequenceService redisSequenceService;

    // 档案

    private static String slotText(int slot) {
        String label = DialysisTimeSlotEnum.getText(slot);
        return label == null ? DialysisTimeSlotEnum.MORNING.getLabel() : label;
    }

    // 处方

    public PageResult<DialysisVO.ArchiveVO> archiveListPage(DialysisDTO.ArchiveQuery query) {
        Page<DialysisVO.ArchiveVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<DialysisVO.ArchiveVO> records = bizDialysisPatientMapper.selectArchivePage(page,
                TextUtil.trimToNull(query.getDialysisNo()), TextUtil.trimToNull(query.getPatientName()),
                query.getAccessType(), query.getStatus());
        records.forEach(this::maskArchivePhone);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 机位

    /**
     * 编辑回显：电话保持明文（前端整对象回写，遮码会把真号洗成星号）
     */
    public DialysisVO.ArchiveVO archiveGetById(Long id) {
        DialysisVO.ArchiveVO vo = bizDialysisPatientMapper.selectArchiveById(id);
        if (vo == null) {
            throw new BusinessException("透析档案不存在或已删除");
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.ArchiveVO archiveUpsert(DialysisDTO.ArchiveUpsert dto) {
        if (dto.getPatientId() == null) {
            throw new BusinessException("请选择患者");
        }
        DialysisVO.ArchiveVO snapshot = bizDialysisPatientMapper.selectPatientSnapshot(dto.getPatientId());
        if (snapshot == null) {
            throw new BusinessException("患者主档不存在，无法建立透析档案");
        }
        if (dto.getFirstDialysisDate().isAfter(LocalDate.now())) {
            throw new BusinessException("首次透析日期不能晚于今天");
        }
        if (bizDialysisPatientMapper.countByPatient(dto.getPatientId(), dto.getId()) > 0) {
            throw new BusinessException("该患者已建透析档案（一人一档）");
        }
        BizDialysisPatient entity;
        if (dto.getId() == null) {
            entity = new BizDialysisPatient();
            entity.setDialysisNo(redisSequenceService.generateDialysisPatientNo());
            entity.setPatientId(dto.getPatientId());
            entity.setStatus(DialysisPatientStatusEnum.ON.getCode());
            entity.setDialysisFreq(NumUtil.orDefault(dto.getDialysisFreq(), DialysisFreqEnum.WEEK_3.getCode()));
        } else {
            entity = requireArchive(dto.getId());
            if (!Objects.equals(entity.getPatientId(), dto.getPatientId())) {
                throw new BusinessException("透析档案不允许换绑患者，请作废后重建");
            }
        }
        entity.setPatientNo(snapshot.getPatientNo());
        entity.setPatientName(snapshot.getPatientName());
        entity.setPhone(snapshot.getPhone());
        entity.setFirstDialysisDate(dto.getFirstDialysisDate());
        entity.setCause(TextUtil.cutToNull(dto.getCause(), MAX_TEXT));
        entity.setAccessType(dto.getAccessType());
        entity.setAccessSite(TextUtil.cutToNull(dto.getAccessSite(), 128));
        if (dto.getDialysisFreq() != null) {
            entity.setDialysisFreq(dto.getDialysisFreq());
        }
        entity.setRemark(TextUtil.cutToNull(dto.getRemark(), MAX_TEXT));
        saveOrThrow(bizDialysisPatientMapper, entity, "档案保存失败");
        return archiveGetById(entity.getId());
    }

    /**
     * 在透 1 ↔ 暂停 2；两者 → 退出 3（终态）。退出/暂停必须有原因，且没有未结束的透析单。
     */
    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.ArchiveVO archiveChangeStatus(DialysisDTO.ArchiveStatus dto) {
        BizDialysisPatient archive = requireArchive(dto.getId());
        int target = dto.getStatus();
        if (archive.getStatus() == DialysisPatientStatusEnum.EXITED.getCode()) {
            throw new BusinessException("档案已退出透析，不能再变更");
        }
        if (target != DialysisPatientStatusEnum.ON.getCode() && !StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("暂停/退出必须填写原因");
        }
        if (target != DialysisPatientStatusEnum.ON.getCode() && bizDialysisSessionMapper.countUnfinished(archive.getId()) > 0) {
            throw new BusinessException("该患者还有已排班或透析中的治疗单，请先处理再变更档案状态");
        }
        archive.setStatus(target);
        archive.setExitReason(target == DialysisPatientStatusEnum.ON.getCode() ? null : TextUtil.cut(dto.getReason().trim(), REASON_MAX));
        if (bizDialysisPatientMapper.updateById(archive) <= 0) {
            throw new BusinessException("档案状态更新失败");
        }
        return archiveGetById(archive.getId());
    }

    // 排班与透析单

    public List<DialysisVO.PrescriptionVO> prescriptionList(Long archiveId) {
        requireArchive(archiveId);
        return bizDialysisPrescriptionMapper.selectByArchive(archiveId);
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.PrescriptionVO prescriptionUpsert(DialysisDTO.PrescriptionUpsert dto) {
        BizDialysisPatient archive = requireArchive(dto.getArchiveId());
        if (archive.getStatus() == DialysisPatientStatusEnum.EXITED.getCode()) {
            throw new BusinessException("该患者已退出透析，不能再开立处方");
        }
        if (dto.getDryWeight().compareTo(BigDecimal.ZERO) <= 0 || dto.getDryWeight().compareTo(new BigDecimal("999.99")) > 0) {
            throw new BusinessException("干体重必须在 0~999.99 kg 之间");
        }
        if (dto.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException("处方生效日期不能晚于今天");
        }
        BizDialysisPrescription entity;
        if (dto.getId() == null) {
            entity = new BizDialysisPrescription();
            entity.setArchiveId(dto.getArchiveId());
            entity.setStatus(DialysisPrescriptionStatusEnum.ACTIVE.getCode());
            entity.setPatientName(archive.getPatientName());
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            entity.setDoctorId(operatorUser.getEmployeeId());
            entity.setDoctorName(operatorUser.getRealName());
        } else {
            entity = requirePrescription(dto.getId());
            if (!Objects.equals(entity.getArchiveId(), dto.getArchiveId())) {
                throw new BusinessException("处方不允许改挂到其他透析档案");
            }
            if (entity.getStatus() == DialysisPrescriptionStatusEnum.STOPPED.getCode()) {
                throw new BusinessException("处方已停用，请新开一张");
            }
        }
        entity.setDryWeight(dto.getDryWeight());
        entity.setDurationMin(NumUtil.orDefault(dto.getDurationMin(), entity.getDurationMin() == null ? 240 : entity.getDurationMin()));
        entity.setBloodFlow(NumUtil.orDefault(dto.getBloodFlow(), entity.getBloodFlow() == null ? 220 : entity.getBloodFlow()));
        entity.setDialyzer(NumUtil.orDefault(dto.getDialyzer(), entity.getDialyzer() == null ? DialyzerTypeEnum.HIGH_FLUX_SYNTHETIC.getCode() : entity.getDialyzer()));
        entity.setAnticoagulant(NumUtil.orDefault(dto.getAnticoagulant(), entity.getAnticoagulant() == null ? DialysisAnticoagulantEnum.HEPARIN.getCode() : entity.getAnticoagulant()));
        entity.setAnticoagDose(TextUtil.cutToNull(dto.getAnticoagDose(), 64));
        entity.setTargetUltraMl(dto.getTargetUltraMl());
        entity.setStartDate(dto.getStartDate());
        saveOrThrow(bizDialysisPrescriptionMapper, entity, "处方保存失败");
        if (dto.getId() == null) {
            stopOtherActive(entity.getArchiveId(), entity.getId());
        }
        return prescriptionList(entity.getArchiveId()).stream()
                .filter(x -> Objects.equals(x.getId(), entity.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("处方保存后读取失败"));
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.PrescriptionVO prescriptionStop(DialysisDTO.PrescriptionStop dto) {
        BizDialysisPrescription prescription = requirePrescription(dto.getId());
        if (prescription.getStatus() == DialysisPrescriptionStatusEnum.STOPPED.getCode()) {
            throw new BusinessException("处方已停用");
        }
        prescription.setStatus(DialysisPrescriptionStatusEnum.STOPPED.getCode());
        prescription.setEndDate(LocalDate.now());
        prescription.setStopReason(TextUtil.cutToNull(dto.getReason(), REASON_MAX));
        if (bizDialysisPrescriptionMapper.updateById(prescription) <= 0) {
            throw new BusinessException("处方停用失败");
        }
        return prescriptionList(prescription.getArchiveId()).stream()
                .filter(x -> Objects.equals(x.getId(), prescription.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("处方停用后读取失败"));
    }

    public PageResult<DialysisVO.MachineVO> machineListPage(DialysisDTO.MachineQuery query) {
        Page<DialysisVO.MachineVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<DialysisVO.MachineVO> records = bizDialysisMachineMapper.selectMachinePage(page,
                TextUtil.trimToNull(query.getMachineNo()), TextUtil.trimToNull(query.getRoomName()), query.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    public List<DialysisVO.MachineVO> machineSelectList() {
        List<BizDialysisMachine> machines = bizDialysisMachineMapper.selectUsable();
        List<DialysisVO.MachineVO> out = new ArrayList<>(machines.size());
        for (BizDialysisMachine machine : machines) {
            DialysisVO.MachineVO vo = new DialysisVO.MachineVO();
            vo.setId(machine.getId());
            vo.setMachineNo(machine.getMachineNo());
            vo.setRoomName(machine.getRoomName());
            vo.setStatus(machine.getStatus());
            vo.setRemark(machine.getRemark());
            out.add(vo);
        }
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.MachineVO machineUpsert(DialysisDTO.MachineUpsert dto) {
        String machineNo = dto.getMachineNo().trim();
        if (bizDialysisMachineMapper.countByNo(machineNo, dto.getId()) > 0) {
            throw new BusinessException("机位号 " + machineNo + " 已存在");
        }
        BizDialysisMachine entity;
        if (dto.getId() == null) {
            entity = new BizDialysisMachine();
            entity.setMachineNo(machineNo);
        } else {
            entity = bizDialysisMachineMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BusinessException("机位不存在或已删除");
            }
            entity.setMachineNo(machineNo);
        }
        entity.setRoomName(TextUtil.cutToNull(dto.getRoomName(), 64));
        entity.setStatus(dto.getStatus());
        entity.setRemark(TextUtil.cutToNull(dto.getRemark(), MAX_TEXT));
        saveOrThrow(bizDialysisMachineMapper, entity, "机位保存失败");
        return machineListPage(onePageMachine(entity.getMachineNo())).getRecords().stream()
                .filter(x -> Objects.equals(x.getId(), entity.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("机位保存后读取失败"));
    }

    public PageResult<DialysisVO.SessionVO> sessionListPage(DialysisDTO.SessionQuery query) {
        Page<DialysisVO.SessionVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<DialysisVO.SessionVO> records = bizDialysisSessionMapper.selectSessionPage(page,
                TextUtil.trimToNull(query.getSessionNo()), TextUtil.trimToNull(query.getPatientName()),
                query.getStartDate(), query.getEndDate(), query.getTimeSlot(),
                query.getMachineId(), query.getArchiveId(), query.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    public DialysisVO.SessionVO sessionGetById(Long id) {
        DialysisVO.SessionVO vo = bizDialysisSessionMapper.selectSessionById(id);
        if (vo == null) {
            throw new BusinessException("透析单不存在或已删除");
        }
        return vo;
    }

    public DialysisVO.BoardVO board(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        DialysisVO.BoardVO board = new DialysisVO.BoardVO();
        board.setDate(day);
        Map<Integer, List<DialysisVO.BoardCellVO>> bySlot = new LinkedHashMap<>();
        int sessionCount = 0;
        int doneCount = 0;
        for (DialysisTimeSlotEnum slot : DialysisTimeSlotEnum.values()) {
            List<DialysisVO.BoardCellVO> cells = bizDialysisSessionMapper.selectBoardSlot(day, slot.getCode());
            bySlot.put(slot.getCode(), cells);
            for (DialysisVO.BoardCellVO cell : cells) {
                if (cell.getSessionId() == null) {
                    continue;
                }
                if (!Objects.equals(cell.getSessionStatus(), DialysisSessionStatusEnum.CANCELLED.getCode())) {
                    sessionCount++;
                }
                if (Objects.equals(cell.getSessionStatus(), DialysisSessionStatusEnum.DONE.getCode())) {
                    doneCount++;
                }
            }
        }
        board.setSlot1(bySlot.get(DialysisTimeSlotEnum.MORNING.getCode()));
        board.setSlot2(bySlot.get(DialysisTimeSlotEnum.AFTERNOON.getCode()));
        board.setSlot3(bySlot.get(DialysisTimeSlotEnum.NIGHT.getCode()));
        board.setSessionCount(sessionCount);
        board.setDoneCount(doneCount);
        return board;
    }

    // 统计

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.SessionVO schedule(DialysisDTO.Schedule dto) {
        BizDialysisPatient archive = requireArchive(dto.getArchiveId());
        if (archive.getStatus() != DialysisPatientStatusEnum.ON.getCode()) {
            throw new BusinessException("该患者档案已暂停或退出，不能排班");
        }
        BizDialysisPrescription prescription = currentActivePrescription(archive.getId());
        BizDialysisMachine machine = requireUsableMachine(dto.getMachineId());
        LocalDate date = dto.getDialysisDate();
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException("透析日期不能晚于今天（跨日排班请按日期分批）");
        }
        int slot = dto.getTimeSlot();
        assertSlotFree(date, slot, machine.getId(), null);

        BizDialysisSession session = new BizDialysisSession();
        session.setSessionNo(redisSequenceService.generateDialysisSessionNo());
        session.setDialysisDate(date);
        session.setTimeSlot(slot);
        session.setMachineId(machine.getId());
        session.setMachineNo(machine.getMachineNo());
        session.setArchiveId(archive.getId());
        session.setPatientId(archive.getPatientId());
        session.setPatientNo(archive.getPatientNo());
        session.setPatientName(archive.getPatientName());
        session.setPrescriptionId(prescription.getId());
        session.setDryWeight(prescription.getDryWeight());
        session.setDurationMin(prescription.getDurationMin());
        session.setBloodFlow(prescription.getBloodFlow());
        session.setDialyzer(prescription.getDialyzer());
        session.setAnticoagulant(prescription.getAnticoagulant());
        session.setStatus(DialysisSessionStatusEnum.SCHEDULED.getCode());
        session.setRemark(TextUtil.cutToNull(dto.getRemark(), MAX_TEXT));
        try {
            if (bizDialysisSessionMapper.insert(session) <= 0) {
                throw new BusinessException("排班失败");
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(slotBusyMessage(date, slot, machine.getMachineNo()));
        }
        return sessionGetById(session.getId());
    }

    // 内部工具

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.SessionVO reschedule(DialysisDTO.Reschedule dto) {
        BizDialysisSession session = requireSession(dto.getId());
        if (session.getStatus() != DialysisSessionStatusEnum.SCHEDULED.getCode()) {
            throw new BusinessException("只有已排班未上机的治疗单允许改期/改机位");
        }
        BizDialysisMachine machine = requireUsableMachine(dto.getMachineId());
        if (dto.getDialysisDate().isAfter(LocalDate.now())) {
            throw new BusinessException("透析日期不能晚于今天");
        }
        int slot = NumUtil.orDefault(dto.getTimeSlot(), session.getTimeSlot());
        assertSlotFree(dto.getDialysisDate(), slot, machine.getId(), session.getId());
        session.setDialysisDate(dto.getDialysisDate());
        session.setTimeSlot(slot);
        session.setMachineId(machine.getId());
        session.setMachineNo(machine.getMachineNo());
        session.setRemark(TextUtil.cutToNull(dto.getRemark(), MAX_TEXT));
        try {
            if (bizDialysisSessionMapper.updateById(session) <= 0) {
                throw new BusinessException("改期失败");
            }
        } catch (DuplicateKeyException e) {
            throw new BusinessException(slotBusyMessage(dto.getDialysisDate(), slot, machine.getMachineNo()));
        }
        return sessionGetById(session.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.SessionVO startSession(DialysisDTO.SessionStart dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDialysisSession session = requireSession(dto.getId());
        if (session.getStatus() != DialysisSessionStatusEnum.SCHEDULED.getCode()) {
            throw new BusinessException("该治疗单不是「已排班」状态，不能上机");
        }
        if (dto.getBeforeWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("透前体重必须大于 0");
        }
        LocalDateTime onTime = dto.getOnTime() == null ? TimeUtil.nowSeconds() : TimeUtil.toSeconds(dto.getOnTime());
        if (onTime.isAfter(TimeUtil.nowSeconds())) {
            throw new BusinessException("上机时间不能是未来");
        }
        session.setBeforeWeight(dto.getBeforeWeight());
        session.setAccessCheck(TextUtil.cut(dto.getAccessCheck().trim(), MAX_TEXT));
        session.setOnTime(onTime);
        session.setOnBy(operatorUser.getRealName());
        session.setStatus(DialysisSessionStatusEnum.ON_MACHINE.getCode());
        if (bizDialysisSessionMapper.updateById(session) <= 0) {
            throw new BusinessException("上机登记失败");
        }
        return sessionGetById(session.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.SessionVO finishSession(DialysisDTO.SessionFinish dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDialysisSession session = requireSession(dto.getId());
        if (session.getStatus() != DialysisSessionStatusEnum.ON_MACHINE.getCode()) {
            throw new BusinessException("该治疗单不是「透析中」状态，不能下机");
        }
        if (dto.getAfterWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("透后体重必须大于 0");
        }
        LocalDateTime offTime = dto.getOffTime() == null ? TimeUtil.nowSeconds() : TimeUtil.toSeconds(dto.getOffTime());
        if (offTime.isBefore(session.getOnTime())) {
            throw new BusinessException("下机时间不能早于上机时间");
        }
        if (offTime.isAfter(TimeUtil.nowSeconds())) {
            throw new BusinessException("下机时间不能是未来");
        }
        session.setAfterWeight(dto.getAfterWeight());
        session.setOffTime(offTime);
        session.setOffBy(operatorUser.getRealName());
        session.setActualDurationMin((int) ChronoUnit.MINUTES.between(session.getOnTime(), offTime));
        // 超滤量 =（透前-透后）kg × 1000；体重记反了会是负数，直接挡回来而不是落一条脏账
        BigDecimal delta = session.getBeforeWeight().subtract(dto.getAfterWeight());
        if (delta.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("透后体重大于透前体重，超滤量会是负数，请核对两次称重");
        }
        session.setUltraMl(delta.multiply(new BigDecimal("1000")).setScale(1, RoundingMode.HALF_UP));
        session.setStatus(DialysisSessionStatusEnum.DONE.getCode());
        if (StringUtils.hasText(dto.getRemark())) {
            session.setRemark(TextUtil.cut(dto.getRemark().trim(), MAX_TEXT));
        }
        if (bizDialysisSessionMapper.updateById(session) <= 0) {
            throw new BusinessException("下机登记失败");
        }
        return sessionGetById(session.getId());
    }

    /**
     * 不良反应：透析中或已完成的治疗单都可登记（下机后迟发反应也要留痕）
     */
    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.SessionVO recordAdverse(DialysisDTO.AdverseUpsert dto) {
        BizDialysisSession session = requireSession(dto.getId());
        if (session.getStatus() != DialysisSessionStatusEnum.ON_MACHINE.getCode()
                && session.getStatus() != DialysisSessionStatusEnum.DONE.getCode()) {
            throw new BusinessException("只有已上机的治疗单允许登记不良反应");
        }
        session.setAdverseType(dto.getAdverseType());
        session.setAdverseDesc(TextUtil.cutToNull(dto.getAdverseDesc(), REASON_MAX));
        if (bizDialysisSessionMapper.updateById(session) <= 0) {
            throw new BusinessException("不良反应登记失败");
        }
        return sessionGetById(session.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public DialysisVO.SessionVO cancelSession(DialysisDTO.SessionCancel dto) {
        BizDialysisSession session = requireSession(dto.getId());
        if (session.getStatus() != DialysisSessionStatusEnum.SCHEDULED.getCode()) {
            throw new BusinessException("已上机的治疗单不能取消，请走下机登记");
        }
        session.setStatus(DialysisSessionStatusEnum.CANCELLED.getCode());
        session.setCancelReason(TextUtil.cut(dto.getReason().trim(), REASON_MAX));
        if (bizDialysisSessionMapper.updateById(session) <= 0) {
            throw new BusinessException("取消失败");
        }
        return sessionGetById(session.getId());
    }

    public DialysisVO.StatsVO stats(DialysisDTO.StatsQuery dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        if (dto.getEndDate().toEpochDay() - dto.getStartDate().toEpochDay() > 366) {
            throw new BusinessException("统计区间最长 366 天");
        }
        DialysisVO.StatsVO stats = bizDialysisSessionMapper.selectStatsSummary(dto.getStartDate(), dto.getEndDate());
        if (stats == null) {
            stats = new DialysisVO.StatsVO();
        }
        // 区间内一条治疗单都没有时 SUM() 全是 NULL，计数字段按 0 出参（均值类保持 null，那是「无数据」不是「0」）
        stats.setSessionTotal(NumUtil.orDefault(stats.getSessionTotal(), 0));
        stats.setDoneCount(NumUtil.orDefault(stats.getDoneCount(), 0));
        stats.setCancelledCount(NumUtil.orDefault(stats.getCancelledCount(), 0));
        stats.setOnMachineCount(NumUtil.orDefault(stats.getOnMachineCount(), 0));
        stats.setScheduledCount(NumUtil.orDefault(stats.getScheduledCount(), 0));
        stats.setAdverseCount(NumUtil.orDefault(stats.getAdverseCount(), 0));
        stats.setStartDate(dto.getStartDate());
        stats.setEndDate(dto.getEndDate());
        int inDialysis = bizDialysisPatientMapper.countInDialysis(dto.getEndDate());
        stats.setInDialysisPatients(inDialysis);
        stats.setSessionsPerPatient(inDialysis == 0 ? null
                : BigDecimal.valueOf(NumUtil.orDefault(stats.getSessionTotal(), 0))
                .divide(BigDecimal.valueOf(inDialysis), 2, RoundingMode.HALF_UP));
        stats.setAdverseTypes(bizDialysisSessionMapper.selectAdverseTypes(dto.getStartDate(), dto.getEndDate()));
        stats.setMachineLoads(bizDialysisSessionMapper.selectMachineLoads(dto.getStartDate(), dto.getEndDate()));
        return stats;
    }

    private void stopOtherActive(Long archiveId, Long keepId) {
        List<BizDialysisPrescription> actives = bizDialysisPrescriptionMapper.selectList(
                new LambdaQueryWrapper<BizDialysisPrescription>()
                        .eq(BizDialysisPrescription::getArchiveId, archiveId)
                        .eq(BizDialysisPrescription::getStatus, DialysisPrescriptionStatusEnum.ACTIVE.getCode()));
        for (BizDialysisPrescription other : actives) {
            if (Objects.equals(other.getId(), keepId)) {
                continue;
            }
            other.setStatus(DialysisPrescriptionStatusEnum.STOPPED.getCode());
            other.setEndDate(LocalDate.now());
            other.setStopReason("新处方生效，自动停用");
            bizDialysisPrescriptionMapper.updateById(other);
        }
    }

    private BizDialysisPrescription currentActivePrescription(Long archiveId) {
        List<BizDialysisPrescription> actives = bizDialysisPrescriptionMapper.selectList(
                new LambdaQueryWrapper<BizDialysisPrescription>()
                        .eq(BizDialysisPrescription::getArchiveId, archiveId)
                        .eq(BizDialysisPrescription::getStatus, DialysisPrescriptionStatusEnum.ACTIVE.getCode())
                        .le(BizDialysisPrescription::getStartDate, LocalDate.now())
                        .orderByDesc(BizDialysisPrescription::getStartDate)
                        .orderByDesc(BizDialysisPrescription::getId)
                        .last("LIMIT 1"));
        if (actives.isEmpty()) {
            throw new BusinessException("该患者还没有生效中的透析处方，请先开处方再排班");
        }
        return actives.get(0);
    }

    private void assertSlotFree(LocalDate date, int slot, Long machineId, Long excludeSessionId) {
        DialysisVO.SessionVO occupant = bizDialysisSessionMapper.selectSlotOccupant(date, slot, machineId);
        if (occupant != null && !Objects.equals(occupant.getId(), excludeSessionId)) {
            throw new BusinessException(slotBusyMessage(date, slot, occupant.getMachineNo()));
        }
    }

    private String slotBusyMessage(LocalDate date, int slot, String machineNo) {
        return date + " " + slotText(slot) + " 机位 " + machineNo + " 已被占用，请换机位或换时段";
    }

    private BizDialysisMachine requireUsableMachine(Long machineId) {
        BizDialysisMachine machine = bizDialysisMachineMapper.selectById(machineId);
        if (machine == null) {
            throw new BusinessException("机位不存在或已删除");
        }
        if (machine.getStatus() != DialysisMachineStatusEnum.USABLE.getCode()) {
            throw new BusinessException("机位 " + machine.getMachineNo() + " 当前为维修/停用状态，不能排班");
        }
        return machine;
    }

    private BizDialysisPatient requireArchive(Long id) {
        BizDialysisPatient archive = bizDialysisPatientMapper.selectById(id);
        if (archive == null) {
            throw new BusinessException("透析档案不存在或已删除");
        }
        return archive;
    }

    private BizDialysisPrescription requirePrescription(Long id) {
        BizDialysisPrescription prescription = bizDialysisPrescriptionMapper.selectById(id);
        if (prescription == null) {
            throw new BusinessException("透析处方不存在或已删除");
        }
        return prescription;
    }

    private BizDialysisSession requireSession(Long id) {
        BizDialysisSession session = bizDialysisSessionMapper.selectById(id);
        if (session == null) {
            throw new BusinessException("透析单不存在或已删除");
        }
        return session;
    }

    private DialysisDTO.MachineQuery onePageMachine(String machineNo) {
        DialysisDTO.MachineQuery query = new DialysisDTO.MachineQuery();
        query.setMachineNo(machineNo);
        query.setPageNum(1);
        query.setPageSize(100);
        return query;
    }

    private void maskArchivePhone(DialysisVO.ArchiveVO vo) {
        vo.setPhoneMasked(SensitiveMaskUtil.maskPhone(vo.getPhone()));
        vo.setPhone(null);
    }

    private <T extends BaseEntity> void saveOrThrow(BaseMapper<T> mapper, T entity, String message) {
        boolean ok = entity.getId() == null ? mapper.insert(entity) > 0 : mapper.updateById(entity) > 0;
        if (!ok) {
            throw new BusinessException(message);
        }
    }
}

