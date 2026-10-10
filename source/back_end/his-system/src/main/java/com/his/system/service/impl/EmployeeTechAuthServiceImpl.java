package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.enums.TechAuthCategoryEnum;
import com.his.common.enums.TechAuthStatusEnum;
import com.his.common.enums.TechLevelEnum;
import com.his.common.enums.TechOverrideSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.*;
import com.his.system.entity.BizTechAuthOverride;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysEmployeeTechAuth;
import com.his.system.mapper.BizTechAuthOverrideMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysEmployeeTechAuthMapper;
import com.his.system.service.DictCacheService;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.EmployeeTechAuthVO;
import com.his.system.vo.TechAuthCheckVO;
import com.his.system.vo.TechAuthOverrideVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 技术授权台账与准入闸实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeTechAuthServiceImpl extends ServiceImpl<SysEmployeeTechAuthMapper, SysEmployeeTechAuth> implements EmployeeTechAuthService {
    /** 授权状态：1-待审批 2-已授权 3-已驳回 4-已收回（唯一口径 TechAuthStatusEnum） */
    /**
     * 越权登记状态：1-待上级确认 2-已确认
     */
    private static final int OV_PENDING = 1;
    private static final int OV_CONFIRMED = 2;

    private static final int REASON_MAX = 500;
    private static final int BASIS_MAX = 200;

    private final DictCacheService dictCacheService;
    private final SysEmployeeTechAuthMapper sysEmployeeTechAuthMapper;
    private final BizTechAuthOverrideMapper bizTechAuthOverrideMapper;
    private final SysEmployeeMapper sysEmployeeMapper;

    // 一、台账查询

    @Override
    public PageResult<EmployeeTechAuthVO> listPage(TechAuthQueryPageDTO query) {
        LambdaQueryWrapper<SysEmployeeTechAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(query.getEmployeeName()), SysEmployeeTechAuth::getEmployeeName, TextUtil.trimToNull(query.getEmployeeName()))
                .eq(query.getEmployeeId() != null, SysEmployeeTechAuth::getEmployeeId, query.getEmployeeId())
                .eq(query.getAuthCategory() != null, SysEmployeeTechAuth::getAuthCategory, query.getAuthCategory())
                .eq(query.getTechLevel() != null, SysEmployeeTechAuth::getTechLevel, query.getTechLevel())
                .eq(query.getAuthStatus() != null, SysEmployeeTechAuth::getAuthStatus, query.getAuthStatus())
                .eq(query.getAuthType() != null, SysEmployeeTechAuth::getAuthType, query.getAuthType());
        if (Objects.equals(1, query.getOnlyEffective())) {
            applyEffective(wrapper, LocalDate.now());
        }
        wrapper.orderByDesc(SysEmployeeTechAuth::getEmployeeId)
                .orderByAsc(SysEmployeeTechAuth::getAuthCategory)
                .orderByDesc(SysEmployeeTechAuth::getId);

        Page<SysEmployeeTechAuth> page = sysEmployeeTechAuthMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<EmployeeTechAuthVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public EmployeeTechAuthVO getById(Long id) {
        return toVO(mustGet(id));
    }

    // 二、登记 / 审批 / 收回 / 删除

    @Override
    public List<EmployeeTechAuthVO> listByEmployee(Long employeeId) {
        if (employeeId == null) {
            return List.of();
        }
        LambdaQueryWrapper<SysEmployeeTechAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployeeTechAuth::getEmployeeId, employeeId)
                .orderByAsc(SysEmployeeTechAuth::getAuthCategory);
        return sysEmployeeTechAuthMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public List<EmployeeTechAuthVO> mine() {
        Long empId = UserUtils.getCurrentUser().getEmployeeId();
        return listByEmployee(empId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(TechAuthUpsertDTO dto) {
        if (TechAuthCategoryEnum.fromCode(dto.getAuthCategory()) == null) {
            throw new BusinessException("授权类别不合法（1-手术 2-麻醉 3-内镜与介入）");
        }
        if (!TechLevelEnum.isValid(dto.getTechLevel())) {
            throw new BusinessException("授权级别不合法（只能授 1~4 级）");
        }
        if (dto.getValidUntil() != null && dto.getValidUntil().isBefore(dto.getValidFrom())) {
            throw new BusinessException("有效期至不能早于生效日期");
        }

        SysEmployee emp = sysEmployeeMapper.selectById(dto.getEmployeeId());
        if (emp == null) {
            throw new BusinessException("员工不存在");
        }

        boolean create = dto.getId() == null;
        SysEmployeeTechAuth entity = create ? new SysEmployeeTechAuth() : mustGet(dto.getId());
        if (!create) {
            if (!Objects.equals(TechAuthStatusEnum.PENDING.getCode(), entity.getAuthStatus()) && !Objects.equals(TechAuthStatusEnum.REJECTED.getCode(), entity.getAuthStatus())) {
                throw new BusinessException(emp.getEmpName() + " 的「"
                        + TechAuthCategoryEnum.getText(entity.getAuthCategory()) + "」授权当前为「"
                        + TechAuthStatusEnum.getText(entity.getAuthStatus())
                        + "」，已生效的授权不允许改字段（需要变级别或换有效期请先收回，再重新授权一条 —— 台账必须保留当时授到几级）");
            }
            // 授权不允许改挂到另一个人名下：id 来自前端，归属只认库里原值
            if (!Objects.equals(entity.getEmployeeId(), dto.getEmployeeId())) {
                throw new BusinessException("不允许把授权记录改挂到另一个员工名下");
            }
        }

        // 同一人同类别同期只能有一条：两条「已授权」并存时闸门取哪条不确定
        if (hasSamePeriod(dto.getEmployeeId(), dto.getAuthCategory(), dto.getValidFrom(), dto.getId())) {
            throw new BusinessException(emp.getEmpName() + " 在 " + dto.getValidFrom() + " 已有一条「"
                    + TechAuthCategoryEnum.getText(dto.getAuthCategory()) + "」授权记录，同一天同类别只能授一条"
                    + "（要调整请先收回原记录）");
        }
        SysEmployeeTechAuth granted = findEffective(dto.getEmployeeId(), dto.getAuthCategory(), dto.getValidFrom());
        if (granted != null && (create || !Objects.equals(granted.getId(), dto.getId()))) {
            throw new BusinessException(emp.getEmpName() + " 的「"
                    + TechAuthCategoryEnum.getText(dto.getAuthCategory()) + "」已有一条生效中的授权（上限"
                    + TechLevelEnum.getText(granted.getTechLevel()) + "，有效期至 "
                    + (granted.getValidUntil() == null ? "长期" : granted.getValidUntil())
                    + "），不能并存第二条；如需变更级别请先收回");
        }

        entity.setEmployeeId(dto.getEmployeeId());
        entity.setEmployeeName(emp.getEmpName());
        entity.setDeptId(emp.getDeptId());
        entity.setDeptName(emp.getDeptName());
        entity.setTitle(emp.getTitle());
        entity.setAuthCategory(dto.getAuthCategory());
        entity.setTechLevel(dto.getTechLevel());
        entity.setItemScope(TextUtil.cutToNull(TextUtil.trimToNull(dto.getItemScope()), 500));
        entity.setAuthType(dto.getAuthType() == null ? 1 : dto.getAuthType());
        entity.setAuthBasis(TextUtil.cutToNull(dto.getAuthBasis(), BASIS_MAX));
        entity.setValidFrom(dto.getValidFrom());
        entity.setValidUntil(dto.getValidUntil());
        entity.setRemark(TextUtil.cutToNull(dto.getRemark(), REASON_MAX));
        if (create) {
            entity.setAuthStatus(TechAuthStatusEnum.PENDING.getCode());
            entity.setApplyBy(UserUtils.getCurrentUser().getRealName());
            entity.setApplyTime(LocalDateTime.now());
            sysEmployeeTechAuthMapper.insert(entity);
        } else {
            sysEmployeeTechAuthMapper.updateById(entity);
        }
        log.info("{}技术授权 emp={} {} 上限={} {}~{}",
                create ? "登记" : "修改", emp.getEmpName(),
                TechAuthCategoryEnum.getText(dto.getAuthCategory()),
                TechLevelEnum.getText(dto.getTechLevel()), dto.getValidFrom(), dto.getValidUntil());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(TechAuthApproveDTO dto) {
        SysEmployeeTechAuth entity = mustGet(dto.getId());
        if (!Objects.equals(TechAuthStatusEnum.PENDING.getCode(), entity.getAuthStatus())) {
            throw new BusinessException("该授权记录当前为「" + TechAuthStatusEnum.getText(entity.getAuthStatus())
                    + "」，只有「待审批」可以审批");
        }
        entity.setAuthStatus(Boolean.TRUE.equals(dto.getApproved()) ? TechAuthStatusEnum.GRANTED.getCode() : TechAuthStatusEnum.REJECTED.getCode());
        entity.setApproverId(UserUtils.getCurrentUser().getEmployeeId());
        entity.setApproverName(UserUtils.getCurrentUser().getRealName());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveOpinion(TextUtil.cutToNull(dto.getApproveOpinion(), REASON_MAX));
        // 审批即生效：原记录若授到更晚日期，收回旧的一律由人工做，这里不自动覆盖
        sysEmployeeTechAuthMapper.updateById(entity);
        log.info("技术授权审批 id={} 结论={} 审批人={}", entity.getId(),
                TechAuthStatusEnum.getText(entity.getAuthStatus()), entity.getApproverName());
    }

    // 三、准入闸

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revoke(TechAuthRevokeDTO dto) {
        SysEmployeeTechAuth entity = mustGet(dto.getId());
        if (!Objects.equals(TechAuthStatusEnum.GRANTED.getCode(), entity.getAuthStatus())) {
            throw new BusinessException("只有「已授权」的记录可以收回，当前为「"
                    + TechAuthStatusEnum.getText(entity.getAuthStatus()) + "」");
        }
        entity.setAuthStatus(TechAuthStatusEnum.REVOKED.getCode());
        entity.setRevokeBy(UserUtils.getCurrentUser().getRealName());
        entity.setRevokeTime(LocalDateTime.now());
        entity.setRevokeReason(TextUtil.cutToNull(dto.getRevokeReason(), REASON_MAX));
        sysEmployeeTechAuthMapper.updateById(entity);
        log.info("技术授权收回 id={} emp={} {} 原因={}", entity.getId(), entity.getEmployeeName(),
                TechAuthCategoryEnum.getText(entity.getAuthCategory()), entity.getRevokeReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        SysEmployeeTechAuth entity = mustGet(id);
        if (!Objects.equals(TechAuthStatusEnum.PENDING.getCode(), entity.getAuthStatus()) && !Objects.equals(TechAuthStatusEnum.REJECTED.getCode(), entity.getAuthStatus())) {
            throw new BusinessException("已授权/已收回的授权记录不许删除（台账留痕），请走「收回」");
        }
        // 本表无 del_flag，deleteById 即物理删，不会占着 uk_emp_cat_from
        sysEmployeeTechAuthMapper.deleteById(id);
    }

    @Override
    public SysEmployeeTechAuth findEffective(Long employeeId, Integer authCategory, LocalDate operateDate) {
        if (employeeId == null || authCategory == null) {
            return null;
        }
        LocalDate date = operateDate == null ? LocalDate.now() : operateDate;
        LambdaQueryWrapper<SysEmployeeTechAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployeeTechAuth::getEmployeeId, employeeId)
                .eq(SysEmployeeTechAuth::getAuthCategory, authCategory)
                .eq(SysEmployeeTechAuth::getAuthStatus, TechAuthStatusEnum.GRANTED.getCode());
        applyEffective(wrapper, date);
        wrapper.orderByDesc(SysEmployeeTechAuth::getTechLevel).last("LIMIT 1");
        return sysEmployeeTechAuthMapper.selectOne(wrapper);
    }

    // 四、越权登记台账

    @Override
    public TechAuthCheckVO checkAuthorized(Long employeeId, Integer authCategory,
                                           Integer requiredLevel, String itemCode, LocalDate operateDate) {
        TechAuthCheckVO vo = new TechAuthCheckVO();
        vo.setEmployeeId(employeeId);
        vo.setAuthCategory(authCategory);
        vo.setAuthCategoryText(TechAuthCategoryEnum.getText(authCategory));
        vo.setRequiredLevel(requiredLevel);
        vo.setAuthorized(false);
        vo.setPassed(false);

        if (employeeId == null) {
            vo.setMessage("当前登录人没有关联员工档案，无法判定技术授权，请先在系统管理里补员工的员工档案");
            return vo;
        }
        SysEmployee emp = sysEmployeeMapper.selectById(employeeId);
        vo.setEmployeeName(emp == null ? null : emp.getEmpName());
        if (emp == null) {
            vo.setMessage("操作者（员工ID " + employeeId + "）档案不存在，无法判定技术授权");
            return vo;
        }
        if (TechAuthCategoryEnum.fromCode(authCategory) == null) {
            vo.setMessage("授权类别不合法，无法判定");
            return vo;
        }

        SysEmployeeTechAuth held = findEffective(employeeId, authCategory, operateDate);
        if (held == null) {
            vo.setMessage(emp.getEmpName() + " 没有「" + TechAuthCategoryEnum.getText(authCategory)
                    + "」类在有效期内的技术授权，不能开展" + TechLevelEnum.getText(requiredLevel)
                    + "操作（《医疗机构手术分级管理办法》要求授权到医师本人）");
            return vo;
        }
        vo.setHeldLevel(held.getTechLevel());
        if (requiredLevel != null && held.getTechLevel() < requiredLevel) {
            vo.setMessage(emp.getEmpName() + " 的「" + TechAuthCategoryEnum.getText(authCategory)
                    + "」授权上限为" + TechLevelEnum.getText(held.getTechLevel())
                    + "，低于本次要求的" + TechLevelEnum.getText(requiredLevel) + "，不能开展");
            return vo;
        }
        if (TextUtil.hasText(held.getItemScope()) && TextUtil.hasText(itemCode)
                && !scopeContains(held.getItemScope(), itemCode)) {
            vo.setMessage(emp.getEmpName() + " 的「" + TechAuthCategoryEnum.getText(authCategory)
                    + "」为限制授权，仅限术式【" + held.getItemScope() + "】，不含本次的 " + itemCode);
            return vo;
        }
        vo.setAuthorized(true);
        vo.setPassed(true);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TechAuthCheckVO gate(TechAuthGateDTO gate) {
        if (gate == null) {
            throw new BusinessException("准入校验缺少入参");
        }
        if (gate.getEmployeeId() == null) {
            gate.setEmployeeId(resolveEmployeeIdByName(gate.getEmployeeName()));
        }
        if (gate.getEmployeeId() == null) {
            throw new BusinessException("准入校验缺少操作者（员工ID）");
        }
        LocalDate date = gate.getOperateDate() == null ? LocalDate.now() : gate.getOperateDate();
        TechAuthCheckVO check = checkAuthorized(gate.getEmployeeId(), gate.getAuthCategory(),
                gate.getRequiredLevel(), gate.getItemCode(), date);
        if (check.isAuthorized()) {
            return check;
        }
        if (!gate.isEmergency()) {
            throw new BusinessException(check.getMessage());
        }
        // 急诊/抢救：等不起，放行但必须留痕，事后由上级确认
        BizTechAuthOverride override = new BizTechAuthOverride();
        override.setSourceType(gate.getSourceType());
        override.setSourceId(gate.getSourceId());
        override.setSourceNo(gate.getSourceNo());
        override.setEmployeeId(gate.getEmployeeId());
        override.setEmployeeName(check.getEmployeeName());
        override.setAuthCategory(gate.getAuthCategory());
        override.setRequiredLevel(gate.getRequiredLevel() == null ? 0 : gate.getRequiredLevel());
        override.setHeldLevel(check.getHeldLevel());
        override.setReason(TextUtil.cutToNull(TextUtil.hasText(gate.getReason())
                ? gate.getReason() : "急诊/抢救越权：" + check.getMessage(), REASON_MAX));
        override.setOccurTime(LocalDateTime.now());
        override.setOverrideStatus(OV_PENDING);
        bizTechAuthOverrideMapper.insert(override);
        check.setPassed(true);
        check.setOverrideId(override.getId());
        log.warn("急诊越权放行：emp={} {} 要求={} 现有={} 单据={} 登记ID={}",
                check.getEmployeeName(), TechAuthCategoryEnum.getText(gate.getAuthCategory()),
                TechLevelEnum.getText(gate.getRequiredLevel()), TechLevelEnum.getText(check.getHeldLevel()),
                TechOverrideSourceEnum.getText(gate.getSourceType()), override.getId());
        return check;
    }

    // 五、内部工具

    @Override
    public PageResult<TechAuthOverrideVO> overrideListPage(TechAuthOverrideQueryPageDTO query) {
        LambdaQueryWrapper<BizTechAuthOverride> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(query.getEmployeeName()), BizTechAuthOverride::getEmployeeName, TextUtil.trimToNull(query.getEmployeeName()))
                .eq(query.getAuthCategory() != null, BizTechAuthOverride::getAuthCategory, query.getAuthCategory())
                .eq(query.getSourceType() != null, BizTechAuthOverride::getSourceType, query.getSourceType())
                .eq(query.getOverrideStatus() != null, BizTechAuthOverride::getOverrideStatus, query.getOverrideStatus())
                .orderByDesc(BizTechAuthOverride::getId);
        Page<BizTechAuthOverride> page = bizTechAuthOverrideMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<TechAuthOverrideVO> records = page.getRecords().stream().map(this::toOverrideVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOverride(TechAuthOverrideConfirmDTO dto) {
        BizTechAuthOverride entity = bizTechAuthOverrideMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("越权登记不存在");
        }
        if (!Objects.equals(OV_PENDING, entity.getOverrideStatus())) {
            throw new BusinessException("该越权登记已确认，不能重复确认");
        }
        Long supervisorId = UserUtils.getCurrentUser().getEmployeeId();
        if (supervisorId != null && supervisorId.equals(entity.getEmployeeId())) {
            throw new BusinessException("越权本人不能确认自己的越权登记（事后追认必须由上级或医务科做，否则等于自己给自己补授权）");
        }
        entity.setOverrideStatus(OV_CONFIRMED);
        entity.setSupervisorId(supervisorId);
        entity.setSupervisorName(UserUtils.getCurrentUser().getRealName());
        entity.setConfirmTime(LocalDateTime.now());
        entity.setConfirmOpinion(TextUtil.cutToNull(dto.getConfirmOpinion(), REASON_MAX));
        bizTechAuthOverrideMapper.updateById(entity);
        log.info("越权登记确认 id={} emp={} 确认人={}", entity.getId(), entity.getEmployeeName(), entity.getSupervisorName());
    }

    /**
     * 按姓名回捞员工ID（术者/内镜医师在库里只有姓名字符串的历史单据用）。
     * <p>查不到与重名都拒：前者多半是外院专家没建档，后者根本判不出是谁，
     * 两种情况下放行都是「闸门形同虚设」。
     */
    private Long resolveEmployeeIdByName(String employeeName) {
        String name = TextUtil.trimToNull(employeeName);
        if (name == null) {
            return null;
        }
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployee::getEmpName, name);
        List<SysEmployee> hits = sysEmployeeMapper.selectList(wrapper);
        if (hits.isEmpty()) {
            throw new BusinessException("未找到医师「" + name + "」的员工档案，无法校验技术授权（请先在系统管理→员工档案建档，或改由选择具体员工）");
        }
        if (hits.size() > 1) {
            throw new BusinessException("医师「" + name + "」有 " + hits.size() + " 条同名员工档案，无法判定是谁的技术授权，请在单据上指定到具体员工");
        }
        return hits.get(0).getId();
    }

    /**
     * 有效期覆盖指定日期（含边界）：valid_from <= date <= valid_until，NULL 有效期 = 长期
     */
    private void applyEffective(LambdaQueryWrapper<SysEmployeeTechAuth> wrapper, LocalDate date) {
        wrapper.le(SysEmployeeTechAuth::getValidFrom, date)
                .and(w -> w.isNull(SysEmployeeTechAuth::getValidUntil)
                        .or().ge(SysEmployeeTechAuth::getValidUntil, date));
    }

    private boolean hasSamePeriod(Long employeeId, Integer category, LocalDate validFrom, Long excludeId) {
        LambdaQueryWrapper<SysEmployeeTechAuth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmployeeTechAuth::getEmployeeId, employeeId)
                .eq(SysEmployeeTechAuth::getAuthCategory, category)
                .eq(SysEmployeeTechAuth::getValidFrom, validFrom)
                .ne(excludeId != null, SysEmployeeTechAuth::getId, excludeId);
        return sysEmployeeTechAuthMapper.exists(wrapper);
    }

    private boolean scopeContains(String itemScope, String itemCode) {
        return Arrays.stream(itemScope.split(","))
                .map(String::trim)
                .anyMatch(s -> s.equalsIgnoreCase(itemCode.trim()));
    }

    private SysEmployeeTechAuth mustGet(Long id) {
        // C-非 web 入参：私有守卫，除 GET 标量参数外还被多个审批 DTO 的内部调用共用（传 dto.getId()），Bean Validation 不覆盖，保留
        if (id == null) {
            throw new BusinessException("授权记录ID不能为空");
        }
        SysEmployeeTechAuth entity = sysEmployeeTechAuthMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("授权记录不存在或已删除");
        }
        return entity;
    }

    private EmployeeTechAuthVO toVO(SysEmployeeTechAuth entity) {
        EmployeeTechAuthVO vo = new EmployeeTechAuthVO();
        vo.setId(entity.getId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setDeptId(entity.getDeptId());
        vo.setDeptName(entity.getDeptName());
        vo.setTitle(entity.getTitle());
        vo.setAuthCategory(entity.getAuthCategory());
        vo.setAuthCategoryText(TechAuthCategoryEnum.getText(entity.getAuthCategory()));
        vo.setTechLevel(entity.getTechLevel());
        vo.setTechLevelText(TechLevelEnum.getText(entity.getTechLevel()));
        vo.setItemScope(entity.getItemScope());
        vo.setAuthType(entity.getAuthType());
        vo.setAuthTypeText(authTypeText(entity.getAuthType()));
        vo.setAuthBasis(entity.getAuthBasis());
        vo.setValidFrom(entity.getValidFrom());
        vo.setValidUntil(entity.getValidUntil());
        vo.setIndefinite(entity.getValidUntil() == null);
        vo.setAuthStatus(entity.getAuthStatus());
        vo.setAuthStatusText(TechAuthStatusEnum.getText(entity.getAuthStatus()));
        LocalDate today = LocalDate.now();
        vo.setEffective(Objects.equals(TechAuthStatusEnum.GRANTED.getCode(), entity.getAuthStatus())
                && !entity.getValidFrom().isAfter(today)
                && (entity.getValidUntil() == null || !entity.getValidUntil().isBefore(today)));
        vo.setCanApprove(Objects.equals(TechAuthStatusEnum.PENDING.getCode(), entity.getAuthStatus()));
        vo.setCanRevoke(Objects.equals(TechAuthStatusEnum.GRANTED.getCode(), entity.getAuthStatus()));
        vo.setCanEdit(Objects.equals(TechAuthStatusEnum.PENDING.getCode(), entity.getAuthStatus()) || Objects.equals(TechAuthStatusEnum.REJECTED.getCode(), entity.getAuthStatus()));
        vo.setApplyBy(entity.getApplyBy());
        vo.setApplyTime(entity.getApplyTime());
        vo.setApproverId(entity.getApproverId());
        vo.setApproverName(entity.getApproverName());
        vo.setApproveTime(entity.getApproveTime());
        vo.setApproveOpinion(entity.getApproveOpinion());
        vo.setRevokeBy(entity.getRevokeBy());
        vo.setRevokeTime(entity.getRevokeTime());
        vo.setRevokeReason(entity.getRevokeReason());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private TechAuthOverrideVO toOverrideVO(BizTechAuthOverride entity) {
        TechAuthOverrideVO vo = new TechAuthOverrideVO();
        vo.setId(entity.getId());
        vo.setSourceType(entity.getSourceType());
        vo.setSourceTypeText(TechOverrideSourceEnum.getText(entity.getSourceType()));
        vo.setSourceId(entity.getSourceId());
        vo.setSourceNo(entity.getSourceNo());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setEmployeeName(entity.getEmployeeName());
        vo.setAuthCategory(entity.getAuthCategory());
        vo.setAuthCategoryText(TechAuthCategoryEnum.getText(entity.getAuthCategory()));
        vo.setRequiredLevel(entity.getRequiredLevel());
        vo.setRequiredLevelText(TechLevelEnum.getText(entity.getRequiredLevel()));
        vo.setHeldLevel(entity.getHeldLevel());
        vo.setHeldLevelText(entity.getHeldLevel() == null ? "无该类别授权" : TechLevelEnum.getText(entity.getHeldLevel()));
        vo.setReason(entity.getReason());
        vo.setOccurTime(entity.getOccurTime());
        vo.setOverrideStatus(entity.getOverrideStatus());
        vo.setOverrideStatusText(Objects.equals(OV_CONFIRMED, entity.getOverrideStatus()) ? "已确认" : "待上级确认");
        vo.setSupervisorId(entity.getSupervisorId());
        vo.setSupervisorName(entity.getSupervisorName());
        vo.setConfirmTime(entity.getConfirmTime());
        vo.setConfirmOpinion(entity.getConfirmOpinion());
        vo.setCreateTime(entity.getCreateTime());
        // 确认键与写入侧同一条规矩：越权本人不能追认自己（否则等于自己给自己补授权），界面上也就不给这个按钮
        Long me = UserUtils.getCurrentUser().getEmployeeId();
        vo.setCanConfirm(Objects.equals(OV_PENDING, entity.getOverrideStatus())
                && !(me != null && me.equals(entity.getEmployeeId())));
        return vo;
    }

    private String authTypeText(Integer type) {
        if (type == null) {
            return "—";
        }
        return dictCacheService.getDicDataLabel(DictType.TECH_AUTH_TYPE, type);
    }
}