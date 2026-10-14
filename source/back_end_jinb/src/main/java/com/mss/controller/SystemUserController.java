package com.mss.controller;

import com.mss.common.base.PageResult;
import com.mss.common.base.Result;
import com.mss.dto.*;
import com.mss.entity.MssUser;
import com.mss.service.SystemUserService;
import com.mss.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理
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

    @PostMapping("/upsert")
    public Result<MssUser> upsert(@Valid @RequestBody UserUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", userService.upsert(dto));
    }

    @PostMapping("/resetPassword")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
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
}
