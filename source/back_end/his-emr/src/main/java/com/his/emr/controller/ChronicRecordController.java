package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.ChronicCancelDTO;
import com.his.emr.dto.ChronicPageQueryDTO;
import com.his.emr.dto.ChronicUpsertDTO;
import com.his.emr.service.ChronicRecordService;
import com.his.spi.vo.ChronicRecordListVO;
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

    /**
     * 慢病建档：建档即完成认定，同一患者的同一病种只保留一条有效档案。
     * 用于「慢病建档与认定」页新增弹框保存时提交。
     */
    @Operation(summary = "慢病建档（建档即认定）")
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('opd:chronicRecord:add')")
    public Result<ChronicRecordListVO> upsert(@RequestBody @Valid ChronicUpsertDTO dto) {
        return Result.success(chronicRecordService.upsert(dto));
    }

    /**
     * 慢病档案作废：只认已认定档案，作废后该患者的长处方资格立即失效。
     * 用于「慢病建档与认定」页列表行的作废操作。
     */
    @Operation(summary = "慢病档案作废（单向：1→2）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('opd:chronicRecord:cancel')")
    public Result<Void> cancel(@RequestBody @Valid ChronicCancelDTO dto) {
        chronicRecordService.cancel(dto);
        return Result.success(null);
    }

    /**
     * 慢病档案分页：按患者、病种、认定状态筛出建档记录。
     * 用于「慢病建档与认定」页查询卡下方的档案表格。
     */
    @Operation(summary = "慢病档案分页（医生站）")
    @PostMapping("/listPage")
    public Result<PageResult<ChronicRecordListVO>> listPage(@Valid @RequestBody ChronicPageQueryDTO dto) {
        return Result.success(chronicRecordService.listPage(dto));
    }

    /**
     * 患者的全部有效慢病档案：已作废的不返回。
     * 供门诊开长处方环节的资格判定回查，同时供随访与慢病管理按人取档。
     */
    @Operation(summary = "患者的有效慢病档案（长处方资格判定用）")
    @GetMapping("/activeByPatient")
    public Result<List<ChronicRecordListVO>> activeByPatient(@RequestParam Long patientId) {
        return Result.success(chronicRecordService.activeList(patientId));
    }
}
