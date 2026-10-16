package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.LogQueryPageDTO;
import com.his.system.service.SysLogService;
import com.his.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日志审计（操作日志 / 登录日志 / 审计日志 / 字段级修改日志）查看控制器。
 */
@Tag(name = "日志审计")
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    @Operation(summary = "操作日志分页")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/operLogListPage")
    public Result<PageResult<OperLogListVO>> operLogListPage(@Valid @RequestBody LogQueryPageDTO query) {
        return Result.success(sysLogService.operLogListPage(query));
    }

    @Operation(summary = "操作日志详情（含请求参数与返回内容）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/operLogDetail")
    public Result<OperLogDetailVO> operLogDetail(@RequestParam Long id) {
        return Result.success(sysLogService.operLogDetail(id));
    }

    @Operation(summary = "登录日志分页（成功失败同表，靠 loginStatus 区分）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/loginLogListPage")
    public Result<PageResult<LoginLogVO>> loginLogListPage(@Valid @RequestBody LogQueryPageDTO query) {
        return Result.success(sysLogService.loginLogListPage(query));
    }

    @Operation(summary = "审计日志分页")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/auditLogListPage")
    public Result<PageResult<AuditLogVO>> auditLogListPage(@Valid @RequestBody LogQueryPageDTO query) {
        return Result.success(sysLogService.auditLogListPage(query));
    }

    @Operation(summary = "审计日志详情")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/auditLogDetail")
    public Result<AuditLogVO> auditLogDetail(@RequestParam Long id) {
        return Result.success(sysLogService.auditLogDetail(id));
    }

    @Operation(summary = "字段级修改日志分页（哪个字段从什么值改成了什么值）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/fieldChangeListPage")
    public Result<PageResult<FieldChangeVO>> fieldChangeListPage(@Valid @RequestBody LogQueryPageDTO query) {
        return Result.success(sysLogService.fieldChangeListPage(query));
    }

    @Operation(summary = "字段级修改日志同批次明细（一次保存改了哪些字段）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/fieldChangeBatch")
    public Result<List<FieldChangeVO>> fieldChangeBatch(@RequestParam String batchNo) {
        return Result.success(sysLogService.fieldChangeBatch(batchNo));
    }

    @Operation(summary = "四本账统计（含近24小时口令爆破嫌疑账号）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/stat")
    public Result<LogStatVO> stat() {
        return Result.success(sysLogService.stat());
    }

    @Operation(summary = "导出 CSV（logType 1-操作 2-登录 3-审计 4-字段变更，最多 5000 行）")
    @PreAuthorize("hasAuthority('system:log:export')")
    @PostMapping("/exportCsv")
    public Result<String> exportCsv(@Valid @RequestBody LogQueryPageDTO query) {
        return Result.success(sysLogService.exportCsv(query));
    }
}
