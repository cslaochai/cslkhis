package com.mis.controller;

import com.mis.common.base.PageVO;
import com.mis.common.base.Result;
import com.mis.dto.InspectionFinishDTO;
import com.mis.dto.InspectionItemUpsertDTO;
import com.mis.dto.InspectionQueryDTO;
import com.mis.dto.InspectionUpsertDTO;
import com.mis.entity.MssInspection;
import com.mis.service.InspectionService;
import com.mis.vo.ActionVO;
import com.mis.vo.InspectionDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 基金监管 - 飞检任务
 */
@RestController
@RequestMapping("/supervise/inspection")
@RequiredArgsConstructor
public class SuperviseInspectionController {

    private final InspectionService inspectionService;

    @GetMapping("/listPage")
    public Result<PageVO<MssInspection>> listPage(InspectionQueryDTO query) {
        return Result.success(inspectionService.inspectionPage(query));
    }

    @PostMapping("/upsert")
    public Result<ActionVO> upsert(@RequestBody @Valid InspectionUpsertDTO dto) {
        return Result.success(inspectionService.upsert(dto));
    }

    @PostMapping("/start")
    public Result<ActionVO> start(@RequestBody @Valid com.mis.dto.IdDTO dto) {
        return Result.success(inspectionService.start(Long.valueOf(dto.getId())));
    }

    @PostMapping("/finish")
    public Result<ActionVO> finish(@RequestBody @Valid InspectionFinishDTO dto) {
        return Result.success(inspectionService.finish(dto));
    }

    @GetMapping("/getDetailById")
    public Result<InspectionDetailVO> getDetail(@RequestParam Long id) {
        return Result.success(inspectionService.getDetail(id));
    }

    @PostMapping("/item/add")
    public Result<ActionVO> addItem(@RequestBody @Valid InspectionItemUpsertDTO dto) {
        return Result.success(inspectionService.addItem(dto));
    }

    @PostMapping("/item/delete")
    public Result<ActionVO> deleteItem(@RequestBody @Valid com.mis.dto.IdDTO dto) {
        return Result.success(inspectionService.deleteItem(Long.valueOf(dto.getId())));
    }

    @PostMapping("/toRecover")
    public Result<ActionVO> toRecover(@RequestBody @Valid com.mis.dto.IdDTO dto) {
        return Result.success(inspectionService.toRecover(Long.valueOf(dto.getId())));
    }
}
