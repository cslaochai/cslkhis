package com.mss.controller;

import com.mss.common.base.PageResult;
import com.mss.common.base.Result;
import com.mss.dto.IdDTO;
import com.mss.dto.RoleUpsertDTO;
import com.mss.entity.MssRole;
import com.mss.service.SystemRoleService;
import com.mss.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理（含菜单授权）
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SystemRoleController {

    private final SystemRoleService roleService;

    @GetMapping("/listPage")
    public Result<PageResult<MssRole>> listPage(@RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(roleService.page(keyword, pageNum, pageSize));
    }

    @GetMapping("/selectList")
    public Result<List<MssRole>> selectList() {
        return Result.success(roleService.selectList());
    }

    @GetMapping("/getDetailById")
    public Result<RoleVO> getDetailById(@RequestParam Long id) {
        return Result.success(roleService.getDetailById(id));
    }

    @PostMapping("/upsert")
    public Result<MssRole> upsert(@Valid @RequestBody RoleUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", roleService.upsert(dto));
    }

    @PostMapping("/deleteById")
    public Result<Void> deleteById(@RequestBody IdDTO dto) {
        roleService.deleteById(Long.valueOf(dto.getId()));
        return Result.success("删除成功", null);
    }
}
