package com.his.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.enums.UserTypeEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.support.SensitiveMaskUtils;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import com.his.system.dto.SysUserPasswordUpsertDTO;
import com.his.system.dto.SysUserQueryPageDTO;
import com.his.system.dto.SysUserUpsertDTO;
import com.his.system.entity.SysRole;
import com.his.system.entity.SysUser;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.SysRoleMapper;
import com.his.system.mapper.SysUserMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.service.EmployeePostService;
import com.his.system.service.SysUserService;
import com.his.system.vo.SysUserListVO;
import com.his.system.vo.EmployeePostVO;
import com.his.system.vo.RoleNameVO;
import com.his.system.vo.UserDetailVO;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.UserTypeEnum;
import com.his.system.support.FieldChangeRecorder;
import com.his.system.support.FieldSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    public static final String DEFAULT_PASSWORD = "123456";

    /** 对象类型：系统用户 */
    private static final String USER = "USER";

    /**
     * 系统用户参与字段级留痕的字段清单。
     *
     * <p><b>password 永远不进这张表</b>：口令密文进日志等于把口令库复制了一份到只读表里，
     * 而日志表的可见面比用户表宽（能导出、能给检查人员看）。重置密码只记动作不记值
     * （见 {@link #resetUserPassword(SysUserPasswordUpsertDTO)}），修改用户这里压根不声明 password 字段。
     *
     * <p>同理不记 openid / avatar / lastLoginTime / lastLoginIp / loginCount：
     * 登录态是系统自己刷的，记进来全是噪音。
     */
    private static final List<FieldSpec> USER_FIELDS = FieldSpec.list(
            FieldSpec.of("userName", "登录账号"),
            FieldSpec.of("realName", "姓名"),
            FieldSpec.render("userType", "用户类型", v -> UserTypeEnum.getText((Integer) v)),
            FieldSpec.render("status", "状态", v -> EnableStatusEnum.getText((Integer) v))
    );

    private final PasswordEncoder passwordEncoder;
    private final SysRoleMapper roleMapper;
    private final SysEmployeeMapper employeeMapper;
    private final RedisSequenceService redisSequenceService;
    private final EmployeePostService employeePostService;
    private final FieldChangeRecorder fieldChangeRecorder;

    @Override
    public PageResult<SysUserListVO> queryUserPage(SysUserQueryPageDTO queryDTO) {
        PageResult<SysUser> result = selectUserPage(
                queryDTO.getUserName(), queryDTO.getDeptId(), queryDTO.getStatus(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
        List<SysUserListVO> voList = result.getRecords().stream().map(user -> {
            SysUserListVO vo = new SysUserListVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPageNum(), result.getPageSize(), result.getPages(), voList);
    }

    // 字段级留痕：账号是权限的载体 —— 把某个账号的用户类型从"普通用户"改成"管理员"、
    // 或者把停用的人重新启用，本质就是一次授权变更，正是审计要点名的"重要安全事件"。

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertUser(SysUserUpsertDTO upsertDTO) {
        if (upsertDTO.getId() == null) {
            if (!addUser(upsertDTO)) {
                throw new BusinessException("新增失败");
            }
            // 建档留痕：账号是权限的载体，"什么时候多了个能登录的账号"本身就要可查。
            SysUser created = selectByUserName(upsertDTO.getUserName());
            if (created != null) {
                fieldChangeRecorder.record(USER, created.getId(), created.getUserName(),
                        created.getRealName(), null, created, USER_FIELDS);
            }
            return;
        }
        // 校验：管理员用户名不允许修改
        SysUser existingUser = this.getById(upsertDTO.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(existingUser.getUserName()) && !existingUser.getUserName().equals(upsertDTO.getUserName())) {
            throw new BusinessException("管理员用户名不允许修改");
        }
        // 校验：管理员状态不允许修改
        if ("admin".equals(existingUser.getUserName()) && !existingUser.getStatus().equals(upsertDTO.getStatus())) {
            throw new BusinessException("管理员状态不允许修改");
        }
        // 校验：患者/其他类型账号由自助注册等流程维护，不允许在本页面修改（含改状态）
        if (isPatientOrOther(existingUser)) {
            throw new BusinessException("患者/其他类型用户不允许编辑");
        }
        if (!updateUser(upsertDTO)) {
            throw new BusinessException("修改失败");
        }
        // 用户类型 / 启用状态直接决定"这人能进哪些页面、还能不能登录"，改它就是授权变更。
        // 拿落库后的真实值比对（不拿 DTO 比）：DTO 里 null 的字段 updateUser 不会写。
        SysUser after = this.getById(upsertDTO.getId());
        if (after != null) {
            fieldChangeRecorder.record(USER, after.getId(), after.getUserName(),
                    after.getRealName(), existingUser, after, USER_FIELDS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long userId) {
        // 校验：管理员不允许删除
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUserName())) {
            throw new BusinessException("管理员不允许删除");
        }
        if (isPatientOrOther(user)) {
            throw new BusinessException("患者/其他类型用户不允许删除");
        }
        if (!deleteUser(userId)) {
            throw new BusinessException("删除失败");
        }
        // 删完对象就没了，只能靠这条留痕回答"这个账号是谁删的、什么时候删的"
        fieldChangeRecorder.recordAction(USER, userId, user.getUserName(), user.getRealName(), "删除账号");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(SysUserPasswordUpsertDTO resetDTO) {
        SysUser user = this.getById(resetDTO.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (isPatientOrOther(user)) {
            throw new BusinessException("患者/其他类型用户不允许重置密码，请引导用户走自助找回");
        }
        if (!resetPassword(resetDTO.getUserId())) {
            throw new BusinessException("重置失败");
        }
        // 只记"重置过"这个动作，**不记新口令是什么** —— 口令进日志就是事故，
        // 哪怕是重置后的临时口令也不行（临时口令往往就是 DEFAULT_PASSWORD 本体）。
        fieldChangeRecorder.recordAction(USER, user.getId(), user.getUserName(), user.getRealName(), "重置密码");
    }

    /** 患者(3)/其他(4) 类型账号不归用户管理维护 */
    private static boolean isPatientOrOther(SysUser user) {
        return Integer.valueOf(3).equals(user.getUserType()) || Integer.valueOf(4).equals(user.getUserType());
    }

    @Override
    public PageResult<SysUser> selectUserPage(String userName, Long deptId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userName), SysUser::getUserName, userName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public SysUser selectByUserName(String userName) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, userName);
        // 不过滤逻辑删除的用户，确保用户名唯一
        return this.getOne(wrapper, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(SysUserUpsertDTO upsertDTO) {
        if ("admin".equals(upsertDTO.getUserName())) {
            throw new BusinessException("不允许添加admin的用户");
        }

        SysUser existing = selectByUserName(upsertDTO.getUserName());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        SysUser sysUser = BeanUtil.copyProperties(upsertDTO, SysUser.class);
        sysUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        sysUser.setStatus(upsertDTO.getStatus());

        // 院内用户：自动创建员工记录
        if (upsertDTO.getUserType() != null && upsertDTO.getUserType() == 1) {
            SysEmployee employee = new SysEmployee();
            employee.setEmpCode(redisSequenceService.generateEmployeeNo());
            employee.setEmpName(upsertDTO.getRealName());
            employee.setGender(upsertDTO.getGender());
            employee.setPhone(upsertDTO.getPhone());
            employee.setEmail(upsertDTO.getEmail());
            employee.setTitle(upsertDTO.getTitle());
            employee.setPosition(upsertDTO.getPosition());
            employee.setSpecialty(upsertDTO.getSpecialty());
            employee.setEducation(upsertDTO.getEducation());
            employee.setBirthDate(upsertDTO.getBirthDate());
            employee.setIdCard(upsertDTO.getIdCard());
            employee.setIsExpert(upsertDTO.getIsExpert());
            employee.setExpertPrice(upsertDTO.getExpertPrice());
            employee.setStatus(YesOrNoEnum.YES.getCode());
            employeeMapper.insert(employee);
            sysUser.setEmpId(employee.getId());

            // 岗位（角色 × 科室）整体替换，角色表由岗位镜像派生，主科室由主岗位回填
            employeePostService.replacePosts(employee.getId(), upsertDTO.getPosts());
        } else {
            //todo：这里还没想好
        }

        return this.save(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUserUpsertDTO upsertDTO) {
        SysUser sysUser = this.getById(upsertDTO.getId());
        if (Objects.isNull(sysUser)) {
            return false;
        }

        // 只更新指定字段
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, upsertDTO.getId())
                .set(SysUser::getRealName, upsertDTO.getRealName())
                .set(SysUser::getStatus, upsertDTO.getStatus())
                .set(SysUser::getUpdateTime, LocalDateTime.now());
        boolean success = this.update(updateWrapper);
        if (!success) {
            return false;
        }

        // 院内用户：更新员工信息
        if (sysUser.getUserType() != null && sysUser.getUserType() == 1 && sysUser.getEmpId() != null) {
            LambdaUpdateWrapper<SysEmployee> empUpdateWrapper = new LambdaUpdateWrapper<>();
            empUpdateWrapper.eq(SysEmployee::getId, sysUser.getEmpId())
                    .set(SysEmployee::getEmpName, upsertDTO.getRealName())
                    .set(SysEmployee::getGender, upsertDTO.getGender())
                    .set(SysEmployee::getPhone, upsertDTO.getPhone())
                    .set(SysEmployee::getEmail, upsertDTO.getEmail())
                    .set(SysEmployee::getTitle, upsertDTO.getTitle())
                    .set(SysEmployee::getPosition, upsertDTO.getPosition())
                    .set(SysEmployee::getSpecialty, upsertDTO.getSpecialty())
                    .set(SysEmployee::getEducation, upsertDTO.getEducation())
                    .set(SysEmployee::getBirthDate, upsertDTO.getBirthDate())
                    .set(SysEmployee::getIdCard, upsertDTO.getIdCard())
                    .set(SysEmployee::getUpdateTime, LocalDateTime.now());
            employeeMapper.update(empUpdateWrapper);

            // 岗位（角色 × 科室）整体替换：主科室随之由主岗位回填，不再接收前端传值
            employeePostService.replacePosts(sysUser.getEmpId(), upsertDTO.getPosts());
        } else {
            //todo：这里还没想好
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        return this.removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUserName())) {
            throw new BusinessException("管理员不允许重置");
        }
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null) {
            return false;
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            return null;
        }

        // 封装用户信息到VO
        UserDetailVO result = new UserDetailVO();
        result.setId(user.getId());
        result.setUserName(user.getUserName());
        result.setRealName(user.getRealName());
        result.setUserType(user.getUserType());
        result.setStatus(user.getStatus());
        result.setCreateTime(user.getCreateTime());
        result.setAvatar(user.getAvatar());
        result.setLastLoginTime(user.getLastLoginTime());
        result.setLastLoginIp(user.getLastLoginIp());
        result.setLoginCount(user.getLoginCount());
        result.setPasswordUpdateTime(user.getPasswordUpdateTime());
        result.setEmpId(user.getEmpId());
        result.setRoleCodes(new ArrayList<>());
        result.setDeptIds(new ArrayList<>());

        if (user.getUserType() != null && user.getUserType() == UserTypeEnum.INNER.getCode()) {
            // D 类保留：校验对象是库内已存的用户（非请求 DTO），必填性取决于用户类型，属关联实体存在性闸
            if (Objects.isNull(user.getEmpId())) {
                throw new BusinessException("院内用户出现员工为空");
            }
            //查询员工信息（院内用户）
            SysEmployee emp = employeeMapper.selectById(user.getEmpId());
            if (emp == null) {
                throw new BusinessException("院内用户出现员工为空");
            }
            result.setEmpNo(emp.getEmpCode());
            result.setEmpType(emp.getEmpType());
            result.setHireDate(emp.getHireDate());
            // 用户自身没配头像时回落到员工头像
            if (result.getAvatar() == null || result.getAvatar().isEmpty()) {
                result.setAvatar(emp.getAvatar());
            }
            result.setGender(emp.getGender());
            result.setTitle(emp.getTitle());
            result.setEducation(emp.getEducation());
            result.setSpecialty(emp.getSpecialty());
            result.setBirthDate(emp.getBirthDate());
            result.setPosition(emp.getPosition());
            result.setTitle(emp.getTitle());
            result.setIdCard(emp.getIdCard());
            result.setDeptId(emp.getDeptId());
            result.setDeptName(emp.getDeptName());
            result.setPhone(emp.getPhone());
            result.setEmail(emp.getEmail());
            // 岗位（角色 × 科室）：编辑表单与顶栏「用户信息」都读这一份
            List<EmployeePostVO> posts = employeePostService.listPosts(user.getEmpId());
            result.setPosts(posts);
            result.setRoleCodes(baseMapper.selectRolesByEmployeeId(user.getEmpId()));
            result.setDeptIds(posts.stream()
                    .map(item -> item.getDeptId() == null ? null : String.valueOf(item.getDeptId()))
                    .filter(Objects::nonNull).distinct().toList());
        } else {
            // 非院内用户：没有员工档案，也就没有岗位与角色
            result.setPosts(Collections.emptyList());
            result.setRoleCodes(Collections.emptyList());
            result.setDeptIds(Collections.emptyList());
        }
        return result;
    }

    /**
     * 本人档案（顶栏「用户信息」）：只读展示口径，手机号/身份证/邮箱在此处脱敏后出参。
     *
     * <p>为什么不直接在 {@link #getUserDetail(Long)} 里打码：那个接口同时是「系统管理 → 用户管理」
     * 编辑表单的回显数据源，回显打码值 → 管理员点保存就把 {@code 188****5678} 写回库里，
     * 脱敏顺带把数据写坏。故展示与编辑分接口，本方法也不接受 userId 入参（只能取自己的，防越权）。
     */
    @Override
    public UserDetailVO getSelfProfile() {
        CurrentUser current = UserUtils.getCurrentUser();
        if (current == null || current.getUserId() == null) {
            throw new BusinessException("未获取到当前登录用户");
        }
        UserDetailVO vo = getUserDetail(current.getUserId());
        if (vo == null) {
            throw new BusinessException("当前登录用户不存在");
        }
        vo.setPhone(SensitiveMaskUtils.maskPhone(vo.getPhone()));
        vo.setIdCard(SensitiveMaskUtils.maskIdCard(vo.getIdCard()));
        vo.setEmail(SensitiveMaskUtils.maskEmail(vo.getEmail()));
        return vo;
    }

    @Override
    public List<RoleNameVO> selectRoleNames(List<String> roleCodes) {        if (CollectionUtils.isEmpty(roleCodes)) {
            return Collections.emptyList();
        }
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .select(SysRole::getRoleCode, SysRole::getRoleName)
                .in(SysRole::getRoleCode, roleCodes)
                .orderByAsc(SysRole::getSortOrder));
        return roles.stream().map(role -> {
            RoleNameVO vo = new RoleNameVO();
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
            return vo;
        }).toList();
    }
}
