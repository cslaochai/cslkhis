package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import com.his.system.dto.MenuUpsertDTO;
import com.his.system.entity.SysMenu;
import com.his.system.mapper.SysMenuMapper;
import com.his.system.service.RolePermissionCache;
import com.his.system.service.SysMenuService;
import com.his.system.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;
    private final RolePermissionCache rolePermissionCache;

    @Override
    public List<MenuVO> tree() {
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder)
                        .orderByAsc(SysMenu::getId));
        List<MenuVO> voList = allMenus.stream().map(this::toVO).collect(Collectors.toList());
        return buildMenuTree(voList, 0L);
    }

    @Override
    public MenuVO getInfo(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        return toVO(menu);
    }

    @Override
    public List<MenuVO> userMenus() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (Objects.isNull(user)) {
            return Collections.emptyList();
        }
        String roleCode = user.getCurrentRole();
        List<SysMenu> menus = StringUtils.hasText(roleCode)
                ? menuMapper.selectMenusByEmployeeIdAndRole(user.getEmployeeId(), roleCode)
                : menuMapper.selectMenusByEmployeeId(user.getEmployeeId());
        List<MenuVO> voList = menus.stream().map(this::toVO).collect(Collectors.toList());
        return buildMenuTree(voList, 0L);
    }

    @Override
    public String upsert(MenuUpsertDTO upsertDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(upsertDTO, menu);
        if (menu.getId() == null) {
            menuMapper.insert(menu);
            // 菜单新增会改变各角色可见的按钮集合，全量失效
            rolePermissionCache.invalidateAll();
            return "新增成功";
        }
        menuMapper.updateById(menu);
        // 菜单的权限码可能被改了:所有角色的权限集合都可能变化,全量失效(角色数级,量小)
        rolePermissionCache.invalidateAll();
        return "修改成功";
    }

    @Override
    public void delete(Long menuId) {
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException("该菜单下有子菜单，不能删除");
        }

        menuMapper.deleteById(menuId);
        // 菜单没了,持有它的角色权限集合变小,全量失效
        rolePermissionCache.invalidateAll();
    }

    private MenuVO toVO(SysMenu menu) {
        if (menu == null) {
            return null;
        }
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }

    private List<MenuVO> buildMenuTree(List<MenuVO> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
                .collect(Collectors.toList());
    }
}
