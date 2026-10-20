package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.service.ExamDeviceService;
import com.his.medicaltech.service.ExamSlotService;
import com.his.medicaltech.vo.ExamApptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检查设备档位与号源控制器：档位挂接、可开展项目配置、时段生成与看板锁放的接口入口。
 */
@Tag(name = "检查预约-设备与号源")
@RestController
@RequestMapping("/medicaltech/examDevice")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:examAppoint:list')")
public class ExamDeviceController {

    private final ExamDeviceService examDeviceService;
    private final ExamSlotService examSlotService;

    /**
     * 设备档位分页，可按设备类型、科室、状态过滤。
     * 用于「检查设备档位与号源」页的查询卡。
     */
    @Operation(summary = "分页查询预约设备")
    @PostMapping("/listPage")
    public Result<PageResult<ExamApptVO.DeviceVO>> listPage(@Valid @RequestBody ExamApptDTO.DeviceQuery query) {
        return Result.success(examDeviceService.listPage(query));
    }

    /**
     * 可预约设备下拉；指定检查项目时只回能做该项目的设备。
     * 用于检查预约中心占号/改约弹框与号源页的设备筛选。
     */
    @Operation(summary = "设备下拉（itemId 传入时只返回能做该项目的设备）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<ExamApptVO.DeviceSelectListVO>> selectList(@RequestParam(required = false) Integer deviceType,
                                                                  @RequestParam(required = false) Long itemId) {
        return Result.success(examDeviceService.selectList(deviceType, itemId));
    }

    /**
     * 设备台账可选候选，供预约档位挂到实体设备。
     * 用于新增/编辑设备档位弹框的台账下拉。
     */
    @Operation(summary = "设备台账候选（只读挂接 sys_equipment）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/equipment/selectList")
    public Result<List<ExamApptVO.EquipmentSelectListVO>> equipmentOptions() {
        return Result.success(examDeviceService.equipmentOptions());
    }

    /**
     * 检查项目候选下拉，支持关键字限量检索。
     * 用于给设备配置可开展项目时的项目选择。
     */
    @Operation(summary = "检查项目候选下拉（配可开展项目用）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/item/selectList")
    public Result<List<ExamApptVO.ItemSelectListVO>> itemCandidates(@RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) Integer limit) {
        return Result.success(examDeviceService.itemCandidates(keyword, limit));
    }

    /**
     * 按主键取设备档位全字段，含可开展检查项目。
     * 供设备档位编辑链路回显单条配置时取数。
     */
    @Operation(summary = "设备详情（含可开展项目）")
    @GetMapping("/getDetailById")
    public Result<ExamApptVO.DeviceVO> getDetailById(@RequestParam Long deviceId) {
        return Result.success(examDeviceService.getDetail(deviceId));
    }

    /**
     * 新建或修改设备档位（开放时间、粒度、并行台数等），口径变更时出参带提醒文案。
     * 用于「新增/编辑设备档位」弹框保存。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "新增/修改设备档位")
    @PostMapping("/deviceUpsert")
    public Result<ExamApptVO.DeviceVO> deviceUpsert(@Valid @RequestBody ExamApptDTO.DeviceUpsert dto) {
        ExamApptVO.DeviceVO vo = examDeviceService.upsert(dto);
        return TextUtil.hasText(vo.getWarning()) ? Result.success(vo.getWarning(), vo) : Result.success(vo);
    }

    /**
     * 移除设备档位配置，存在未到检的在办预约时服务端拦截。
     * 用于「检查设备档位与号源」页的行删除动作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:delete')")
    @Operation(summary = "删除设备档位（有未完成预约时拒绝）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long deviceId) {
        examDeviceService.deleteById(deviceId);
        return Result.success("设备档位已删除", true);
    }

    /**
     * 列出设备当前配置的可开展检查项目。
     * 用于号源页查看与维护单台设备的项目清单。
     */
    @Operation(summary = "设备可开展项目列表")
    @GetMapping("/itemList")
    public Result<List<ExamApptVO.DeviceItemVO>> itemList(@RequestParam Long deviceId) {
        return Result.success(examDeviceService.itemList(deviceId));
    }

    /**
     * 整表覆盖保存设备的可开展项目，返回保存后的项目数。
     * 用于号源页设备项目配置的保存动作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "保存设备可开展项目（覆盖式）")
    @PostMapping("/itemSave")
    public Result<Integer> itemSave(@Valid @RequestBody ExamApptDTO.DeviceItemSave dto) {
        int n = examDeviceService.saveItems(dto);
        return Result.success("已保存 " + n + " 个可开展项目", n);
    }

    /**
     * 按设备开放口径生成或补齐指定日期的号源格子，已有占号的格子不动。
     * 用于号源页档位保存后的补号动作，以及预约中心占号/改约弹框选格子前的补号。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "生成（补齐）分时段号源")
    @PostMapping("/slotGenerate")
    public Result<ExamApptVO.SlotEnsureVO> slotGenerate(@Valid @RequestBody ExamApptDTO.SlotEnsure dto) {
        return Result.success(examSlotService.ensureSlots(dto));
    }

    /**
     * 按设备与日期区间取号源格子及占用计数。
     * 用于号源页的看板表格与占号/改约弹框的选格网格。
     */
    @Operation(summary = "号源看板")
    @PostMapping("/slotBoard")
    public Result<ExamApptVO.SlotBoardVO> slotBoard(@Valid @RequestBody ExamApptDTO.SlotQuery dto) {
        return Result.success(examSlotService.board(dto));
    }

    /**
     * 人工锁闭或放开某个时段，锁号必须带原因，已有占号的格子不允许锁。
     * 用于号源页看板格子的锁号/放号操作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "锁号/放号")
    @PostMapping("/slotToggle")
    public Result<Boolean> slotToggle(@Valid @RequestBody ExamApptDTO.SlotToggle dto) {
        examSlotService.toggle(dto);
        return Result.success(dto.getStatus() == 0 ? "该时段已锁号" : "该时段已放号", true);
    }

    /**
     * 以预约单为事实重算号源占用计数，纠出漂移的格子。
     * 用于号源页改完档位口径后的人工对账动作。
     */
    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "号源对账（以预约单为事实复算计数）")
    @PostMapping("/slotRecalc")
    public Result<ExamApptVO.SlotRecalcVO> slotRecalc(@Valid @RequestBody ExamApptDTO.SlotRecalc dto) {
        return Result.success(examSlotService.recalc(dto));
    }
}
