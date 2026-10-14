package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mis.common.exception.BusinessException;
import com.mis.config.JwtUtils;
import com.mis.config.LoginUser;
import com.mis.config.UserContext;
import com.mis.dto.ChangePasswordDTO;
import com.mis.dto.LoginRequestDTO;
import com.mis.entity.*;
import com.mis.mapper.*;
import com.mis.service.AuthService;
import com.mis.vo.LoginVO;
import com.mis.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 认证实现（轻量版：BCrypt + JWT，无 Spring Security）
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MssUserMapper userMapper;
    private final MssEmployeeMapper employeeMapper;
    private final MssEmployeePostMapper employeePostMapper;
    private final MssRoleMapper roleMapper;
    private final MssDepartmentMapper departmentMapper;
    private final MssMenuMapper menuMapper;
    private final MssRoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginRequestDTO dto, HttpServletRequest request) {
        MssUser user = userMapper.selectOne(new LambdaQueryWrapper<MssUser>()
                .eq(MssUser::getUserName, dto.getUsername())
                .last("LIMIT 1"));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        if (user.getStatus() != null && user.getStatus() == 2) {
            throw new BusinessException("账号已锁定，请联系管理员");
        }

        // 岗位 = 角色 × 科室：取该员工主岗位（is_primary=1），无主岗位则取第一条
        MssEmployeePost post = resolvePrimaryPost(user.getEmpId());
        if (post == null) {
            throw new BusinessException("账号未配置岗位（角色×科室），请联系管理员在岗位管理中配置");
        }
        MssRole role = roleMapper.selectById(post.getRoleId());
        if (role == null || (role.getStatus() != null && role.getStatus() == 0)) {
            throw new BusinessException("岗位角色不可用，请联系管理员");
        }
        MssDepartment dept = departmentMapper.selectById(post.getDeptId());

        String token = jwtUtils.generateToken(user.getId(), user.getUserName(), role.getRoleCode(),
                dept == null ? null : dept.getId(), dept == null ? null : dept.getDeptName());

        // 记录登录习惯
        MssUser update = new MssUser();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        update.setLastLoginIp(clientIp(request));
        update.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        userMapper.updateById(update);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUserName());
        vo.setRealName(user.getRealName());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDeptId(dept == null ? null : dept.getId());
        vo.setDeptName(dept == null ? null : dept.getDeptName());
        return vo;
    }

    @Override
    public UserInfoVO info() {
        LoginUser loginUser = requireLogin();
        MssUser user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(401, "登录已失效");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUserName());
        vo.setRealName(user.getRealName());
        vo.setEmpId(user.getEmpId());
        vo.setAvatar(user.getAvatar());
        vo.setRoleCode(loginUser.getRoleCode());
        vo.setDeptId(loginUser.getDeptId());
        vo.setDeptName(loginUser.getDeptName());

        // 员工姓名
        MssEmployee emp = user.getEmpId() == null ? null : employeeMapper.selectById(user.getEmpId());
        if (emp != null) {
            vo.setEmpName(emp.getEmpName());
            if (vo.getDeptName() == null) {
                vo.setDeptName(emp.getDeptName());
            }
        }

        // 全部角色（按员工岗位）
        List<MssRole> roles = rolesOfEmployee(user.getEmpId());
        vo.setRoleCodes(roles.stream().map(MssRole::getRoleCode).collect(Collectors.toList()));
        vo.setRoleNames(roles.stream().map(MssRole::getRoleName).collect(Collectors.toList()));
        String roleName = roles.stream()
                .filter(r -> Objects.equals(r.getRoleCode(), loginUser.getRoleCode()))
                .map(MssRole::getRoleName)
                .findFirst().orElse(null);
        vo.setRoleName(roleName);

        // 当前角色权限码（菜单 permission 集合）
        vo.setPermissions(permissionsOfRole(loginUser.getRoleCode()));
        return vo;
    }

    @Override
    public List<MssMenu> menus() {
        LoginUser loginUser = requireLogin();
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<MssRoleMenu>()
                        .eq(MssRoleMenu::getRoleId, roleIdOfCode(loginUser.getRoleCode())))
                .stream().map(MssRoleMenu::getMenuId).collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<MssMenu> menus = menuMapper.selectBatchIds(menuIds).stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() <= 2)
                .filter(m -> m.getStatus() == null || m.getStatus() == 1)
                .filter(m -> m.getIsVisible() == null || m.getIsVisible() == 1)
                .sorted(Comparator.comparing(m -> m.getSortOrder() == null ? 0 : m.getSortOrder()))
                .collect(Collectors.toList());
        return buildTree(menus);
    }

    @Override
    public void logout() {
        // 无状态 token 方案：前端清 token 即登出，服务端无事可做（后续可接黑名单）
        UserContext.clear();
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        LoginUser loginUser = requireLogin();
        MssUser user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(401, "登录已失效");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        MssUser update = new MssUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        update.setPasswordUpdateTime(LocalDateTime.now());
        userMapper.updateById(update);
    }

    // ------------------------------------------------------------ 内部工具

    private LoginUser requireLogin() {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException(401, "未登录");
        }
        return loginUser;
    }

    /**
     * 取员工主岗位（is_primary=1），无主岗位则取第一条（按 id 升序）
     */
    private MssEmployeePost resolvePrimaryPost(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        List<MssEmployeePost> posts = employeePostMapper.selectList(new LambdaQueryWrapper<MssEmployeePost>()
                .eq(MssEmployeePost::getEmployeeId, employeeId)
                .orderByAsc(MssEmployeePost::getId));
        return posts.stream()
                .filter(p -> Objects.equals(p.getIsPrimary(), 1))
                .findFirst()
                .orElse(posts.isEmpty() ? null : posts.get(0));
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

    private Long roleIdOfCode(String roleCode) {
        MssRole role = roleMapper.selectOne(new LambdaQueryWrapper<MssRole>()
                .eq(MssRole::getRoleCode, roleCode).last("LIMIT 1"));
        return role == null ? null : role.getId();
    }

    private List<String> permissionsOfRole(String roleCode) {
        Long roleId = roleIdOfCode(roleCode);
        if (roleId == null) {
            return new ArrayList<>();
        }
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<MssRoleMenu>()
                        .eq(MssRoleMenu::getRoleId, roleId))
                .stream().map(MssRoleMenu::getMenuId).collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        return menuMapper.selectBatchIds(menuIds).stream()
                .map(MssMenu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<MssMenu> buildTree(List<MssMenu> menus) {
        List<MssMenu> tree = new ArrayList<>();
        for (MssMenu m : menus) {
            if (m.getParentId() == null || m.getParentId() == 0) {
                tree.add(m);
            }
        }
        for (MssMenu root : tree) {
            fillChildren(root, menus);
        }
        return tree;
    }

    private void fillChildren(MssMenu parent, List<MssMenu> menus) {
        List<MssMenu> children = menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parent.getId()))
                .sorted(Comparator.comparing(m -> m.getSortOrder() == null ? 0 : m.getSortOrder()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (MssMenu child : children) {
            fillChildren(child, menus);
        }
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }
}
