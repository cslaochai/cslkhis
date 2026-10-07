package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.DutyPostQueryPageDTO;
import com.his.system.dto.DutyPostUpsertDTO;
import com.his.system.entity.BizDutyPost;
import com.his.system.entity.BizShift;
import com.his.system.entity.SysDepartment;
import com.his.system.entity.SysWard;
import com.his.system.mapper.BizDutyPostMapper;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.mapper.SysWardMapper;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.service.DutyPostService;
import com.his.system.service.ShiftService;
import com.his.system.vo.DutyPostSelectListVO;
import com.his.system.vo.DutyPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 值守点位服务实现。
 */
@Service
@RequiredArgsConstructor
public class DutyPostServiceImpl extends ServiceImpl<BizDutyPostMapper, BizDutyPost> implements DutyPostService {
    private final DeptScopeProvider deptScopeProvider;

    private final ShiftService shiftService;
    private final SysWardMapper sysWardMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    /**
     * 停用判定：状态为空视为未停用（缺省启用），只有显式停用才拦
     */
    private static boolean disabled(Integer status) {
        return status != null && status == EnableStatusEnum.DISABLED.getCode();
    }

    @Override
    public List<DutyPostSelectListVO> selectListVO(Integer dutyScope, Integer orgType, Long orgId) {
        List<BizDutyPost> rows = list(scoped(new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getStatus, EnableStatusEnum.ENABLED.getCode())
                .eq(dutyScope != null, BizDutyPost::getDutyScope, dutyScope)
                .eq(orgType != null, BizDutyPost::getOrgType, orgType)
                .eq(orgId != null, BizDutyPost::getOrgId, orgId)
                .orderByAsc(BizDutyPost::getSortNo).orderByAsc(BizDutyPost::getId)));
        Map<Long, BizShift> shifts = shiftService.mapByIds(idsOf(rows));
        List<DutyPostSelectListVO> vos = new ArrayList<>(rows.size());
        for (BizDutyPost row : rows) {
            BizShift shift = shifts.get(row.getShiftId());
            DutyPostSelectListVO vo = new DutyPostSelectListVO();
            vo.setId(row.getId());
            vo.setPostCode(row.getPostCode());
            vo.setPostName(row.getPostName());
            vo.setDutyScope(row.getDutyScope());
            vo.setDutyScopeText(DutyScopeEnum.getText(row.getDutyScope()));
            vo.setRoleType(row.getRoleType());
            vo.setRoleTypeText(DutyRoleTypeEnum.getText(row.getRoleType()));
            vo.setDutyLevel(row.getDutyLevel());
            vo.setDutyLevelText(DutyLevelEnum.getText(row.getDutyLevel()));
            vo.setAttendMode(row.getAttendMode());
            vo.setAttendModeText(AttendModeEnum.getText(row.getAttendMode()));
            vo.setShiftId(row.getShiftId());
            vo.setShiftName(shift == null ? null : shift.getShiftName());
            vo.setStartTime(shift == null ? null : shift.getStartTime());
            vo.setEndTime(shift == null ? null : shift.getEndTime());
            vo.setRequiredStaffType(row.getRequiredStaffType());
            vo.setPhone(row.getPhone());
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public PageResult<DutyPostVO> pageVO(DutyPostQueryPageDTO dto) {
        LambdaQueryWrapper<BizDutyPost> wrapper = scoped(new LambdaQueryWrapper<BizDutyPost>()
                .eq(dto.getDutyScope() != null, BizDutyPost::getDutyScope, dto.getDutyScope())
                .eq(dto.getOrgType() != null, BizDutyPost::getOrgType, dto.getOrgType())
                .eq(dto.getOrgId() != null, BizDutyPost::getOrgId, dto.getOrgId())
                .eq(dto.getStatus() != null, BizDutyPost::getStatus, dto.getStatus()));
        if (TextUtil.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(BizDutyPost::getPostName, keyword)
                    .or().like(BizDutyPost::getPostCode, keyword));
        }
        wrapper.orderByAsc(BizDutyPost::getSortNo).orderByAsc(BizDutyPost::getId);
        Page<BizDutyPost> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        Map<Long, BizShift> shifts = shiftService.mapByIds(idsOf(page.getRecords()));
        List<DutyPostVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizDutyPost row : page.getRecords()) {
            BizShift shift = shifts.get(row.getShiftId());
            DutyPostVO vo = new DutyPostVO();
            vo.setId(row.getId());
            vo.setPostCode(row.getPostCode());
            vo.setPostName(row.getPostName());
            vo.setDutyScope(row.getDutyScope());
            vo.setDutyScopeText(DutyScopeEnum.getText(row.getDutyScope()));
            vo.setOrgType(row.getOrgType());
            vo.setOrgTypeText(OrgUnitTypeEnum.getText(row.getOrgType()));
            vo.setOrgId(row.getOrgId());
            vo.setOrgName(row.getOrgId() == null || row.getOrgId() == 0L
                    ? OrgUnitTypeEnum.HOSPITAL.getLabel() : unitNameOf(row.getOrgType(), row.getOrgId()));
            vo.setRoleType(row.getRoleType());
            vo.setRoleTypeText(DutyRoleTypeEnum.getText(row.getRoleType()));
            vo.setDutyLevel(row.getDutyLevel());
            vo.setDutyLevelText(DutyLevelEnum.getText(row.getDutyLevel()));
            vo.setAttendMode(row.getAttendMode());
            vo.setAttendModeText(AttendModeEnum.getText(row.getAttendMode()));
            vo.setShiftId(row.getShiftId());
            vo.setShiftName(shift == null ? null : shift.getShiftName());
            vo.setStartTime(shift == null ? null : shift.getStartTime());
            vo.setEndTime(shift == null ? null : shift.getEndTime());
            vo.setRequiredStaffType(row.getRequiredStaffType());
            vo.setRequiredStaffTypeName(StaffTypeEnum.getText(row.getRequiredStaffType()));
            vo.setPhone(row.getPhone());
            vo.setSortNo(row.getSortNo());
            vo.setStatus(row.getStatus());
            vo.setRemark(row.getRemark());
            vos.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public Long upsert(DutyPostUpsertDTO dto) {
        BizDutyPost post = new BizDutyPost();
        post.setId(dto.getId());
        post.setPostCode(dto.getPostCode().trim());
        post.setPostName(dto.getPostName().trim());
        post.setDutyScope(dto.getDutyScope());
        post.setOrgType(dto.getOrgType() == null ? OrgUnitTypeEnum.HOSPITAL.getCode() : dto.getOrgType());
        post.setOrgId(dto.getOrgId() == null ? 0L : dto.getOrgId());
        post.setRoleType(dto.getRoleType());
        post.setDutyLevel(dto.getDutyLevel());
        post.setAttendMode(dto.getAttendMode());
        post.setShiftId(dto.getShiftId());
        post.setRequiredStaffType(dto.getRequiredStaffType());
        post.setPhone(dto.getPhone());
        post.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
        post.setStatus(dto.getStatus() == null ? EnableStatusEnum.ENABLED.getCode() : dto.getStatus());
        post.setRemark(dto.getRemark());

        if (DutyScopeEnum.fromCode(post.getDutyScope()) == null) {
            throw new BusinessException("责任范围只允许 " + DutyScopeEnum.whitelistText());
        }
        if (OrgUnitTypeEnum.fromCode(post.getOrgType()) == null) {
            throw new BusinessException("排班单元类型只允许 " + OrgUnitTypeEnum.whitelistText());
        }
        if (DutyRoleTypeEnum.fromCode(post.getRoleType()) == null) {
            throw new BusinessException("班内角色只允许 " + DutyRoleTypeEnum.whitelistText());
        }
        // 层级与响应形态（sql/202）：
        //  - 层级缺省按责任范围推：临床科室点位默认一线（住院医师驻守），其余默认为「不适用」；
        //  - 响应形态缺省按层级推：一线留院值班，二线三线听班。
        //  让调用方少传两个字段，但不允许推完还是个非法值 —— 推错了比不推更麻烦。
        applyDutyLevel(post);
        if (post.getRequiredStaffType() != null && StaffTypeEnum.fromCode(post.getRequiredStaffType()) == null) {
            throw new BusinessException("应到岗位类别只允许 " + StaffTypeEnum.whitelistText());
        }
        // 点位必须挂在一个真实启用且配了时限的班次上，否则「今天几点到几点归他」无从回答
        BizShift shift = shiftService.getById(post.getShiftId());
        if (shift == null) {
            throw new BusinessException("所选班次不存在或已删除，请重新选择");
        }
        if (disabled(shift.getStatus())) {
            throw new BusinessException("班次「" + shift.getShiftName() + "」已停用，不能作为值守班次的时限来源");
        }
        // 单元归属：病区级必须是启用的病区，科室级必须是启用科室，全院级固定为 0
        applyUnit(post);
        // 同编码防重（排除自身）：点位编码是唯一键，撞了就是一条重复键报错
        LambdaQueryWrapper<BizDutyPost> dup = new LambdaQueryWrapper<BizDutyPost>()
                .eq(BizDutyPost::getPostCode, post.getPostCode())
                .ne(post.getId() != null, BizDutyPost::getId, post.getId());
        if (count(dup) > 0) {
            throw new BusinessException("点位编码「" + post.getPostCode() + "」已存在");
        }
        saveOrUpdate(post);
        return post.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("点位不存在或已删除");
        }
        // 唯一键不含删除标志 → 物理删（软删的行会继续占着编码，复用同码必撞键）
        baseMapper.purgeById(id);
    }

    /**
     * 层级 + 响应形态的兜底与校验。
     *
     * <p><b>为什么临床科室点位强制要层级</b>：医师值班点位存在的唯一目的就是「出事了知道升给谁」。
     * 一个 duty_level=0 的临床点位，排上去也是个没有升级路径的死位 —— 宁可落库时拦住。
     */
    private void applyDutyLevel(BizDutyPost post) {
        if (post.getDutyLevel() == null) {
            post.setDutyLevel(DutyScopeEnum.CLINICAL.getCode() == post.getDutyScope()
                    ? DutyLevelEnum.FIRST.getCode() : DutyLevelEnum.NONE.getCode());
        }
        if (DutyLevelEnum.fromCode(post.getDutyLevel()) == null) {
            throw new BusinessException("值班层级只允许 " + DutyLevelEnum.whitelistText());
        }
        if (DutyScopeEnum.CLINICAL.getCode() == post.getDutyScope()
                && DutyLevelEnum.NONE.getCode() == post.getDutyLevel()) {
            throw new BusinessException("临床科室值班点位必须指定层级（1-一线 2-二线 3-三线）");
        }
        if (post.getAttendMode() == null) {
            // 二线三线是听班（在家待命）；一线留院驻守；「不适用」沿用列默认值留院值班，
            // 行政总值班那批老点位本来就是 3，别为了统一把它们的语义改掉。
            post.setAttendMode(DutyLevelEnum.SECOND.getCode() == post.getDutyLevel()
                    || DutyLevelEnum.THIRD.getCode() == post.getDutyLevel()
                    ? AttendModeEnum.ON_CALL.getCode() : AttendModeEnum.IN_HOSPITAL.getCode());
        }
        if (AttendModeEnum.fromCode(post.getAttendMode()) == null) {
            throw new BusinessException("响应形态只允许 " + AttendModeEnum.whitelistText());
        }
    }

    private void applyUnit(BizDutyPost post) {
        if (OrgUnitTypeEnum.HOSPITAL.getCode() == post.getOrgType()) {
            post.setOrgId(0L);
            return;
        }
        if (post.getOrgId() == null || post.getOrgId() == 0L) {
            throw new BusinessException("请选择值守点位所属的" + OrgUnitTypeEnum.getText(post.getOrgType()));
        }
        if (OrgUnitTypeEnum.WARD.getCode() == post.getOrgType()) {
            SysWard ward = sysWardMapper.selectById(post.getOrgId());
            if (ward == null || disabled(ward.getStatus())) {
                throw new BusinessException("所选病区不存在或已停用");
            }
        } else {
            SysDepartment dept = sysDepartmentMapper.selectById(post.getOrgId());
            if (dept == null || disabled(dept.getStatus())) {
                throw new BusinessException("所选科室不存在或已停用");
            }
        }
    }

    /**
     * 数据范围收口：受限岗位只看得见自己科室的科属点位；全院级点位对所有人开放
     * （「今天全院谁负责」不是敏感信息，收口等于让人半夜找不到打电话的对象）。
     */
    private LambdaQueryWrapper<BizDutyPost> scoped(LambdaQueryWrapper<BizDutyPost> wrapper) {
        if (!deptScopeProvider.isScoped()) {
            return wrapper;
        }
        Set<Long> allowed = new HashSet<>(deptScopeProvider.allowedDeptIds());
        wrapper.and(w -> w.eq(BizDutyPost::getOrgType, OrgUnitTypeEnum.HOSPITAL.getCode())
                .or(o -> o.in(!allowed.isEmpty(), BizDutyPost::getOrgId, allowed)
                        .in(BizDutyPost::getOrgType, OrgUnitTypeEnum.DEPT.getCode(), OrgUnitTypeEnum.WARD.getCode())));
        return wrapper;
    }

    private String unitNameOf(Integer orgType, Long orgId) {
        if (OrgUnitTypeEnum.WARD.getCode() == orgType) {
            SysWard ward = sysWardMapper.selectById(orgId);
            return ward == null ? null : ward.getWardName();
        }
        SysDepartment dept = sysDepartmentMapper.selectById(orgId);
        return dept == null ? null : dept.getDeptName();
    }

    private Set<Long> idsOf(List<BizDutyPost> rows) {
        Set<Long> ids = new HashSet<>();
        for (BizDutyPost row : rows) {
            if (row.getShiftId() != null) {
                ids.add(row.getShiftId());
            }
        }
        return ids;
    }
}
