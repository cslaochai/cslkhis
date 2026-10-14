package com.mss.controller;

import com.mss.common.base.Result;
import com.mss.entity.MssDepartment;
import com.mss.service.SystemDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 经办科室（岗位配置下拉）
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SystemDeptController {

    private final SystemDeptService deptService;

    @GetMapping("/selectList")
    public Result<List<MssDepartment>> selectList() {
        return Result.success(deptService.selectList());
    }
}
