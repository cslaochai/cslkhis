package com.his.platform.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.EnableStatusEnum;
import com.his.spi.dto.PatientAccountCreateDTO;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.ClinicRoomBriefVO;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.DictItemVO;
import com.his.spi.vo.EmployeeBriefVO;
import com.his.spi.vo.EmployeePostBriefVO;
import com.his.spi.vo.Icd10BriefVO;
import com.his.spi.vo.RoleBriefVO;
import com.his.spi.vo.UserBriefVO;
import com.his.spi.vo.WardBriefVO;
import com.his.platform.entity.SysClinicRoom;
import com.his.platform.entity.SysConfig;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysDictData;
import com.his.platform.entity.SysEmployee;
import com.his.platform.entity.SysEmployeePost;
import com.his.platform.entity.SysIcd10;
import com.his.platform.entity.SysRole;
import com.his.platform.entity.SysUser;
import com.his.platform.entity.SysWard;
import com.his.platform.mapper.SysClinicRoomMapper;
import com.his.platform.mapper.SysConfigMapper;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeePostMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.mapper.SysRoleMapper;
import com.his.platform.mapper.SysUserMapper;
import com.his.platform.mapper.SysWardMapper;
import com.his.platform.service.DictCacheService;
import com.his.platform.service.Icd10Service;
import com.his.platform.service.SysUserService;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 系统主数据对外端口实现：主档实体的读取与摘要转换只在本类发生，实体不出模块。
 */
@Service
@RequiredArgsConstructor
public class SystemFacadeServiceImpl implements SystemFacadeService {

    private final SysDepartmentMapper sysDepartmentMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    private final SysWardMapper sysWardMapper;

    private final SysEmployeePostMapper sysEmployeePostMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysUserMapper sysUserMapper;

    private final SysUserService sysUserService;

    private final SysClinicRoomMapper sysClinicRoomMapper;

    private final SysConfigMapper sysConfigMapper;

    private final DictCacheService dictCacheService;

    private final Icd10Service icd10Service;

    @Override
    public DeptBriefVO findDepartment(Long deptId) {
        return deptId == null ? null : toDeptBrief(sysDepartmentMapper.selectById(deptId));
    }

    @Override
    public List<DeptBriefVO> listEnabledDepartments() {
        List<SysDepartment> departments = sysDepartmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getStatus, EnableStatusEnum.ENABLED.getCode())
                        .orderByAsc(SysDepartment::getSortOrder)
                        .orderByAsc(SysDepartment::getId));
        return toDeptBriefList(departments);
    }

    @Override
    public List<DeptBriefVO> listDepartmentsByIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        return toDeptBriefList(sysDepartmentMapper.selectBatchIds(deptIds));
    }

    @Override
    public List<DeptBriefVO> listDepartmentsByScheduleAuditor(Long employeeId) {
        if (employeeId == null) {
            return List.of();
        }
        List<SysDepartment> departments = sysDepartmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getScheduleAuditorId, employeeId));
        return toDeptBriefList(departments);
    }

    @Override
    public List<DeptBriefVO> listDepartmentsByOutpatientAuditor(Long employeeId) {
        if (employeeId == null) {
            return List.of();
        }
        List<SysDepartment> departments = sysDepartmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getOutpatientAuditorId, employeeId));
        return toDeptBriefList(departments);
    }

    @Override
    public EmployeeBriefVO findEmployee(Long employeeId) {
        return employeeId == null ? null : toEmployeeBrief(sysEmployeeMapper.selectById(employeeId));
    }

    @Override
    public List<EmployeeBriefVO> listEmployeesByIds(Collection<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return List.of();
        }
        return toEmployeeBriefList(sysEmployeeMapper.selectBatchIds(employeeIds));
    }

    @Override
    public List<Long> searchEmployeeIds(String keyword) {
        if (!TextUtil.hasText(keyword)) {
            return List.of();
        }
        String text = keyword.trim();
        List<SysEmployee> employees = sysEmployeeMapper.selectList(
                new LambdaQueryWrapper<SysEmployee>()
                        .and(w -> w.like(SysEmployee::getEmpName, text)
                                .or().like(SysEmployee::getEmpCode, text))
                        .select(SysEmployee::getId));
        List<Long> ids = new ArrayList<>(employees.size());
        for (SysEmployee employee : employees) {
            if (employee.getId() != null) {
                ids.add(employee.getId());
            }
        }
        return ids;
    }

    @Override
    public String getEmployeeSignImage(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        SysEmployee employee = sysEmployeeMapper.selectById(employeeId);
        return employee == null ? null : employee.getSignImage();
    }

    @Override
    public WardBriefVO findWard(Long wardId) {
        return wardId == null ? null : toWardBrief(sysWardMapper.selectById(wardId));
    }

    @Override
    public List<WardBriefVO> listEnabledWards() {
        List<SysWard> wards = sysWardMapper.selectList(new LambdaQueryWrapper<SysWard>()
                .eq(SysWard::getStatus, EnableStatusEnum.ENABLED.getCode())
                .orderByAsc(SysWard::getWardCode)
                .orderByAsc(SysWard::getWardId));
        List<WardBriefVO> result = new ArrayList<>(wards.size());
        for (SysWard ward : wards) {
            result.add(toWardBrief(ward));
        }
        return result;
    }

    @Override
    public EmployeePostBriefVO findEmployeePost(Long postId) {
        return postId == null ? null : toPostBrief(sysEmployeePostMapper.selectById(postId));
    }

    @Override
    public List<EmployeePostBriefVO> listEmployeePosts(Long employeeId) {
        if (employeeId == null) {
            return List.of();
        }
        List<SysEmployeePost> posts = sysEmployeePostMapper.selectList(
                new LambdaQueryWrapper<SysEmployeePost>()
                        .eq(SysEmployeePost::getEmployeeId, employeeId)
                        .orderByAsc(SysEmployeePost::getId));
        List<EmployeePostBriefVO> result = new ArrayList<>(posts.size());
        for (SysEmployeePost post : posts) {
            result.add(toPostBrief(post));
        }
        return result;
    }

    @Override
    public RoleBriefVO findRole(Long roleId) {
        if (roleId == null) {
            return null;
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            return null;
        }
        RoleBriefVO vo = new RoleBriefVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setStaffType(role.getStaffType());
        return vo;
    }

    @Override
    public List<EmployeeBriefVO> listActiveEmployeesByDept(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        return toEmployeeBriefList(sysEmployeeMapper.selectList(
                new LambdaQueryWrapper<SysEmployee>()
                        .eq(SysEmployee::getDeptId, deptId)
                        .eq(SysEmployee::getStatus, EnableStatusEnum.ENABLED.getCode())));
    }

    @Override
    public List<EmployeeBriefVO> listActiveEmployeesByTitles(Long deptId, Collection<String> titles) {
        if (deptId == null || titles == null || titles.isEmpty()) {
            return List.of();
        }
        return toEmployeeBriefList(sysEmployeeMapper.selectList(
                new LambdaQueryWrapper<SysEmployee>()
                        .eq(SysEmployee::getDeptId, deptId)
                        .in(SysEmployee::getTitle, titles)
                        .eq(SysEmployee::getStatus, EnableStatusEnum.ENABLED.getCode())));
    }

    @Override
    public List<EmployeeBriefVO> listEmployeesByName(String empName) {
        if (!TextUtil.hasText(empName)) {
            return List.of();
        }
        return toEmployeeBriefList(sysEmployeeMapper.selectList(
                new LambdaQueryWrapper<SysEmployee>().eq(SysEmployee::getEmpName, empName.trim())));
    }

    @Override
    public boolean accountNameExists(String userName) {
        if (!TextUtil.hasText(userName)) {
            return false;
        }
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName.trim())) > 0;
    }

    @Override
    public UserBriefVO findUserByName(String userName) {
        if (!TextUtil.hasText(userName)) {
            return null;
        }
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, userName.trim()).last("LIMIT 1"));
        if (user == null) {
            return null;
        }
        UserBriefVO vo = new UserBriefVO();
        vo.setId(user.getId());
        vo.setUserName(user.getUserName());
        vo.setRealName(user.getRealName());
        vo.setEmpId(user.getEmpId());
        return vo;
    }

    @Override
    public Long createPatientAccount(PatientAccountCreateDTO dto) {
        return sysUserService.createPatientAccount(dto);
    }

    @Override
    public void bindOpenid(Long userId, String openid) {
        sysUserService.bindOpenid(userId, openid);
    }

    @Override
    public ClinicRoomBriefVO findClinicRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        SysClinicRoom room = sysClinicRoomMapper.selectById(roomId);
        if (room == null) {
            return null;
        }
        ClinicRoomBriefVO vo = new ClinicRoomBriefVO();
        vo.setId(room.getId());
        vo.setName(room.getName());
        vo.setQueuePrefix(room.getQueuePrefix());
        return vo;
    }

    @Override
    public String findConfigValue(String configKey) {
        if (!TextUtil.hasText(configKey)) {
            return null;
        }
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey.trim()).last("LIMIT 1"));
        return config == null ? null : TextUtil.trimToNull(config.getConfigValue());
    }

    @Override
    public List<DictItemVO> listDictItems(String dictType) {
        List<SysDictData> dictList = dictCacheService.getDictDataByTypeRaw(dictType);
        if (dictList == null || dictList.isEmpty()) {
            return List.of();
        }
        List<DictItemVO> result = new ArrayList<>(dictList.size());
        for (SysDictData item : dictList) {
            DictItemVO vo = new DictItemVO();
            vo.setDictValue(item.getDictValue());
            vo.setDictLabel(item.getDictLabel());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Icd10BriefVO> searchIcd10(String keyword, int limit) {
        return toIcd10BriefList(icd10Service.search(keyword, limit));
    }

    @Override
    public List<Icd10BriefVO> listActiveIcd10() {
        return toIcd10BriefList(icd10Service.activeCodes());
    }

    @Override
    public void refreshIcd10Cache() {
        icd10Service.refreshCache();
    }

    private List<DeptBriefVO> toDeptBriefList(List<SysDepartment> departments) {
        List<DeptBriefVO> result = new ArrayList<>(departments.size());
        for (SysDepartment department : departments) {
            result.add(toDeptBrief(department));
        }
        return result;
    }

    private List<EmployeeBriefVO> toEmployeeBriefList(List<SysEmployee> employees) {
        List<EmployeeBriefVO> result = new ArrayList<>(employees.size());
        for (SysEmployee employee : employees) {
            result.add(toEmployeeBrief(employee));
        }
        return result;
    }

    private DeptBriefVO toDeptBrief(SysDepartment department) {
        if (department == null) {
            return null;
        }
        DeptBriefVO vo = new DeptBriefVO();
        vo.setId(department.getId());
        vo.setDeptCode(department.getDeptCode());
        vo.setDeptName(department.getDeptName());
        vo.setDeptType(department.getDeptType());
        vo.setStatus(department.getStatus());
        vo.setOutpatientAuditorId(department.getOutpatientAuditorId());
        vo.setNursingAuditorId(department.getNursingAuditorId());
        vo.setScheduleAuditorId(department.getScheduleAuditorId());
        return vo;
    }

    private EmployeeBriefVO toEmployeeBrief(SysEmployee employee) {
        if (employee == null) {
            return null;
        }
        EmployeeBriefVO vo = new EmployeeBriefVO();
        vo.setId(employee.getId());
        vo.setEmpCode(employee.getEmpCode());
        vo.setEmpName(employee.getEmpName());
        vo.setEmpType(employee.getEmpType());
        vo.setDeptId(employee.getDeptId());
        vo.setDeptName(employee.getDeptName());
        vo.setPhone(employee.getPhone());
        vo.setTitle(employee.getTitle());
        vo.setStatus(employee.getStatus());
        vo.setIsFixed(employee.getIsFixed());
        vo.setIsRegistered(employee.getIsRegistered());
        return vo;
    }

    private WardBriefVO toWardBrief(SysWard ward) {
        if (ward == null) {
            return null;
        }
        WardBriefVO vo = new WardBriefVO();
        vo.setWardId(ward.getWardId());
        vo.setWardCode(ward.getWardCode());
        vo.setWardName(ward.getWardName());
        vo.setDeptId(ward.getDeptId());
        vo.setStatus(ward.getStatus());
        return vo;
    }

    private EmployeePostBriefVO toPostBrief(SysEmployeePost post) {
        if (post == null) {
            return null;
        }
        EmployeePostBriefVO vo = new EmployeePostBriefVO();
        vo.setId(post.getId());
        vo.setEmployeeId(post.getEmployeeId());
        vo.setRoleId(post.getRoleId());
        vo.setDeptId(post.getDeptId());
        vo.setIsPrimary(post.getIsPrimary());
        return vo;
    }

    private List<Icd10BriefVO> toIcd10BriefList(List<SysIcd10> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        List<Icd10BriefVO> result = new ArrayList<>(codes.size());
        for (SysIcd10 code : codes) {
            Icd10BriefVO vo = new Icd10BriefVO();
            vo.setIcdCode(code.getIcdCode());
            vo.setIcdName(code.getIcdName());
            vo.setIcdCategory(code.getIcdCategory());
            vo.setSortOrder(code.getSortOrder());
            result.add(vo);
        }
        return result;
    }
}
