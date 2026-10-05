package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.exception.BusinessException;
import com.his.security.entity.CurrentUser;
import com.his.security.utils.JwtUtils;
import com.his.security.PasswordCipher;
import com.his.security.UserUtils;
import com.his.system.dto.ChangePasswordDTO;
import com.his.system.dto.LoginRequestDTO;
import com.his.system.dto.SwitchPostDTO;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.AuthService;
import com.his.system.service.SysUserService;
import com.his.system.vo.LoginVO;
import com.his.system.vo.PublicKeyVO;
import com.his.system.vo.UserLoginVO;
import com.his.system.vo.UserRolesVO;
import com.his.system.service.EmployeePostService;
import com.his.system.vo.EmployeePostVO;
import com.his.system.vo.SwitchPostVO;
import com.his.system.support.SysLoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final EmployeePostService employeePostService;
    private final SysUserMapper userMapper;
    private final SysLoginLogService loginLogService;
    private final PasswordCipher passwordCipher;

    @Override
    public LoginVO login(LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        String username = loginRequestDTO == null ? null : loginRequestDTO.getUsername();

        // 口令在浏览器里就用 SM2 公钥加密了，这里还原成明文再交给 Spring Security 去比 BCrypt。
        // 「还原失败」必须落登录日志：拿明文口令直接撞接口本身就是要留痕的行为，
        // 不加这条的话，有人在扫描器里灌 123456 试探，登录日志里什么都不会留下。
        String cipherPassword = loginRequestDTO == null ? null : loginRequestDTO.getPassword();
        String password;
        try {
            password = passwordCipher.decrypt(cipherPassword);
        } catch (RuntimeException e) {
            loginLogService.record(username, request, false, "口令密文非法：" + e.getMessage());
            throw e;
        }

        CurrentUser currentUser;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            currentUser = (CurrentUser) authentication.getPrincipal();
        } catch (AuthenticationException e) {
            // 失败登录必须落一条：口令爆破在系统里留下的唯一痕迹就是这本账，
            // 只记成功登录的话，"有人拿 admin 试了两百次口令"在审计上等于没发生（等保三级 8.1.4）。
            loginLogService.record(username, request, false, "认证失败：" + e.getMessage());
            throw e;
        }

        String currentRole = determineCurrentRole(currentUser, loginRequestDTO.getRoleCode());

        // 账号存在但没角色/没岗位，同样算登录失败
        if (currentRole == null) {
            String msg = (loginRequestDTO.getRoleCode() == null || loginRequestDTO.getRoleCode().isEmpty())
                    ? "当前用户未能分配任何角色，请联系管理员分配角色"
                    : "您没有该角色权限，请使用其他角色进行登录";
            loginLogService.record(username, currentUser.getUserId(), currentUser.getRealName(), request, false, msg);
            throw new BusinessException(msg);
        }

        recordLoginHabit(currentUser, clientIp(request));

        // 登录落点也必须落在一个**岗位**上。原先只认角色、科室沿用主科室快照，
        // 于是「主科室没给这个角色配岗位」时会签出错配 token —— 菜单按医生画、数据按药房取，
        // 正是本模块要消灭的那类静默错配。
        Long landingDeptId = currentUser.getDeptId();
        String landingDeptName = currentUser.getDeptName();
        if (currentUser.getEmployeeId() != null) {
            EmployeePostVO post = employeePostService.resolvePrimaryPost(currentUser.getEmployeeId(), currentRole);
            if (post == null) {
                String msg = "该角色尚未分配任何科室岗位，请联系管理员在员工档案中配置";
                loginLogService.record(username, currentUser.getUserId(), currentUser.getRealName(), request, false, msg);
                throw new BusinessException(msg);
            }
            landingDeptId = post.getDeptId();
            landingDeptName = post.getDeptName();
        }

        // token 同时携带当前角色与当前科室：科室随会话走，见 JwtUtils#generateToken
        String token = jwtUtils.generateToken(currentUser.getUserId(), currentUser.getUsername(), currentRole,
                landingDeptId, landingDeptName);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(currentUser.getUserId());
        vo.setUsername(currentUser.getUsername());
        vo.setRealName(currentUser.getRealName());
        vo.setCurrentRole(currentRole);
        vo.setPatientId(currentUser.getPatientId());
        vo.setUserType(currentUser.getUserType());

        loginLogService.record(username, currentUser.getUserId(), currentUser.getRealName(), request, true,
                "登录成功（角色 " + currentRole + "，" + landingDeptName + "）");
        return vo;
    }

    @Override
    public UserLoginVO currentUserInfo() {
        CurrentUser currentUser = requireLogin();

        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(currentUser.getUserId());
        vo.setUsername(currentUser.getUsername());
        vo.setRealName(currentUser.getRealName());
        vo.setDeptId(currentUser.getDeptId());
        vo.setDeptName(currentUser.getDeptName());
        vo.setRoles(currentUser.getRoles());
        vo.setRoleNames(userService.selectRoleNames(currentUser.getRoles()));
        vo.setPermissions(currentUser.getPermissions());
        vo.setCurrentRole(currentUser.getCurrentRole());
        return vo;
    }

    @Override
    public UserRolesVO currentUserRoles() {
        CurrentUser currentUser = requireLogin();

        UserRolesVO vo = new UserRolesVO();
        vo.setCurrentRole(currentUser.getCurrentRole());
        vo.setRoles(currentUser.getRoles());
        return vo;
    }

    @Override
    public List<EmployeePostVO> currentUserPosts() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            return Collections.emptyList();
        }
        return employeePostService.listActivePosts(currentUser.getEmployeeId());
    }

    @Override
    public SwitchPostVO switchPost(SwitchPostDTO switchPostDTO) {
        return employeePostService.switchPost(requireLogin(), switchPostDTO.getRoleCode(), switchPostDTO.getDeptId());
    }

    @Override
    public PublicKeyVO publicKey() {
        PublicKeyVO vo = new PublicKeyVO();
        vo.setKeyId(passwordCipher.getKeyId());
        vo.setPublicKey(passwordCipher.getPublicKeyHex());
        return vo;
    }

    @Override
    public void updatePassword(ChangePasswordDTO changePasswordDTO) {
        CurrentUser currentUser = requireLogin();
        Long userId = currentUser.getUserId();

        // 改密码是「新口令第一次上网」的场合，与登录同一对 SM2 公钥加密，明文一律拒收；
        // 不收口的话，登录加密就只拦了半条链路 —— 新口令照样在网络上裸奔。
        String oldPassword = passwordCipher.decrypt(changePasswordDTO == null ? null : changePasswordDTO.getOldPassword());
        String newPassword = passwordCipher.decrypt(changePasswordDTO == null ? null : changePasswordDTO.getNewPassword());

        boolean success = userService.changePassword(userId, oldPassword, newPassword);

        if (!success) {
            throw new BusinessException("旧密码错误");
        }

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, userId)
                .set(SysUser::getPasswordUpdateTime, LocalDateTime.now());
        userMapper.update(null, updateWrapper);
    }

    @Override
    public void logout(HttpServletRequest request) {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser != null) {
            loginLogService.record(currentUser.getUsername(), currentUser.getUserId(), currentUser.getRealName(),
                    request, true, "退出登录");
        }
    }

    private void recordLoginHabit(CurrentUser currentUser, String clientIp) {
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, currentUser.getUserId())
                .set(SysUser::getLastLoginTime, LocalDateTime.now())
                .set(SysUser::getLastLoginIp, clientIp)
                .set(SysUser::getLoginCount, currentUser.getLoginCount() != null ? currentUser.getLoginCount() + 1 : 1);
        userMapper.update(updateWrapper);
    }

    private CurrentUser requireLogin() {
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "未登录");
        }
        return currentUser;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }

    /**
     * 确定当前角色
     *
     * @param currentUser 登录用户
     * @param roleCode    选择的角色编码（空字符串 = 登录页选「其他」，即不指定角色）
     * @return 当前角色编码，null 表示所选角色不在该用户角色列表内（多角色账号选错）
     */
    private String determineCurrentRole(CurrentUser currentUser, String roleCode) {
        List<String> roles = currentUser.getRoles();
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        // 单角色账号：角色选择无意义——登录页只有少数常用角色卡片，
        // 选错卡片不应把用户挡在门外，直接使用其唯一角色。
        if (roles.size() == 1) {
            return roles.get(0);
        }

        // 多角色账号：未指定角色（选「其他」）时取其角色列表中的第一个
        // （roles 已按角色维护顺序排序，见 SysUserMapper#selectRolesByEmployeeId）
        if (roleCode == null || roleCode.isEmpty()) {
            return roles.get(0);
        }

        // 多角色账号必须选自己拥有的角色
        return roles.contains(roleCode) ? roleCode : null;
    }
}
