package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.EmployeePostStatusEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.platform.dto.EmployeePostDTO;
import com.his.platform.entity.*;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.mapper.SysEmployeePostMapper;
import com.his.platform.mapper.SysRoleMapper;
import com.his.platform.service.EmployeePostService;
import com.his.platform.service.SysAuditLogService;
import com.his.platform.service.UserIdentityCache;
import com.his.common.util.JwtUtils;
import com.his.platform.vo.EmployeePostVO;
import com.his.platform.vo.SwitchPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 岗位（角色 × 科室）的实现 —— 「以什么身份在哪个科室执业」的唯一判定点。
 */
@Service
@RequiredArgsConstructor
public class EmployeePostServiceImpl extends ServiceImpl<SysEmployeePostMapper, SysEmployeePost> implements EmployeePostService {

    private final SysEmployeePostMapper sysEmployeePostMapper;
    private final SysEmployeeMapper sysEmployeeMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final JwtUtils jwtUtils;
    private final SysAuditLogService sysAuditLogService;
    private final UserIdentityCache userIdentityCache;

    /**
     * 岗位当前是否生效（sql/113）：两侧 NULL = 不限，存量数据行为中性。
     */
    private static boolean onDuty(EmployeePostVO post, LocalDate today) {
        return (post.getEffectiveDate() == null || !today.isBefore(post.getEffectiveDate()))
                && (post.getExpireDate() == null || !today.isAfter(post.getExpireDate()));
    }

    /**
     * 状态是派生值而不是列 + 定时任务：加列就多一个「日期已过、状态还没刷」的漂移窗口，
     * 「已失效」的唯一事实来源永远是 expireDate。
     */
    private static Integer statusOf(EmployeePostVO post, LocalDate today) {
        return post.getExpireDate() != null && today.isAfter(post.getExpireDate())
                ? EmployeePostStatusEnum.EXPIRED.getCode() : EmployeePostStatusEnum.ACTIVE.getCode();
    }

    @Override
    public List<EmployeePostVO> listPosts(Long employeeId) {
        if (employeeId == null) {
            return List.of();
        }
        List<EmployeePostVO> posts = sysEmployeePostMapper.getEmployeePosts(employeeId);
        LocalDate today = LocalDate.now();
        posts.forEach(p -> p.setPostStatus(statusOf(p, today)));
        return posts;
    }

    @Override
    public List<EmployeePostVO> listActivePosts(Long employeeId) {
        LocalDate today = LocalDate.now();
        return listPosts(employeeId).stream().filter(p -> onDuty(p, today)).toList();
    }

    /**
     * 以 {@code roleCode} 登录/切换时落在哪一条岗位。
     *
     * <p>主岗位是**人**的属性，一个人只有一条，所以非主岗位的那个角色并没有"自己的默认科室"可查，
     * 这里现算：<b>先取该角色下与主岗位同科室的那条</b>（一人两个角色多半在同一科室执业，
     * 登录后不该被扔到另一个科室去），该角色在这个科室没有岗位时再退到它的第一条。
     */
    @Override
    public EmployeePostVO resolvePrimaryPost(Long employeeId, String roleCode) {
        if (employeeId == null || !TextUtil.hasText(roleCode)) {
            return null;
        }
        // 登录落点只认生效中的岗位（sql/113）：已失效/尚未生效的角色不能签出 token
        List<EmployeePostVO> posts = listActivePosts(employeeId);
        Long primaryDeptId = listPosts(employeeId).stream()
                .filter(p -> Objects.equals(YesOrNoEnum.YES.getCode(), p.getIsPrimary()))
                .map(EmployeePostVO::getDeptId)
                .findFirst()
                .orElse(null);
        List<EmployeePostVO> ofRole = posts.stream()
                .filter(p -> roleCode.equals(p.getRoleCode()))
                .toList();
        return ofRole.stream()
                .filter(p -> Objects.equals(p.getDeptId(), primaryDeptId))
                .findFirst()
                .orElseGet(() -> ofRole.stream().findFirst().orElse(null));
    }

    @Override
    public SwitchPostVO switchPost(CurrentUser user, String roleCode, Long deptId) {
        if (!TextUtil.hasText(roleCode) || deptId == null) {
            throw new BusinessException(400, "切换岗位必须同时指定角色和科室");
        }
        if (user.getEmployeeId() == null) {
            throw new BusinessException(400, "当前账号不是院内员工，没有可切换的岗位");
        }

        List<EmployeePostVO> posts = listPosts(user.getEmployeeId());
        String from = describeCurrent(posts, user);

        EmployeePostVO target;
        try {
            target = matchPost(posts, roleCode, deptId);
            checkOnDuty(target);
        } catch (BusinessException e) {
            sysAuditLogService.record(user.getEmployeeId(), user.getRealName(), "认证", "切换岗位",
                    "employee", user.getEmployeeId(),
                    from + " → 试图切换 " + roleCode + "/" + deptId, false, e.getMessage());
            throw e;
        }

        user.setCurrentRole(target.getRoleCode());
        user.setDeptId(target.getDeptId());
        user.setDeptName(target.getDeptName());

        SwitchPostVO vo = new SwitchPostVO();
        vo.setCurrentRole(target.getRoleCode());
        vo.setRoleName(target.getRoleName());
        vo.setDeptId(target.getDeptId());
        vo.setDeptName(target.getDeptName());
        vo.setToken(jwtUtils.generateToken(user.getUserId(), user.getUsername(),
                target.getRoleCode(), target.getDeptId(), target.getDeptName()));

        sysAuditLogService.record(user.getEmployeeId(), user.getRealName(), "认证", "切换岗位",
                "employee", user.getEmployeeId(), from + " → " + label(target), true, null);
        return vo;
    }

    /**
     * 整体替换岗位。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmployeePostVO replacePosts(Long employeeId, List<EmployeePostDTO> input) {
        if (employeeId == null) {
            return null;
        }
        if (input == null) {
            return currentPrimary(employeeId);
        }
        List<EmployeePostDTO> rows = input.stream().filter(Objects::nonNull).toList();

        checkNoDuplicate(rows);
        Map<String, SysRole> roleByCode = loadRoles(rows);
        Map<Long, SysDepartment> deptById = loadDepts(rows);

        sysEmployeePostMapper.delete(new LambdaQueryWrapper<SysEmployeePost>()
                .eq(SysEmployeePost::getEmployeeId, employeeId));

        boolean anyClaimed = rows.stream().anyMatch(r -> Objects.equals(YesOrNoEnum.YES.getCode(), r.getIsPrimary()));
        boolean primaryTaken = false;
        List<SysEmployeePost> saved = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            EmployeePostDTO row = rows.get(i);
            SysRole role = roleByCode.get(row.getRoleCode());
            SysDepartment dept = deptById.get(row.getDeptId());
            SysEmployeePost post = new SysEmployeePost();
            post.setEmployeeId(employeeId);
            post.setRoleId(role.getId());
            post.setDeptId(dept.getId());
            boolean primary = !primaryTaken && (anyClaimed
                    ? Objects.equals(YesOrNoEnum.YES.getCode(), row.getIsPrimary()) : i == 0);
            if (primary) {
                primaryTaken = true;
            }
            post.setIsPrimary(primary ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
            post.setEffectiveDate(row.getEffectiveDate());
            post.setExpireDate(row.getExpireDate());
            sysEmployeePostMapper.insert(post);
            saved.add(post);
        }

        EmployeePostVO primary = toVO(pickPrimary(saved), roleByCode, deptById);
        syncPrimaryDept(employeeId, primary);
        // 岗位 = 角色 × 科室是鉴权直接 join 的事实来源，角色集合变了，名下账号的身份缓存必须清
        userIdentityCache.invalidateByEmployeeId(employeeId);
        return primary;
    }

    /**
     * 把主岗位回填到员工的科室ID / dept_name。
     *
     * <p>员工表上这两个字段仍是「这个人在哪个科室」的取数口径（号源、排班名册、
     * 病历签名等都直接读它），而岗位表里它是那**唯一一条**主岗位的科室。
     * 谁写岗位谁负责回填，否则两边会漂：改了岗位、员工表还停在旧科室。
     */
    private void syncPrimaryDept(Long employeeId, EmployeePostVO primary) {
        sysEmployeeMapper.update(null, new LambdaUpdateWrapper<SysEmployee>()
                .eq(SysEmployee::getId, employeeId)
                .set(SysEmployee::getDeptId, primary == null ? null : primary.getDeptId())
                .set(SysEmployee::getDeptName, primary == null ? null : primary.getDeptName()));
    }

    /**
     * 在本人岗位里找到 (角色, 科室) 这一行，找不到就报出**原因**而不是笼统的「无权切换」：
     * 是压根没这个角色的岗位，还是这个角色在别的科室 —— 测试账号经常是后者，
     * 只说「无权限」会让人以为分配漏了，白跑一趟管理端。
     */
    private EmployeePostVO matchPost(List<EmployeePostVO> posts, String roleCode, Long deptId) {
        return posts.stream()
                .filter(p -> roleCode.equals(p.getRoleCode()) && Objects.equals(p.getDeptId(), deptId))
                .findFirst()
                .orElseThrow(() -> mismatchError(posts, roleCode));
    }

    private BusinessException mismatchError(List<EmployeePostVO> posts, String roleCode) {
        List<EmployeePostVO> sameRole = posts.stream()
                .filter(p -> roleCode.equals(p.getRoleCode()))
                .toList();
        if (sameRole.isEmpty()) {
            String owned = posts.stream().map(EmployeePostVO::getRoleName).distinct().collect(Collectors.joining("、"));
            return new BusinessException(400, "您没有被分配该角色的岗位"
                    + (owned.isEmpty() ? "（请联系管理员在员工档案里分配岗位）" : "，您当前有权限的角色：" + owned));
        }
        String depts = sameRole.stream().map(EmployeePostVO::getDeptName).collect(Collectors.joining("、"));
        return new BusinessException(400, "该角色只在以下科室分配了岗位：" + depts + "，请重新选择科室");
    }

    /**
     * 岗位存在但不在生效期内同样切不过去
     */
    private void checkOnDuty(EmployeePostVO post) {
        LocalDate today = LocalDate.now();
        if (onDuty(post, today)) {
            return;
        }
        if (post.getExpireDate() != null && today.isAfter(post.getExpireDate())) {
            throw new BusinessException(400, "该岗位已于 " + post.getExpireDate() + " 失效，请联系管理员");
        }
        throw new BusinessException(400, "该岗位 " + post.getEffectiveDate() + " 才生效，当前还不能切换");
    }

    /**
     * 该员工当前的主岗位（getEmployeePosts 已按 is_primary desc 排序，取第一条即可）。
     */
    private EmployeePostVO currentPrimary(Long employeeId) {
        List<EmployeePostVO> posts = listPosts(employeeId);
        return posts.isEmpty() ? null : posts.get(0);
    }

    private void checkNoDuplicate(List<EmployeePostDTO> rows) {
        Set<String> seen = new HashSet<>();
        for (EmployeePostDTO row : rows) {
            if (!TextUtil.hasText(row.getRoleCode()) || row.getDeptId() == null) {
                throw new BusinessException(400, "岗位必须同时选定角色和科室");
            }
            if (row.getEffectiveDate() != null && row.getExpireDate() != null
                    && row.getExpireDate().isBefore(row.getEffectiveDate())) {
                throw new BusinessException(400, "岗位失效日期不能早于生效日期：" + row.getRoleCode());
            }
            if (!seen.add(row.getRoleCode() + "#" + row.getDeptId())) {
                throw new BusinessException(400, "有重复的岗位（同一角色在同一科室只能配一条）");
            }
        }
    }

    private Map<String, SysRole> loadRoles(List<EmployeePostDTO> rows) {
        Set<String> codes = rows.stream().map(EmployeePostDTO::getRoleCode)
                .filter(TextUtil::hasText).collect(Collectors.toSet());
        if (codes.isEmpty()) {
            return Map.of();
        }
        Map<String, SysRole> byCode = sysRoleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, codes)).stream()
                .collect(Collectors.toMap(SysRole::getRoleCode, Function.identity(), (a, b) -> a));
        for (String code : codes) {
            if (!byCode.containsKey(code)) {
                throw new BusinessException(400, "角色编码不存在：" + code);
            }
        }
        return byCode;
    }

    private Map<Long, SysDepartment> loadDepts(List<EmployeePostDTO> rows) {
        Set<Long> ids = rows.stream().map(EmployeePostDTO::getDeptId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, SysDepartment> byId = sysDepartmentMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysDepartment::getId, Function.identity(), (a, b) -> a));
        for (Long id : ids) {
            if (!byId.containsKey(id)) {
                throw new BusinessException(400, "科室不存在或已删除：" + id);
            }
        }
        return byId;
    }

    /**
     * 一条岗位里取主岗位 —— {@code replacePosts} 已归一化成「一人只有一条」，
     * 所以这里就是那条；兜底的 orElse(first) 只服务调用方传了未归一化集合的情况。
     */
    private SysEmployeePost pickPrimary(List<SysEmployeePost> saved) {
        return saved.stream()
                .filter(p -> Objects.equals(YesOrNoEnum.YES.getCode(), p.getIsPrimary()))
                .findFirst()
                .orElseGet(() -> saved.stream().findFirst().orElse(null));
    }

    private EmployeePostVO toVO(SysEmployeePost post, Map<String, SysRole> roleByCode, Map<Long, SysDepartment> deptById) {
        if (post == null) {
            return null;
        }
        SysRole role = roleByCode.values().stream()
                .filter(r -> r.getId().equals(post.getRoleId()))
                .findFirst()
                .orElse(null);
        SysDepartment dept = deptById.get(post.getDeptId());
        if (role == null || dept == null) {
            return null;
        }
        EmployeePostVO vo = new EmployeePostVO();
        vo.setRoleId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDeptId(dept.getId());
        vo.setDeptCode(dept.getDeptCode());
        vo.setDeptName(dept.getDeptName());
        vo.setIsPrimary(post.getIsPrimary());
        vo.setEffectiveDate(post.getEffectiveDate());
        vo.setExpireDate(post.getExpireDate());
        vo.setPostStatus(statusOf(vo, LocalDate.now()));
        return vo;
    }

    private String describeCurrent(List<EmployeePostVO> posts, CurrentUser user) {
        String roleName = posts.stream()
                .filter(p -> Objects.equals(p.getRoleCode(), user.getCurrentRole()))
                .map(EmployeePostVO::getRoleName)
                .findFirst()
                .orElse(user.getCurrentRole());
        // 与顶栏「切换岗位」同序：先认在哪个科室，再认以什么身份执业
        return (TextUtil.hasText(user.getDeptName()) ? user.getDeptName() : "未指定科室")
                + "·" + roleName;
    }

    private String label(EmployeePostVO post) {
        return post.getDeptName() + "·" + post.getRoleName();
    }
}
