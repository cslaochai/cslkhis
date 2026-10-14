package com.mss.service;

import com.mss.dto.MenuUpsertDTO;
import com.mss.entity.MssMenu;

import java.util.List;

/**
 * 菜单管理
 */
public interface SystemMenuService {

    List<MssMenu> tree();

    MssMenu upsert(MenuUpsertDTO dto);

    void deleteById(Long id);
}
