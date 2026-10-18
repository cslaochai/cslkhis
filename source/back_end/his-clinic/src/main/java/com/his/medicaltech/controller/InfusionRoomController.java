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
 * 门诊输液站控制器：门诊输液室（M10）的座位 → 入座 → 皮试（≥15 分钟观察窗）
 * → 开始（滴速）→ 巡视 → 结束这条护理过程链的接口入口。
 */
@Tag(name = "门诊输液室")
@RestController
@RequestMapping("/medicaltech/infusionRoom")
@RequiredArgsConstructor
public class InfusionRoomController {

    private final InfusionRoomService infusionRoomService;

    /**
     * 今日输液看板：座位图与各状态计数一次取回。
     * 用于门诊输液站页顶部统计卡与座位图。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "今日看板（座位图 + 各状态计数）")
    @GetMapping("/board")
    public Result<InfusionRoomVO.Board> board() {
        return Result.success(infusionRoomService.board());
    }

    /**
     * 座位图：全部输液座位及占用它们的输液单摘要。
     * 供入座环节挑选空位时单独取座位清单。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "座位图（含占用输液单摘要）")
    @GetMapping("/seats")
    public Result<List<InfusionRoomVO.Seat>> seats() {
        return Result.success(infusionRoomService.seats());
    }

    /**
     * 新增或修改输液座位，已被输液单占用的座位不允许改动。
     * 用于门诊输液站页座位图上的新增与调整座位。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:add')")
    @Operation(summary = "座位新增/修改（占用中不可改）")
    @PostMapping("/seatUpsert")
    public Result<InfusionRoomVO.Seat> seatUpsert(@Valid @RequestBody InfusionRoomDTO.SeatUpsertDTO dto) {
        return Result.success(infusionRoomService.seatUpsert(dto));
    }

    /**
     * 患者入座：建立输液单并占用座位，needSkinTest=1 时先落在待皮试。
     * 用于门诊输液站页的治疗执行单入科登记。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "入座（建输液单 + 占座；needSkinTest=1 先皮试）")
    @PostMapping("/admit")
    public Result<InfusionRoomVO.Infusion> admit(@Valid @RequestBody InfusionRoomDTO.AdmitDTO dto) {
        return Result.success(infusionRoomService.admit(dto));
    }

    /**
     * 为待皮试的输液单执行皮试，登记皮试时刻起算观察窗。
     * 用于门诊输液站页待皮试患者的「打皮试」。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "打皮试（输液单需处于待皮试）")
    @PostMapping("/skinTest")
    public Result<InfusionRoomVO.Infusion> skinTest(@Valid @RequestBody InfusionRoomDTO.SkinTestCreate dto) {
        return Result.success(infusionRoomService.skinTest(dto));
    }

    /**
     * 判读皮试结果：观察不足 15 分钟拒绝，阳性自动取消输液单并释放座位。
     * 用于门诊输液站页打皮试后的「结果判读」。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "皮试判读（观察不足15分钟拒绝；阳性自动取消输液单并释放座位）")
    @PostMapping("/skinTestResult")
    public Result<InfusionRoomVO.Infusion> skinTestResult(@Valid @RequestBody InfusionRoomDTO.SkinTestResult dto) {
        return Result.success(infusionRoomService.skinTestResult(dto));
    }

    /**
     * 开始输注：待输注且皮试阴性方可执行，同时记录起始滴速。
     * 用于门诊输液站页座位上的「开始输注」。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "开始输注（待输注 + 皮试阴性；记录起始滴速）")
    @PostMapping("/start")
    public Result<InfusionRoomVO.Infusion> start(@Valid @RequestBody InfusionRoomDTO.Start dto) {
        return Result.success(infusionRoomService.start(dto));
    }

    /**
     * 记一次输液巡视：滴速、余量与巡视备注。
     * 用于门诊输液站页输液中患者的巡视登记。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "巡视（输液中；滴速/余量/备注）")
    @PostMapping("/round")
    public Result<InfusionRoomVO.Round> round(@Valid @RequestBody InfusionRoomDTO.Round dto) {
        return Result.success(infusionRoomService.round(dto));
    }

    /**
     * 结束输注并释放座位，adverseFlag=1 时不良反应描述必填。
     * 用于门诊输液站页拔针收尾时的完成登记。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "结束输注（adverseFlag=1 时描述必填；释放座位）")
    @PostMapping("/finish")
    public Result<InfusionRoomVO.Infusion> finish(@Valid @RequestBody InfusionRoomDTO.Finish dto) {
        return Result.success(infusionRoomService.finish(dto));
    }

    /**
     * 取消输液单：非终态可取消、必填原因，同时释放座位。
     * 用于门诊输液站页患者停液或转走时的作废。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:edit')")
    @Operation(summary = "取消（非终态；必填原因；释放座位）")
    @PostMapping("/cancel")
    public Result<InfusionRoomVO.Infusion> cancel(@Valid @RequestBody InfusionRoomDTO.Cancel dto) {
        return Result.success(infusionRoomService.cancel(dto));
    }

    /**
     * 今日输液单分页。
     * 用于门诊输液站页输液单列表的查询卡。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "今日输液单分页")
    @PostMapping("/listPage")
    public Result<PageResult<InfusionRoomVO.Infusion>> listPage(@Valid @RequestBody InfusionRoomDTO.InfusionQuery query) {
        return Result.success(infusionRoomService.listPage(query));
    }

    /**
     * 取某张输液单的全部巡视记录，按时间升序。
     * 用于门诊输液站页展开单条巡视轨迹的弹框。
     */
    @PreAuthorize("hasAuthority('medtech:infusion:list')")
    @Operation(summary = "某输液单的巡视记录（时间升序）")
    @GetMapping("/rounds")
    public Result<List<InfusionRoomVO.Round>> rounds(@RequestParam Long infusionId) {
        return Result.success(infusionRoomService.rounds(infusionId));
    }
}
