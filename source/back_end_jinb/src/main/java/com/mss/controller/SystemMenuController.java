package com.mss.controller;

import com.mss.common.base.Result;
import com.mss.dto.IdDTO;
import com.mss.dto.MenuUpsertDTO;
import com.mss.entity.MssMenu;
import com.mss.service.SystemMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理（树形）
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SystemMenuController {

    private final SystemMenuService menuService;

    @GetMapping("/tree")
    public Result<List<MssMenu>> tree() {
        return Result.success(menuService.tree());
    }

    @PostMapping("/upsert")
    public Result<MssMenu> upsert(@Valid @RequestBody MenuUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", menuService.upsert(dto));
    }

    @PostMapping("/deleteById")
    public Result<Void> deleteById(@RequestBody IdDTO dto) {
        menuService.deleteById(Long.valueOf(dto.getId()));
        return Result.success("删除成功", null);
    }
}
