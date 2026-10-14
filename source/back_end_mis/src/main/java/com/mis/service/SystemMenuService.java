package com.mis.service;

import com.mis.dto.MenuUpsertDTO;
import com.mis.entity.MssMenu;

import java.util.List;

/**
 * 菜单管理
 */
public interface SystemMenuService {

    List<MssMenu> tree();

    MssMenu getById(Long id);

    MssMenu upsert(MenuUpsertDTO dto);

    void deleteById(Long id);
}
