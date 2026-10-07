package com.his.operation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TimeUtil;
import com.his.operation.dto.SafetyCheckSignDTO;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.entity.BizOperationSafetyCheck;
import com.his.operation.enums.OperationApplyStatusEnum;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.mapper.BizOperationSafetyCheckMapper;
import com.his.operation.service.OperationSafetyCheckService;
import com.his.operation.support.SafetyCheckItems;
import com.his.operation.vo.SafetyCheckVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 手术安全核查服务实现（sql/134）。
 *
 * <p>四条硬规则都在本类落地，且每条都写清"为什么"：
 * <ol>
 *   <li><b>一时段一行、签了就是事实</b>：没有 update/delete 入口；
 *       {@code uk_check_apply_phase} 兜底"同一时段签两次"。</li>
 *   <li><b>三时段必须按顺序</b>：没签 Sign In 就不能签 Time Out ——
 *       跳过诱导前直接签"切皮前已核对"，就是把没做的事写成做过。</li>
 *   <li><b>三方必须互为不同的人</b>：一个人包签三方 = 没有核查。</li>
 *   <li><b>手术完成后不许补签</b>：术后补一条核查记录是伪造（与"术后补术前核对"同条原则）；
 *       只允许在「已排期 / 术前核对完成」两个状态签。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationSafetyCheckServiceImpl extends ServiceImpl<BizOperationSafetyCheckMapper, BizOperationSafetyCheck> implements OperationSafetyCheckService {

    private final BizOperationSafetyCheckMapper bizOperationSafetyCheckMapper;
    private final BizOperationApplyMapper bizOperationApplyMapper;

    @Override
    public List<SafetyCheckVO.PhaseCard> cardsByApply(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        BizOperationApply apply = bizOperationApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("手术申请单不存在");
        }
        Map<Integer, SafetyCheckVO> signedByPhase = new HashMap<>();
        bizOperationSafetyCheckMapper.selectByApply(applyId).forEach(row -> signedByPhase.put(row.getPhase(), toVO(row)));

        List<SafetyCheckVO.PhaseCard> cards = new ArrayList<>();
        for (Integer phase : SafetyCheckItems.ALL_PHASES) {
            SafetyCheckVO.PhaseCard card = new SafetyCheckVO.PhaseCard();
            card.setPhase(phase);
            card.setPhaseText(SafetyCheckItems.phaseText(phase));
            List<SafetyCheckVO.CheckItem> items = new ArrayList<>();
            SafetyCheckItems.itemsOf(phase).forEach((code, label) -> {
                SafetyCheckVO.CheckItem item = new SafetyCheckVO.CheckItem();
                item.setCode(code);
                item.setLabel(label);
                item.setRequired(SafetyCheckItems.requiredOf(phase).contains(code));
                items.add(item);
            });
            card.setItems(items);
            card.setSigned(signedByPhase.get(phase));
            fillCanSign(card, apply, signedByPhase);
            cards.add(card);
        }
        return cards;
    }

    // 判定与工具

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sign(SafetyCheckSignDTO dto) {
        if (!SafetyCheckItems.isValidPhase(dto.getPhase())) {
            throw new BusinessException("核查时段只允许 1-麻醉诱导前 2-手术开始前 3-患者离开手术室前，当前="
                    + dto.getPhase());
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        int phase = dto.getPhase();

        BizOperationApply apply = bizOperationApplyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new BusinessException("手术申请单不存在");
        }
        Integer status = apply.getOperationStatus();
        if (Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), status)) {
            throw new BusinessException("手术单 " + apply.getApplyNo()
                    + " 尚未排台，不能做安全核查（手术间/时段/主刀都没定，三方核对的是一个不存在的手术）");
        }
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), status)) {
            throw new BusinessException("手术单 " + apply.getApplyNo() + " 已取消，不存在要核查的手术");
        }
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), status)) {
            throw new BusinessException("手术单 " + apply.getApplyNo()
                    + " 已完成，不能再补签安全核查 —— 术后补一条核查记录是伪造，核查的价值就在「在切皮之前核过」");
        }

        List<BizOperationSafetyCheck> signed = bizOperationSafetyCheckMapper.selectByApply(apply.getId());
        int maxPhase = 0;
        for (BizOperationSafetyCheck row : signed) {
            maxPhase = Math.max(maxPhase, row.getPhase());
        }
        if (signed.stream().anyMatch(r -> Objects.equals(r.getPhase(), phase))) {
            throw new BusinessException("「" + SafetyCheckItems.phaseText(phase)
                    + "」已经签过核查（只增不改不删，发现签错请联系病案质控走缺陷流程，而不是再签一张）");
        }
        if (phase != maxPhase + 1) {
            throw new BusinessException("三时段必须按顺序签：当前只能签「"
                    + SafetyCheckItems.phaseText(maxPhase + 1) + "」，不能跳过「"
                    + SafetyCheckItems.phaseText(maxPhase + 1) + "」直接签「"
                    + SafetyCheckItems.phaseText(phase) + "」");
        }

        Set<Integer> codes;
        try {
            codes = SafetyCheckItems.parse(phase, dto.getItems());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        if (codes.isEmpty()) {
            throw new BusinessException("核查项一项都没勾 —— 逐项确认才是核对，空勾等于没核");
        }
        List<String> missing = SafetyCheckItems.missingRequired(phase, codes);
        if (!missing.isEmpty()) {
            throw new BusinessException("「" + SafetyCheckItems.phaseText(phase)
                    + "」必核项未完成：" + String.join("；", missing));
        }

        Long surgeonId = requireEmp(dto.getSurgeonId(), "手术医师");
        Long anesthetistId = requireEmp(dto.getAnesthetistId(), "麻醉医师");
        Long nurseId = requireEmp(dto.getNurseId(), "手术室护士");
        if (Objects.equals(surgeonId, anesthetistId) || Objects.equals(surgeonId, nurseId)
                || Objects.equals(anesthetistId, nurseId)) {
            throw new BusinessException("三方签名必须是三个互不同的人（一个人包签三方 = 没有核查）");
        }

        LocalDateTime now = TimeUtil.nowSeconds();
        BizOperationSafetyCheck entity = new BizOperationSafetyCheck();
        entity.setCheckNo(nextCheckNo());
        entity.setApplyId(apply.getId());
        entity.setApplyNo(apply.getApplyNo());
        entity.setAdmissionId(apply.getAdmissionId());
        entity.setPatientId(apply.getPatientId());
        entity.setPatientName(apply.getPatientName());
        entity.setOperationName(apply.getPlannedOperationName());
        entity.setOperationRoom(apply.getOperationRoom());
        entity.setPhase(phase);
        entity.setItems(SafetyCheckItems.serialize(codes));
        entity.setNote(dto.getNote());
        entity.setSurgeonId(surgeonId);
        entity.setSurgeonName(employeeNameOf(surgeonId));
        entity.setSurgeonSignTime(now);
        entity.setAnesthetistId(anesthetistId);
        entity.setAnesthetistName(employeeNameOf(anesthetistId));
        entity.setAnesthetistSignTime(now);
        entity.setNurseId(nurseId);
        entity.setNurseName(employeeNameOf(nurseId));
        entity.setNurseSignTime(now);
        entity.setRecorderId(operatorUser.getEmployeeId());
        entity.setRecorderName(operatorUser.getRealName());
        entity.setCheckTime(now);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        bizOperationSafetyCheckMapper.insert(entity);
        log.info("安全核查签单 applyNo={} 时段={} 术者={} 麻醉={} 护士={} 录入人={}",
                apply.getApplyNo(), SafetyCheckItems.phaseText(phase),
                entity.getSurgeonName(), entity.getAnesthetistName(), entity.getNurseName(), operatorUser.getRealName());
        return entity.getCheckNo();
    }

    /**
     * 某时段能否签：状态机（1/2）+ 顺序（maxPhase+1）+ 未签过。不可签必须给出原因。
     */
    private void fillCanSign(SafetyCheckVO.PhaseCard card, BizOperationApply apply,
                             Map<Integer, SafetyCheckVO> signedByPhase) {
        Integer status = apply.getOperationStatus();
        boolean inFlow = Objects.equals(OperationApplyStatusEnum.SCHEDULED.getCode(), status)
                || Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), status);
        int maxPhase = signedByPhase.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        int phase = card.getPhase();
        if (signedByPhase.containsKey(phase)) {
            card.setCanSign(false);
            card.setCannotSignReason("该时段已签，核查记录只增不改");
            return;
        }
        if (!inFlow) {
            card.setCanSign(false);
            card.setCannotSignReason("手术当前状态为「" + OperationApplyStatusEnum.labelOrUnknown(status)
                    + "」，只有在途手术可以签核查");
            return;
        }
        if (phase != maxPhase + 1) {
            card.setCanSign(false);
            card.setCannotSignReason("必须先签「" + SafetyCheckItems.phaseText(maxPhase + 1) + "」");
            return;
        }
        card.setCanSign(true);
    }

    private Long requireEmp(Long empId, String who) {
        // C-非 DTO 入参：私有 helper 校验方法参数 + 动态拼接文案；三方签名 DTO 已各带 @NotNull，
        // web 入口由 @Valid 拦截，此 helper 承担带角色名的兜底提示，保留
        if (empId == null) {
            throw new BusinessException(who + "签名不能为空（三方缺一不可）");
        }
        return empId;
    }

    private String employeeNameOf(Long empId) {
        String name = bizOperationSafetyCheckMapper.selectEmployeeName(empId);
        return StringUtils.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    private String nextCheckNo() {
        String prefix = "HC" + LocalDate.now().format(DateFormats.COMPACT_DATE);
        long seq = bizOperationSafetyCheckMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private SafetyCheckVO toVO(BizOperationSafetyCheck row) {
        SafetyCheckVO vo = new SafetyCheckVO();
        vo.setId(row.getId());
        vo.setCheckNo(row.getCheckNo());
        vo.setApplyId(row.getApplyId());
        vo.setApplyNo(row.getApplyNo());
        vo.setPatientName(row.getPatientName());
        vo.setOperationName(row.getOperationName());
        vo.setOperationRoom(row.getOperationRoom());
        vo.setPhase(row.getPhase());
        vo.setPhaseText(SafetyCheckItems.phaseText(row.getPhase()));
        vo.setItems(row.getItems());
        vo.setItemsText(SafetyCheckItems.summaryText(row.getPhase(), row.getItems()));
        vo.setNote(row.getNote());
        vo.setSurgeonId(row.getSurgeonId());
        vo.setSurgeonName(row.getSurgeonName());
        vo.setAnesthetistId(row.getAnesthetistId());
        vo.setAnesthetistName(row.getAnesthetistName());
        vo.setNurseId(row.getNurseId());
        vo.setNurseName(row.getNurseName());
        vo.setRecorderName(row.getRecorderName());
        vo.setCheckTime(row.getCheckTime() == null ? null : row.getCheckTime().format(DateFormats.DATETIME));
        vo.setRemark(row.getRemark());
        return vo;
    }
}
