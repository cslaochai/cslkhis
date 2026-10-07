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
 * 检查预约设备与号源接口（URL 前缀 /medicaltech/examDevice）
 */
@Tag(name = "检查预约-设备与号源")
@RestController
@RequestMapping("/medicaltech/examDevice")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:examAppoint:list')")
public class ExamDeviceController {

    private final ExamDeviceService examDeviceService;
    private final ExamSlotService examSlotService;

    @Operation(summary = "分页查询预约设备")
    @PostMapping("/listPage")
    public Result<PageResult<ExamApptVO.DeviceVO>> listPage(@Valid @RequestBody ExamApptDTO.DeviceQuery query) {
        return Result.success(examDeviceService.listPage(query));
    }

    @Operation(summary = "设备下拉（itemId 传入时只返回能做该项目的设备）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<ExamApptVO.DeviceSelectListVO>> selectList(@RequestParam(required = false) Integer deviceType,
                                                                  @RequestParam(required = false) Long itemId) {
        return Result.success(examDeviceService.selectList(deviceType, itemId));
    }

    @Operation(summary = "设备台账候选（只读挂接 sys_equipment）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/equipment/selectList")
    public Result<List<ExamApptVO.EquipmentSelectListVO>> equipmentOptions() {
        return Result.success(examDeviceService.equipmentOptions());
    }

    @Operation(summary = "检查项目候选下拉（配可开展项目用）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/item/selectList")
    public Result<List<ExamApptVO.ItemSelectListVO>> itemCandidates(@RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) Integer limit) {
        return Result.success(examDeviceService.itemCandidates(keyword, limit));
    }

    @Operation(summary = "设备详情（含可开展项目）")
    @GetMapping("/getDetailById")
    public Result<ExamApptVO.DeviceVO> getDetailById(@RequestParam Long deviceId) {
        return Result.success(examDeviceService.getDetail(deviceId));
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "新增/修改设备档位")
    @PostMapping("/deviceUpsert")
    public Result<ExamApptVO.DeviceVO> deviceUpsert(@Valid @RequestBody ExamApptDTO.DeviceUpsert dto) {
        ExamApptVO.DeviceVO vo = examDeviceService.upsert(dto);
        return TextUtil.hasText(vo.getWarning()) ? Result.success(vo.getWarning(), vo) : Result.success(vo);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:delete')")
    @Operation(summary = "删除设备档位（有未完成预约时拒绝）")
    @DeleteMapping("/deleteById")
    public Result<Boolean> deleteById(@RequestParam Long deviceId) {
        examDeviceService.deleteById(deviceId);
        return Result.success("设备档位已删除", true);
    }

    @Operation(summary = "设备可开展项目列表")
    @GetMapping("/itemList")
    public Result<List<ExamApptVO.DeviceItemVO>> itemList(@RequestParam Long deviceId) {
        return Result.success(examDeviceService.itemList(deviceId));
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "保存设备可开展项目（覆盖式）")
    @PostMapping("/itemSave")
    public Result<Integer> itemSave(@Valid @RequestBody ExamApptDTO.DeviceItemSave dto) {
        int n = examDeviceService.saveItems(dto);
        return Result.success("已保存 " + n + " 个可开展项目", n);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:add')")
    @Operation(summary = "生成（补齐）分时段号源")
    @PostMapping("/slotGenerate")
    public Result<ExamApptVO.SlotEnsureVO> slotGenerate(@Valid @RequestBody ExamApptDTO.SlotEnsure dto) {
        return Result.success(examSlotService.ensureSlots(dto));
    }

    @Operation(summary = "号源看板")
    @PostMapping("/slotBoard")
    public Result<ExamApptVO.SlotBoardVO> slotBoard(@Valid @RequestBody ExamApptDTO.SlotQuery dto) {
        return Result.success(examSlotService.board(dto));
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "锁号/放号")
    @PostMapping("/slotToggle")
    public Result<Boolean> slotToggle(@Valid @RequestBody ExamApptDTO.SlotToggle dto) {
        examSlotService.toggle(dto);
        return Result.success(dto.getStatus() == 0 ? "该时段已锁号" : "该时段已放号", true);
    }

    @PreAuthorize("hasAuthority('medtech:examAppoint:edit')")
    @Operation(summary = "号源对账（以预约单为事实复算计数）")
    @PostMapping("/slotRecalc")
    public Result<ExamApptVO.SlotRecalcVO> slotRecalc(@Valid @RequestBody ExamApptDTO.SlotRecalc dto) {
        return Result.success(examSlotService.recalc(dto));
    }
}
