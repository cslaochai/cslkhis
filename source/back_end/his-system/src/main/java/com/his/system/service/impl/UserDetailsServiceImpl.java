package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.security.entity.CurrentUser;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysEmployeeMapper employeeMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已禁用: " + username);
        }
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(user.getId());
        currentUser.setUsername(user.getUserName());
        currentUser.setPassword(user.getPassword());
        currentUser.setRealName(user.getRealName());
        currentUser.setUserType(user.getUserType());
        currentUser.setPatientId(user.getPatientId());
        currentUser.setEmployeeId(user.getEmpId());
        currentUser.setLoginCount(user.getLoginCount());
        // 根据用户类型处理
        if (user.getUserType() != null && user.getUserType() == 1 && user.getEmpId() != null) {
            // 院内用户：通过employee_id关联员工表
            SysEmployee employee = employeeMapper.selectById(user.getEmpId());
            if (Objects.isNull(employee)) {
                throw new UsernameNotFoundException("员工数据不存在: " + username);
            }
            currentUser.setEmployeeName(employee.getEmpName());
            // 通过员工ID查询角色
            List<String> empRoles = userMapper.selectRolesByEmployeeId(user.getEmpId());
            // 通过员工ID查询权限
            List<String> permissions = userMapper.selectPermissionsByEmployeeId(user.getEmpId());
            // 获取员工的主科室信息
            currentUser.setDeptId(employee.getDeptId());
            currentUser.setDeptName(employee.getDeptName());
            currentUser.setRoles(empRoles);
            currentUser.setPermissions(permissions);
        } else if (user.getUserType() != null && user.getUserType() == 3 && user.getPatientId() != null) {
            // 患者用户：授予患者角色，绑定患者主档ID（用于患者级数据查询）
            currentUser.setPatientId(user.getPatientId());
            currentUser.setRoles(java.util.Collections.singletonList("PATIENT"));
        } else {
            // 院外用户等：暂无角色，登录后无权限（保持原行为）
        }
        return currentUser;
    }
}
