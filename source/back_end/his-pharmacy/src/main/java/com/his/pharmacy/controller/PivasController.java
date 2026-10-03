package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.PivasActionDTO;
import com.his.pharmacy.dto.PivasAuditDTO;
import com.his.pharmacy.dto.PivasGenerateDTO;
import com.his.pharmacy.dto.PivasQueryPageDTO;
import com.his.pharmacy.service.PivasService;
import com.his.pharmacy.vo.PivasCandidateVO;
import com.his.pharmacy.vo.PivasStatsVO;
import com.his.pharmacy.vo.PivasVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 静脉用药调配中心 PIVAS。
 *
 * <p>链路：候选预览/生成 → 药师审方（通过/退回）→ 打标签排队取号 → 调配 → 成品核对发放。
 * 计费不在本链（仍走住院摆药），本控制器只做调配过程留痕；操作人一律服务端取当前登录人。
 * 按项目规范 @PreAuthorize 全部标到方法（类级注解会罩住未标注方法）。
 */
@Tag(name = "静配中心")
@RestController
@RequestMapping("/pharmacy/pivas")
@RequiredArgsConstructor
public class PivasController {

    private final PivasService pivasService;

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

    @PreAuthorize("hasAuthority('pharmacy:pivas:add')")
    @Operation(summary = "生成静配单（同入院同日复用主单、明细追加，幂等；新明细待审方）")
    @PostMapping("/generate")
    public Result<PivasVO> generate(@Valid @RequestBody PivasGenerateDTO dto) {
        return Result.success("生成成功", pivasService.generate(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:pivas:list')")
    @Operation(summary = "静配单分页")
    @PostMapping("/listPage")
    public Result<PageResult<PivasVO>> listPage(@RequestBody PivasQueryPageDTO dto) {
        return Result.success(pivasService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:pivas:list')")
    @Operation(summary = "静配单详情（主单+明细）")
    @GetMapping("/getDetailById")
    public Result<PivasVO> getDetailById(@RequestParam Long id) {
        return Result.success(pivasService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "审方（通过 1→2 / 退回 1→0，退回原因必填）")
    @PostMapping("/auditItem")
    public Result<PivasVO> auditItem(@Valid @RequestBody PivasAuditDTO dto) {
        return Result.success(Boolean.TRUE.equals(dto.getPass()) ? "审方通过" : "已退回",
                pivasService.auditItem(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "打标签排队（整单已审方明细 2→3 取排队号，标签打印预留）")
    @PostMapping("/labelBatch")
    public Result<PivasVO> labelBatch(@Valid @RequestBody PivasActionDTO dto) {
        return Result.success("已排队取号", pivasService.labelBatch(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "调配（3→4）")
    @PostMapping("/compoundItem")
    public Result<PivasVO> compoundItem(@Valid @RequestBody PivasActionDTO dto) {
        return Result.success("调配完成", pivasService.compoundItem(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:pivas:edit')")
    @Operation(summary = "核对发放（4→5，终态）")
    @PostMapping("/verifyItem")
    public Result<PivasVO> verifyItem(@Valid @RequestBody PivasActionDTO dto) {
        return Result.success("已核对发放", pivasService.verifyItem(dto));
    }

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
