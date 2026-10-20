package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.LogPageQueryDTO;
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

    /**
     * 按操作人、模块、时间等条件分页查询操作日志（只记写动作，读接口不入库）。
     * 用于系统管理「日志审计」页的「操作日志」页签表格。
     */
    @Operation(summary = "操作日志分页")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/operLogListPage")
    public Result<PageResult<OperLogListVO>> operLogListPage(@Valid @RequestBody LogPageQueryDTO query) {
        return Result.success(sysLogService.operLogListPage(query));
    }

    /**
     * 取单条操作日志的完整现场：请求参数与返回内容，凭据类字段已打码。
     * 用于「操作日志」页签行内「详情」弹框，追查一次写操作到底提交了什么。
     */
    @Operation(summary = "操作日志详情（含请求参数与返回内容）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/operLogDetail")
    public Result<OperLogDetailVO> operLogDetail(@RequestParam Long id) {
        return Result.success(sysLogService.operLogDetail(id));
    }

    /**
     * 按用户名、IP、结果等条件分页查询登录日志，成功与失败同表按登录状态区分。
     * 用于「日志审计」页的「登录日志」页签，排查账号被爆破或异常登录。
     */
    @Operation(summary = "登录日志分页（成功失败同表，靠 loginStatus 区分）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/loginLogListPage")
    public Result<PageResult<LoginLogVO>> loginLogListPage(@Valid @RequestBody LogPageQueryDTO query) {
        return Result.success(sysLogService.loginLogListPage(query));
    }

    /**
     * 按模块、操作、对象等条件分页查询业务侧显式写入的审计日志。
     * 用于「日志审计」页的「审计日志」页签，回答谁对哪个业务对象做了什么。
     */
    @Operation(summary = "审计日志分页")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/auditLogListPage")
    public Result<PageResult<AuditLogVO>> auditLogListPage(@Valid @RequestBody LogPageQueryDTO query) {
        return Result.success(sysLogService.auditLogListPage(query));
    }

    /**
     * 取单条审计日志的完整内容，含业务侧写入的详情文本与失败原因。
     * 用于「审计日志」页签行内「详情」弹框。
     */
    @Operation(summary = "审计日志详情")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/auditLogDetail")
    public Result<AuditLogVO> auditLogDetail(@RequestParam Long id) {
        return Result.success(sysLogService.auditLogDetail(id));
    }

    /**
     * 按对象类型、对象标识、字段名分页查询字段级修改记录。
     * 用于「日志审计」页的「字段变更」页签，定位某个业务对象的字段是谁在什么时候改的。
     */
    @Operation(summary = "字段级修改日志分页（哪个字段从什么值改成了什么值）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @PostMapping("/fieldChangeListPage")
    public Result<PageResult<FieldChangeVO>> fieldChangeListPage(@Valid @RequestBody LogPageQueryDTO query) {
        return Result.success(sysLogService.fieldChangeListPage(query));
    }

    /**
     * 按批次号取同一次保存落下的全部字段变更行。
     * 用于「字段变更」页签行内的批次详情：一次保存改了五个字段会拆成五行，按批次才看得全这一刀。
     */
    @Operation(summary = "字段级修改日志同批次明细（一次保存改了哪些字段）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/fieldChangeBatch")
    public Result<List<FieldChangeVO>> fieldChangeBatch(@RequestParam String batchNo) {
        return Result.success(sysLogService.fieldChangeBatch(batchNo));
    }

    /**
     * 汇总操作、登录、审计、字段变更四本账的量，并给出近 24 小时口令爆破嫌疑账号。
     * 用于「日志审计」页顶部的统计卡与安全提醒条。
     */
    @Operation(summary = "四本账统计（含近24小时口令爆破嫌疑账号）")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping("/stat")
    public Result<LogStatVO> stat() {
        return Result.success(sysLogService.stat());
    }

    /**
     * 按当前页签的查询条件导出日志文本，日志类型决定导哪本账，单次上限 5000 行。
     * 用于各日志页签的「导出」动作，走导出专用取数通道，不受页面分页上限约束。
     */
    @Operation(summary = "导出 CSV（logType 1-操作 2-登录 3-审计 4-字段变更，最多 5000 行）")
    @PreAuthorize("hasAuthority('system:log:export')")
    @PostMapping("/exportCsv")
    public Result<String> exportCsv(@Valid @RequestBody LogPageQueryDTO query) {
        return Result.success(sysLogService.exportCsv(query));
    }
}
