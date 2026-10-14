package com.mis.controller;

import com.mis.common.base.PageResult;
import com.mis.common.base.Result;
import com.mis.dto.*;
import com.mis.entity.MssUser;
import com.mis.service.SystemUserService;
import com.mis.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理（兼容 HIS 契约：POST listPage / userUpsert / resetPassword，DELETE deleteById，GET getById）
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SystemUserController {

    private final SystemUserService userService;

    @GetMapping("/listPage")
    public Result<PageResult<UserVO>> listPage(UserQueryPageDTO dto) {
        return Result.success(userService.page(dto));
    }

    @PostMapping("/listPage")
    public Result<PageResult<UserVO>> listPagePost(@RequestBody UserQueryPageDTO dto) {
        return Result.success(userService.page(dto));
    }

    @GetMapping("/getById")
    public Result<UserVO> getById(@RequestParam(required = false) Long id,
                                  @RequestParam(required = false) Long userId) {
        return Result.success(userService.detail(id != null ? id : userId));
    }

    @PostMapping("/upsert")
    public Result<MssUser> upsert(@Valid @RequestBody UserUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", userService.upsert(dto));
    }

    @PostMapping("/userUpsert")
    public Result<MssUser> userUpsert(@Valid @RequestBody UserUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", userService.upsert(dto));
    }

    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success("密码重置成功", null);
    }

    @PostMapping("/changeStatus")
    public Result<Void> changeStatus(@Valid @RequestBody UserStatusDTO dto) {
        userService.changeStatus(dto);
        return Result.success("状态更新成功", null);
    }

    @PostMapping("/deleteById")
    public Result<Void> deleteById(@RequestBody IdDTO dto) {
        userService.deleteById(Long.valueOf(dto.getId()));
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/deleteById")
    public Result<Void> deleteByIdParam(@RequestParam(required = false) Long userId) {
        userService.deleteById(userId);
        return Result.success("删除成功", null);
    }
}
