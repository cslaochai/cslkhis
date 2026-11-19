package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.platform.dto.MenuUpsertDTO;
import com.his.common.entity.CurrentUser;
import com.his.platform.entity.SysMenu;
import com.his.platform.mapper.SysMenuMapper;
import com.his.platform.service.RolePermissionCache;
import com.his.platform.service.SysMenuService;
import com.his.platform.service.UserIdentityCache;
import com.his.common.utils.UserUtils;
import com.his.platform.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final RolePermissionCache rolePermissionCache;
    private final UserIdentityCache userIdentityCache;

    /** 门户目录 id：其下为登录即见的固定入口区（工作台 / 统一待办 / 急诊值班） */
    private static final Long PORTAL_ROOT_ID = 100L;

    @Override
    public List<MenuVO> tree() {
        List<SysMenu> allMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder)
                        .orderByAsc(SysMenu::getId));
        List<MenuVO> voList = allMenus.stream().map(this::toVO).collect(Collectors.toList());
        return buildMenuTree(voList, 0L);
    }

    @Override
    public MenuVO getInfo(Long menuId) {
        SysMenu menu = sysMenuMapper.selectById(menuId);
        return toVO(menu);
    }

    @Override
    public List<MenuVO> userMenus() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (Objects.isNull(user)) {
            return Collections.emptyList();
        }
        String roleCode = user.getCurrentRole();
        List<SysMenu> menus = TextUtil.hasText(roleCode)
                ? sysMenuMapper.selectMenusByEmployeeIdAndRole(user.getEmployeeId(), roleCode)
                : sysMenuMapper.selectMenusByEmployeeId(user.getEmployeeId());
        List<MenuVO> voList = mergeWithPortalEntries(menus).stream().map(this::toVO).collect(Collectors.toList());
        return buildMenuTree(voList, 0L);
    }

    /**
     * 门户入口区（工作台 / 统一待办 / 急诊值班）不按角色授权过滤：这几项是登录后第一眼要用的入口，
     * 角色没被授就整片看不见等于「登进来没门」。只补 is_visible=1 且 status=1 的行，
     * 被隐藏（如 102 消息中心）或停用的菜单不会被这条通路复活；其余菜单仍严格按 sys_role_menu 判定。
     */
    private List<SysMenu> mergeWithPortalEntries(List<SysMenu> granted) {
        List<SysMenu> portal = lambdaQuery()
                .and(w -> w.eq(SysMenu::getId, PORTAL_ROOT_ID).or().eq(SysMenu::getParentId, PORTAL_ROOT_ID))
                .eq(SysMenu::getIsVisible, 1)
                .eq(SysMenu::getStatus, 1)
                .list();
        Set<Long> grantedIds = granted.stream().map(SysMenu::getId).collect(Collectors.toCollection(HashSet::new));
        List<SysMenu> merged = new ArrayList<>(granted);
        portal.stream().filter(menu -> grantedIds.add(menu.getId())).forEach(merged::add);
        merged.sort(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SysMenu::getId));
        return merged;
    }

    @Override
    public String upsert(MenuUpsertDTO upsertDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(upsertDTO, menu);
        if (menu.getId() == null) {
            sysMenuMapper.insert(menu);
            // 菜单新增会改变各角色可见的按钮集合，全量失效
            rolePermissionCache.invalidateAll();
            userIdentityCache.invalidateAll();
            return "新增成功";
        }
        sysMenuMapper.updateById(menu);
        // 菜单的权限码可能被改了:所有角色的权限集合都可能变化,全量失效(角色数级,量小)
        rolePermissionCache.invalidateAll();
        userIdentityCache.invalidateAll();
        return "修改成功";
    }

    @Override
    public void delete(Long menuId) {
        Long childCount = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId));
        if (childCount > 0) {
            throw new BusinessException("该菜单下有子菜单，不能删除");
        }

        sysMenuMapper.deleteById(menuId);
        // 菜单没了,持有它的角色权限集合变小,全量失效
        rolePermissionCache.invalidateAll();
        userIdentityCache.invalidateAll();
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
