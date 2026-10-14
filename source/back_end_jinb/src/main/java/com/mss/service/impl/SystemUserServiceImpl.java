package com.mss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mss.common.base.PageResult;
import com.mss.common.exception.BusinessException;
import com.mss.dto.ResetPasswordDTO;
import com.mss.dto.UserQueryPageDTO;
import com.mss.dto.UserStatusDTO;
import com.mss.dto.UserUpsertDTO;
import com.mss.entity.MssEmployee;
import com.mss.entity.MssEmployeePost;
import com.mss.entity.MssRole;
import com.mss.entity.MssUser;
import com.mss.mapper.MssEmployeeMapper;
import com.mss.mapper.MssEmployeePostMapper;
import com.mss.mapper.MssRoleMapper;
import com.mss.mapper.MssUserMapper;
import com.mss.service.SystemUserService;
import com.mss.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理实现
 */
@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {

    private final MssUserMapper userMapper;
    private final MssEmployeeMapper employeeMapper;
    private final MssEmployeePostMapper employeePostMapper;
    private final MssRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> page(UserQueryPageDTO dto) {
        Page<MssUser> page = userMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()),
                new LambdaQueryWrapper<MssUser>()
                        .and(dto.getKeyword() != null && !dto.getKeyword().isEmpty(), w -> w
                                .like(MssUser::getUserName, dto.getKeyword())
                                .or()
                                .like(MssUser::getRealName, dto.getKeyword()))
                        .eq(dto.getStatus() != null, MssUser::getStatus, dto.getStatus())
                        .orderByDesc(MssUser::getCreateTime));

        List<UserVO> vos = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public MssUser upsert(UserUpsertDTO dto) {
        MssUser user = new MssUser();
        BeanUtils.copyProperties(dto, user);

        if (dto.getId() == null) {
            Long exist = userMapper.selectCount(new LambdaQueryWrapper<MssUser>()
                    .eq(MssUser::getUserName, dto.getUserName()));
            if (exist != null && exist > 0) {
                throw new BusinessException("用户名已存在");
            }
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                throw new BusinessException("新增用户必须设置初始密码");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setLoginCount(0);
            user.setCreateBy(UserContextName());
            userMapper.insert(user);
        } else {
            MssUser exist = userMapper.selectById(dto.getId());
            if (exist == null) {
                throw new BusinessException("用户不存在");
            }
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
                user.setPasswordUpdateTime(LocalDateTime.now());
            } else {
                user.setPassword(null);
            }
            user.setUpdateBy(UserContextName());
            userMapper.updateById(user);
        }
        return user;
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        MssUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        MssUser update = new MssUser();
        update.setId(dto.getId());
        update.setPassword(passwordEncoder.encode(dto.getPassword()));
        update.setPasswordUpdateTime(LocalDateTime.now());
        update.setUpdateBy(UserContextName());
        userMapper.updateById(update);
    }

    @Override
    public void changeStatus(UserStatusDTO dto) {
        MssUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        MssUser update = new MssUser();
        update.setId(dto.getId());
        update.setStatus(dto.getStatus());
        update.setUpdateBy(UserContextName());
        userMapper.updateById(update);
    }

    @Override
    public void deleteById(Long id) {
        MssUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    // ------------------------------------------------------------ 内部工具

    private UserVO toVO(MssUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        MssEmployee emp = user.getEmpId() == null ? null : employeeMapper.selectById(user.getEmpId());
        if (emp != null) {
            vo.setEmpName(emp.getEmpName());
            vo.setDeptId(emp.getDeptId());
            vo.setDeptName(emp.getDeptName());
        }
        List<MssRole> roles = rolesOfEmployee(user.getEmpId());
        vo.setRoleCodes(roles.stream().map(MssRole::getRoleCode).collect(Collectors.toList()));
        vo.setRoleNames(roles.stream().map(MssRole::getRoleName).collect(Collectors.toList()));
        return vo;
    }

    private List<MssRole> rolesOfEmployee(Long employeeId) {
        if (employeeId == null) {
            return new ArrayList<>();
        }
        List<Long> roleIds = employeePostMapper.selectList(new LambdaQueryWrapper<MssEmployeePost>()
                        .eq(MssEmployeePost::getEmployeeId, employeeId))
                .stream().map(MssEmployeePost::getRoleId).distinct().collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return roleMapper.selectBatchIds(roleIds);
    }

    private String UserContextName() {
        com.mss.config.LoginUser loginUser = com.mss.config.UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
