package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.DeathRegistrationDTO;
import com.his.patient.entity.BizDeathRegistration;
import com.his.patient.enums.DeathRegisterCopyEnum;
import com.his.patient.enums.DeathRegisterStatusEnum;
import com.his.patient.enums.DeathTypeEnum;
import com.his.patient.mapper.BizDeathRegistrationMapper;
import com.his.patient.service.DeathRegistrationService;
import com.his.patient.vo.DeathRegisterVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 死亡登记簿服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathRegistrationServiceImpl extends ServiceImpl<BizDeathRegistrationMapper, BizDeathRegistration> implements DeathRegistrationService {

    private static final int DESC_MAX = 500;
    private static final int UNIT_MAX = 100;

    private final BizDeathRegistrationMapper bizDeathRegistrationMapper;
    private final RedisSequenceService redisSequenceService;
    private final DeptScopeService deptScopeService;

    /**
     * 前端没选证明时自动挂该住院当前有效证明；选了别张证明必须属于本次住院
     */
    private static Long resolveCertId(Long certId, DeathRegisterVO.Base base) {
        if (certId == null) {
            return base.getCertId();
        }
        if (Objects.equals(certId, base.getCertId())) {
            return certId;
        }
        throw new BusinessException("所选死亡证明不属于本次住院（或已作废）");
    }

    /**
     * 领取联次：只留合法码值、去重、按升序，前端多选传法不一也不用担心。
     * 复数字段（"1,2,3"）@InEnum 校验不了（注解只管单值），逐个 split 调枚举判定
     */
    private static String normalizeCopies(String copies) {
        if (!TextUtil.hasText(copies)) {
            return null;
        }
        java.util.TreeSet<String> set = new java.util.TreeSet<>();
        for (String part : copies.split(",")) {
            String v = part.trim();
            if (v.isEmpty()) {
                continue;
            }
            if (!DeathRegisterCopyEnum.isValid(v)) {
                throw new BusinessException("联次取值不合法：" + v + "（1-记录联 2-户籍联 3-殡葬联 4-家属联）");
            }
            set.add(v);
        }
        return set.isEmpty() ? null : String.join(",", set);
    }

    // 内部

    private static Integer flag(Integer value) {
        return value == null ? 0 : (Objects.equals(value, 1) ? 1 : 0);
    }

    @Override
    public PageResult<DeathRegisterVO.Row> listPage(DeathRegistrationDTO.QueryPage query) {
        Page<DeathRegisterVO.Row> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<Long> deptIds = deptScopeService.scopedDeptIds(null);
        List<DeathRegisterVO.Row> records = bizDeathRegistrationMapper.selectRegisterPage(page, TextUtil.trimToNull(query.getKeyword()),
                query.getRegisterStatus(), query.getDeathType(), query.getPoliceFlag(), query.getDisputeFlag(),
                TimeUtil.dayStart(query.getStartDate()), TimeUtil.dayEnd(query.getEndDate()), deptIds);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public DeathRegisterVO.Detail getDetailById(Long id) {
        DeathRegisterVO.Detail detail = bizDeathRegistrationMapper.selectRegisterDetail(id);
        if (detail == null) {
            throw new BusinessException("死亡登记不存在或已删除");
        }
        deptScopeService.assertDeptAccessible(detail.getDeathDeptId());
        return detail;
    }

    /**
     * 登记底稿/候选（新建时服务端带出死者与证明摘要，前端不必自己拼）
     */
    @Override
    public DeathRegisterVO.Base base(Long admissionId) {
        DeathRegisterVO.Base base = bizDeathRegistrationMapper.selectRegisterBase(admissionId);
        if (base == null) {
            throw new BusinessException("住院记录不存在");
        }
        deptScopeService.assertDeptAccessible(base.getDeathDeptId());
        return base;
    }

    @Override
    public List<DeathRegisterVO.Base> admissionCandidates(String keyword, Integer limit) {
        int size = limit == null || limit <= 0 || limit > 200 ? 50 : limit;
        return bizDeathRegistrationMapper.selectDeathAdmissions(TextUtil.trimToNull(keyword), size, deptScopeService.scopedDeptIds(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(DeathRegistrationDTO.Upsert dto) {
        DeathRegisterVO.Base base = bizDeathRegistrationMapper.selectRegisterBase(dto.getAdmissionId());
        if (base == null) {
            throw new BusinessException("住院记录不存在");
        }
        deptScopeService.assertDeptAccessible(base.getDeathDeptId());
        if (!Boolean.TRUE.equals(base.getDeathDischarged())) {
            throw new BusinessException("该住院尚未办理「死亡」离院，死亡事实未确认，不能登记");
        }
        Integer deathType = dto.getDeathType();

        BizDeathRegistration register;
        if (dto.getId() == null) {
            if (bizDeathRegistrationMapper.countActiveByAdmission(dto.getAdmissionId(), null) > 0) {
                throw new BusinessException("该次住院已有死亡登记（一次住院只允许一条有效登记，登错请作废后重登）");
            }
            register = new BizDeathRegistration();
            register.setRegisterNo(redisSequenceService.generateDeathRegisterNo());
            register.setAdmissionId(dto.getAdmissionId());
            register.setRegisterStatus(DeathRegisterStatusEnum.DRAFT.getCode());
        } else {
            register = requireRegister(dto.getId());
            if (!Objects.equals(register.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("死亡登记不允许改挂到另一次住院");
            }
            if (!Objects.equals(register.getRegisterStatus(), DeathRegisterStatusEnum.DRAFT.getCode())) {
                throw new BusinessException(DeathRegisterStatusEnum.labelOrUnknown(register.getRegisterStatus()) + "的登记不能修改，只能作废后重登");
            }
            if (bizDeathRegistrationMapper.countActiveByAdmission(dto.getAdmissionId(), register.getId()) > 0) {
                throw new BusinessException("该次住院已有另一条死亡登记");
            }
        }

        register.setPatientId(base.getPatientId());
        register.setPatientName(base.getPatientName());
        register.setDeathTime(base.getDeathTime());
        register.setDeathDeptId(base.getDeathDeptId());
        register.setDeathDeptName(TextUtil.cutToNull(base.getDeathDeptName(), UNIT_MAX));
        register.setDeathBedNo(TextUtil.cutToNull(base.getDeathBedNo(), 16));
        register.setCertId(resolveCertId(dto.getCertId(), base));
        register.setDeathType(deathType);
        register.setPoliceFlag(flag(dto.getPoliceFlag()));
        register.setPoliceOrg(TextUtil.cutToNull(dto.getPoliceOrg(), UNIT_MAX));
        register.setPoliceCaseNo(TextUtil.cutToNull(dto.getPoliceCaseNo(), 64));
        register.setPoliceReportTime(TimeUtil.toSeconds(dto.getPoliceReportTime()));
        register.setForensicFlag(flag(dto.getForensicFlag()));
        register.setBodyDisposal(dto.getBodyDisposal());
        register.setBodyUnit(TextUtil.cutToNull(dto.getBodyUnit(), UNIT_MAX));
        register.setBodyTransportTime(TimeUtil.toSeconds(dto.getBodyTransportTime()));
        register.setRelativeName(TextUtil.cutToNull(dto.getRelativeName(), 50));
        register.setRelativeRelation(TextUtil.cutToNull(dto.getRelativeRelation(), 20));
        register.setRelativePhone(TextUtil.cutToNull(dto.getRelativePhone(), 20));
        register.setReceivedCopies(normalizeCopies(dto.getReceivedCopies()));
        register.setReceiveTime(TimeUtil.toSeconds(dto.getReceiveTime()));
        register.setDisputeFlag(flag(dto.getDisputeFlag()));
        register.setDisputeDesc(TextUtil.cutToNull(dto.getDisputeDesc(), DESC_MAX));
        register.setRemark(TextUtil.cutToNull(dto.getRemark(), DESC_MAX));
        // 报了案就得有报案时间兜底：只勾「已报公安」却填不出公安信息的，等同于没报
        if (Objects.equals(register.getPoliceFlag(), 1) && register.getPoliceReportTime() == null) {
            throw new BusinessException("已报公安的必须填写报案时间（否则事后无法与公安记录对上）");
        }
        upsert(register);
        return register.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(DeathRegistrationDTO.Confirm dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizDeathRegistration register = requireRegister(dto.getId());
        deptScopeService.assertDeptAccessible(register.getDeathDeptId());
        if (!Objects.equals(register.getRegisterStatus(), DeathRegisterStatusEnum.DRAFT.getCode())) {
            throw new BusinessException("只有草稿登记可确认（当前：" + DeathRegisterStatusEnum.labelOrUnknown(register.getRegisterStatus()) + "）");
        }
        if (register.getDeathType() != null && register.getDeathType() != DeathTypeEnum.DISEASE.getCode()
                && !Objects.equals(register.getPoliceFlag(), YesOrNoEnum.YES.getCode())) {
            throw new BusinessException("非疾病死亡/死因不明必须先报公安（并尽可能由法医出具）才能确认登记——"
                    + "法定要求，不能当疾病死亡处理掉");
        }
        if (register.getBodyDisposal() == null) {
            // ③业务规则：确认环节闸门，校验的是库中登记记录的状态而非本接口入参，DTO 注解表达不了
            throw new BusinessException("确认登记前必须登记尸体处理方式（遗体交给谁是处置闭环的一半）");
        }
        register.setRegisterStatus(DeathRegisterStatusEnum.DONE.getCode());
        register.setRegistrarId(operatorUser.getEmployeeId());
        register.setRegistrarName(operatorUser.getRealName());
        register.setRegisterTime(TimeUtil.nowSeconds());
        upsert(register);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidRegister(DeathRegistrationDTO.VoidRegister dto) {
        BizDeathRegistration register = requireRegister(dto.getId());
        deptScopeService.assertDeptAccessible(register.getDeathDeptId());
        if (Objects.equals(register.getRegisterStatus(), DeathRegisterStatusEnum.VOIDED.getCode())) {
            throw new BusinessException("该登记已作废，无需重复作废");
        }
        register.setRegisterStatus(DeathRegisterStatusEnum.VOIDED.getCode());
        register.setVoidReason(TextUtil.cut(dto.getReason().trim(), DESC_MAX));
        register.setVoidTime(TimeUtil.nowSeconds());
        upsert(register);
    }

    private BizDeathRegistration requireRegister(Long id) {
        BizDeathRegistration register = id == null ? null : bizDeathRegistrationMapper.selectById(id);
        if (register == null) {
            throw new BusinessException("死亡登记不存在或已删除");
        }
        return register;
    }

    private void upsert(BizDeathRegistration register) {
        if (register.getId() == null) {
            bizDeathRegistrationMapper.insert(register);
        } else if (bizDeathRegistrationMapper.updateById(register) <= 0) {
            throw new BusinessException("死亡登记保存失败，请重试");
        }
    }
}
