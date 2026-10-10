package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.AmpouleReturnDTO;
import com.his.emr.dto.NarcoticRegisterQueryPageDTO;
import com.his.emr.service.NarcoticControlService;
import com.his.emr.vo.BizNarcoticRegisterVO;
import com.his.emr.vo.NarcoticPrecheckVO;
import com.his.emr.vo.NarcoticRegisterCountVO;
import com.his.emr.vo.NarcoticViolationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 麻精药品专册控制器（G10）。
 */
@Tag(name = "麻精药品专册")
@RestController
@RequestMapping("/narcotic")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('pharmacy:narcotic:list', 'pharmacy:dispensing:list')")
public class NarcoticRegisterController {

    private final NarcoticControlService narcoticControlService;

    @Operation(summary = "麻精药品专册分页查询")
    @PostMapping("/listPage")
    public Result<PageResult<BizNarcoticRegisterVO>> listPage(@Valid @RequestBody NarcoticRegisterQueryPageDTO query) {
        return Result.success(narcoticControlService.listPage(query));
    }

    @Operation(summary = "专册计数（总登记 / 待回收空安瓿 / 已回收空安瓿）")
    @GetMapping("/statusCount")
    public Result<NarcoticRegisterCountVO> statusCount() {
        return Result.success(narcoticControlService.statusCount());
    }

    @PreAuthorize("hasAuthority('pharmacy:narcotic:edit')")
    @Operation(summary = "空安瓿回收 / 剩余液销毁登记")
    @PostMapping("/ampouleReturn")
    public Result<BizNarcoticRegisterVO> ampouleReturn(@Valid @RequestBody AmpouleReturnDTO dto) {
        return Result.success(narcoticControlService.updateAmpouleReturn(dto));
    }

    @Operation(summary = "处方麻精限量预检（返回违规清单，空=通过）")
    @GetMapping("/checkPrescription")
    public Result<List<NarcoticViolationVO>> checkPrescription(@RequestParam Long prescriptionId,
                                                               @RequestParam(required = false) String overLimitReason) {
        return Result.success(narcoticControlService.checkPrescription(prescriptionId, overLimitReason));
    }

    @Operation(summary = "处方麻精预检（含管制明细清单与双人复核要求）")
    @GetMapping("/precheck")
    public Result<NarcoticPrecheckVO> precheck(@RequestParam Long prescriptionId,
                                               @RequestParam(required = false) String overLimitReason) {
        return Result.success(narcoticControlService.precheck(prescriptionId, overLimitReason));
    }
}
