package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.InfusionRoomDTO;
import com.his.medicaltech.service.InfusionRoomService;
import com.his.medicaltech.vo.InfusionRoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门诊输液室（M10）：座位 → 入座 → 皮试（≥15 分钟观察窗）→ 开始（滴速）
 */
@Tag(name = "门诊输液室")
@RestController
@RequestMapping("/medicaltech/infusionRoom")
@RequiredArgsConstructor
public class InfusionRoomController {

    private final InfusionRoomService infusionRoomService;

    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "今日看板（座位图 + 各状态计数）")
    @GetMapping("/board")
    public Result<InfusionRoomVO.Board> board() {
        return Result.success(infusionRoomService.board());
    }

    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "座位图（含占用输液单摘要）")
    @GetMapping("/seats")
    public Result<List<InfusionRoomVO.Seat>> seats() {
        return Result.success(infusionRoomService.seats());
    }

    @PreAuthorize("hasAuthority('medtech:infusion:add')")
    @Operation(summary = "座位新增/修改（占用中不可改）")
    @PostMapping("/seatUpsert")
    public Result<InfusionRoomVO.Seat> seatUpsert(@Valid @RequestBody InfusionRoomDTO.SeatUpsert dto) {
        return Result.success(infusionRoomService.seatUpsert(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "入座（建输液单 + 占座；needSkinTest=1 先皮试）")
    @PostMapping("/admit")
    public Result<InfusionRoomVO.Infusion> admit(@Valid @RequestBody InfusionRoomDTO.Admit dto) {
        return Result.success(infusionRoomService.admit(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "打皮试（输液单需处于待皮试）")
    @PostMapping("/skinTest")
    public Result<InfusionRoomVO.Infusion> skinTest(@Valid @RequestBody InfusionRoomDTO.SkinTestCreate dto) {
        return Result.success(infusionRoomService.skinTest(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "皮试判读（观察不足15分钟拒绝；阳性自动取消输液单并释放座位）")
    @PostMapping("/skinTestResult")
    public Result<InfusionRoomVO.Infusion> skinTestResult(@Valid @RequestBody InfusionRoomDTO.SkinTestResult dto) {
        return Result.success(infusionRoomService.skinTestResult(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "开始输注（待输注 + 皮试阴性；记录起始滴速）")
    @PostMapping("/start")
    public Result<InfusionRoomVO.Infusion> start(@Valid @RequestBody InfusionRoomDTO.Start dto) {
        return Result.success(infusionRoomService.start(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "巡视（输液中；滴速/余量/备注）")
    @PostMapping("/round")
    public Result<InfusionRoomVO.Round> round(@Valid @RequestBody InfusionRoomDTO.Round dto) {
        return Result.success(infusionRoomService.round(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "结束输注（adverseFlag=1 时描述必填；释放座位）")
    @PostMapping("/finish")
    public Result<InfusionRoomVO.Infusion> finish(@Valid @RequestBody InfusionRoomDTO.Finish dto) {
        return Result.success(infusionRoomService.finish(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "取消（非终态；必填原因；释放座位）")
    @PostMapping("/cancel")
    public Result<InfusionRoomVO.Infusion> cancel(@Valid @RequestBody InfusionRoomDTO.Cancel dto) {
        return Result.success(infusionRoomService.cancel(dto));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "今日输液单分页")
    @PostMapping("/listPage")
    public Result<PageResult<InfusionRoomVO.Infusion>> listPage(@Valid @RequestBody InfusionRoomDTO.InfusionQuery query) {
        return Result.success(infusionRoomService.listPage(query));
    }

    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "某输液单的巡视记录（时间升序）")
    @GetMapping("/rounds")
    public Result<List<InfusionRoomVO.Round>> rounds(@RequestParam Long infusionId) {
        return Result.success(infusionRoomService.rounds(infusionId));
    }
}
