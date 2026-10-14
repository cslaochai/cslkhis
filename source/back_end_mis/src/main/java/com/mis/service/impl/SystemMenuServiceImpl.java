package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mis.common.exception.BusinessException;
import com.mis.config.LoginUser;
import com.mis.config.UserContext;
import com.mis.dto.MenuUpsertDTO;
import com.mis.entity.MssMenu;
import com.mis.entity.MssRoleMenu;
import com.mis.mapper.MssMenuMapper;
import com.mis.mapper.MssRoleMenuMapper;
import com.mis.service.SystemMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 菜单管理实现（树形）
 */
@Service
@RequiredArgsConstructor
public class SystemMenuServiceImpl implements SystemMenuService {

    private final MssMenuMapper menuMapper;
    private final MssRoleMenuMapper roleMenuMapper;

    @Override
    public List<MssMenu> tree() {
        List<MssMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<MssMenu>()
                .orderByAsc(MssMenu::getSortOrder)
                .orderByAsc(MssMenu::getId));
        return buildTree(menus);
    }

    @Override
    public MssMenu getById(Long id) {
        MssMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Override
    public MssMenu upsert(MenuUpsertDTO dto) {
        MssMenu menu = new MssMenu();
        BeanUtils.copyProperties(dto, menu);
        String operator = operatorName();

        if (dto.getId() == null) {
            if (dto.getMenuKey() != null && !dto.getMenuKey().isEmpty()) {
                Long exist = menuMapper.selectCount(new LambdaQueryWrapper<MssMenu>()
                        .eq(MssMenu::getMenuKey, dto.getMenuKey()));
                if (exist != null && exist > 0) {
                    throw new BusinessException("菜单标识已存在");
                }
            }
            menu.setCreateBy(operator);
            menuMapper.insert(menu);
        } else {
            if (menuMapper.selectById(dto.getId()) == null) {
                throw new BusinessException("菜单不存在");
            }
            menu.setUpdateBy(operator);
            menuMapper.updateById(menu);
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        MssMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        // 目录删除连带子菜单；子菜单删除连带角色授权
        List<Long> ids = new ArrayList<>();
        collectTreeIds(id, ids);
        ids.add(id);
        menuMapper.deleteBatchIds(ids);
        roleMenuMapper.delete(new LambdaQueryWrapper<MssRoleMenu>().in(MssRoleMenu::getMenuId, ids));
    }

    private void collectTreeIds(Long parentId, List<Long> out) {
        List<MssMenu> children = menuMapper.selectList(new LambdaQueryWrapper<MssMenu>()
                .eq(MssMenu::getParentId, parentId));
        for (MssMenu child : children) {
            out.add(child.getId());
            collectTreeIds(child.getId(), out);
        }
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

    private String operatorName() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
