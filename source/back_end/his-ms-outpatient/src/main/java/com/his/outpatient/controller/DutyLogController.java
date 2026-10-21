package com.his.outpatient.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.outpatient.dto.DutyLogHandoverDTO;
import com.his.outpatient.dto.DutyLogPageQueryDTO;
import com.his.outpatient.dto.DutyLogUpsertDTO;
import com.his.outpatient.service.DutyLogService;
import com.his.outpatient.vo.DutyLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 值班日志 / 交班本（sql/170，挂在菜单 806「总值班排班」的第二个 Tab）。
 */
@Tag(name = "全院总值班值班日志")
@RestController
@RequestMapping("/system/dutyLog")
@RequiredArgsConstructor
public class DutyLogController {

    private final DutyLogService dutyLogService;

    /**
     * 按日期、类别等条件分页查询总值班日志。
     * 用于「总值班日志（交班本）」页的日志表格查询卡。
     */
    @Operation(summary = "分页查询值班日志")
    @PreAuthorize("hasAuthority('org:duty:list')")
    @PostMapping("/listPage")
    public Result<PageResult<DutyLogVO>> listPage(@Valid @RequestBody DutyLogPageQueryDTO queryDTO) {
        return Result.success(dutyLogService.listPage(queryDTO));
    }

    /**
     * 取交班对象是当前登录人、尚未签收的遗留事项。
     * 用于该页顶部的「待我签收」提醒区：接班人一进页面就看到上一班压下来的事，不必自己翻列表撞运气。
     */
    @Operation(summary = "待我签收的遗留事项（交班对象 = 当前登录人）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/pendingMine")
    public Result<List<DutyLogVO>> pendingMine() {
        return Result.success(dutyLogService.pendingMine());
    }

    /**
     * 登记或修改一条值班日志，传主键即改、不传即建，返回日志主键。
     * 用于该页的日志登记/编辑弹框保存，记录值班事件、遗留事项与巡查结果。
     */
    @Operation(summary = "登记或修改值班日志（值班人留空 = 当前总值班）")
    @PreAuthorize("hasAuthority('org:duty:log:edit')")
    @PostMapping("/dutyLogUpsert")
    public Result<Long> dutyLogUpsert(@RequestBody @Valid DutyLogUpsertDTO upsertDTO) {
        Long id = dutyLogService.upsert(upsertDTO);
        return Result.success(upsertDTO.getId() == null ? "登记成功" : "修改成功", id);
    }

    /**
     * 把本班未闭环的事项交给接班人，交班后进入待签收状态。
     * 用于该页的交班动作：接班人留空时按排班顺延给下一班总值班。
     */
    @Operation(summary = "交班（接班人留空 = 下一班总值班）")
    @PreAuthorize("hasAuthority('org:duty:log:handover')")
    @PostMapping("/dutyLogHandover")
    public Result<Void> dutyLogHandover(@RequestBody @Valid DutyLogHandoverDTO dto) {
        dutyLogService.handover(dto);
        return Result.success("已交班，接班人会收到待办", null);
    }

    /**
     * 接班人签收交班过来的遗留事项，闭环这条交班。
     * 用于待签收提醒区或日志行的签收动作，只有被交班人本人签得掉。
     */
    @Operation(summary = "签收（只有接班人本人能签收）")
    @PreAuthorize("hasAuthority('org:duty:log:handover')")
    @PostMapping("/dutyLogAck")
    public Result<Void> dutyLogAck(@RequestParam Long id) {
        dutyLogService.ack(id);
        return Result.success("已签收，交班闭环", null);
    }

    /**
     * 按主键删除一条值班日志。
     * 用于该页日志行的删除确认；已经签收闭环的日志删不掉，只能留档。
     */
    @Operation(summary = "删除值班日志（已签收的不能删）")
    @PreAuthorize("hasAuthority('org:duty:log:delete')")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        dutyLogService.deleteById(id);
        return Result.success("删除成功", null);
    }
}
