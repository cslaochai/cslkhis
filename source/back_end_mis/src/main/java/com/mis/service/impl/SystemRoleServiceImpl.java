package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageResult;
import com.mis.common.exception.BusinessException;
import com.mis.config.LoginUser;
import com.mis.config.UserContext;
import com.mis.dto.RoleMenuSaveDTO;
import com.mis.dto.RoleUpsertDTO;
import com.mis.entity.MssRole;
import com.mis.entity.MssRoleMenu;
import com.mis.mapper.MssRoleMapper;
import com.mis.mapper.MssRoleMenuMapper;
import com.mis.service.SystemRoleService;
import com.mis.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理实现（授权 = 整单替换 mss_role_menu）
 */
@Service
@RequiredArgsConstructor
public class SystemRoleServiceImpl implements SystemRoleService {

    private final MssRoleMapper roleMapper;
    private final MssRoleMenuMapper roleMenuMapper;

    @Override
    public PageResult<MssRole> page(String keyword, long pageNum, long pageSize) {
        Page<MssRole> page = roleMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MssRole>()
                        .and(keyword != null && !keyword.isEmpty(), w -> w
                                .like(MssRole::getRoleName, keyword)
                                .or()
                                .like(MssRole::getRoleCode, keyword))
                        .orderByAsc(MssRole::getSortOrder)
                        .orderByAsc(MssRole::getId));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<MssRole> selectList() {
        return roleMapper.selectList(new LambdaQueryWrapper<MssRole>()
                .eq(MssRole::getStatus, 1)
                .orderByAsc(MssRole::getSortOrder));
    }

    @Override
    public RoleVO getDetailById(Long id) {
        MssRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        vo.setMenuIds(menuIdsOfRole(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MssRole upsert(RoleUpsertDTO dto) {
        MssRole role = new MssRole();
        BeanUtils.copyProperties(dto, role);
        String operator = operatorName();

        if (dto.getId() == null) {
            // HIS 口径：新增时角色编码缺省自动生成 ROLE_ + 递增序号
            if (dto.getRoleCode() == null || dto.getRoleCode().isEmpty()) {
                dto.setRoleCode(generateRoleCode());
                role.setRoleCode(dto.getRoleCode());
            }
            Long exist = roleMapper.selectCount(new LambdaQueryWrapper<MssRole>()
                    .eq(MssRole::getRoleCode, dto.getRoleCode()));
            if (exist != null && exist > 0) {
                throw new BusinessException("角色编码已存在");
            }
            role.setCreateBy(operator);
            roleMapper.insert(role);
        } else {
            if (roleMapper.selectById(dto.getId()) == null) {
                throw new BusinessException("角色不存在");
            }
            role.setUpdateBy(operator);
            roleMapper.updateById(role);
        }

        // 授权整单替换：本表无 del_flag，先物理删再插
        if (dto.getMenuIds() != null) {
            roleMenuMapper.delete(new LambdaQueryWrapper<MssRoleMenu>().eq(MssRoleMenu::getRoleId, role.getId()));
            insertRoleMenus(role.getId(), dto.getMenuIds());
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        MssRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<MssRoleMenu>().eq(MssRoleMenu::getRoleId, id));
    }

    @Override
    public List<String> getMenuIds(Long roleId) {
        return menuIdsOfRole(roleId).stream().map(String::valueOf).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleMenu(RoleMenuSaveDTO dto) {
        MssRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<MssRoleMenu>().eq(MssRoleMenu::getRoleId, dto.getRoleId()));
        insertRoleMenus(dto.getRoleId(), dto.getMenuIds());
    }

    private String generateRoleCode() {
        long max = roleMapper.selectCount(new LambdaQueryWrapper<>());
        String code;
        do {
            code = "ROLE_" + String.format("%03d", ++max);
        } while (roleMapper.selectCount(new LambdaQueryWrapper<MssRole>()
                .eq(MssRole::getRoleCode, code)) > 0);
        return code;
    }

    private List<Long> menuIdsOfRole(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<MssRoleMenu>()
                        .eq(MssRoleMenu::getRoleId, roleId))
                .stream().map(MssRoleMenu::getMenuId).collect(Collectors.toList());
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds == null ? new ArrayList<Long>() : menuIds) {
            MssRoleMenu rm = new MssRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            rm.setCreateBy(operatorName());
            roleMenuMapper.insert(rm);
        }
    }

    private String operatorName() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
