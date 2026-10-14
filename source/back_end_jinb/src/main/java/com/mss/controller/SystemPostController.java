package com.mss.controller;

import com.mss.common.base.PageResult;
import com.mss.common.base.Result;
import com.mss.dto.PostReplaceDTO;
import com.mss.entity.MssEmployee;
import com.mss.service.SystemPostService;
import com.mss.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工岗位管理（岗位 = 角色 × 科室）
 */
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class SystemPostController {

    private final SystemPostService postService;

    @GetMapping("/listPage")
    public Result<PageResult<PostVO>> listPage(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(postService.page(keyword, pageNum, pageSize));
    }

    @GetMapping("/ofEmployee")
    public Result<List<PostVO>> ofEmployee(@RequestParam Long employeeId) {
        return Result.success(postService.postsOfEmployee(employeeId));
    }

    @PostMapping("/replace")
    public Result<Void> replace(@Valid @RequestBody PostReplaceDTO dto) {
        postService.replacePosts(dto);
        return Result.success("岗位保存成功", null);
    }

    @GetMapping("/employeeSelect")
    public Result<List<MssEmployee>> employeeSelect(@RequestParam(required = false) String keyword) {
        return Result.success(postService.employeeSelect(keyword));
    }
}
