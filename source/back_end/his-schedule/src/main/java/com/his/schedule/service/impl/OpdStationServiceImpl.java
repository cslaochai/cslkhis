package com.his.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.OpdStationKindEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.outpatient.dto.OpdAssignPageQueryDTO;
import com.his.outpatient.dto.OpdAssignUpsertDTO;
import com.his.schedule.dto.OpdStationPageQueryDTO;
import com.his.schedule.dto.OpdStationUpsertDTO;
import com.his.schedule.entity.BizOpdStation;
import com.his.schedule.entity.BizOpdStationAssignment;
import com.his.outpatient.entity.BizShift;
import com.his.outpatient.entity.SysDepartment;
import com.his.outpatient.entity.SysEmployee;
import com.his.outpatient.entity.SysWard;
import com.his.schedule.mapper.BizOpdStationAssignmentMapper;
import com.his.schedule.mapper.BizOpdStationMapper;
import com.his.outpatient.mapper.SysDepartmentMapper;
import com.his.outpatient.mapper.SysEmployeeMapper;
import com.his.outpatient.mapper.SysWardMapper;
import com.his.schedule.service.OpdStationService;
import com.his.outpatient.service.ShiftService;
import com.his.outpatient.vo.OpdAssignCandidateVO;
import com.his.outpatient.vo.OpdAssignVO;
import com.his.schedule.vo.OpdStationSelectVO;
import com.his.schedule.vo.OpdStationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 门诊公共工位字典 + 工位派岗服务实现（sql/277）。
 */
@Service
@RequiredArgsConstructor
public class OpdStationServiceImpl extends ServiceImpl<BizOpdStationMapper, BizOpdStation>
        implements OpdStationService {

    private final BizOpdStationAssignmentMapper assignmentMapper;
    private final ShiftService shiftService;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final SysWardMapper sysWardMapper;

    // ---------------------------------------------------------------------
    // 工位字典
    // ---------------------------------------------------------------------

    @Override
    public List<OpdStationSelectVO> selectListVO(Integer stationKind) {
        List<BizOpdStation> rows = list(new LambdaQueryWrapper<BizOpdStation>()
                .eq(BizOpdStation::getStatus, EnableStatusEnum.ENABLED.getCode())
                .eq(stationKind != null, BizOpdStation::getStationKind, stationKind)
                .orderByAsc(BizOpdStation::getSortNo).orderByAsc(BizOpdStation::getId));
        List<OpdStationSelectVO> vos = new ArrayList<>(rows.size());
        for (BizOpdStation row : rows) {
            OpdStationSelectVO vo = new OpdStationSelectVO();
            vo.setId(row.getId());
            vo.setStationCode(row.getStationCode());
            vo.setStationName(row.getStationName());
            vo.setStationKind(row.getStationKind());
            vo.setStationKindText(OpdStationKindEnum.getLabel(row.getStationKind()));
            vo.setCapacity(row.getCapacity());
            vo.setPhone(row.getPhone());
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public PageResult<OpdStationVO> pageStation(OpdStationPageQueryDTO dto) {
        LambdaQueryWrapper<BizOpdStation> wrapper = new LambdaQueryWrapper<BizOpdStation>()
                .eq(dto.getStationKind() != null, BizOpdStation::getStationKind, dto.getStationKind())
                .eq(dto.getStatus() != null, BizOpdStation::getStatus, dto.getStatus());
        if (TextUtil.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            wrapper.and(w -> w.like(BizOpdStation::getStationName, kw)
                    .or().like(BizOpdStation::getStationCode, kw));
        }
        wrapper.orderByAsc(BizOpdStation::getSortNo).orderByAsc(BizOpdStation::getId);
        Page<BizOpdStation> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<OpdStationVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizOpdStation row : page.getRecords()) {
            OpdStationVO vo = new OpdStationVO();
            vo.setId(row.getId());
            vo.setStationCode(row.getStationCode());
            vo.setStationName(row.getStationName());
            vo.setStationKind(row.getStationKind());
            vo.setStationKindText(OpdStationKindEnum.getLabel(row.getStationKind()));
            vo.setRequiredStaffType(row.getRequiredStaffType());
            vo.setRequiredStaffTypeName(StaffTypeEnum.getLabel(row.getRequiredStaffType()));
            vo.setCapacity(row.getCapacity());
            vo.setPhone(row.getPhone());
            vo.setSortNo(row.getSortNo());
            vo.setStatus(row.getStatus());
            vo.setRemark(row.getRemark());
            vos.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public Long upsertStation(OpdStationUpsertDTO dto) {
        if (OpdStationKindEnum.fromCode(dto.getStationKind()) == null) {
            throw new BusinessException("工位类别取值不合法");
        }
        if (dto.getRequiredStaffType() != null && StaffTypeEnum.fromCode(dto.getRequiredStaffType()) == null) {
            throw new BusinessException("应到岗位类别取值不合法");
        }
        BizOpdStation station = new BizOpdStation();
        station.setId(dto.getId());
        station.setStationCode(dto.getStationCode().trim());
        station.setStationName(dto.getStationName().trim());
        station.setStationKind(dto.getStationKind());
        station.setRequiredStaffType(dto.getRequiredStaffType() == null ? StaffTypeEnum.NURSE.getCode() : dto.getRequiredStaffType());
        station.setCapacity(dto.getCapacity() == null || dto.getCapacity() < 1 ? 1 : dto.getCapacity());
        station.setPhone(dto.getPhone());
        station.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
        station.setStatus(dto.getStatus() == null ? EnableStatusEnum.ENABLED.getCode() : dto.getStatus());
        station.setRemark(dto.getRemark());
        // 编码全局唯一（排除自身）
        Long dupCount = count(new LambdaQueryWrapper<BizOpdStation>()
                .eq(BizOpdStation::getStationCode, station.getStationCode())
                .ne(station.getId() != null, BizOpdStation::getId, station.getId()));
        if (dupCount > 0) {
            throw new BusinessException("工位编码「" + station.getStationCode() + "」已存在");
        }
        saveOrUpdate(station);
        return station.getId();
    }

    @Override
    public void deleteStation(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("工位不存在或已删除");
        }
        // 已被派岗占用的工位不允许删位留空班
        Long used = assignmentMapper.selectCount(new LambdaQueryWrapper<BizOpdStationAssignment>()
                .eq(BizOpdStationAssignment::getStationId, id)
                .eq(BizOpdStationAssignment::getStatus, EnableStatusEnum.ENABLED.getCode()));
        if (used != null && used > 0) {
            throw new BusinessException("该工位已有在岗派岗记录，请先撤销派岗再删除");
        }
        baseMapper.purgeById(id);
    }

    // ---------------------------------------------------------------------
    // 工位派岗
    // ---------------------------------------------------------------------

    @Override
    public PageResult<OpdAssignVO> pageAssign(OpdAssignPageQueryDTO dto) {
        LambdaQueryWrapper<BizOpdStationAssignment> wrapper = new LambdaQueryWrapper<BizOpdStationAssignment>()
                .ge(dto.getStartDate() != null, BizOpdStationAssignment::getAssignDate, dto.getStartDate())
                .le(dto.getEndDate() != null, BizOpdStationAssignment::getAssignDate, dto.getEndDate())
                .eq(dto.getStationId() != null, BizOpdStationAssignment::getStationId, dto.getStationId())
                .eq(dto.getStationKind() != null, BizOpdStationAssignment::getStationKind, dto.getStationKind())
                .eq(dto.getIsLoan() != null, BizOpdStationAssignment::getIsLoan, dto.getIsLoan())
                .eq(BizOpdStationAssignment::getStatus, EnableStatusEnum.ENABLED.getCode());
        if (TextUtil.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            wrapper.and(w -> w.like(BizOpdStationAssignment::getEmployeeName, kw)
                    .or().like(BizOpdStationAssignment::getEmpCode, kw));
        }
        wrapper.orderByDesc(BizOpdStationAssignment::getAssignDate)
                .orderByAsc(BizOpdStationAssignment::getStationId)
                .orderByAsc(BizOpdStationAssignment::getId);
        Page<BizOpdStationAssignment> page = assignmentMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        Map<Long, BizShift> shifts = shiftService.mapByIds(shiftIdsOfAssign(page.getRecords()));
        List<OpdAssignVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizOpdStationAssignment row : page.getRecords()) {
            vos.add(toAssignVO(row, shifts.get(row.getShiftId())));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public List<OpdAssignCandidateVO> listAssignCandidates(Long stationId, LocalDate assignDate) {
        BizOpdStation station = requireStation(stationId);
        List<OpdAssignCandidateVO> rows = assignmentMapper.selectOnDutyCandidates(
                assignDate, station.getRequiredStaffType(), null);
        // 同一个人当天在两个单元各有一条上班行时只留一条候选：下拉里一个名字不能出现两次
        Map<Long, OpdAssignCandidateVO> byEmployee = new LinkedHashMap<>();
        for (OpdAssignCandidateVO row : rows) {
            byEmployee.putIfAbsent(row.getEmployeeId(), row);
        }
        return new ArrayList<>(byEmployee.values());
    }

    @Override
    public Long upsertAssign(OpdAssignUpsertDTO dto) {
        BizOpdStation station = requireStation(dto.getStationId());
        SysEmployee employee = sysEmployeeMapper.selectById(dto.getEmployeeId());
        if (employee == null || employee.getDelFlag() != null && employee.getDelFlag() == 1) {
            throw new BusinessException("所选员工不存在或已删除");
        }
        // 在岗闸 + 岗位闸：人先被排班层派出上班，工位只是当天把她挪到门诊公共位上。
        // 绕过这条链就会落出「当天根本没上班却站在分诊台」的行，工时与缺勤对不上。
        Integer requiredStaffType = station.getRequiredStaffType();
        List<OpdAssignCandidateVO> matched = assignmentMapper.selectOnDutyCandidates(
                dto.getAssignDate(), requiredStaffType, employee.getId());
        if (matched.isEmpty()) {
            List<OpdAssignCandidateVO> onDutyAnyPost = assignmentMapper.selectOnDutyCandidates(
                    dto.getAssignDate(), null, employee.getId());
            if (onDutyAnyPost.isEmpty()) {
                throw new BusinessException("「" + employee.getEmpName() + "」在 " + dto.getAssignDate()
                        + " 没有已发布的上班排班，不能派岗：请先在护理排班把这天排好并提交审核发布");
            }
            throw new BusinessException("「" + employee.getEmpName() + "」当天排的是"
                    + StaffTypeEnum.getLabel(onDutyAnyPost.get(0).getStaffType()) + "，「"
                    + station.getStationName() + "」的应到岗位类别是"
                    + StaffTypeEnum.getLabel(requiredStaffType) + "：请换人或到工位字典调整应到岗位类别");
        }
        // 班次只取派岗入参：工位是位置、不带班次，时段由本次派岗指定（可空）
        Long shiftId = dto.getShiftId();
        BizShift shift = shiftId == null ? null : shiftService.getById(shiftId);
        if (shiftId != null && (shift == null || isDisabled(shift.getStatus()))) {
            throw new BusinessException("所选班次不存在或已停用，请重新选择");
        }

        BizOpdStationAssignment row = new BizOpdStationAssignment();
        row.setId(dto.getId());
        row.setAssignDate(dto.getAssignDate());
        row.setStationId(station.getId());
        row.setStationName(station.getStationName());
        row.setStationKind(station.getStationKind());
        row.setEmployeeId(employee.getId());
        row.setEmployeeName(employee.getEmpName());
        row.setEmpCode(employee.getEmpCode());
        // 岗位类别取当天排班事实，不接收前端手填（与出勤底座同一个口径，两处写两副面孔必漂移）
        row.setStaffType(matched.get(0).getStaffType());
        row.setShiftId(shiftId);
        row.setStartTime(shift == null ? null : shift.getStartTime());
        row.setEndTime(shift == null ? null : shift.getEndTime());
        row.setAssignMode(1);
        row.setStatus(EnableStatusEnum.ENABLED.getCode());
        row.setRemark(dto.getRemark());

        // 借调：is_loan=1 必须给出借出方单元，并补名称快照；非借调清空来源
        boolean loan = dto.getIsLoan() != null && dto.getIsLoan() == 1;
        row.setIsLoan(loan ? 1 : 0);
        if (loan) {
            if (dto.getSourceOrgType() == null) {
                throw new BusinessException("借调请选择借出方单元类型（科室/病区）");
            }
            boolean hasName = TextUtil.hasText(dto.getSourceOrgName());
            if (!hasName && dto.getSourceOrgId() == null) {
                throw new BusinessException("借调请填写或选择借出方单元");
            }
            row.setSourceOrgType(dto.getSourceOrgType());
            row.setSourceOrgId(dto.getSourceOrgId());
            row.setSourceOrgName(hasName ? dto.getSourceOrgName().trim()
                    : sourceUnitName(dto.getSourceOrgType(), dto.getSourceOrgId()));
        } else {
            row.setSourceOrgType(null);
            row.setSourceOrgId(null);
            row.setSourceOrgName(null);
        }

        // 派岗只新增不修改：前端无编辑入口，误传 id 一律拦下，撤销后重新派
        if (dto.getId() != null) {
            throw new BusinessException("派岗记录不支持修改，请先撤销原派岗再重新安排");
        }
        // 查重：同一员工同一天只占一个门诊公共位（日期+工位+员工）。
        // 不在应用层比较班次：shiftId 可能为 NULL，而 DB 唯一键里 NULL 互不相等，按班次判会放过重复落库。
        Long dup = assignmentMapper.selectCount(new LambdaQueryWrapper<BizOpdStationAssignment>()
                .eq(BizOpdStationAssignment::getAssignDate, row.getAssignDate())
                .eq(BizOpdStationAssignment::getStationId, row.getStationId())
                .eq(BizOpdStationAssignment::getEmployeeId, row.getEmployeeId()));
        if (dup != null && dup > 0) {
            throw new BusinessException("该员工当天已在这个工位排班，请勿重复派岗");
        }
        // 容量闸：一个位同一天的在岗人数不能超过字典里的「建议同时在岗人数」
        Integer capacity = station.getCapacity();
        if (capacity != null && capacity > 0) {
            Long occupied = assignmentMapper.selectCount(new LambdaQueryWrapper<BizOpdStationAssignment>()
                    .eq(BizOpdStationAssignment::getAssignDate, row.getAssignDate())
                    .eq(BizOpdStationAssignment::getStationId, row.getStationId())
                    .eq(BizOpdStationAssignment::getStatus, EnableStatusEnum.ENABLED.getCode()));
            if (occupied != null && occupied >= capacity) {
                throw new BusinessException("「" + station.getStationName() + "」当天已派 " + occupied
                        + " 人，已到建议同时在岗人数 " + capacity + "：请先撤销一条或换个工位");
            }
        }
        assignmentMapper.insert(row);
        return row.getId();
    }

    @Override
    public void deleteAssign(Long id) {
        BizOpdStationAssignment row = assignmentMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("派岗记录不存在或已撤销");
        }
        assignmentMapper.purgeById(id);
    }

    // ---------------------------------------------------------------------
    // 内部
    // ---------------------------------------------------------------------

    private BizOpdStation requireStation(Long stationId) {
        BizOpdStation station = getById(stationId);
        if (station == null || isDisabled(station.getStatus())) {
            throw new BusinessException("所选工位不存在或已停用，请重新选择");
        }
        return station;
    }

    private boolean isDisabled(Integer status) {
        return status != null && status == EnableStatusEnum.DISABLED.getCode();
    }

    private OpdAssignVO toAssignVO(BizOpdStationAssignment row, BizShift shift) {
        OpdAssignVO vo = new OpdAssignVO();
        vo.setId(row.getId());
        vo.setAssignDate(row.getAssignDate());
        vo.setStationId(row.getStationId());
        vo.setStationName(row.getStationName());
        vo.setStationKind(row.getStationKind());
        vo.setStationKindText(OpdStationKindEnum.getLabel(row.getStationKind()));
        vo.setEmployeeId(row.getEmployeeId());
        vo.setEmployeeName(row.getEmployeeName());
        vo.setEmpCode(row.getEmpCode());
        vo.setStaffType(row.getStaffType());
        vo.setStaffTypeName(StaffTypeEnum.getLabel(row.getStaffType()));
        vo.setShiftId(row.getShiftId());
        vo.setShiftName(shift == null ? null : shift.getShiftName());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        vo.setIsLoan(row.getIsLoan());
        vo.setIsLoanText(row.getIsLoan() != null && row.getIsLoan() == 1 ? "借调" : "本岗");
        vo.setSourceOrgType(row.getSourceOrgType());
        vo.setSourceOrgId(row.getSourceOrgId());
        vo.setSourceOrgName(row.getSourceOrgName());
        vo.setAssignMode(row.getAssignMode());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        return vo;
    }

    private String sourceUnitName(Integer orgType, Long orgId) {
        if (orgType == null || orgId == null) {
            return null;
        }
        if (orgType == 2) {
            SysWard ward = sysWardMapper.selectById(orgId);
            return ward == null ? null : ward.getWardName();
        }
        SysDepartment dept = sysDepartmentMapper.selectById(orgId);
        return dept == null ? null : dept.getDeptName();
    }

    private Set<Long> shiftIdsOfAssign(List<BizOpdStationAssignment> rows) {
        Set<Long> ids = new HashSet<>();
        for (BizOpdStationAssignment r : rows) {
            if (r.getShiftId() != null) {
                ids.add(r.getShiftId());
            }
        }
        return ids;
    }
}
