package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.ArchiveBorrowApplyDTO;
import com.his.emr.dto.ArchiveBorrowAuditDTO;
import com.his.emr.dto.ArchiveBorrowQueryPageDTO;
import com.his.emr.service.ArchiveBorrowService;
import com.his.emr.vo.ArchiveBorrowStatsVO;
import com.his.emr.vo.ArchiveBorrowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病案借阅/复印控制器
 */
@Tag(name = "病案借阅复印")
@RestController
@RequestMapping("/charge/archiveBorrow")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('emr:archiveBorrow:list')")
public class ArchiveBorrowController {

    private final ArchiveBorrowService borrowService;

    @Operation(summary = "分页查询借阅/复印单")
    @PostMapping("/listPage")
    public Result<PageResult<ArchiveBorrowVO>> listPage(@Valid @RequestBody ArchiveBorrowQueryPageDTO queryDTO) {
        return Result.success(borrowService.page(queryDTO));
    }

    @Operation(summary = "借阅/复印单详情")
    @GetMapping("/getDetailById")
    public Result<ArchiveBorrowVO> getDetailById(@RequestParam Long id) {
        return Result.success(borrowService.getDetailById(id));
    }

    @Operation(summary = "工作台统计（待审核/已借出/超期未还/已归还）")
    @GetMapping("/stats")
    public Result<ArchiveBorrowStatsVO> stats() {
        return Result.success(borrowService.stats());
    }

    @PreAuthorize("hasAuthority('emr:archiveBorrow:add')")
    @Operation(summary = "申请借阅/复印")
    @PostMapping("/apply")
    public Result<ArchiveBorrowVO> apply(@Valid @RequestBody ArchiveBorrowApplyDTO dto) {
        Long id = borrowService.apply(dto);
        // 创建必须回 VO（Result<Long> 只装 count）：前端拿到完整单据可直接展示
        return Result.success("申请成功", borrowService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('emr:archiveBorrow:edit')")
    @Operation(summary = "审核（通过：借阅→已借出/复印→已复印；拒绝→已拒绝）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody ArchiveBorrowAuditDTO dto) {
        borrowService.audit(dto);
        return Result.success("审核完成", null);
    }

    @PreAuthorize("hasAuthority('emr:archiveBorrow:edit')")
    @Operation(summary = "归还（借阅单 2→3）")
    @PostMapping("/giveBack")
    public Result<Void> giveBack(@RequestParam Long id) {
        borrowService.giveBack(id);
        return Result.success("归还成功", null);
    }

    @PreAuthorize("hasAuthority('emr:archiveBorrow:delete')")
    @Operation(summary = "删除待审核单（仅申请人本人）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        borrowService.deleteById(id);
        return Result.success("删除成功", null);
    }

    @PreAuthorize("hasAuthority('emr:archiveBorrow:edit')")
    @Operation(summary = "手动补跑借阅超期提醒")
    @PostMapping("/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        int sent = borrowService.notifyOverdue();
        return Result.success("已发送 " + sent + " 条借阅超期提醒", sent);
    }
}
