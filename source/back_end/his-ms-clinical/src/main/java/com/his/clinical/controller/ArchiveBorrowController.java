package com.his.clinical.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.clinical.dto.ArchiveBorrowApplyDTO;
import com.his.clinical.dto.ArchiveBorrowAuditDTO;
import com.his.clinical.dto.ArchiveBorrowPageQueryDTO;
import com.his.clinical.service.ArchiveBorrowService;
import com.his.clinical.vo.ArchiveBorrowStatsVO;
import com.his.clinical.vo.ArchiveBorrowVO;
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

    private final ArchiveBorrowService archiveBorrowService;

    /**
     * 借阅/复印单分页：按用途、单据状态、单号或患者关键字筛出借出与复印台账。
     * 用于「病案借阅复印」页查询卡下方的单据表格。
     */
    @Operation(summary = "分页查询借阅/复印单")
    @PostMapping("/listPage")
    public Result<PageResult<ArchiveBorrowVO>> listPage(@Valid @RequestBody ArchiveBorrowPageQueryDTO queryDTO) {
        return Result.success(archiveBorrowService.page(queryDTO));
    }

    /**
     * 单张借阅/复印单详情：申请事由、审核结论与归还情况。
     * 用于列表行点开详情弹框时按单据编号取数。
     */
    @Operation(summary = "借阅/复印单详情")
    @GetMapping("/getDetailById")
    public Result<ArchiveBorrowVO> getDetailById(@RequestParam Long id) {
        return Result.success(archiveBorrowService.getDetailById(id));
    }

    /**
     * 病案室工作台汇总：待审核、已借出、超期未还、已归还四类单量。
     * 用于「病案借阅复印」页顶部统计卡。
     */
    @Operation(summary = "工作台统计（待审核/已借出/超期未还/已归还）")
    @GetMapping("/stats")
    public Result<ArchiveBorrowStatsVO> stats() {
        return Result.success(archiveBorrowService.stats());
    }

    /**
     * 递交借阅或复印申请：生成一张待审核单据并回完整单据。
     * 用于「病案借阅复印」页申请弹框保存时提交。
     */
    @PreAuthorize("hasAuthority('emr:archiveBorrow:add')")
    @Operation(summary = "申请借阅/复印")
    @PostMapping("/apply")
    public Result<ArchiveBorrowVO> apply(@Valid @RequestBody ArchiveBorrowApplyDTO dto) {
        Long id = archiveBorrowService.apply(dto);
        // 创建必须回 VO（Result<Long> 只装 count）：前端拿到完整单据可直接展示
        return Result.success("申请成功", archiveBorrowService.getDetailById(id));
    }

    /**
     * 审核借阅/复印申请：通过后按申请用途分别置为已借出或已复印，拒绝则直接终态。
     * 用于病案室管理员在列表上对一张待审核单据放行或驳回。
     */
    @PreAuthorize("hasAuthority('emr:archiveBorrow:edit')")
    @Operation(summary = "审核（通过：借阅→已借出/复印→已复印；拒绝→已拒绝）")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody ArchiveBorrowAuditDTO dto) {
        archiveBorrowService.audit(dto);
        return Result.success("审核完成", null);
    }

    /**
     * 借阅归还登记：把已借出的单据结为已归还，复印单没有这一步。
     * 用于病案室收到还回的纸质病历后在列表行确认。
     */
    @PreAuthorize("hasAuthority('emr:archiveBorrow:edit')")
    @Operation(summary = "归还（借阅单 2→3）")
    @PostMapping("/giveBack")
    public Result<Void> giveBack(@RequestParam Long id) {
        archiveBorrowService.giveBack(id);
        return Result.success("归还成功", null);
    }

    /**
     * 撤回申请：删除还停在待审核的单据，且只允许申请人本人操作。
     * 用于列表行的删除按钮，申请填错或不再需要时收回。
     */
    @PreAuthorize("hasAuthority('emr:archiveBorrow:delete')")
    @Operation(summary = "删除待审核单（仅申请人本人）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        archiveBorrowService.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 借阅超期提醒补跑：给应还日期已过仍未归还的申请人各发一条提醒，返回发送条数。
     * 超期提醒正常由每日早间定时任务下发，本入口供病案室在列表上手动补一次。
     */
    @PreAuthorize("hasAuthority('emr:archiveBorrow:edit')")
    @Operation(summary = "手动补跑借阅超期提醒")
    @PostMapping("/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        int sent = archiveBorrowService.notifyOverdue();
        return Result.success("已发送 " + sent + " 条借阅超期提醒", sent);
    }
}
