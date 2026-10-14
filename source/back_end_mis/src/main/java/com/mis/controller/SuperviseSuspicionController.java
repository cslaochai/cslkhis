package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.SuspicionHandleDTO;
import com.mis.dto.SuspicionQueryDTO;
import com.mis.entity.MssSuspicion;
import com.mis.service.SuspicionService;
import com.mis.vo.ActionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 基金监管 - 疑点稽核
 */
@RestController
@RequestMapping("/supervise/suspicion")
@RequiredArgsConstructor
public class SuperviseSuspicionController {

    private final SuspicionService suspicionService;

    @GetMapping("/listPage")
    public Result<PageVO<MssSuspicion>> listPage(SuspicionQueryDTO query) {
        return Result.success(suspicionService.suspicionPage(query));
    }

    @PostMapping("/run")
    public Result<ActionVO> run() {
        return Result.success(suspicionService.runAudit());
    }

    @GetMapping("/getDetailById")
    public Result<MssSuspicion> getDetail(@RequestParam Long id) {
        return Result.success(suspicionService.getDetail(id));
    }

    @PostMapping("/confirm")
    public Result<ActionVO> confirm(@RequestBody @Valid SuspicionHandleDTO dto) {
        return Result.success(suspicionService.confirm(dto));
    }

    @PostMapping("/exclude")
    public Result<ActionVO> exclude(@RequestBody @Valid SuspicionHandleDTO dto) {
        return Result.success(suspicionService.exclude(dto));
    }
}
