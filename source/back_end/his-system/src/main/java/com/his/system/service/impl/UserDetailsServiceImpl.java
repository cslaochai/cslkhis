package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.UserTypeEnum;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null || user.getStatus() != EnableStatusEnum.ENABLED.getCode()) {
            throw new UsernameNotFoundException("用户不存在或者已被禁用: " + username);
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
        if (UserTypeEnum.INNER.getCode() == user.getUserType() && user.getEmpId() != null) {
            // 院内员工
            SysEmployee employee = sysEmployeeMapper.selectById(user.getEmpId());
            if (Objects.isNull(employee)) {
                throw new UsernameNotFoundException("员工数据不存在: " + username);
            }
            currentUser.setEmployeeName(employee.getEmpName());
            // 通过员工ID查询角色
            List<String> empRoles = sysUserMapper.selectRolesByEmployeeId(user.getEmpId());
            // 通过员工ID查询权限
            List<String> permissions = sysUserMapper.selectPermissionsByEmployeeId(user.getEmpId());
            // 获取员工的主科室信息
            currentUser.setDeptId(employee.getDeptId());
            currentUser.setDeptName(employee.getDeptName());
            currentUser.setRoles(empRoles);
            currentUser.setPermissions(permissions);
        } else if (UserTypeEnum.PATIENT.getCode() == user.getUserType()) {
            // 患者用户：授予患者角色，绑定患者主档ID（用于患者级数据查询）
            currentUser.setPatientId(user.getPatientId());
            currentUser.setRoles(Collections.singletonList("PATIENT"));
        } else {
            // 院外用户等：暂无角色，登录后无权限（保持原行为）
            throw new UsernameNotFoundException("暂只支持能院内用户和患者登录");
        }
        return currentUser;
    }
}
