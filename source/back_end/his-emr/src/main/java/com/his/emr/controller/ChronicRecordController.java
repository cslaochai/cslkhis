package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.ChronicCancelDTO;
import com.his.emr.dto.ChronicQueryPageDTO;
import com.his.emr.dto.ChronicUpsertDTO;
import com.his.emr.service.ChronicRecordService;
import com.his.emr.vo.ChronicRecordListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 慢病建档/认定
 */
@Tag(name = "慢病建档/认定")
@RestController
@RequestMapping("/chronic")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:chronicRecord:list')")
public class ChronicRecordController {

    private final ChronicRecordService chronicRecordService;

    @Operation(summary = "慢病建档（建档即认定）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('opd:chronicRecord:add')")
    public Result<ChronicRecordListVO> upsert(@RequestBody @Valid ChronicUpsertDTO dto) {
        return Result.success(chronicRecordService.upsert(dto));
    }

    @Operation(summary = "慢病档案作废（单向：1→2）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('opd:chronicRecord:cancel')")
    public Result<Void> cancel(@RequestBody @Valid ChronicCancelDTO dto) {
        chronicRecordService.cancel(dto);
        return Result.success(null);
    }

    @Operation(summary = "慢病档案分页（医生站）")
    @PostMapping("/listPage")
    public Result<PageResult<ChronicRecordListVO>> listPage(@Valid @RequestBody ChronicQueryPageDTO dto) {
        return Result.success(chronicRecordService.listPage(dto));
    }

    @Operation(summary = "患者的有效慢病档案（长处方资格判定用）")
    @GetMapping("/activeByPatient")
    public Result<List<ChronicRecordListVO>> activeByPatient(@RequestParam Long patientId) {
        return Result.success(chronicRecordService.activeList(patientId));
    }
}
