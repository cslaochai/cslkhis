package com.mss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mss.common.base.PageResult;
import com.mss.common.exception.BusinessException;
import com.mss.config.LoginUser;
import com.mss.config.UserContext;
import com.mss.dto.PostItemDTO;
import com.mss.dto.PostReplaceDTO;
import com.mss.entity.MssDepartment;
import com.mss.entity.MssEmployee;
import com.mss.entity.MssEmployeePost;
import com.mss.entity.MssRole;
import com.mss.mapper.MssDepartmentMapper;
import com.mss.mapper.MssEmployeeMapper;
import com.mss.mapper.MssEmployeePostMapper;
import com.mss.mapper.MssRoleMapper;
import com.mss.service.SystemPostService;
import com.mss.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 员工岗位管理实现（整单替换，本表无 del_flag 走物理删）
 */
@Service
@RequiredArgsConstructor
public class SystemPostServiceImpl implements SystemPostService {

    private final MssEmployeePostMapper employeePostMapper;
    private final MssEmployeeMapper employeeMapper;
    private final MssRoleMapper roleMapper;
    private final MssDepartmentMapper departmentMapper;

    @Override
    public PageResult<PostVO> page(String keyword, long pageNum, long pageSize) {
        List<Long> empIds = null;
        if (keyword != null && !keyword.isEmpty()) {
            empIds = employeeMapper.selectList(new LambdaQueryWrapper<MssEmployee>()
                            .like(MssEmployee::getEmpName, keyword)
                            .or()
                            .like(MssEmployee::getEmpCode, keyword))
                    .stream().map(MssEmployee::getId).collect(Collectors.toList());
            if (empIds.isEmpty()) {
                return PageResult.of(0, pageNum, pageSize, 0, new ArrayList<>());
            }
        }
        Page<MssEmployeePost> page = employeePostMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MssEmployeePost>()
                        .in(empIds != null && !empIds.isEmpty(), MssEmployeePost::getEmployeeId, empIds)
                        .orderByAsc(MssEmployeePost::getEmployeeId)
                        .orderByDesc(MssEmployeePost::getIsPrimary)
                        .orderByAsc(MssEmployeePost::getId));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
    }

    @Override
    public List<PostVO> postsOfEmployee(Long employeeId) {
        List<MssEmployeePost> posts = employeePostMapper.selectList(new LambdaQueryWrapper<MssEmployeePost>()
                .eq(MssEmployeePost::getEmployeeId, employeeId)
                .orderByDesc(MssEmployeePost::getIsPrimary)
                .orderByAsc(MssEmployeePost::getId));
        return posts.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePosts(PostReplaceDTO dto) {
        MssEmployee employee = employeeMapper.selectById(dto.getEmployeeId());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        // 本表无 del_flag：先物理删旧行再插入，避免占用唯一键
        employeePostMapper.delete(new LambdaQueryWrapper<MssEmployeePost>()
                .eq(MssEmployeePost::getEmployeeId, dto.getEmployeeId()));

        List<PostItemDTO> posts = dto.getPosts();
        if (posts == null || posts.isEmpty()) {
            return;
        }
        // 硬不变量：恰好一条主岗位；多条主岗位只认第一条，没有则把第一条提为主岗位
        boolean hasPrimary = posts.stream().anyMatch(p -> Objects.equals(p.getIsPrimary(), 1));
        String operator = operatorName();
        for (int i = 0; i < posts.size(); i++) {
            PostItemDTO item = posts.get(i);
            if (item.getRoleId() == null || item.getDeptId() == null) {
                continue;
            }
            MssEmployeePost post = new MssEmployeePost();
            post.setEmployeeId(dto.getEmployeeId());
            post.setRoleId(item.getRoleId());
            post.setDeptId(item.getDeptId());
            post.setIsPrimary(hasPrimary
                    ? (Objects.equals(item.getIsPrimary(), 1) ? 1 : 0)
                    : (i == 0 ? 1 : 0));
            post.setEffectiveDate(item.getEffectiveDate());
            post.setExpireDate(item.getExpireDate());
            post.setCreateBy(operator);
            employeePostMapper.insert(post);
        }
    }

    @Override
    public List<MssEmployee> employeeSelect(String keyword) {
        return employeeMapper.selectList(new LambdaQueryWrapper<MssEmployee>()
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(MssEmployee::getEmpName, keyword)
                        .or()
                        .like(MssEmployee::getEmpCode, keyword))
                .orderByAsc(MssEmployee::getId)
                .last("LIMIT 50"));
    }

    // ------------------------------------------------------------ 内部工具

    private PostVO toVO(MssEmployeePost post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setEmployeeId(post.getEmployeeId());
        vo.setRoleId(post.getRoleId());
        vo.setDeptId(post.getDeptId());
        vo.setIsPrimary(post.getIsPrimary());
        vo.setEffectiveDate(post.getEffectiveDate());
        vo.setExpireDate(post.getExpireDate());

        MssEmployee emp = employeeMapper.selectById(post.getEmployeeId());
        if (emp != null) {
            vo.setEmpCode(emp.getEmpCode());
            vo.setEmpName(emp.getEmpName());
        }
        MssRole role = roleMapper.selectById(post.getRoleId());
        if (role != null) {
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
        }
        MssDepartment dept = departmentMapper.selectById(post.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getDeptName());
        }
        return vo;
    }

    private String operatorName() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
