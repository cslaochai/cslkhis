package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.ExecStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.InfusionActionDTO;
import com.his.patient.entity.BizInfusionRound;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.entity.BizInpatientOrderExec;
import com.his.patient.mapper.BizInfusionRoundMapper;
import com.his.patient.mapper.BizInpatientOrderExecMapper;
import com.his.patient.mapper.BizInpatientOrderMapper;
import com.his.patient.service.InpatientInfusionService;
import com.his.patient.vo.InfusionRoundVO;
import com.his.patient.vo.InpatientOrderExecVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 输液执行闭环实现（G14）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientInfusionServiceImpl extends ServiceImpl<BizInpatientOrderExecMapper, BizInpatientOrderExec> implements InpatientInfusionService {

    /**
     * 静脉类给药途径关键词（口径单点：VO 的 infusion 布尔与闭环校验共用本方法）
     */
    private static final String[] INFUSION_KEYWORDS = {"静滴", "静注", "静推", "静脉", "泵入"};

    private final BizInpatientOrderExecMapper bizInpatientOrderExecMapper;

    private final BizInpatientOrderMapper bizInpatientOrderMapper;

    private final BizInfusionRoundMapper bizInfusionRoundMapper;

    private final DeptScopeService deptScopeService;

    /**
     * 输液闭环无科室字段，经入院记录的归属科室收口
     */
    private void assertAdmissionAccessible(Long admissionId) {
        Long deptId = bizInpatientOrderExecMapper.selectAdmissionDeptId(admissionId);
        if (!deptScopeService.canAccessDept(deptId)) {
            throw new BusinessException("该数据所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * 给药途径是否静脉类（唯一口径，InpatientOrderServiceImpl.decorateExec 也走这里）
     */
    public static boolean isInfusionRoute(String route) {
        if (!TextUtil.hasText(route)) {
            return false;
        }
        for (String kw : INFUSION_KEYWORDS) {
            if (route.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InpatientOrderExecVO start(InfusionActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientOrderExec exec = requireExec(dto.getExecId());
        assertAdmissionAccessible(exec.getAdmissionId());
        requireInfusion(exec);
        requireDone(exec);
        if (exec.getInfusionStartTime() != null) {
            throw new BusinessException("该执行行已在 "
                    + exec.getInfusionStartTime() + " 开始输注，不能重复开始");
        }
        exec.setInfusionStartTime(TimeUtil.nowSeconds());
        exec.setDripRate(requireDripRate(dto.getDripRate()));
        bizInpatientOrderExecMapper.updateById(exec);
        log.info("输液开始 execId={} dripRate={} 护士={}", exec.getId(), exec.getDripRate(), operatorUser.getRealName());
        return toVO(exec);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InfusionRoundVO round(InfusionActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientOrderExec exec = requireExec(dto.getExecId());
        assertAdmissionAccessible(exec.getAdmissionId());
        requireStarted(exec);
        if (exec.getInfusionEndTime() != null) {
            throw new BusinessException("该袋已于 " + exec.getInfusionEndTime() + " 结束输注，不能补录巡视（结束后补的观察是假记录）");
        }
        LocalDateTime roundTime = dto.getRoundTime() == null ? TimeUtil.nowSeconds() : TimeUtil.toSeconds(dto.getRoundTime());
        if (roundTime.isBefore(exec.getInfusionStartTime())) {
            throw new BusinessException("巡视时间（" + roundTime + "）早于开始输注时间（"
                    + exec.getInfusionStartTime() + "）——落在外面的巡视是无效观察");
        }
        if (dto.getDripRate() != null && (dto.getDripRate() < 1 || dto.getDripRate() > 300)) {
            throw new BusinessException("滴速取值超出可信范围（1~300 滴/分）");
        }
        if (dto.getRemainingVolume() != null && dto.getRemainingVolume() < 0) {
            throw new BusinessException("余量不能为负数");
        }

        BizInfusionRound round = new BizInfusionRound();
        round.setExecId(exec.getId());
        round.setOrderId(exec.getOrderId());
        round.setAdmissionId(exec.getAdmissionId());
        round.setRoundTime(roundTime);
        round.setDripRate(dto.getDripRate());
        round.setRemainingVolume(dto.getRemainingVolume());
        round.setRoundNurseId(operatorUser.getEmployeeId());
        round.setRoundNurseName(operatorUser.getRealName());
        round.setRemark(dto.getRemark());
        bizInfusionRoundMapper.insert(round);
        log.info("输液巡视 execId={} time={} dripRate={} 护士={}",
                exec.getId(), roundTime, dto.getDripRate(), round.getRoundNurseName());
        return toRoundVO(round);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InpatientOrderExecVO finish(InfusionActionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientOrderExec exec = requireExec(dto.getExecId());
        assertAdmissionAccessible(exec.getAdmissionId());
        requireStarted(exec);
        if (exec.getInfusionEndTime() != null) {
            throw new BusinessException("该执行行已在 "
                    + exec.getInfusionEndTime() + " 结束输注，不能重复结束");
        }
        boolean adverse = dto.getAdverseFlag() != null && dto.getAdverseFlag() == 1;
        if (adverse && !TextUtil.hasText(dto.getAdverseNote())) {
            // ①条件必填：只有标记了不良反应才必填描述，@NotBlank 会把无反应的正常结束挡成 400
            throw new BusinessException("标记了输液不良反应，必须填写不良反应描述（事后追溯的起点）");
        }
        LocalDateTime endTime = TimeUtil.nowSeconds();
        if (endTime.isBefore(exec.getInfusionStartTime())) {
            throw new BusinessException("结束时间早于开始时间（时钟异常？），拒绝落库");
        }
        exec.setInfusionEndTime(endTime);
        exec.setAdverseFlag(adverse ? 1 : 0);
        exec.setAdverseNote(adverse ? dto.getAdverseNote() : null);
        bizInpatientOrderExecMapper.updateById(exec);
        log.info("输液结束 execId={} adverse={} 护士={}", exec.getId(), adverse, operatorUser.getRealName());
        return toVO(exec);
    }

    // 校验与私有

    @Override
    public List<InfusionRoundVO> rounds(Long execId) {
        BizInpatientOrderExec exec = requireExec(execId);
        assertAdmissionAccessible(exec.getAdmissionId());
        return bizInfusionRoundMapper.selectRoundsByExecId(execId);
    }

    private BizInpatientOrderExec requireExec(Long execId) {
        // 非空已由三处入参 DTO 的 @NotNull + @Valid 收口（仅 start/round/finish 调用），此处不重复判空
        BizInpatientOrderExec exec = bizInpatientOrderExecMapper.selectById(execId);
        if (exec == null || Integer.valueOf(1).equals(exec.getDelFlag())) {
            throw new BusinessException("执行记录不存在");
        }
        return exec;
    }

    private void requireInfusion(BizInpatientOrderExec exec) {
        BizInpatientOrder order = bizInpatientOrderMapper.selectById(exec.getOrderId());
        String route = order == null ? null : order.getRoute();
        if (!isInfusionRoute(route)) {
            throw new BusinessException("给药途径「" + (route == null ? "空" : route)
                    + "」不是静脉类，无输液闭环（仅静滴/静注/静推/泵入）");
        }
    }

    private void requireDone(BizInpatientOrderExec exec) {
        if (!Objects.equals(ExecStatusEnum.EXECUTED.getCode(), exec.getExecStatus())) {
            throw new BusinessException("该执行行尚未执行（状态码 " + exec.getExecStatus()
                    + "），先执行医嘱再走输液闭环");
        }
    }

    private void requireStarted(BizInpatientOrderExec exec) {
        requireDone(exec);
        requireInfusion(exec);
        if (exec.getInfusionStartTime() == null) {
            throw new BusinessException("该执行行尚未开始输注，先「开始」再巡视/结束");
        }
    }

    private Integer requireDripRate(Integer dripRate) {
        if (dripRate == null) {
            throw new BusinessException("开始输注必须记录滴速（滴/分）——没有滴速的开始不是可追溯的观察");
        }
        if (dripRate < 1 || dripRate > 300) {
            throw new BusinessException("滴速取值超出可信范围（1~300 滴/分）");
        }
        return dripRate;
    }

    private InpatientOrderExecVO toVO(BizInpatientOrderExec exec) {
        InpatientOrderExecVO vo = new InpatientOrderExecVO();
        vo.setId(exec.getId());
        vo.setOrderId(exec.getOrderId());
        vo.setAdmissionId(exec.getAdmissionId());
        vo.setPatientId(exec.getPatientId());
        vo.setExecSeq(exec.getExecSeq());
        vo.setPlanDate(exec.getPlanDate());
        vo.setPlanTime(exec.getPlanTime());
        vo.setExecTime(exec.getExecTime());
        vo.setExecNurseId(exec.getExecNurseId());
        vo.setExecNurseName(exec.getExecNurseName());
        vo.setExecStatus(exec.getExecStatus());
        vo.setExecNote(exec.getExecNote());
        vo.setInfusionStartTime(exec.getInfusionStartTime());
        vo.setDripRate(exec.getDripRate());
        vo.setInfusionEndTime(exec.getInfusionEndTime());
        vo.setAdverseFlag(exec.getAdverseFlag());
        vo.setAdverseNote(exec.getAdverseNote());
        vo.setFeeRecordId(exec.getFeeRecordId());
        vo.setFeeNo(exec.getFeeNo());
        vo.setCharged(exec.getFeeRecordId() != null);
        vo.setInfusion(true);
        return vo;
    }

    private InfusionRoundVO toRoundVO(BizInfusionRound round) {
        InfusionRoundVO vo = new InfusionRoundVO();
        vo.setId(round.getId());
        vo.setExecId(round.getExecId());
        vo.setRoundTime(round.getRoundTime());
        vo.setDripRate(round.getDripRate());
        vo.setRemainingVolume(round.getRemainingVolume());
        vo.setRoundNurseId(round.getRoundNurseId());
        vo.setRoundNurseName(round.getRoundNurseName());
        vo.setRemark(round.getRemark());
        return vo;
    }

}
