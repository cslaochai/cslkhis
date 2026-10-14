package com.mis.controller;

import com.mis.common.base.PageResult;
import com.mis.common.base.Result;
import com.mis.dto.IdDTO;
import com.mis.dto.RoleListPageDTO;
import com.mis.dto.RoleMenuSaveDTO;
import com.mis.dto.RoleUpsertDTO;
import com.mis.entity.MssRole;
import com.mis.service.SystemRoleService;
import com.mis.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理（含菜单授权；兼容 HIS 契约：POST listPage/selectList/roleUpsert/saveRoleMenu，GET getById/getMenuIds）
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

    @PostMapping("/listPage")
    public Result<PageResult<MssRole>> listPagePost(@RequestBody RoleListPageDTO dto) {
        return Result.success(roleService.page(dto.getRoleName(), dto.getPage(), dto.getSize()));
    }

    @GetMapping("/selectList")
    public Result<List<MssRole>> selectList() {
        return Result.success(roleService.selectList());
    }

    @PostMapping("/selectList")
    public Result<List<MssRole>> selectListPost() {
        return Result.success(roleService.selectList());
    }

    @GetMapping("/getDetailById")
    public Result<RoleVO> getDetailById(@RequestParam Long id) {
        return Result.success(roleService.getDetailById(id));
    }

    @GetMapping("/getById")
    public Result<RoleVO> getById(@RequestParam(required = false) Long roleId,
                                  @RequestParam(required = false) Long id) {
        return Result.success(roleService.getDetailById(roleId != null ? roleId : id));
    }

    @GetMapping("/getMenuIds")
    public Result<List<String>> getMenuIds(@RequestParam Long roleId) {
        return Result.success(roleService.getMenuIds(roleId));
    }

    @PostMapping("/upsert")
    public Result<MssRole> upsert(@Valid @RequestBody RoleUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", roleService.upsert(dto));
    }

    @PostMapping("/roleUpsert")
    public Result<MssRole> roleUpsert(@Valid @RequestBody RoleUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", roleService.upsert(dto));
    }

    @PostMapping("/saveRoleMenu")
    public Result<Void> saveRoleMenu(@Valid @RequestBody RoleMenuSaveDTO dto) {
        roleService.saveRoleMenu(dto);
        return Result.success("菜单权限保存成功", null);
    }

    @PostMapping("/deleteById")
    public Result<Void> deleteById(@RequestBody IdDTO dto) {
        roleService.deleteById(Long.valueOf(dto.getId()));
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/deleteById")
    public Result<Void> deleteByIdParam(@RequestParam(required = false) Long roleId) {
        roleService.deleteById(roleId);
        return Result.success("删除成功", null);
    }
}
