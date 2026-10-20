package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.PivasActionDTO;
import com.his.pharmacy.dto.PivasAuditDTO;
import com.his.pharmacy.dto.PivasGenerateDTO;
import com.his.pharmacy.dto.PivasPageQueryDTO;
import com.his.pharmacy.service.PivasService;
import com.his.pharmacy.vo.PivasCandidateVO;
import com.his.pharmacy.vo.PivasStatsVO;
import com.his.pharmacy.vo.PivasVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 静脉用药调配中心控制器：住院静脉用药医嘱从候选入单到核对发放全过程的接口入口。
 */
@Tag(name = "静配中心")
@RestController
@RequestMapping("/pharmacy/pivas")
@RequiredArgsConstructor
public class PivasController {

    private final PivasService pivasService;

    /**
     * 可静配医嘱候选：按病区（可缩小到单名住院患者）与调配日期，筛出住院摆药里给药途径为静脉的医嘱。
     * 用于静配中心工作台生成静配单前的预览，只读，不动库存也不动费用。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:list')")
    @Operation(summary = "可静配医嘱候选（生成前预览）")
    @GetMapping("/candidates")
    public Result<List<PivasCandidateVO>> candidates(@RequestParam Long wardId,
                                                     @RequestParam(required = false) Long admissionId,
                                                     @RequestParam(required = false)
                                                     @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
                                                     LocalDate admixDate) {
        return Result.success(pivasService.candidates(wardId, admissionId, admixDate));
    }

    /**
     * 生成静配单：同一次入院同一天复用已有主单追加明细，重复发起不会开出第二张单，新明细一律待审方。
     * 用于静配中心工作台把候选医嘱落成调配任务单；调配日期落在未来会被拒。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:add')")
    @Operation(summary = "生成静配单（同入院同日复用主单、明细追加，幂等；新明细待审方）")
    @PostMapping("/generate")
    public Result<PivasVO> generate(@Valid @RequestBody PivasGenerateDTO dto) {
        return Result.success("生成成功", pivasService.generate(dto));
    }

    /**
     * 静配单分页：按病区、调配日期、患者姓名、单据状态翻页，行上带患者与明细条数。
     * 用于静配中心工作台的静配单列表查询卡。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:list')")
    @Operation(summary = "静配单分页")
    @PostMapping("/listPage")
    public Result<PageResult<PivasVO>> listPage(@Valid @RequestBody PivasPageQueryDTO dto) {
        return Result.success(pivasService.listPage(dto));
    }

    /**
     * 静配单详情：主单与其下全部明细（含每条的调配进度、排队号、审方人与退回原因）。
     * 用于静配详情弹框，审方、打标签、调配、核对四个动作都在这里逐条推进。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:list')")
    @Operation(summary = "静配单详情（主单+明细）")
    @GetMapping("/getDetailById")
    public Result<PivasVO> getDetailById(@RequestParam Long id) {
        return Result.success(pivasService.getDetailById(id));
    }

    /**
     * 明细审方：药师逐条判定，通过推进到已审方，退回置为已拒配且退回原因必填（该医嘱之后可重新入单）。
     * 用于静配详情弹框的药师审方环节；单下还有待审方明细时不允许打标签。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "审方（通过 1→2 / 退回 1→0，退回原因必填）")
    @PostMapping("/auditItem")
    public Result<PivasVO> auditItem(@Valid @RequestBody PivasAuditDTO dto) {
        return Result.success(Boolean.TRUE.equals(dto.getPass()) ? "审方通过" : "已退回",
                pivasService.auditItem(dto));
    }

    /**
     * 整单打标签排队：把该单下所有已审方明细一次性推进到已排队并按调配日取排队号，标签打印在本步预留。
     * 用于静配详情弹框的「打标签」动作，是审方完成与调配之间的唯一入口。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "打标签排队（整单已审方明细 2→3 取排队号，标签打印预留）")
    @PostMapping("/labelBatch")
    public Result<PivasVO> labelBatch(@Valid @RequestBody PivasActionDTO dto) {
        return Result.success("已排队取号", pivasService.labelBatch(dto));
    }

    /**
     * 单条明细调配完成：已排队的明细推进到已调配并记调配人与时间。
     * 用于静配详情弹框按条登记配液动作；状态只能顺序推进，跳级会被拒。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "调配（3→4）")
    @PostMapping("/compoundItem")
    public Result<PivasVO> compoundItem(@Valid @RequestBody PivasActionDTO dto) {
        return Result.success("调配完成", pivasService.compoundItem(dto));
    }

    /**
     * 单条明细核对发放：已调配的明细推进到已核对发放，是本链终态。
     * 用于静配详情弹框成品出舱前的核对；计费不在本链，仍走住院摆药。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "核对发放（4→5，终态）")
    @PostMapping("/verifyItem")
    public Result<PivasVO> verifyItem(@Valid @RequestBody PivasActionDTO dto) {
        return Result.success("已核对发放", pivasService.verifyItem(dto));
    }

    /**
     * 静配工作量统计：按调配日期（可再加病区）汇总静配单数与各状态明细条数。
     * 用于静配工作台顶部的统计卡；日期不传按当天，病区不传按当前岗位能看到的数据范围收口。
     */
    @PreAuthorize("hasAuthority('pharmacy:pivas:list')")
    @Operation(summary = "统计（按调配日期+可选病区）")
    @GetMapping("/stats")
    public Result<PivasStatsVO> stats(@RequestParam(required = false)
                                      @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
                                      LocalDate admixDate,
                                      @RequestParam(required = false) Long wardId) {
        return Result.success(pivasService.stats(admixDate, wardId));
    }
}
