package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.WardDispenseActionDTO;
import com.his.pharmacy.dto.WardDispenseGenerateDTO;
import com.his.pharmacy.dto.WardDispensePageQueryDTO;
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
 */
@Tag(name = "住院摆药")
@RestController
@RequestMapping("/pharmacy/wardDispense")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:wardDispense:list')")
public class WardDispenseController {

    private final WardDispenseService wardDispenseService;

    /**
     * 列出指定病区（可缩到单名患者、指定摆药日期）当天可摆药的长期医嘱行。
     * 用于住院摆药页「生成摆药单」区块的生成前预览，先看有没有可摆的再下单。
     */
    @Operation(summary = "可摆药医嘱候选（生成前预览）")
    @GetMapping("/candidates")
    public Result<List<WardDispenseCandidateVO>> candidates(@RequestParam Long wardId,
                                                            @RequestParam(required = false) Long admissionId,
                                                            @RequestParam(required = false)
                                                            @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
                                                            LocalDate dispenseDate) {
        return Result.success(wardDispenseService.candidates(wardId, admissionId, dispenseDate));
    }

    /**
     * 按病区把可摆药医嘱展开成摆药明细，同入院同日复用主单只追加明细，重复点不产生第二张单。
     * 用于住院摆药页每日按病区批量生成摆药单的动作，此时只建单不动药房库存。
     */
    @PreAuthorize("hasAuthority('pharmacy:wardDispense:add')")
    @Operation(summary = "生成摆药单（同入院同日复用主单、明细追加，幂等）")
    @PostMapping("/generate")
    public Result<WardDispenseVO> generate(@Valid @RequestBody WardDispenseGenerateDTO dto) {
        return Result.success("生成成功", wardDispenseService.generate(dto));
    }

    /**
     * 摆药单分页，可按病区、摆药日期与患者姓名筛。
     * 用于住院摆药页的摆药单列表，药师从这里进单逐条配药。
     */
    @Operation(summary = "摆药单分页")
    @PostMapping("/listPage")
    public Result<PageResult<WardDispenseVO>> listPage(@Valid @RequestBody WardDispensePageQueryDTO dto) {
        return Result.success(wardDispenseService.listPage(dto));
    }

    /**
     * 取摆药主单与其全部明细行（含每行的配药、核对、退药状态）。
     * 用于住院摆药页点开的主单详情弹框，配药/核对/退药三个动作都在这个弹框里发起并回刷本接口。
     */
    @Operation(summary = "摆药单详情（主单+明细）")
    @GetMapping("/getDetailById")
    public Result<WardDispenseVO> getDetailById(@RequestParam Long id) {
        return Result.success(wardDispenseService.getDetailById(id));
    }

    /**
     * 对一条摆药明细执行配药：按药房侧批次先到期先出扣减库存，并把这笔药费记进患者账单。
     * 用于摆药单详情弹框的配药按钮，这是本单唯一真正动药房库存的一步。
     */
    @PreAuthorize("hasAuthority('pharmacy:wardDispense:edit')")
    @Operation(summary = "配药（FEFO 扣库存+计费，1→2）")
    @PostMapping("/dispenseItem")
    public Result<WardDispenseVO> dispenseItem(@Valid @RequestBody WardDispenseActionDTO dto) {
        return Result.success("配药成功", wardDispenseService.dispenseItem(dto));
    }

    /**
     * 病区护士对已配药的明细做接收核对，只推进状态不碰库存与费用。
     * 用于摆药单详情弹框的核对按钮，药送到病区后由护士逐行确认。
     */
    @PreAuthorize("hasAuthority('pharmacy:wardDispense:edit')")
    @Operation(summary = "病区核对（2→3）")
    @PostMapping("/checkItem")
    public Result<WardDispenseVO> checkItem(@Valid @RequestBody WardDispenseActionDTO dto) {
        return Result.success("核对成功", wardDispenseService.checkItem(dto));
    }

    /**
     * 病区把已配药（或已核对）的明细退回：药回药房库位并记退药回库流水，同时红冲那笔记账，明细进终态不可逆。
     * 用于摆药单详情弹框的退药按钮，需填退药原因；这一步是收费侧退款的前置事实，不退药就退不了钱。
     */
    @PreAuthorize("hasAuthority('pharmacy:wardDispense:edit')")
    @Operation(summary = "退药（回库+负冲账，2/3→4，终态不可逆）")
    @PostMapping("/returnItem")
    public Result<WardDispenseVO> returnItem(@Valid @RequestBody WardDispenseActionDTO dto) {
        return Result.success("退药成功", wardDispenseService.returnItem(dto));
    }

    /**
     * 按摆药日期（可再限定病区）汇总各明细状态的件数，用于判断当天还有多少药没配、没核对。
     * 用于住院摆药页顶部的统计卡，与列表同条件刷新。
     */
    @Operation(summary = "统计（按日期+可选病区）")
    @GetMapping("/stats")
    public Result<WardDispenseStatsVO> stats(@RequestParam(required = false)
                                             @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
                                             LocalDate dispenseDate,
                                             @RequestParam(required = false) Long wardId) {
        return Result.success(wardDispenseService.stats(dispenseDate, wardId));
    }
}
