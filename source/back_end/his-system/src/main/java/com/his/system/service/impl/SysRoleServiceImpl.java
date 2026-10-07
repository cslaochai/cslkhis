package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.system.dto.RoleMenuUpsertDTO;
import com.his.system.dto.SysRoleQueryDTO;
import com.his.system.dto.SysRoleQueryPageDTO;
import com.his.system.dto.SysRoleUpsertDTO;
import com.his.system.entity.SysMenu;
import com.his.system.entity.SysRole;
import com.his.system.entity.SysRoleMenu;
import com.his.system.mapper.SysMenuMapper;
import com.his.system.mapper.SysRoleMapper;
import com.his.system.mapper.SysRoleMenuMapper;
import com.his.system.service.RolePermissionCache;
import com.his.system.service.SysRoleService;
import com.his.system.vo.RoleSelectListVO;
import com.his.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    /**
     * 内置系统管理员角色编码。它承载的是管理端自身的入口权限，所以刻意不允许把菜单清空。
     */
    private static final String SYSTEM_ADMIN_ROLE_CODE = "10012";

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;
    private final RedisSequenceService redisSequenceService;
    private final RolePermissionCache rolePermissionCache;

    @Override
    public PageResult<RoleVO> listPage(SysRoleQueryPageDTO queryDTO) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRoleName()), SysRole::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus())
                .orderByAsc(SysRole::getSortOrder);
        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<RoleVO> voList = page.getRecords().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<RoleSelectListVO> selectList(SysRoleQueryDTO queryDTO) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRoleName()), SysRole::getRoleName, queryDTO.getRoleName())
                .eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus())
                .orderByAsc(SysRole::getSortOrder);
        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        return roles.stream().map(role -> {
            RoleSelectListVO vo = new RoleSelectListVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public RoleVO getInfo(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    @Override
    public String upsert(SysRoleUpsertDTO upsertDTO) {
        SysRole role = new SysRole();
        BeanUtils.copyProperties(upsertDTO, role);
        if (role.getId() == null) {
            role.setRoleCode(redisSequenceService.generateRoleCode());

            // 新增角色默认为自定义角色
            if (role.getRoleType() == null) {
                role.setRoleType(0);
            }

            sysRoleMapper.insert(role);
            return "新增成功";
        }
        // 系统角色不允许修改角色类型
        SysRole existingRole = sysRoleMapper.selectById(role.getId());
        if (existingRole != null && existingRole.getRoleType() == 1) {
            role.setRoleType(1);
            role.setRoleCode(existingRole.getRoleCode());
        }

        sysRoleMapper.updateById(role);
        return "修改成功";
    }

    @Override
    public void delete(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (role.getRoleType() != null && role.getRoleType() == 1) {
            throw new BusinessException("系统角色不允许删除");
        }

        sysRoleMapper.deleteById(roleId);
        // 同步清理该角色的菜单权限关联，避免留下指向已删角色的孤儿记录
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        // 角色删了,它的权限缓存必须失效(下个同码角色不能继承旧权限)
        rolePermissionCache.invalidate(role.getRoleCode());
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        List<SysRoleMenu> list = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        return list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    public void saveRoleMenu(RoleMenuUpsertDTO upsertDTO) {
        SysRole role = sysRoleMapper.selectById(upsertDTO.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        List<Long> menuIds = upsertDTO.getMenuIds();
        boolean empty = (menuIds == null || menuIds.isEmpty());

        // 系统管理员必须始终保留菜单：一旦被清空，管理端入口随之消失，就再也改不回来了
        if (SYSTEM_ADMIN_ROLE_CODE.equals(role.getRoleCode()) && empty) {
            throw new BusinessException("系统管理员角色的菜单权限不允许清空");
        }

        // 菜单树按父子逐级挂载，若只勾了二级菜单而漏掉所属目录，该菜单在侧边栏不会显示
        Set<Long> finalMenuIds = empty ? new HashSet<>() : resolveWithParents(menuIds);

        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, upsertDTO.getRoleId()));
        for (Long menuId : finalMenuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(upsertDTO.getRoleId());
            roleMenu.setMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
        }
        // 授权变更即时生效:失效该角色的权限缓存(下一个请求重查)
        rolePermissionCache.invalidate(role.getRoleCode());
    }

    /**
     * 把菜单ID集合补齐为「含所有祖先目录」的集合。
     */
    private Set<Long> resolveWithParents(List<Long> menuIds) {
        Map<Long, Long> parentMap = new HashMap<>();
        for (SysMenu menu : sysMenuMapper.selectList(null)) {
            parentMap.put(menu.getId(), menu.getParentId());
        }
        Set<Long> result = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            Long parentId = parentMap.get(menuId);
            while (parentId != null && parentId != 0L) {
                result.add(parentId);
                parentId = parentMap.get(parentId);
            }
        }
        return result;
    }
}
