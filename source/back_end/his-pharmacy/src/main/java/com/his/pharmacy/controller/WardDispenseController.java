package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.WardDispenseActionDTO;
import com.his.pharmacy.dto.WardDispenseGenerateDTO;
import com.his.pharmacy.dto.WardDispenseQueryPageDTO;
import com.his.pharmacy.service.WardDispenseService;
import com.his.pharmacy.vo.WardDispenseCandidateVO;
import com.his.pharmacy.vo.WardDispenseStatsVO;
import com.his.pharmacy.vo.WardDispenseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 住院摆药（G13）。
 *
 * <p>链路：候选预览/生成 → 药房 FEFO 配药+计费 → 病区核对 → 退药回库+负冲账。
 * 操作人一律服务端取当前登录人；创建类接口回 VO 不回裸 id。
 */
@Tag(name = "住院摆药")
@RestController
@RequestMapping("/pharmacy/wardDispense")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:wardDispense:list')")
public class WardDispenseController {

    private final WardDispenseService wardDispenseService;

    @Operation(summary = "可摆药医嘱候选（生成前预览）")
    @GetMapping("/candidates")
    public Result<List<WardDispenseCandidateVO>> candidates(@RequestParam Long wardId,
                                                            @RequestParam(required = false) Long admissionId,
                                                            @RequestParam(required = false)
                                                            @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
                                                            LocalDate dispenseDate) {
        return Result.success(wardDispenseService.candidates(wardId, admissionId, dispenseDate));
    }

    @PreAuthorize("hasAuthority('pharmacy:wardDispense:add')")
    @Operation(summary = "生成摆药单（同入院同日复用主单、明细追加，幂等）")
    @PostMapping("/generate")
    public Result<WardDispenseVO> generate(@Valid @RequestBody WardDispenseGenerateDTO dto) {
        return Result.success("生成成功", wardDispenseService.generate(dto));
    }

    @Operation(summary = "摆药单分页")
    @PostMapping("/listPage")
    public Result<PageResult<WardDispenseVO>> listPage(@Valid @RequestBody WardDispenseQueryPageDTO dto) {
        return Result.success(wardDispenseService.listPage(dto));
    }

    @Operation(summary = "摆药单详情（主单+明细）")
    @GetMapping("/getDetailById")
    public Result<WardDispenseVO> getDetailById(@RequestParam Long id) {
        return Result.success(wardDispenseService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('pharmacy:wardDispense:edit')")
    @Operation(summary = "配药（FEFO 扣库存+计费，1→2）")
    @PostMapping("/dispenseItem")
    public Result<WardDispenseVO> dispenseItem(@Valid @RequestBody WardDispenseActionDTO dto) {
        return Result.success("配药成功", wardDispenseService.dispenseItem(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:wardDispense:edit')")
    @Operation(summary = "病区核对（2→3）")
    @PostMapping("/checkItem")
    public Result<WardDispenseVO> checkItem(@Valid @RequestBody WardDispenseActionDTO dto) {
        return Result.success("核对成功", wardDispenseService.checkItem(dto));
    }

    @PreAuthorize("hasAuthority('pharmacy:wardDispense:edit')")
    @Operation(summary = "退药（回库+负冲账，2/3→4，终态不可逆）")
    @PostMapping("/returnItem")
    public Result<WardDispenseVO> returnItem(@Valid @RequestBody WardDispenseActionDTO dto) {
        return Result.success("退药成功", wardDispenseService.returnItem(dto));
    }

    @Operation(summary = "统计（按日期+可选病区）")
    @GetMapping("/stats")
    public Result<WardDispenseStatsVO> stats(@RequestParam(required = false)
                                             @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
                                             LocalDate dispenseDate,
                                             @RequestParam(required = false) Long wardId) {
        return Result.success(wardDispenseService.stats(dispenseDate, wardId));
    }
}
